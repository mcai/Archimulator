/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

/**
 * Least recently used (LRU) policy.
 *
 * @author Min Cai
 * @param <StateT> the state type of the parent evictable cache
 */
public class LRUPolicy<StateT extends Serializable> extends StackBasedCacheReplacementPolicy<StateT> {
    /**
     * Create a least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public LRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @param tag the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        return new CacheAccess<StateT>(this.getCache(), access, set, this.getLRU(set), tag);
    }

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @param way the way
     */
    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.setMRU(set, way);
    }

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @param way the way
     */
    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.setMRU(set, way);
    }
}
