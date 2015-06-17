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
package archimulator.uncore.cache.replacement.partitioned;

import archimulator.common.report.ReportNode;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.partitioning.Partitioner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Static partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class StaticPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> implements Partitioner {
    private int numMainThreadWays;

    /**
     * Create a static partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public StaticPartitionedLRUPolicy(EvictableCache<StateT> cache) {
        this(cache, cache.getExperiment().getNumMainThreadWaysInStaticPartitionedLRUPolicy());
    }

    /**
     * Create a static partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache             the parent evictable cache
     * @param numMainThreadWays the number of main thread ways
     */
    public StaticPartitionedLRUPolicy(EvictableCache<StateT> cache, int numMainThreadWays) {
        super(cache);
        this.numMainThreadWays = numMainThreadWays;
    }

    @Override
    protected List<Integer> getPartition(int set) {
        return new ArrayList<Integer>() {{
            add(numMainThreadWays);
            add(getCache().getAssociativity() - numMainThreadWays);
        }};
    }

    @Override
    public List<Integer> getPartition() {
        return getPartition(0);
    }

    @Override
    public void setShouldIncludePredicate(Predicate<Integer> shouldIncludePredicate) {
    }
}
