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
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper.getThreadIdentifier;

/**
 * Partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class PartitionedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private Map<Integer, Long> numPartitionsPerWay;

    /**
     * Create a partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public PartitionedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.numPartitionsPerWay = new TreeMap<>();
        for (int i = 0; i < cache.getAssociativity(); i++) {
            this.numPartitionsPerWay.put(i, 0L);
        }
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        if (CachePartitioningHelper.canPartition(getCache())) {
            int numUsedWays = 0;

            for (int way = 0; way < this.getCache().getAssociativity(); way++) {
                CacheLine<StateT> line = this.getCache().getLine(set, way);
                if (line.getAccess() != null && getThreadIdentifier(line.getAccess().getThread()) == getThreadIdentifier(access.getThread())) {
                    numUsedWays++;
                }
            }

            if (numUsedWays < doGetPartition(set).get(getThreadIdentifier(access.getThread()))) {
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
        } else {
            return new CacheAccess<StateT>(this.getCache(), access, set, this.getLRU(set), tag);
        }
    }

    private List<Integer> doGetPartition(int set) {
        List<Integer> partition = getPartition(set);
        numPartitionsPerWay.put(partition.get(0), numPartitionsPerWay.get(partition.get(0)) + 1);
        return partition;
    }

    /**
     * Get the partition for the specified set.
     *
     * @param set the set
     * @return the partition for the specified set
     */
    protected abstract List<Integer> getPartition(int set);

    /**
     * Get the number of partitions with the specified main thread way.
     *
     * @return the number of partitions with the specified main thread way
     */
    public Map<Integer, Long> getNumPartitionsPerWay() {
        return numPartitionsPerWay;
    }
}
