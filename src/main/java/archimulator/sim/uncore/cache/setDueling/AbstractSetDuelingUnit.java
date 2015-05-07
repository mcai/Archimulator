/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.cache.setDueling;

import archimulator.sim.uncore.cache.Cache;

import java.util.*;

/**
 * Set dueling unit.
 *
 * @author Min Cai
 */
public abstract class AbstractSetDuelingUnit<SetDuelingMonitorT extends AbstractSetDuelingUnit.SetDuelingMonitor> {
    /**
     * Set dueling monitor.
     */
    public class SetDuelingMonitor {
        private int threadId;
        private int policyId;

        /**
         * Create a set dueling monitor.
         *
         * @param threadId the thread ID
         * @param policyId the policy ID
         */
        public SetDuelingMonitor(int threadId, int policyId) {
            this.threadId = threadId;
            this.policyId = policyId;
        }

        /**
         * Get the thread ID.
         *
         * @return the thread ID
         */
        public int getThreadId() {
            return threadId;
        }

        /**
         * Get the policy ID.
         *
         * @return the policy ID
         */
        public int getPolicyId() {
            return policyId;
        }
    }

    /**
     * Binding.
     */
    protected class Binding {
        private int set;
        private int threadId;
        private int policyId;

        /**
         * Create a binding.
         *
         * @param set      the set index
         * @param threadId the thread ID
         * @param policyId the policy ID
         */
        private Binding(int set, int threadId, int policyId) {
            this.set = set;
            this.threadId = threadId;
            this.policyId = policyId;
        }

        /**
         * Get the set index.
         *
         * @return the set index
         */
        public int getSet() {
            return set;
        }

        /**
         * Get the thread ID.
         *
         * @return the thread ID
         */
        public int getThreadId() {
            return threadId;
        }

        /**
         * Get the policy ID.
         *
         * @return the policy ID
         */
        public int getPolicyId() {
            return policyId;
        }
    }

    /**
     * Followers.
     */
    public static final int FOLLOWERS = -1;

    private Cache<?> cache;

    private int numThreads;

    private int numSetDuelingMonitorsPerThread;

    private int numSetsPerSetDuelingMonitor;

    private List<Binding> bindings;

    private Map<Integer, List<SetDuelingMonitorT>> setDuelingMonitors;

    private Random random;

    /**
     * Create a set dueling unit.
     *
     * @param cache                          the parent cache
     * @param numThreads                     the number of threads
     * @param numSetDuelingMonitorsPerThread the number of set dueling monitors per thread
     * @param numSetsPerSetDuelingMonitor    the number of sets per set dueling monitor
     */
    public AbstractSetDuelingUnit(final Cache<?> cache, int numThreads, final int numSetDuelingMonitorsPerThread, int numSetsPerSetDuelingMonitor) {
        this.cache = cache;
        this.numThreads = numThreads;
        this.numSetDuelingMonitorsPerThread = numSetDuelingMonitorsPerThread;
        this.numSetsPerSetDuelingMonitor = numSetsPerSetDuelingMonitor;

        this.random = new Random(13);

        this.bindings = new ArrayList<>();
        for (int i = 0; i < this.cache.getNumSets(); i++) {
            this.bindings.add(new Binding(i, -1, FOLLOWERS));
        }

        this.setDuelingMonitors = new TreeMap<>();
        for (int threadId = 0; threadId < this.numThreads; threadId++) {
            this.setDuelingMonitors.put(threadId, new ArrayList<>());

            for (int policyId = 0; policyId < this.numSetDuelingMonitorsPerThread; policyId++) {
                this.setDuelingMonitors.get(threadId).add(this.createSetDuelingMonitor(threadId, policyId));
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

        for (int threadId = 0; threadId < this.numThreads; threadId++) {
            for (int policyId = 0; policyId < this.numSetDuelingMonitorsPerThread; policyId++) {
                for (int k = 0; k < this.numSetsPerSetDuelingMonitor; k++) {
                    int set;

                    do {
                        set = this.random.nextInt(this.cache.getNumSets());
                    }
                    while (this.bindings.get(set).policyId != FOLLOWERS);

                    this.bindings.set(set, new Binding(set, threadId, policyId));
                }
            }
        }
    }

    /**
     * Create a set dueling monitor for the specified thread ID and policy ID.
     *
     * @param threadId the thread ID
     * @param policyId the policy ID
     * @return a set dueling monitor created for the specified thread ID and policy ID
     */
    protected abstract SetDuelingMonitorT createSetDuelingMonitor(int threadId, int policyId);

    /**
     * Get the policy ID for the specified set.
     *
     * @param set      the set index
     * @param threadId the thread ID
     * @return the policy ID for the specified set
     */
    public int getPolicyId(int set, int threadId) {
        Binding binding = this.getBinding(set);

        if (binding.threadId == threadId) {
            return binding.policyId;
        }

        return getBestPolicyId(threadId);
    }

    /**
     * Get the list of set dueling monitors for the specified thread ID.
     *
     * @param threadId the thread ID
     * @return the list of set dueling monitors for the specified thread ID
     */
    public List<SetDuelingMonitorT> getSetDuelingMonitors(int threadId) {
        return this.setDuelingMonitors.get(threadId);
    }

    /**
     * Get the set dueling monitor for the specified thread ID and policy ID.
     *
     * @param threadId the thread ID
     * @param policyId the policy ID
     * @return the set dueling monitor for the specified thread ID and policy ID
     */
    public SetDuelingMonitorT getSetDuelingMonitor(int threadId, int policyId) {
        return this.setDuelingMonitors.get(threadId).get(policyId);
    }

    /**
     * Get the binding for the specified set index.
     *
     * @param set the set index
     * @return the binding for the specified set index
     */
    public Binding getBinding(int set) {
        return this.bindings.get(set);
    }

    /**
     * Get the best policy ID.
     *
     * @return the best policy ID
     */
    public abstract int getBestPolicyId(int threadId);

    /**
     * Get the parent cache.
     *
     * @return the parent cache
     */
    public Cache<?> getCache() {
        return cache;
    }

    /**
     * Get the number of threads.
     *
     * @return the number of threads
     */
    public int getNumThreads() {
        return numThreads;
    }

    /**
     * Get the number of set dueling monitors per thread.
     *
     * @return the number of set dueling monitors per thread
     */
    public int getNumSetDuelingMonitorsPerThread() {
        return numSetDuelingMonitorsPerThread;
    }

    /**
     * Get the number of sets per set dueling monitor.
     *
     * @return the number of sets per set dueling monitor
     */
    public int getNumSetsPerSetDuelingMonitor() {
        return numSetsPerSetDuelingMonitor;
    }
}
