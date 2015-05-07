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
package archimulator.sim.uncore.cache.stackDistanceProfile;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Stack distance profiling helper.
 *
 * @author Min Cai
 */
public class StackDistanceProfilingHelper implements Reportable {
    private DirectoryController l2Controller;
    private Map<Integer, Stack<Integer>> l2LruStacks;
    private StackDistanceProfile l2StackDistanceProfile;

    /**
     * Create a stack distance profiling helper.
     *
     * @param simulation the simulation
     */
    public StackDistanceProfilingHelper(Simulation simulation) {
        this.l2Controller = simulation.getProcessor().getMemoryHierarchy().getL2Controller();
        this.l2LruStacks = new TreeMap<>();
        this.l2StackDistanceProfile = new StackDistanceProfile(this.l2Controller.getCache().getAssociativity());

        simulation.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, event -> {
            if (event.getCacheController() == l2Controller) {
                profileStackDistance(event.isHitInCache(), event.getWay(), event.getAccess());
            }
        });
    }

    /**
     * Profile the stack distance for an access.
     *
     * @param hitInCache a value indicating whether the access hits in the cache or not
     * @param way        the way
     * @param access     the memory hierarchy access
     */
    private void profileStackDistance(boolean hitInCache, int way, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.l2Controller.getCache().getSet(tag);

        Stack<Integer> lruStack = getLruStack(set);

        int stackDistance = lruStack.search(tag);

        if (stackDistance != -1) {
            lruStack.remove((Integer) tag);
            stackDistance--;
        }

        lruStack.push(tag);

        if (lruStack.size() > this.l2Controller.getCache().getAssociativity()) {
            lruStack.remove(lruStack.size() - 1);
        }

        this.l2Controller.getBlockingEventDispatcher().dispatch(new StackDistanceProfiledEvent(this.l2Controller, access, hitInCache, set, way, stackDistance));

        if (stackDistance == -1) {
            this.l2StackDistanceProfile.incrementMissCounter();
        } else {
            this.l2StackDistanceProfile.incrementHitCounter(stackDistance);
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "stackDistanceProfilingHelper") {
            {
                getChildren().add(new ReportNode(this, "l2StackDistanceProfile/hitCounters", getL2StackDistanceProfile().getHitCounters() + ""));
                getChildren().add(new ReportNode(this, "l2StackDistanceProfile/missCounter", getL2StackDistanceProfile().getMissCounter() + ""));
                getChildren().add(new ReportNode(this, "assumedNumMissesDistribution", getAssumedNumMissesDistribution() + ""));
            }
        });
    }

    /**
     * Get the LRU stack for the specified set in the L2 cache.
     *
     * @param set the set index
     * @return the LRU stack for the specified set in the L2 cache
     */
    private Stack<Integer> getLruStack(int set) {
        if (!this.l2LruStacks.containsKey(set)) {
            this.l2LruStacks.put(set, new Stack<>());
        }

        return this.l2LruStacks.get(set);
    }

    /**
     * Get the number of misses for the assumed associativity.
     *
     * @param associativity the assumed associativity
     * @return the number of misses for the assumed associativity
     */
    public int getAssumedNumMisses(int associativity) {
        if (associativity > this.l2Controller.getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        int numMisses = 0;

        for (int i = associativity - 1; i < this.l2Controller.getCache().getAssociativity(); i++) {
            numMisses += this.l2StackDistanceProfile.getHitCounters().get(i);
        }

        numMisses += this.l2StackDistanceProfile.getMissCounter();

        return numMisses;
    }

    /**
     * Get the distribution of the number of misses for the assumed associativities.
     *
     * @return the distribution of the number of misses for the assumed associativities
     */
    public Map<Integer, Integer> getAssumedNumMissesDistribution() {
        Map<Integer, Integer> result = new LinkedHashMap<>();

        for (int associativity = 1; associativity <= this.l2Controller.getCache().getAssociativity(); associativity++) {
            result.put(associativity, this.getAssumedNumMisses(associativity));
        }

        return result;
    }

    /**
     * Get the stack distance profile for the L2 cache.
     *
     * @return the stack distance profile for the L2 cache
     */
    public StackDistanceProfile getL2StackDistanceProfile() {
        return l2StackDistanceProfile;
    }

    /**
     * An event when a stack distance is profiled.
     */
    public class StackDistanceProfiledEvent extends SimulationEvent {
        private GeneralCacheController cacheController;
        private MemoryHierarchyAccess access;
        private boolean hitInCache;
        private int set;
        private int way;
        private int stackDistance;

        /**
         * Create an event when a stack distance is profiled.
         *
         * @param cacheController the cache controller
         * @param access          the memory hierarchy access
         * @param hitInCache      a value indicating whether the access hits in the cache or not
         * @param set             the set index
         * @param way             the way
         * @param stackDistance   the stack distance
         */
        public StackDistanceProfiledEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, boolean hitInCache, int set, int way, int stackDistance) {
            super(cacheController);

            this.cacheController = cacheController;
            this.access = access;
            this.hitInCache = hitInCache;
            this.set = set;
            this.way = way;
            this.stackDistance = stackDistance;
        }

        /**
         * Get the cache controller.
         *
         * @return the cache controller
         */
        public GeneralCacheController<?, ?> getCacheController() {
            return cacheController;
        }

        /**
         * Get the memory hierarchy access.
         *
         * @return the memory hierarchy access
         */
        public MemoryHierarchyAccess getAccess() {
            return access;
        }

        /**
         * Get a value indicating whether the access hits in the cache or not.
         *
         * @return a value indicating whether the access hits in the cache or not
         */
        public boolean isHitInCache() {
            return hitInCache;
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
         * Get the way.
         *
         * @return the way
         */
        public int getWay() {
            return way;
        }

        /**
         * Get the stack distance.
         *
         * @return the stack position
         */
        public int getStackDistance() {
            return stackDistance;
        }
    }
}
