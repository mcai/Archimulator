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
import net.pickapack.action.Action;

import java.util.*;

/**
 * Set dueling unit for set dueling partitioned least recently used (LRU) policy.
 *
 * @author Min Cai
 */
public class SetDuelingUnit {
    /**
     * Set dueling monitor type.
     */
    public enum SetDuelingMonitorType {
        /**
         * Followers.
         */
        FOLLOWERS,

        /**
         * Policy 1.
         */
        POLICY1,

        /**
         * Policy 2.
         */
        POLICY2,

        /**
         * Policy 3.
         */
        POLICY3
    }

    /**
     * Set dueling monitor.
     */
    private class SetDuelingMonitor {
        private SetDuelingMonitorType type;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor() {
            this.type = SetDuelingMonitorType.FOLLOWERS;
        }
    }

    private Cache<?> cache;
    private Map<SetDuelingMonitorType, Long> policySelectionCounter;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int setDuelingMonitorSize;

    private Random random;

    private int numCyclesElapsedPerInterval;

    private long numIntervals;
    private int numCyclesElapsed;

    /**
     * Create a set dueling unit for set dueling partitioned least recently used (LRU) policy.
     *
     * @param cache the parent cache
     * @param setDuelingMonitorSize the set dueling monitor size
     */
    public SetDuelingUnit(final Cache<?> cache, int setDuelingMonitorSize) {
        this.cache = cache;

        this.setDuelingMonitorSize = setDuelingMonitorSize;

        this.random = new Random(13);

        this.policySelectionCounter = new TreeMap<SetDuelingMonitorType, Long>();
        this.policySelectionCounter.put(SetDuelingMonitorType.POLICY1, 0L);
        this.policySelectionCounter.put(SetDuelingMonitorType.POLICY2, 0L);
        this.policySelectionCounter.put(SetDuelingMonitorType.POLICY3, 0L);

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
                        policySelectionCounter.put(SetDuelingMonitorType.POLICY1, 0L);
                        policySelectionCounter.put(SetDuelingMonitorType.POLICY2, 0L);
                        policySelectionCounter.put(SetDuelingMonitorType.POLICY3, 0L);

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
        int numTotalSetDuelingMonitors = 3;

        if (this.cache.getNumSets() < this.setDuelingMonitorSize * numTotalSetDuelingMonitors) {
            throw new IllegalArgumentException();
        }

        for (int p = 0; p < numTotalSetDuelingMonitors; p++) {
            for (int ldr = 0; ldr < this.setDuelingMonitorSize; ldr++) {
                int set;
                do {
                    set = this.random.nextInt(this.cache.getNumSets());
                }
                while (this.setDuelingMonitors.get(set).type != SetDuelingMonitorType.FOLLOWERS);

                switch (p) {
                    case 0:
                        this.setDuelingMonitors.get(set).type = SetDuelingMonitorType.POLICY1;
                        break;
                    case 1:
                        this.setDuelingMonitors.get(set).type = SetDuelingMonitorType.POLICY2;
                        break;
                    case 2:
                        this.setDuelingMonitors.get(set).type = SetDuelingMonitorType.POLICY3;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
            }
        }
    }

    /**
     * Get the partitioning policy type for the specified set.
     *
     * @param set the set
     * @return the partitioning policy type for the specified set
     */
    public SetDuelingMonitorType getPartitioningPolicyType(int set) {
        if (this.setDuelingMonitors.get(set).type == SetDuelingMonitorType.POLICY1) {
            return SetDuelingMonitorType.POLICY1;
        } else if (this.setDuelingMonitors.get(set).type == SetDuelingMonitorType.POLICY2) {
            return SetDuelingMonitorType.POLICY2;
        } else if (this.setDuelingMonitors.get(set).type == SetDuelingMonitorType.POLICY3) {
            return SetDuelingMonitorType.POLICY3;
        } else {
            return Collections.min(this.policySelectionCounter.keySet(), new Comparator<SetDuelingMonitorType>() {
                @Override
                public int compare(SetDuelingMonitorType o1, SetDuelingMonitorType o2) {
                    Long value1 = policySelectionCounter.get(o1);
                    Long value2 = policySelectionCounter.get(o2);
                    return value1.compareTo(value2);
                }
            });
        }
    }

    /**
     * Record a useful helper thread L2 request.
     *
     * @param set the set index
     */
    public void recordUsefulHelperThreadL2Request(int set) {
        SetDuelingMonitorType sdmType = this.setDuelingMonitors.get(set).type;

        if (sdmType != SetDuelingMonitorType.FOLLOWERS) {
            this.policySelectionCounter.put(sdmType, this.policySelectionCounter.get(sdmType) + 1);
        }
    }
}
