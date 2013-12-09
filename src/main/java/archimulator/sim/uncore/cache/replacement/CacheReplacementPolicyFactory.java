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
import archimulator.sim.uncore.cache.replacement.costAware.helperThread.HelperThreadSensitiveLRUPolicy;
import archimulator.sim.uncore.cache.replacement.costAware.mlp.LinearMLPAwareLRUPolicy;
import archimulator.sim.uncore.cache.replacement.costAware.mlp.MLPSensitiveLRUPolicy;
import archimulator.sim.uncore.cache.replacement.deadBlockPrediction.DeadBlockPredictionBasedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadAwareLRUPolicy3;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadIntervalAwareLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.HelperThreadAndMLPAwarePartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.SimpleStaticPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.StaticPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.cpiBased.CPIBasedPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.minMiss.MinMissPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.mlpAware.MLPAwarePartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.partitioned.setDueling.SetDuelingPartitionedLRUPolicy;
import archimulator.sim.uncore.cache.replacement.prefetchAware.HelperThreadPrefetchAccuracyBasedHelperThreadAwareAndReuseDistancePredictionSetDuelingPolicy;
import archimulator.sim.uncore.cache.replacement.prefetchAware.HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy;
import archimulator.sim.uncore.cache.replacement.prefetchAware.PrefetchAwareHMLRUPolicy;
import archimulator.sim.uncore.cache.replacement.prefetchAware.PrefetchAwareSetDuelingHMLRUPolicy;
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
                return new LRUPolicy<>(cache);
            case LFU:
                return new LFUPolicy<>(cache);
            case RANDOM:
                return new RandomPolicy<>(cache);
            case HELPER_THREAD_INTERVAL_AWARE_LRU:
                return new HelperThreadIntervalAwareLRUPolicy<>(cache);
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
            case HELPER_THREAD_PREFETCH_ACCURACY_BASED_PREFETCH_AWARE_AND_REUSE_DISTANCE_PREDICTION_SET_DUELING:
                return new HelperThreadPrefetchAccuracyBasedHelperThreadAwareAndReuseDistancePredictionSetDuelingPolicy<>(cache);
            case HELPER_THREAD_USEFUL_PREFETCH_BASED_PREFETCH_AWARE_AND_REUSE_DISTANCE_PREDICTION_SET_DUELING:
                return new HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy<>(cache);
            case DEAD_BLOCK_PREDICTION_LRU:
                return new DeadBlockPredictionBasedLRUPolicy<>(cache);
            default:
                throw new IllegalArgumentException();
        }
    }
}
