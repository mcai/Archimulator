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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.partitioning.Partitioner;
import archimulator.sim.uncore.cache.partitioning.cpiBased.CPIBasedCachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.minMiss.MinMissCachePartitioningHelper;
import archimulator.sim.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadAwareLRUPolicy;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadIntervalAwareLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.HelperThreadAndMLPAwarePartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.StaticPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.cpiBased.CPIBasedPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.minMiss.MinMissPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.mlpAware.MLPAwarePartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.setDueling.SetDuelingPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.rereferenceIntervalPrediction.RereferenceIntervalPredictionPolicy;
import archimulator.sim.uncore.cache.replacement.reuseDistancePrediction.ReuseDistancePredictionPolicy;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Cache replacement policy factory.
 *
 * @author Min Cai
 */
public class CacheReplacementPolicyFactory {
    /**
     * Create a cache replacement policy for the specified evictable cache and based on the specified type.
     *
     * @param <StateT>                   the state type of the evictable cache
     * @param cacheReplacementPolicyType the cache replacement policy type
     * @param cache                      the evictable cache
     * @return the newly created cache replacement policy for the specified evictable cache and based on the specified type
     */
    public static <StateT extends Serializable> CacheReplacementPolicy<StateT> createCacheReplacementPolicy(CacheReplacementPolicyType cacheReplacementPolicyType, final EvictableCache<StateT> cache) {
        switch (cacheReplacementPolicyType) {
            case LRU:
                return new LRUPolicy<StateT>(cache);
            case LFU:
                return new LFUPolicy<StateT>(cache);
            case RANDOM:
                return new RandomPolicy<StateT>(cache);
            case HELPER_THREAD_AWARE_LRU:
                return new HelperThreadAwareLRUPolicy<StateT>(cache, false);
            case HELPER_THREAD_INTERVAL_AWARE_LRU:
                return new HelperThreadIntervalAwareLRUPolicy<StateT>(cache);
            case HELPER_THREAD_AWARE_BREAKDOWN_LRU:
                return new HelperThreadAwareLRUPolicy<StateT>(cache, true);
            case REUSE_DISTANCE_PREDICTION:
                return new ReuseDistancePredictionPolicy<StateT>(cache);
            case REREFERENCE_INTERVAL_PREDICTION:
                return new RereferenceIntervalPredictionPolicy<StateT>(cache);
            case STATIC_CACHE_PARTITIONING_LRU:
                return new StaticPartitionedLRUPolicy<StateT>(cache);
            case CPI_BASED_CACHE_PARTITIONING_LRU:
                return new CPIBasedPartitionedLRUPolicy<StateT>(cache);
            case MIN_MISS_CACHE_PARTITIONING_LRU:
                return new MinMissPartitionedLRUPolicy<StateT>(cache);
            case MLP_AWARE_CACHE_PARTITIONING_LRU:
                return new MLPAwarePartitionedLRUPolicy<StateT>(cache);
            case SET_DUELING_CACHE_PARTITIONING_LRU:
                return new SetDuelingPartitionedLRUPolicy<StateT>(
                        cache,
                        new CPIBasedCachePartitioningHelper(cache),
                        new MinMissCachePartitioningHelper(cache),
                        new MLPAwareCachePartitioningHelper(cache)
                );
            case SET_DUELING_STATIC_CACHE_PARTITIONING_LRU:
                return new SetDuelingPartitionedLRUPolicy<StateT>(cache, new ArrayList<Partitioner>() {{
                    for(int i = 1; i < cache.getAssociativity(); i++) {
                        add(new StaticPartitionedLRUPolicy<StateT>(cache, i));
                    }
                }});
            case HELPER_THREAD_AND_MLP_AWARE_CACHE_PARTITIONING_LRU:
                return new HelperThreadAndMLPAwarePartitionedLRUPolicy<StateT>(cache);
            default:
                throw new IllegalArgumentException();
        }
    }
}
