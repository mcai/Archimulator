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

import net.pickapack.math.SaturatingCounter;

import java.util.*;

/**
 * Set dueling unit.
 *
 * @author Min Cai
 */
public abstract class SetDuelingUnit {
    /**
     * Followers.
     */
    public static final int FOLLOWERS = -1;

    private int numThreads;

    private int numSetDuelingMonitorsPerThread;

    /**
     * Set dueling monitor.
     */
    private class SetDuelingMonitor {
        private int threadId;
        private int policyId;
        private SaturatingCounter counter;

        /**
         * Create a set dueling monitor.
         */
        private SetDuelingMonitor(int threadId, int policyId) {
            this.threadId = threadId;
            this.policyId = policyId;
            this.counter = new SaturatingCounter(0, 1, 1 << 10, 0);
        }
    }

    /**
     * Binding.
     */
    private class Binding {
        private int set;
        private int threadId;
        private int policyId;

        /**
         * Create a binding.
         *
         * @param set the set index
         * @param threadId the thread ID
         * @param policyId the policy ID
         */
        private Binding(int set, int threadId, int policyId) {
            this.set = set;
            this.threadId = threadId;
            this.policyId = policyId;
        }
    }

    private List<Binding> bindings;

    private Cache<?> cache;

    private Map<Integer, List<SetDuelingMonitor>> setDuelingMonitors;

    private int numSetsPerSetDuelingMonitor;

    private Random random;

    /**
     * Create a set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numThreads                     the number of threads
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public SetDuelingUnit(final Cache<?> cache, int numThreads, final int numSetDuelingMonitorsPerThread, int numSetsPerSetDuelingMonitor) {
        this.cache = cache;

        this.numThreads = numThreads;
        this.numSetsPerSetDuelingMonitor = numSetsPerSetDuelingMonitor;
        this.numSetDuelingMonitorsPerThread = numSetDuelingMonitorsPerThread;

        this.random = new Random(13);

        this.bindings = new ArrayList<>();
        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.bindings.add(new Binding(i, -1, FOLLOWERS));
        }

        this.setDuelingMonitors = new TreeMap<>();
        for(int i = 0; i < this.numThreads; i++) {
            this.setDuelingMonitors.put(i, new ArrayList<>());

            for (int j = 0; j < this.numSetDuelingMonitorsPerThread; j++) {
                this.setDuelingMonitors.get(i).add(new SetDuelingMonitor(i, j));
            }
        }

        this.initializeSetDuelingMonitorsRandomly();
    }

    /**
     * Randomly assign sets to set dueling monitors.
     */
    private void initializeSetDuelingMonitorsRandomly() {
        if (this.cache.getNumSets() < this.numSetsPerSetDuelingMonitor * this.numSetDuelingMonitorsPerThread * this.numThreads) {
            throw new IllegalArgumentException();
        }

        for(int i = 0; i < this.numThreads; i++) {
            for (int j = 0; j < numSetDuelingMonitorsPerThread; j++) {
                for (int k = 0; k < this.numSetsPerSetDuelingMonitor; k++) {
                    int set;

                    do {
                        set = this.random.nextInt(this.cache.getNumSets());
                    }
                    while (this.bindings.get(set).policyId != FOLLOWERS);

                    this.bindings.set(set, new Binding(i, i, j));
                }
            }
        }
    }

    /**
     * Get the policy ID for the specified set.
     *
     * @param set the set index
     * @param threadId the thread ID
     * @return the policy ID for the specified set
     */
    public int getPolicyId(int set, int threadId) {
        Binding binding = this.bindings.get(set);

        if(binding.threadId == threadId) {
            return binding.policyId;
        }

        return getBestPolicyId(threadId);
    }

    /**
     * Get the best policy ID.
     *
     * @return the best policy ID
     */
    private int getBestPolicyId(int threadId) {
        return this.setDuelingMonitors.get(threadId).stream().min(
                Comparator.comparing(setDuelingMonitor -> setDuelingMonitor.counter.getValue())
        ).get().policyId;
    }

    /**
     * Increment the specified set's corresponding set dueling monitor's saturating counter value.
     *
     * @param set the set index
     * @param threadId the thread ID
     */
    protected void inc(int set, int threadId) {
        int policyId = this.bindings.get(set).policyId;

        if (policyId != FOLLOWERS) {
            this.setDuelingMonitors.get(threadId).get(policyId).counter.update(true);

            if (this.setDuelingMonitors.get(threadId).stream().anyMatch(
                    setDuelingMonitor -> setDuelingMonitor.counter.getValue() == setDuelingMonitor.counter.getMaxValue()
            )) {
                this.setDuelingMonitors.get(threadId).forEach(setDuelingMonitor -> setDuelingMonitor.counter.reset());
            }
        }
    }
}
