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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

/**
 * Cache replacement policy.
 *
 * @param <StateT>
 * @author Min Cai
 */
public abstract class CacheReplacementPolicy<StateT extends Serializable> {
    private EvictableCache<StateT> cache;

    /**
     * Create a cache replacement policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public CacheReplacementPolicy(EvictableCache<StateT> cache) {
        this.cache = cache;
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    public abstract CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag);

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    public abstract void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way);

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    public abstract void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way);

    /**
     * Get the parent evictable cache.
     *
     * @return the parent evictable cache
     */
    public EvictableCache<StateT> getCache() {
        return cache;
    }
}
