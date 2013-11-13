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

import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import net.pickapack.math.SaturatingCounter;

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

    private int numSetDuelingMonitors;

    /**
     * Set dueling monitor.
     */
    private class SetDuelingMonitor {
        private int policy;
        private SaturatingCounter counter;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor(int policy) {
            this.policy = policy;
            this.counter = new SaturatingCounter(0, 1, 1 << 10, 0);
        }
    }

    private List<Integer> policiesPerSet;

    private Cache<?> cache;

    private List<SetDuelingMonitor> setDuelingMonitors;

    private int numSetsPerSetDuelingMonitor;

    private Random random;

    /**
     * Create a set dueling unit.
     *
     * @param cache                       the parent cache
     * @param numSetsPerSetDuelingMonitor the number of sets per set dueling monitor
     * @param numSetDuelingMonitors       the number of set dueling monitors
     */
    public SetDuelingUnit(final Cache<?> cache, int numSetsPerSetDuelingMonitor, final int numSetDuelingMonitors) {
        this.cache = cache;

        this.numSetsPerSetDuelingMonitor = numSetsPerSetDuelingMonitor;
        this.numSetDuelingMonitors = numSetDuelingMonitors;

        this.random = new Random(13);

        this.policiesPerSet = new ArrayList<>();

        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.policiesPerSet.add(FOLLOWERS);
        }

        this.setDuelingMonitors = new ArrayList<>();
        for (int i = 0; i < this.numSetDuelingMonitors; i++) {
            this.setDuelingMonitors.add(new SetDuelingMonitor(i));
        }

        this.initializeSetDuelingMonitorsRandomly();

        this.cache.getBlockingEventDispatcher().addListener(
                HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent.class,
                event -> recordMainThreadL2Miss(event.getSet())
        );
    }

    /**
     * Randomly assign sets to set dueling monitors.
     */
    private void initializeSetDuelingMonitorsRandomly() {
        if (this.cache.getNumSets() < this.numSetsPerSetDuelingMonitor * numSetDuelingMonitors) {
            throw new IllegalArgumentException();
        }

        for (int policy = 0; policy < numSetDuelingMonitors; policy++) {
            for (int i = 0; i < this.numSetsPerSetDuelingMonitor; i++) {
                int set;

                do {
                    set = this.random.nextInt(this.cache.getNumSets());
                }
                while (this.policiesPerSet.get(set) != FOLLOWERS);

                this.policiesPerSet.set(set, policy);
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
        int policy = this.policiesPerSet.get(set);
        return policy == FOLLOWERS ? getBestPolicyType() : policy;
    }

    /**
     * Get the best policy.
     *
     * @return the best policy
     */
    private int getBestPolicyType() {
        return Collections.min(
                this.setDuelingMonitors,
                Comparator.comparing(setDuelingMonitor -> setDuelingMonitor.counter.getValue())).policy;
    }

    /**
     * Record a main thread L2 miss.
     *
     * @param set the set index
     */
    private void recordMainThreadL2Miss(int set) {
        int policy = this.policiesPerSet.get(set);

        if (policy != FOLLOWERS) {
            this.setDuelingMonitors.get(policy).counter.update(true);
            this.setDuelingMonitors.get(policy).counter.update(true);

            this.setDuelingMonitors.forEach(setDuelingMonitor -> {
                if(setDuelingMonitor.policy != policy) {
                    setDuelingMonitor.counter.update(false);
                }
            });

            boolean saturatedCounterFound = false;

            for(SetDuelingMonitor setDuelingMonitor : this.setDuelingMonitors) {
                if(setDuelingMonitor.counter.getValue() == setDuelingMonitor.counter.getMaxValue()) {
                    saturatedCounterFound = true;
                    break;
                }
            }

            if(saturatedCounterFound) {
                this.setDuelingMonitors.forEach(setDuelingMonitor -> {
                    setDuelingMonitor.counter.reset();
                });
            }
        }
    }
}
