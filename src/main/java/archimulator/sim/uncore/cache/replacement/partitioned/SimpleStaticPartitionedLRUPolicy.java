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
import archimulator.sim.uncore.cache.partitioning.Partitioner;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import net.pickapack.action.Predicate;
import net.pickapack.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper.getThreadIdentifier;

/**
 * Simple static partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SimpleStaticPartitionedLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> implements Partitioner {
    private List<Integer> partitions;
    private List<Pair<Integer,Integer>> partitionBoundaries;

    /**
     * Create a simple static partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public SimpleStaticPartitionedLRUPolicy(EvictableCache<StateT> cache) {
        this(cache, cache.getExperiment().getArchitecture().getNumMainThreadWaysInStaticPartitionedLRUPolicy());
    }

    /**
     * Create a simple static partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public SimpleStaticPartitionedLRUPolicy(EvictableCache<StateT> cache, final int numMainThreadWays) {
        super(cache);

        this.partitions = new ArrayList<Integer>() {{
            add(numMainThreadWays);
            add(getCache().getAssociativity() - numMainThreadWays);
        }};

        this.partitionBoundaries = new ArrayList<>();

        int previousWay;
        int currentWay = 0;

        for (Integer partition : this.partitions) {
            previousWay = currentWay;
            currentWay += partition;

            this.partitionBoundaries.add(new Pair<>(previousWay, currentWay - 1));
        }
    }

    @Override
    public CacheAccess<StateT> newMiss(MemoryHierarchyAccess access, int set, int address) {
        int tag = this.getCache().getTag(address);
        Pair<Integer, Integer> partitionBoundary = doGetPartitionBoundary(getThreadIdentifier(access.getThread()));

        for(int way = partitionBoundary.getFirst(); way <= partitionBoundary.getSecond(); way++) {
            CacheLine<StateT> line = this.getCache().getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<>(this.getCache(), access, set, way, tag);
            }
        }

        return this.handleReplacement(access, set, tag);
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
        Pair<Integer, Integer> partitionBoundary = doGetPartitionBoundary(getThreadIdentifier(access.getThread()));

        for (int stackPosition = this.getCache().getAssociativity() - 1; stackPosition >= 0; stackPosition--) {
            int way = this.getWayInStackPosition(set, stackPosition);

            if(way >= partitionBoundary.getFirst() && way <= partitionBoundary.getSecond()) {
                return new CacheAccess<>(this.getCache(), access, set, way, tag);
            }
        }

        throw new IllegalArgumentException();
    }

    private Pair<Integer, Integer> doGetPartitionBoundary(int set) {
        return partitionBoundaries.get(set);
    }

    @Override
    public List<Integer> getPartition() {
        return partitions;
    }

    @Override
    public void setShouldIncludePredicate(Predicate<Integer> shouldIncludePredicate) {
    }
}
