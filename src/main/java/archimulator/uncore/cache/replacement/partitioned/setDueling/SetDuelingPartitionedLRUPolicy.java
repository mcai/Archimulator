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
package archimulator.uncore.cache.replacement.partitioned.setDueling;

import archimulator.common.report.ReportNode;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.setDueling.MainThreadL2MissBasedSetDuelingUnit;
import archimulator.uncore.cache.setDueling.AbstractSetDuelingUnit;
import archimulator.uncore.cache.partitioning.Partitioner;
import archimulator.uncore.cache.replacement.partitioned.PartitionedLRUPolicy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set dueling partitioned least recently used (LRU) policy based on helper thread L2 request breakdown.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SetDuelingPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private List<Partitioner> partitioners;
    private AbstractSetDuelingUnit setDuelingUnit;

    private List<Long> numCachePartitioningHelpersUsed;

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache        the parent evictable cache
     * @param partitioners the array of partitioners
     */
    public SetDuelingPartitionedLRUPolicy(
            final EvictableCache<StateT> cache,
            Partitioner... partitioners
    ) {
        this(cache, Arrays.asList(partitioners));
    }

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache        the parent evictable cache
     * @param partitioners the list of partitioners
     */
    public SetDuelingPartitionedLRUPolicy(
            final EvictableCache<StateT> cache,
            List<Partitioner> partitioners
    ) {
        super(cache);

        this.numCachePartitioningHelpersUsed = new ArrayList<>();

        this.partitioners = partitioners;

        for (int i = 0; i < this.partitioners.size(); i++) {
            this.numCachePartitioningHelpersUsed.add(0L);

            Partitioner cachePartitioningHelper = this.partitioners.get(i);

            final int tempI = i;
            cachePartitioningHelper.setShouldIncludePredicate(set -> setDuelingUnit.getPolicyId(set, 0) == tempI);
        }

        this.setDuelingUnit = new MainThreadL2MissBasedSetDuelingUnit(cache, this.partitioners.size(), 6);
    }

    @Override
    protected List<Integer> getPartition(int set) {
        int policyId = setDuelingUnit.getPolicyId(set, 0);

        Partitioner partitioningHelper = this.partitioners.get(policyId);
        this.numCachePartitioningHelpersUsed.set(policyId, this.numCachePartitioningHelpersUsed.get(policyId) + 1);

        return partitioningHelper.getPartition();
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "setDuelingPartitionedLRUPolicy") {{
            for (int i = 0; i < partitioners.size(); i++) {
                getChildren().add(new ReportNode(this, "numCachePartitioningHelperUsed[" + i + "]", numCachePartitioningHelpersUsed.get(i) + ""));
            }

            for (int way : getNumPartitionsPerWay().keySet()) {
                getChildren().add(new ReportNode(this, "numPartitionsPerWay[" + way + "]", getNumPartitionsPerWay().get(way) + ""));
            }

            for (Partitioner cachePartitioningHelper : partitioners) {
                cachePartitioningHelper.dumpStats(reportNode);
            }
        }});
    }

    /**
     * get the list of partitioners.
     *
     * @return the list of partitioners
     */
    public List<Partitioner> getPartitioners() {
        return partitioners;
    }
}
