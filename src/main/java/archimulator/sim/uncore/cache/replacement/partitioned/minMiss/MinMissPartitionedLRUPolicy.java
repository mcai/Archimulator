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
package archimulator.sim.uncore.cache.replacement.partitioned.minMiss;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.minMiss.MinMissCachePartitioningHelper;
import archimulator.sim.uncore.cache.replacement.partitioned.PartitionedLRUPolicy;

import java.io.Serializable;
import java.util.List;

/**
 * Min-miss partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class MinMissPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private MinMissCachePartitioningHelper minMissCachePartitioningHelper;

    /**
     * Create a min-miss partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public MinMissPartitionedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
        this.minMissCachePartitioningHelper = new MinMissCachePartitioningHelper(cache.getSimulation());
    }

    /**
     * Get the partition.
     *
     * @return the partition
     */
    @Override
    protected List<Integer> getPartition() {
        return getMinMissCachePartitioningHelper().getPartition();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        getMinMissCachePartitioningHelper().dumpStats(reportNode);
    }

    /**
     * get the min-miss cache partitioning helper.
     *
     * @return the min-miss cache partitioning helper
     */
    public MinMissCachePartitioningHelper getMinMissCachePartitioningHelper() {
        return minMissCachePartitioningHelper;
    }
}
