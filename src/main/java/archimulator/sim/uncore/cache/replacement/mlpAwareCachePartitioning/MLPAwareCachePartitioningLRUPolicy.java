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
package archimulator.sim.uncore.cache.replacement.mlpAwareCachePartitioning;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;

/**
 * MLP-aware cache partitioning based least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class MLPAwareCachePartitioningLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a MLP-aware cache partitioning based least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public MLPAwareCachePartitioningLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        int numUsedWays = 0;

        for (int way = 0; way < this.getCache().getAssociativity(); way++) {
            CacheLine<StateT> line = this.getCache().getLine(set, way);
            if (line.getAccess() != null && line.getAccess().getThread().getId() == access.getThread().getId()) {
                numUsedWays++;
            }
        }

        if (numUsedWays < getCache().getSimulation().getMlpAwareCachePartitioningHelper().getPartition().get(access.getThread().getId())) {
            for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
                int way = this.getWayInStackPosition(set, stackPosition);
                CacheLine<StateT> line = this.getCache().getLine(set, way);
                if (line.getAccess() != null && line.getAccess().getThread().getId() != access.getThread().getId()) {
                    return new CacheAccess<StateT>(this.getCache(), access, set, way, tag);
                }
            }
        } else {
            for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
                int way = this.getWayInStackPosition(set, stackPosition);
                CacheLine<StateT> line = this.getCache().getLine(set, way);
                if (line.getAccess() != null && line.getAccess().getThread().getId() == access.getThread().getId()) {
                    return new CacheAccess<StateT>(this.getCache(), access, set, way, tag);
                }
            }
        }

        throw new IllegalArgumentException();
    }
}
