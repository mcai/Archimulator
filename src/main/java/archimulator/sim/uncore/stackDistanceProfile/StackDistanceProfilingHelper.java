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
package archimulator.sim.uncore.stackDistanceProfile;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.sim.uncore.cache.replacement.StackBasedCacheReplacementPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import net.pickapack.action.Action1;

/**
 * Stack distance profiling helper.
 *
 * @author Min Cai
 */
public class StackDistanceProfilingHelper {
    private DirectoryController l2CacheController;
    private StackDistanceProfile l2CacheStackDistanceProfile;
    private StackBasedCacheReplacementPolicy<DirectoryControllerState> l2CacheReplacementPolicy;

    /**
     * Create a stack distance profiling helper.
     *
     * @param simulation the simulation
     */
    public StackDistanceProfilingHelper(Simulation simulation) {
        this.l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();
        this.l2CacheStackDistanceProfile = new StackDistanceProfile(this.l2CacheController.getCache().getAssociativity());

        CacheReplacementPolicy<DirectoryControllerState> replacementPolicy = this.l2CacheController.getCache().getReplacementPolicy();
        if(!(replacementPolicy instanceof StackBasedCacheReplacementPolicy)) {
            return;
        }

        this.l2CacheReplacementPolicy = (StackBasedCacheReplacementPolicy<DirectoryControllerState>) replacementPolicy;

        simulation.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController() == l2CacheController) {
                    profileStackDistance(event.isHitInCache(), event.getWay(), event.getAccess());
                }
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
        int set = this.l2CacheController.getCache().getSet(tag);

        int stackPosition = hitInCache ? l2CacheReplacementPolicy.getStackPosition(set, way) : -1;

        this.l2CacheController.getBlockingEventDispatcher().dispatch(new StackDistanceProfiledEvent(this.l2CacheController, access, hitInCache, set, way, stackPosition));

        if (hitInCache) {
            this.l2CacheStackDistanceProfile.incHitCounter(stackPosition);
        } else {
            this.l2CacheStackDistanceProfile.incMissCounter();
        }
    }

    /**
     * Get the stack distance profile for the L2 cache.
     *
     * @return the stack distance profile for the L2 cache
     */
    public StackDistanceProfile getL2CacheStackDistanceProfile() {
        return l2CacheStackDistanceProfile;
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
