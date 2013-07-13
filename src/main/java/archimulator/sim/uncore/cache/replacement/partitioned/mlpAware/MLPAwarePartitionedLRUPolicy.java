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
package archimulator.sim.uncore.cache.replacement.partitioned.mlpAware;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper;
import archimulator.sim.uncore.cache.replacement.partitioned.PartitionedLRUPolicy;
import net.pickapack.action.Predicate;

import java.io.Serializable;
import java.util.List;

/**
 * MLP-aware partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class MLPAwarePartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private MLPAwareCachePartitioningHelper mlpAwareCachePartitioningHelper;

    /**
     * Create a MLP-aware partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public MLPAwarePartitionedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.mlpAwareCachePartitioningHelper = new MLPAwareCachePartitioningHelper(cache);
        this.mlpAwareCachePartitioningHelper.setShouldIncludePredicate(new Predicate<Integer>() {
            @Override
            public boolean apply(Integer param) {
                return true;
            }
        });
    }

    @Override
    protected List<Integer> getPartition(int set) {
        return getMlpAwareCachePartitioningHelper().getPartition();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        for (int way : getNumPartitionsPerWay().keySet()) {
            reportNode.getChildren().add(new ReportNode(reportNode, "numPartitionsPerWay[" + way + "]", getNumPartitionsPerWay().get(way) + ""));
        }
        getMlpAwareCachePartitioningHelper().dumpStats(reportNode);
    }

    /**
     * Get the MLP-aware cache partitioning helper.
     *
     * @return the MLP-aware cache partitioning helper
     */
    public MLPAwareCachePartitioningHelper getMlpAwareCachePartitioningHelper() {
        return mlpAwareCachePartitioningHelper;
    }
}
