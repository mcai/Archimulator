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
package archimulator.sim.uncore.mlpAwareCachePartitioning;

import archimulator.sim.common.Simulation;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.sim.uncore.cache.replacement.StackBasedCacheReplacementPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheControllerLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.sim.uncore.mlp.PendingL2Miss;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;

import java.util.*;

/**
 * Memory level parallelism (MLP) aware cache partitioning helper.
 *
 * @author Min Cai
 */
public class MLPAwareCachePartitioningHelper {
    private DirectoryController l2CacheController;
    private Map<Integer, PendingL2Miss> pendingL2Misses;
    private L2CacheMissMLPCostProfile l2CacheMissMLPCostProfile;
    private Map<Integer, Stack<Integer>> l2CacheLruStacks;

    /**
     * Create an MLP aware cache partitioning helper.
     *
     * @param simulation the simulation
     */
    public MLPAwareCachePartitioningHelper(Simulation simulation) {
        this(simulation.getProcessor().getMemoryHierarchy().getL2CacheController());
    }

    /**
     * Create an MLP aware cache partitioning helper.
     *
     * @param l2CacheController the L2 cache controller
     */
    public MLPAwareCachePartitioningHelper(final DirectoryController l2CacheController) {
        this.l2CacheController = l2CacheController;

        this.pendingL2Misses = new LinkedHashMap<Integer, PendingL2Miss>();

        this.l2CacheMissMLPCostProfile = new L2CacheMissMLPCostProfile(this.l2CacheController.getCache().getAssociativity());

        this.l2CacheLruStacks = new TreeMap<Integer, Stack<Integer>>();

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(MLPAwareCachePartitioningHelper.this.l2CacheController) && !event.isHitInCache()) {
                    profileBeginServicingL2CacheMiss(event.isHitInCache(), event.getWay(), event.getAccess());
                }
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(LastLevelCacheControllerLineInsertEvent.class, new Action1<LastLevelCacheControllerLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheControllerLineInsertEvent event) {
                if (event.getCacheController().equals(MLPAwareCachePartitioningHelper.this.l2CacheController)) {
                    profileEndServicingL2CacheMiss(event.getAccess());
                }
            }
        });

        l2CacheController.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                updateL2CacheMlpCostsPerCycle();
            }
        });
    }

    /**
     * To be invoked per cycle for updating MLP-costs for in-flight L2 cache accesses.
     */
    private void updateL2CacheMlpCostsPerCycle() {
        for (PendingL2Miss pendingL2Miss : this.pendingL2Misses.values()) {
            int stackDistance = 1; //TODO
            pendingL2Miss.setMlpCost(pendingL2Miss.getMlpCost() + (double) 1 / this.l2CacheMissMLPCostProfile.getN(stackDistance));
        }
    }

    /**
     * Profile the beginning of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileBeginServicingL2CacheMiss(boolean hitInCache, int way, MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();
        int set = this.l2CacheController.getCache().getSet(tag);

        Stack<Integer> lruStack = getLruStack(set);

        final int stackDistance = lruStack.search(tag);

        if (stackDistance != -1) {
            lruStack.remove((Integer) tag);
        }
        lruStack.push(tag);

        PendingL2Miss pendingL2Miss = new PendingL2Miss(access, l2CacheController.getCycleAccurateEventQueue().getCurrentCycle()) {
            {
                setStackDistance(stackDistance);
            }
        };
        this.pendingL2Misses.put(tag, pendingL2Miss);

        this.l2CacheMissMLPCostProfile.incCounter(stackDistance);
    }

    /**
     * Profile the end of servicing an L2 cache request.
     *
     * @param access the memory hierarchy access
     */
    private void profileEndServicingL2CacheMiss(MemoryHierarchyAccess access) {
        int tag = access.getPhysicalTag();

        PendingL2Miss pendingL2Miss = this.pendingL2Misses.get(tag);
        pendingL2Miss.setEndCycle(this.l2CacheController.getCycleAccurateEventQueue().getCurrentCycle());

        this.l2CacheMissMLPCostProfile.decCounter(pendingL2Miss.getStackDistance());

        this.pendingL2Misses.remove(tag);
    }

    /**
     * Get the LRU stack for the specified set in the L2 cache.
     *
     * @param set the set index
     * @return the LRU stack for the specified set in the L2 cache
     */
    private Stack<Integer> getLruStack(int set) {
        if (!this.l2CacheLruStacks.containsKey(set)) {
            this.l2CacheLruStacks.put(set, new Stack<Integer>());
        }

        return this.l2CacheLruStacks.get(set);
    }

    /**
     * L2 cache miss MLP-cost profile.
     */
    public class L2CacheMissMLPCostProfile {
        private List<Integer> counters;

        /**
         * Create an L2 cache miss MLP-cost profile.
         *
         * @param associativity the associativity
         */
        public L2CacheMissMLPCostProfile(int associativity) {
            this.counters = new ArrayList<Integer>();

            for (int i = 0; i < associativity; i++) {
                this.counters.add(0);
            }
        }

        /**
         * Increment the hit counter for the specified stack distance.
         *
         * @param stackDistance the stack distance
         */
        public void incCounter(int stackDistance) {
            this.getCounters().set(stackDistance, this.getCounters().get(stackDistance) + 1);
        }

        /**
         * Decrement the hit counter for the specified stack distance.
         *
         * @param stackDistance the stack distance
         */
        public void decCounter(int stackDistance) {
            this.getCounters().set(stackDistance, this.getCounters().get(stackDistance) - 1);
        }

        /**
         * Get the value of N, which is the number of L2 accesses with stack distance greater than or equal to the specified stack distance.
         *
         * @param stackDistance the stack distance
         * @return the value of N, which is the number of L2 accesses with stack distance greater than or equal to the specified stack distance
         */
        public int getN(int stackDistance) {
            int n = 0;

            for(int i = stackDistance - 1; i < this.counters.size(); i++) {
                n += this.counters.get(i);
            }

            return n;
        }

        /**
         * Get the list of hit counters.
         *
         * @return the list of hit counters
         */
        public List<Integer> getCounters() {
            return counters;
        }
    }
}
