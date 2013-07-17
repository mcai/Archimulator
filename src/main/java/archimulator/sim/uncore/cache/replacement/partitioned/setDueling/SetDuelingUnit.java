/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.uncore.cache.replacement.partitioned.setDueling;

import archimulator.sim.common.SimulationType;
import archimulator.sim.uncore.cache.Cache;
import archimulator.util.IntervalCounter;
import net.pickapack.action.Action;

import java.util.*;

/**
 * Set dueling unit for set dueling partitioned least recently used (LRU) policy.
 *
 * @author Min Cai
 */
public class SetDuelingUnit {
    /**
     * Followers.
     */
    public static final int FOLLOWERS = -1;

    private int numTotalSetDuelingMonitors;

    /**
     * Set dueling monitor.
     */
    private class SetDuelingMonitor {
        private int policy;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor() {
            this.policy = FOLLOWERS;
        }
    }

    private Cache<?> cache;
    private Map<Integer, IntervalCounter> numUsefulHelperThreadL2CacheRequestsPerPolicy;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int setDuelingMonitorSize;

    private Random random;

    private int numCyclesElapsedPerInterval;

    private long numIntervals;
    private int numCyclesElapsed;

    /**
     * Create a set dueling unit for set dueling partitioned least recently used (LRU) policy.
     *
     * @param cache                      the parent cache
     * @param setDuelingMonitorSize      the set dueling monitor size
     * @param numTotalSetDuelingMonitors the number of total set dueling monitors
     */
    public SetDuelingUnit(final Cache<?> cache, int setDuelingMonitorSize, final int numTotalSetDuelingMonitors) {
        this.cache = cache;

        this.setDuelingMonitorSize = setDuelingMonitorSize;
        this.numTotalSetDuelingMonitors = numTotalSetDuelingMonitors;

        this.random = new Random(13);

        this.numUsefulHelperThreadL2CacheRequestsPerPolicy = new TreeMap<Integer, IntervalCounter>();
        for (int i = 0; i < this.numTotalSetDuelingMonitors; i++) {
            this.numUsefulHelperThreadL2CacheRequestsPerPolicy.put(i, new IntervalCounter());
        }

        this.setDuelingMonitors = new ArrayList<SetDuelingMonitor>();

        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.setDuelingMonitors.add(new SetDuelingMonitor());
        }

        this.initSetDuelingMonitorsRandomly();

        this.numCyclesElapsedPerInterval = 5000000;

        this.cache.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                if (cache.getSimulation().getType() != SimulationType.FAST_FORWARD) {
                    numCyclesElapsed++;

                    if (numCyclesElapsed == numCyclesElapsedPerInterval) {
                        int bestPolicy = getBestPolicy();

                        for (int i = 0; i < numTotalSetDuelingMonitors; i++) {
                            IntervalCounter intervalCounter = numUsefulHelperThreadL2CacheRequestsPerPolicy.get(i);

                            System.out.printf(
                                    "numUsefulHelperThreadL2CacheRequestsPerPolicy[interval: %d, %d]=%s%s\n",
                                    numIntervals,
                                    i,
                                    intervalCounter,
                                    (intervalCounter.getValue() > 0 && i == bestPolicy) ? "*" : ""
                            );

                            intervalCounter.newInterval();
                        }

                        System.out.println();

                        numCyclesElapsed = 0;
                        numIntervals++;
                    }
                }
            }
        });
    }

    /**
     * Randomly assign sets to set dueling monitors.
     */
    private void initSetDuelingMonitorsRandomly() {
        if (this.cache.getNumSets() < this.setDuelingMonitorSize * numTotalSetDuelingMonitors) {
            throw new IllegalArgumentException();
        }

        for (int policy = 0; policy < numTotalSetDuelingMonitors; policy++) {
            for (int i = 0; i < this.setDuelingMonitorSize; i++) {
                int set;

                do {
                    set = this.random.nextInt(this.cache.getNumSets());
                }
                while (this.setDuelingMonitors.get(set).policy != FOLLOWERS);

                this.setDuelingMonitors.get(set).policy = policy;
            }
        }
    }

    /**
     * Get the partitioning policy type for the specified set.
     *
     * @param set the set
     * @return the partitioning policy type for the specified set
     */
    public int getPartitioningPolicyType(int set) {
        int policy = this.setDuelingMonitors.get(set).policy;
        return policy == FOLLOWERS ? getBestPolicy() : policy;
    }

    /**
     * Get the best policy.
     *
     * @return the best policy
     */
    private Integer getBestPolicy() {
        return Collections.max(this.numUsefulHelperThreadL2CacheRequestsPerPolicy.keySet(), new Comparator<Integer>() {
            @Override
            public int compare(Integer policy1, Integer policy2) {
                Long value1 = numUsefulHelperThreadL2CacheRequestsPerPolicy.get(policy1).getValue();
                Long value2 = numUsefulHelperThreadL2CacheRequestsPerPolicy.get(policy2).getValue();
                return value1.compareTo(value2);
            }
        });
    }

    /**
     * Record a useful helper thread L2 request.
     *
     * @param set the set index
     */
    public void recordUsefulHelperThreadL2Request(int set) {
        int policy = this.setDuelingMonitors.get(set).policy;

        if (policy != FOLLOWERS) {
            this.numUsefulHelperThreadL2CacheRequestsPerPolicy.get(policy).increment();
        }
    }

    /**
     * Get the measuring interval in cycles.
     *
     * @return the measuring interval in cycles
     */
    public int getNumCyclesElapsedPerInterval() {
        return numCyclesElapsedPerInterval;
    }

    /**
     * Get the number of intervals.
     *
     * @return the number of intervals
     */
    public long getNumIntervals() {
        return numIntervals;
    }

    /**
     * Get the number of cycles elapsed.
     *
     * @return the number of cycles elapsed
     */
    public int getNumCyclesElapsed() {
        return numCyclesElapsed;
    }
}
