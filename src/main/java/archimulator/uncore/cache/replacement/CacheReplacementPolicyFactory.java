/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement;

import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.partitioning.Partitioner;
import archimulator.uncore.cache.partitioning.cpiBased.CPIBasedCachePartitioningHelper;
import archimulator.uncore.cache.partitioning.minMiss.MinMissCachePartitioningHelper;
import archimulator.uncore.cache.partitioning.mlpAware.MLPAwareCachePartitioningHelper;
import archimulator.uncore.cache.replacement.costAware.helperThread.HelperThreadSensitiveLRUPolicy;
import archimulator.uncore.cache.replacement.costAware.mlp.LinearMLPAwareLRUPolicy;
import archimulator.uncore.cache.replacement.costAware.mlp.MLPSensitiveLRUPolicy;
import archimulator.uncore.cache.replacement.deadBlockPrediction.DeadBlockPredictionBasedLRUPolicy;
import archimulator.uncore.cache.replacement.helperThread.HelperThreadAwareLRUPolicy3;
import archimulator.uncore.cache.replacement.partitioned.HelperThreadAndMLPAwarePartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.SimpleStaticPartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.StaticPartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.cpiBased.CPIBasedPartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.minMiss.MinMissPartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.mlpAware.MLPAwarePartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.partitioned.setDueling.SetDuelingPartitionedLRUPolicy;
import archimulator.uncore.cache.replacement.prefetchAware.HelperThreadPrefetchAccuracyBasedHelperThreadAwareAndReuseDistancePredictionSetDuelingPolicy;
import archimulator.uncore.cache.replacement.prefetchAware.HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy;
import archimulator.uncore.cache.replacement.prefetchAware.PrefetchAwareHMLRUPolicy;
import archimulator.uncore.cache.replacement.prefetchAware.PrefetchAwareSetDuelingHMLRUPolicy;
import archimulator.uncore.cache.replacement.rereferenceIntervalPrediction.RereferenceIntervalPredictionPolicy;
import archimulator.uncore.cache.replacement.reuseDistancePrediction.ReuseDistancePredictionPolicy;

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
                return new LRUPolicy<>(cache);
            case LFU:
                return new LFUPolicy<>(cache);
            case RANDOM:
                return new RandomPolicy<>(cache);
            case HELPER_THREAD_AWARE_LRU_3:
                return new HelperThreadAwareLRUPolicy3<>(cache);
            case REUSE_DISTANCE_PREDICTION:
                return new ReuseDistancePredictionPolicy<>(cache);
            case REREFERENCE_INTERVAL_PREDICTION:
                return new RereferenceIntervalPredictionPolicy<>(cache);
            case STATIC_CACHE_PARTITIONING_LRU:
                return new StaticPartitionedLRUPolicy<>(cache);
            case SIMPLE_STATIC_CACHE_PARTITIONING_LRU:
                return new SimpleStaticPartitionedLRUPolicy<>(cache);
            case CPI_BASED_CACHE_PARTITIONING_LRU:
                return new CPIBasedPartitionedLRUPolicy<>(cache);
            case MIN_MISS_CACHE_PARTITIONING_LRU:
                return new MinMissPartitionedLRUPolicy<>(cache);
            case MLP_AWARE_CACHE_PARTITIONING_LRU:
                return new MLPAwarePartitionedLRUPolicy<>(cache);
            case SET_DUELING_CACHE_PARTITIONING_LRU:
                return new SetDuelingPartitionedLRUPolicy<>(
                        cache,
                        new CPIBasedCachePartitioningHelper(cache),
                        new MinMissCachePartitioningHelper(cache),
                        new MLPAwareCachePartitioningHelper(cache)
                );
            case SET_DUELING_STATIC_CACHE_PARTITIONING_LRU:
                return new SetDuelingPartitionedLRUPolicy<>(cache, new ArrayList<Partitioner>() {{
                    for (int i = 1; i < cache.getAssociativity(); i++) {
                        add(new StaticPartitionedLRUPolicy<>(cache, i));
                    }
                }});
            case HELPER_THREAD_AND_MLP_AWARE_CACHE_PARTITIONING_LRU:
                return new HelperThreadAndMLPAwarePartitionedLRUPolicy<>(cache);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_0:
                return new LinearMLPAwareLRUPolicy<>(cache, 0);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_1:
                return new LinearMLPAwareLRUPolicy<>(cache, 1);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_2:
                return new LinearMLPAwareLRUPolicy<>(cache, 2);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_3:
                return new LinearMLPAwareLRUPolicy<>(cache, 3);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_4:
                return new LinearMLPAwareLRUPolicy<>(cache, 4);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_5:
                return new LinearMLPAwareLRUPolicy<>(cache, 5);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_6:
                return new LinearMLPAwareLRUPolicy<>(cache, 6);
            case LINEAR_MLP_AWARE_LRU_LAMBDA_7:
                return new LinearMLPAwareLRUPolicy<>(cache, 7);
            case MLP_SENSITIVE_LRU:
                return new MLPSensitiveLRUPolicy<>(cache);
            case HELPER_THREAD_SENSITIVE_LRU:
                return new HelperThreadSensitiveLRUPolicy<>(cache);
            case PREFETCH_AWARE_R_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.R);
            case PREFETCH_AWARE_H_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.H);
            case PREFETCH_AWARE_M_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.M);
            case PREFETCH_AWARE_RH_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.RH);
            case PREFETCH_AWARE_HM_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.HM);
            case PREFETCH_AWARE_RM_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.RM);
            case PREFETCH_AWARE_RHM_LRU:
                return new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.RHM);
            case PREFETCH_AWARE_SET_DUELING_HM_LRU:
                return new PrefetchAwareSetDuelingHMLRUPolicy<>(cache);
            case HT_PREF_ACC_BASED_PREF_AND_RDP_SET_DUELING:
                return new HelperThreadPrefetchAccuracyBasedHelperThreadAwareAndReuseDistancePredictionSetDuelingPolicy<>(cache);
            case HT_USEFUL_PREF_BASED_PREF_AND_RDP_SET_DUELING:
                return new HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy<>(cache);
            case DEAD_BLOCK_PREDICTION_LRU:
                return new DeadBlockPredictionBasedLRUPolicy<>(cache);
            default:
                throw new IllegalArgumentException();
        }
    }
}
