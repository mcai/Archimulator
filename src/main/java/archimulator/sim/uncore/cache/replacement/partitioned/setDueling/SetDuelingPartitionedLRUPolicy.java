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
import net.pickapack.action.Predicate;

import java.io.Serializable;
import java.util.List;

/**
 * Set dueling partitioned least recently used (LRU) policy based on helper thread L2 request breakdown.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SetDuelingPartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    private CachePartitioningHelper cachePartitioningHelper1;
    private CachePartitioningHelper cachePartitioningHelper2;
    private CachePartitioningHelper cachePartitioningHelper3;
    private SetDuelingUnit setDuelingUnit;

    private long numCachePartitioningHelper1Used;
    private long numCachePartitioningHelper2Used;
    private long numCachePartitioningHelper3Used;

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache                    the parent evictable cache
     * @param cachePartitioningHelper1 the first cache partitioning helper
     * @param cachePartitioningHelper2 the second cache partitioning helper
     * @param cachePartitioningHelper3 the third cache partitioning helper
     */
    public SetDuelingPartitionedLRUPolicy(
            final EvictableCache<StateT> cache,
            CachePartitioningHelper cachePartitioningHelper1,
            CachePartitioningHelper cachePartitioningHelper2,
            CachePartitioningHelper cachePartitioningHelper3
    ) {
        super(cache);

        this.cachePartitioningHelper1 = cachePartitioningHelper1;
        this.cachePartitioningHelper2 = cachePartitioningHelper2;
        this.cachePartitioningHelper3 = cachePartitioningHelper3;

        this.cachePartitioningHelper1.setShouldIncludePredicate(new Predicate<Integer>() {
            @Override
            public boolean apply(Integer set) {
                return setDuelingUnit.getPartitioningPolicyType(set) ==
                        SetDuelingUnit.SetDuelingMonitorType.POLICY1;
            }
        });

        this.cachePartitioningHelper2.setShouldIncludePredicate(new Predicate<Integer>() {
            @Override
            public boolean apply(Integer set) {
                return setDuelingUnit.getPartitioningPolicyType(set) ==
                        SetDuelingUnit.SetDuelingMonitorType.POLICY2;
            }
        });

        this.cachePartitioningHelper3.setShouldIncludePredicate(new Predicate<Integer>() {
            @Override
            public boolean apply(Integer set) {
                return setDuelingUnit.getPartitioningPolicyType(set) ==
                        SetDuelingUnit.SetDuelingMonitorType.POLICY3;
            }
        });

        this.setDuelingUnit = new SetDuelingUnit(cache, 6);

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
        SetDuelingUnit.SetDuelingMonitorType setDuelingMonitorType = setDuelingUnit.getPartitioningPolicyType(set);

        CachePartitioningHelper partitioningHelper;

        switch (setDuelingMonitorType) {
            case POLICY1:
                partitioningHelper = getCachePartitioningHelper1();
                numCachePartitioningHelper1Used++;
                break;
            case POLICY2:
                partitioningHelper = getCachePartitioningHelper2();
                numCachePartitioningHelper2Used++;
                break;
            case POLICY3:
                partitioningHelper = getCachePartitioningHelper3();
                numCachePartitioningHelper3Used++;
                break;
            default:
                throw new IllegalArgumentException();
        }

        return partitioningHelper.getPartition();
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "setDuelingPartitionedLRUPolicy") {{
            getChildren().add(new ReportNode(this, "numCachePartitioningHelper1Used", numCachePartitioningHelper1Used + ""));
            getChildren().add(new ReportNode(this, "numCachePartitioningHelper2Used", numCachePartitioningHelper2Used + ""));
            getChildren().add(new ReportNode(this, "numCachePartitioningHelper3Used", numCachePartitioningHelper3Used + ""));

            for (int way : getNumPartitionsPerWay().keySet()) {
                getChildren().add(new ReportNode(this, "numPartitionsPerWay[" + way + "]", getNumPartitionsPerWay().get(way) + ""));
            }

            getCachePartitioningHelper1().dumpStats(reportNode);
            getCachePartitioningHelper2().dumpStats(reportNode);
            getCachePartitioningHelper3().dumpStats(reportNode);
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

    /**
     * Get the third cache partitioning helper.
     *
     * @return the third cache partitioning helper
     */
    public CachePartitioningHelper getCachePartitioningHelper3() {
        return cachePartitioningHelper3;
    }
}
