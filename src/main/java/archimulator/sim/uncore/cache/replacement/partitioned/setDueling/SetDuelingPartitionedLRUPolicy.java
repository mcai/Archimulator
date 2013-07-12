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
import archimulator.sim.uncore.cache.replacement.partitioned.PartitionedLRUPolicy;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import net.pickapack.action.Action1;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Set dueling partitioned least recently used (LRU) policy based on helper thread L2 request breakdown.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SetDuelingPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private CachePartitioningHelper cachePartitioningHelper1;
    private CachePartitioningHelper cachePartitioningHelper2;
    private SetDuelingUnit setDuelingUnit;

    private long numCachePartitioningHelper1Used;
    private long numCachePartitioningHelper2Used;
    private Map<Integer, Long> numPartitionsPerWay;

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache                    the parent evictable cache
     * @param cachePartitioningHelper1 the first cache partitioning helper
     * @param cachePartitioningHelper2 the second cache partitioning helper
     */
    public SetDuelingPartitionedLRUPolicy(
            final EvictableCache<StateT> cache,
            CachePartitioningHelper cachePartitioningHelper1,
            CachePartitioningHelper cachePartitioningHelper2
    ) {
        super(cache);
        this.cachePartitioningHelper1 = cachePartitioningHelper1;
        this.cachePartitioningHelper2 = cachePartitioningHelper2;
        this.setDuelingUnit = new SetDuelingUnit(cache, (1 << 10) - 1, 6);

        this.numPartitionsPerWay = new TreeMap<Integer, Long>();
        for (int i = 0; i < cache.getAssociativity(); i++) {
            this.numPartitionsPerWay.put(i, 0L);
        }

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent event) {
                setDuelingUnit.recordUsefulHelperThreadL2Request(event.getSet());
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent event) {
                setDuelingUnit.recordUsefulHelperThreadL2Request(event.getSet());
            }
        });
    }

    @Override
    protected List<Integer> getPartition(int set) {
        CachePartitioningHelper partitioningHelper =
                setDuelingUnit.getPartitioningPolicyType(set) ==
                        SetDuelingUnit.SetDuelingMonitorType.POLICY1 ?
                        getCachePartitioningHelper1() :
                        getCachePartitioningHelper2();

        if (partitioningHelper == cachePartitioningHelper1) {
            numCachePartitioningHelper1Used++;
        } else {
            numCachePartitioningHelper2Used++;
        }

        List<Integer> partition = partitioningHelper.getPartition(set);
        numPartitionsPerWay.put(partition.get(0), numPartitionsPerWay.get(partition.get(0)) + 1);

        return partition;
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "setDuelingPartitionedLRUPolicy") {{
            getChildren().add(new ReportNode(this, "numCachePartitioningHelper1Used", numCachePartitioningHelper1Used + ""));
            getChildren().add(new ReportNode(this, "numCachePartitioningHelper2Used", numCachePartitioningHelper2Used + ""));

            for (int way : numPartitionsPerWay.keySet()) {
                getChildren().add(new ReportNode(this, "numPartitionsPerWay[" + way + "]", numPartitionsPerWay.get(way) + ""));
            }

            getCachePartitioningHelper1().dumpStats(reportNode);
            getCachePartitioningHelper2().dumpStats(reportNode);
        }});
    }

    /**
     * get the first cache partitioning helper.
     *
     * @return the first cache partitioning helper
     */
    public CachePartitioningHelper getCachePartitioningHelper1() {
        return cachePartitioningHelper1;
    }

    /**
     * Get the second cache partitioning helper.
     *
     * @return the second cache partitioning helper
     */
    public CachePartitioningHelper getCachePartitioningHelper2() {
        return cachePartitioningHelper2;
    }
}
