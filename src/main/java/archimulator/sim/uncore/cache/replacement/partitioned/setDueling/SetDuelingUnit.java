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

import archimulator.sim.uncore.cache.Cache;
import net.pickapack.math.SaturatingCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Set dueling unit for set dueling partitioned least recently used (LRU) policy.
 *
 * @author Min Cai
 */
public class SetDuelingUnit {
    private Cache<?> cache;
    private SaturatingCounter policySelectionCounter;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int setDuelingMonitorSize;

    private Random random;

    /**
     * Create a set dueling unit for set dueling partitioned least recently used (LRU) policy.
     *
     * @param cache the parent cache
     * @param policySelectionCounterMaxValue the maximum value of the policy selection counter
     * @param setDuelingMonitorSize the set dueling monitor size
     */
    public SetDuelingUnit(Cache<?> cache, int policySelectionCounterMaxValue, int setDuelingMonitorSize) {
        this.cache = cache;

        this.setDuelingMonitorSize = setDuelingMonitorSize;

        this.random = new Random(13);

        int policySelectionCounterThreshold = (int) ((float) policySelectionCounterMaxValue * 0.5);

        this.policySelectionCounter = new SaturatingCounter(0, policySelectionCounterThreshold, policySelectionCounterThreshold, 0);

        this.setDuelingMonitors = new ArrayList<SetDuelingMonitor>();

        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.setDuelingMonitors.add(new SetDuelingMonitor());
        }

        this.initSetDuelingMonitorsRandomly();
    }

    /**
     * Randomly assign sets to SDMs.
     */
    private void initSetDuelingMonitorsRandomly() {
        int totalSdmCount = 2;

        if (this.cache.getNumSets() < this.setDuelingMonitorSize * totalSdmCount) {
            throw new IllegalArgumentException();
        }

        /* When using multiple cache banks, seeding is to ensure that all banks use the same sampled sets */
        for (int p = 0; p < totalSdmCount; p++) {
            for (int ldr = 0; ldr < this.setDuelingMonitorSize; ldr++) {
                int set;
                do {
                    set = this.random.nextInt(this.cache.getNumSets());
                }
                while (this.setDuelingMonitors.get(set).type != SetDuelingMonitorType.FOLLOWERS);

                /* Set the Leader Set Type (NF or BF) */
                this.setDuelingMonitors.get(set).type = p == 0 ? SetDuelingMonitorType.POLICY1 : SetDuelingMonitorType.POLICY2;
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
        } else {
            return this.policySelectionCounter.isTaken() ? SetDuelingMonitorType.POLICY1 : SetDuelingMonitorType.POLICY2;
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
            this.policySelectionCounter.update(sdmType == SetDuelingMonitorType.POLICY1);
        }
    }

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
        POLICY2
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
}
