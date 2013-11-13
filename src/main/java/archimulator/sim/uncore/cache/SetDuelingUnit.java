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
package archimulator.sim.uncore.cache;

import archimulator.sim.common.SimulationType;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.util.IntervalCounter;

import java.util.*;

/**
 * Set dueling unit.
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
    private Map<Integer, IntervalCounter> numMainThreadL2Misses;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int setDuelingMonitorSize;

    private Random random;

    private int numCyclesElapsedPerInterval;

    private long numIntervals;
    private int numCyclesElapsed;

    /**
     * Create a set dueling unit.
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

        this.numMainThreadL2Misses = new TreeMap<>();
        for (int i = 0; i < this.numTotalSetDuelingMonitors; i++) {
            this.numMainThreadL2Misses.put(i, new IntervalCounter());
        }

        this.setDuelingMonitors = new ArrayList<>();

        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.setDuelingMonitors.add(new SetDuelingMonitor());
        }

        this.initializeSetDuelingMonitorsRandomly();

        this.numCyclesElapsedPerInterval = 5000000;

        this.cache.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (cache.getSimulation().getType() != SimulationType.FAST_FORWARD) {
                numCyclesElapsed++;

                if (numCyclesElapsed == numCyclesElapsedPerInterval) {
                    int bestPolicy = getBestPolicy();

                    for (int i = 0; i < numTotalSetDuelingMonitors; i++) {
                        numMainThreadL2Misses.get(i).newInterval();
                    }

                    numCyclesElapsed = 0;
                    numIntervals++;
                }
            }
        });

        this.cache.getBlockingEventDispatcher().addListener(
                HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent.class,
                event -> recordMainThreadL2Miss(event.getSet())
        );
    }

    /**
     * Randomly assign sets to set dueling monitors.
     */
    private void initializeSetDuelingMonitorsRandomly() {
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
     * Get the policy type for the specified set.
     *
     * @param set the set
     * @return the policy type for the specified set
     */
    public int getPolicyType(int set) {
        int policy = this.setDuelingMonitors.get(set).policy;
        return policy == FOLLOWERS ? getBestPolicy() : policy;
    }

    /**
     * Get the best policy.
     *
     * @return the best policy
     */
    private Integer getBestPolicy() {
        return Collections.min(
                this.numMainThreadL2Misses.keySet(),
                Comparator.comparing(policy -> numMainThreadL2Misses.get(policy).getValue())
        );
    }

    /**
     * Record a main thread L2 miss.
     *
     * @param set the set index
     */
    private void recordMainThreadL2Miss(int set) {
        int policy = this.setDuelingMonitors.get(set).policy;

        if (policy != FOLLOWERS) {
            this.numMainThreadL2Misses.get(policy).increment();
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
