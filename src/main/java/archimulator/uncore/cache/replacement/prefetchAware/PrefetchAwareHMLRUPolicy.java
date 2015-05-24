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
package archimulator.uncore.cache.replacement.prefetchAware;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.MemoryHierarchyAccessType;
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.LRUPolicy;
import archimulator.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;
import java.util.Random;

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
         * Alter only replacement handling.
         */
        R,

        /**
         * Alter only cache hit handling.
         */
        H,

        /**
         * Alter only cache miss handling.
         */
        M,

        /**
         * Alter both cache replacement and hit handling.
         */
        RH,

        /**
         * Alter both cache hit and miss handling.
         */
        HM,

        /**
         * Alter both cache replacement and miss handling.
         */
        RM,

        /**
         * Alter cache replacement, hit and miss handling.
         */
        RHM;

        /**
         * Get a value indicating whether cache replacement should be handled or not.
         *
         * @return a value indicating whether cache replacement should be handled or not
         */
        public boolean alterReplacement() {
            return this == R || this == RM || this == RHM;
        }

        /**
         * Get a value indicating whether cache hits should be handled or not.
         *
         * @return a value indicating whether cache hits should be handled or not
         */
        public boolean alterPromotionOnHit() {
            return this == H || this == HM || this == RHM;
        }

        /**
         * Get a value indicating whether cache misses should be handled or not.
         *
         * @return a value indicating whether cache misses should be handled or not
         */
        public boolean alterInsertionOnMiss() {
            return this == M || this == HM || this == RHM;
        }
    }

    private PolicyType type;

    private int bimodalSuggestionThrottle;

    private Random random;

    /**
     * Create a prefetch aware HM least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public PrefetchAwareHMLRUPolicy(EvictableCache<StateT> cache, PolicyType type) {
        super(cache);

        this.type = type;

        this.bimodalSuggestionThrottle = 50;

        this.random = new Random(13);
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        if(this.type.alterReplacement() && this.shouldVictimizeMainThreadLine()) {
            for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
                int way = this.getWayInStackPosition(set, stackPosition);
                if (lineFoundIsMainThread(set, way)) {
                    //priority: useful HT > useless HT > MT
                    return new CacheAccess<>(this.getCache(), access, set, way, tag);
                }
            }
        }

        return new CacheAccess<>(this.getCache(), access, set, this.getLRU(set), tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if(this.type.alterPromotionOnHit() && access.getType() == MemoryHierarchyAccessType.LOAD && requesterIsMainThread(access) && lineFoundIsHelperThread(set, way)) {
            // HT-MT inter-thread hit, never used again: low locality => Demote to LRU position
            this.setLRU(set, way);
            return;
        }

        super.handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if(this.type.alterInsertionOnMiss() && access.getType() == MemoryHierarchyAccessType.LOAD && (requesterIsMainThread(access) || !isUseful(access.getVirtualPc()))) {
            // Thrashing MT miss or Non-useful HT miss, prevented from thrashing: low locality => insert in LRU position
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
     * Get a value indicating whether the line found in the specified set index and way is brought by the main thread or not.
     *
     * @param set the set index
     * @param way the way
     * @return a value indicating whether the line found in the specified set index and way is brought by the main thread or not
     */
    private boolean lineFoundIsMainThread(int set, int way) {
        return HelperThreadingHelper.isMainThread(this.getCache().getLine(set, way).getAccess().getThread());
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
        return getCache().getSimulation().getHelperThreadL2RequestProfilingHelper()
                .getHelperThreadL2RequestUsefulnessPredictor().predict(pc);
    }

    /**
     * Get a value indicating whether the main thread LRU line should be victimized for replacement or not.
     *
     * @return a value indicating whether the main thread LRU line should be victimized for replacement or not
     */
    private boolean shouldVictimizeMainThreadLine() {
        return this.random.nextInt(100) >= this.bimodalSuggestionThrottle;
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
