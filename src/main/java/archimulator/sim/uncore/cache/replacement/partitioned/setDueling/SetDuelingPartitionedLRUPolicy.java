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
    private List<CachePartitioningHelper> cachePartitioningHelpers;
    private SetDuelingUnit setDuelingUnit;

    private List<Long> numCachePartitioningHelpersUsed;

    /**
     * Create a set dueling partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache                    the parent evictable cache
     * @param cachePartitioningHelpers the array of cache partitioning helpers
     */
    public SetDuelingPartitionedLRUPolicy(
            final EvictableCache<StateT> cache,
            CachePartitioningHelper... cachePartitioningHelpers
    ) {
        super(cache);

        this.numCachePartitioningHelpersUsed = new ArrayList<Long>();

        this.cachePartitioningHelpers = Arrays.asList(cachePartitioningHelpers);

        for(int i = 0; i < this.cachePartitioningHelpers.size(); i++) {
            this.numCachePartitioningHelpersUsed.add(0L);

            CachePartitioningHelper cachePartitioningHelper = this.cachePartitioningHelpers.get(i);

            final int tempI = i;
            cachePartitioningHelper.setShouldIncludePredicate(new Predicate<Integer>() {
                @Override
                public boolean apply(Integer set) {
                    return setDuelingUnit.getPartitioningPolicyType(set) == tempI;
                }
            });
        }

        this.setDuelingUnit = new SetDuelingUnit(cache, 6, this.cachePartitioningHelpers.size());

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
        int setDuelingMonitorType = setDuelingUnit.getPartitioningPolicyType(set);

        CachePartitioningHelper partitioningHelper = this.cachePartitioningHelpers.get(setDuelingMonitorType);
        this.numCachePartitioningHelpersUsed.set(setDuelingMonitorType, this.numCachePartitioningHelpersUsed.get(setDuelingMonitorType) + 1);

        return partitioningHelper.getPartition();
    }

    @Override
    public void dumpStats(final ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "setDuelingPartitionedLRUPolicy") {{
            for (int i = 0; i < cachePartitioningHelpers.size(); i++) {
                getChildren().add(new ReportNode(this, "numCachePartitioningHelperUsed[" + i + "]", numCachePartitioningHelpersUsed.get(i) + ""));
            }

            for (int way : getNumPartitionsPerWay().keySet()) {
                getChildren().add(new ReportNode(this, "numPartitionsPerWay[" + way + "]", getNumPartitionsPerWay().get(way) + ""));
            }

            for (CachePartitioningHelper cachePartitioningHelper : cachePartitioningHelpers) {
                cachePartitioningHelper.dumpStats(reportNode);
            }
        }});
    }

    /**
     * get the list of cache partitioning helpers.
     *
     * @return the list of cache partitioning helpers
     */
    public List<CachePartitioningHelper> getCachePartitioningHelpers() {
        return cachePartitioningHelpers;
    }
}
