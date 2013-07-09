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
package archimulator.sim.uncore.cache.replacement.partitioned.setDueling;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.CachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.minMiss.MinMissCachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper;
import archimulator.sim.uncore.cache.replacement.partitioned.PartitionedLRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerServiceNonblockingRequestEvent;
import net.pickapack.action.Action1;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Set dueling partitioned least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SetDuelingPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private MinMissCachePartitioningHelper minMissCachePartitioningHelper;
    private MLPAwareCachePartitioningHelper mlpAwareCachePartitioningHelper;
    private SetDuelingUnit setDuelingUnit;

    private long numMinMissUsed;
    private long minMlpAwareUsed;
    private Map<Integer, Long> numPartitionsPerWay;

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public SetDuelingPartitionedLRUPolicy(final EvictableCache<StateT> cache) {
        super(cache);
        this.minMissCachePartitioningHelper = new MinMissCachePartitioningHelper(cache);
        this.mlpAwareCachePartitioningHelper = new MLPAwareCachePartitioningHelper(cache);
        this.setDuelingUnit = new SetDuelingUnit(cache, ((1 << 10) - 1), 6);

        this.numPartitionsPerWay = new TreeMap<Integer, Long>();
        for(int i = 0; i < cache.getAssociativity(); i++) {
            this.numPartitionsPerWay.put(i, 0L);
        }

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerServiceNonblockingRequestEvent.class, new Action1<GeneralCacheControllerServiceNonblockingRequestEvent>() {
            public void apply(GeneralCacheControllerServiceNonblockingRequestEvent event) {
                if (event.getCacheController() == cache.getSimulation().getProcessor().getMemoryHierarchy().getL2CacheController() && !event.isHitInCache()) {
                    if (event.getAccess().getThread().getNum() == 0) { // TODO: To be refactored; if it is from the main thread for helper threaded prefetching.
                        setDuelingUnit.recordMiss(event.getSet());
                    }
                }
            }
        });
    }

    @Override
    protected List<Integer> getPartition(int set) {
        CachePartitioningHelper partitioningHelper =
                setDuelingUnit.getPartitioningPolicyType(set) ==
                        SetDuelingUnit.SetDuelingMonitorType.POLICY1 ?
                        getMinMissCachePartitioningHelper() :
                        getMlpAwareCachePartitioningHelper();

        if (partitioningHelper == minMissCachePartitioningHelper) {
            numMinMissUsed++;
        } else {
            minMlpAwareUsed++;
        }

        List<Integer> partition = partitioningHelper.getPartition(set);
        numPartitionsPerWay.put(partition.get(0), numPartitionsPerWay.get(partition.get(0)) + 1);

        return partition;
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "setDuelingPartitionedLRUPolicy") {{
            getChildren().add(new ReportNode(this, "numMinMissUsed", numMinMissUsed + ""));
            getChildren().add(new ReportNode(this, "minMlpAwareUsed", minMlpAwareUsed + ""));

            for(int way : numPartitionsPerWay.keySet()) {
                getChildren().add(new ReportNode(this, "numPartitionsPerWay[" + way + "]", numPartitionsPerWay.get(way) + ""));
            }

            getMinMissCachePartitioningHelper().dumpStats(reportNode);
            getMlpAwareCachePartitioningHelper().dumpStats(reportNode);
        }});
    }

    /**
     * get the min-miss cache partitioning helper.
     *
     * @return the min-miss cache partitioning helper
     */
    public MinMissCachePartitioningHelper getMinMissCachePartitioningHelper() {
        return minMissCachePartitioningHelper;
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
