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
package archimulator.sim.uncore.cache.replacement.prefetchAware;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Prefetch aware HM Least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class PrefetchAwareHMLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Policy type.
     */
    public enum PolicyType {
        /**
         * Alter only cache miss handling.
         */
        M,

        /**
         * Alter only cache hit handling.
         */
        H,

        /**
         * Alter both cache hit and miss handling.
         */
        HM;

        /**
         * Get a value indicating whether cache hits should be handled or not.
         *
         * @return a value indicating whether cache hits should be handled or not
         */
        public boolean handleHits() {
            return this == H || this == HM;
        }

        /**
         * Get a value indicating whether cache misses should be handled or not.
         *
         * @return a value indicating whether cache misses should be handled or not
         */
        public boolean handleMisses() {
            return this == M || this == HM;
        }
    }

    private PolicyType type;

    /**
     * Create a prefetch aware HM least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public PrefetchAwareHMLRUPolicy(EvictableCache<StateT> cache, PolicyType type) {
        super(cache);
        this.type = type;
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if(this.type.handleHits() && access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsMainThread(access) && lineFoundIsHelperThread(set, way)) {
            // HT-MT inter-thread hit, never used again: low locality => Demote to LRU position
            this.setLRU(set, way);
            return;
        }

        super.handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if(this.type.handleMisses() && access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsHelperThread(access) && !isUseful(access.getVirtualPc())) {
            // Non-useful HT miss, prevented from thrashing: low locality => insert in LRU position
            this.setLRU(set, way);
            return;
        }

        super.handleInsertionOnMiss(access, set, way);
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the main thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the main thread or not
     */
    private boolean requesterIsMainThread(MemoryHierarchyAccess access) {
        return HelperThreadingHelper.isMainThread(access.getThread());
    }

    /**
     * Get a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not.
     *
     * @param access the memory hierarchy access
     * @return a value indicating whether the requester of the specified memory hierarchy access is the helper thread or not
     */
    private boolean requesterIsHelperThread(MemoryHierarchyAccess access) {
        return HelperThreadingHelper.isHelperThread(access.getThread());
    }

    /**
     * Get a value indicating whether the line found in the specified set index and way is brought by the helper thread or not.
     *
     * @param set the set index
     * @param way the way
     * @return a value indicating whether the line found in the specified set index and way is brought by the helper thread or not
     */
    private boolean lineFoundIsHelperThread(int set, int way) {
        return HelperThreadingHelper.isHelperThread(this.getCache().getLine(set, way).getAccess().getThread());
    }

    /**
     * Get a value indicating whether prefetches coming from the specified PC address are predicted as useful or not.
     *
     * @param pc the PC address
     * @return a value indicating whether prefetches coming from the specified PC address are predicted as useful or not
     */
    private boolean isUseful(int pc) {
        return getCache().getSimulation().getHelperThreadL2CacheRequestProfilingHelper()
                .getHelperThreadL2CacheRequestUsefulnessPredictor().predict(pc);
    }

    /**
     * Get the policy type.
     *
     * @return the policy type
     */
    public PolicyType getType() {
        return type;
    }
}
