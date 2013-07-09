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
package archimulator.sim.uncore.cache.replacement.partitioned;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;
import java.util.List;

import static archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper.getThreadIdentifier;

/**
 * Partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class PartitionedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public PartitionedLRUPolicy(EvictableCache<StateT> cache) {
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
            if (line.getAccess() != null && getThreadIdentifier(line.getAccess().getThread()) == getThreadIdentifier(access.getThread())) {
                numUsedWays++;
            }
        }

        if (numUsedWays < getPartition(set).get(getThreadIdentifier(access.getThread()))) {
            for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
                int way = this.getWayInStackPosition(set, stackPosition);
                CacheLine<StateT> line = this.getCache().getLine(set, way);
                if (line.getAccess() != null && getThreadIdentifier(line.getAccess().getThread()) != getThreadIdentifier(access.getThread())) {
                    return new CacheAccess<StateT>(this.getCache(), access, set, way, tag);
                }
            }
        } else {
            for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
                int way = this.getWayInStackPosition(set, stackPosition);
                CacheLine<StateT> line = this.getCache().getLine(set, way);
                if (line.getAccess() != null && getThreadIdentifier(line.getAccess().getThread()) == getThreadIdentifier(access.getThread())) {
                    return new CacheAccess<StateT>(this.getCache(), access, set, way, tag);
                }
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Get the partition for the specified set.
     *
     * @param set the set
     * @return the partition for the specified set
     */
    protected abstract List<Integer> getPartition(int set);
}
