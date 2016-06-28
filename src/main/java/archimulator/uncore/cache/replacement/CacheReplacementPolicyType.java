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

/**
 * Cache replacement policy type.
 *
 * @author Min Cai
 */
public enum CacheReplacementPolicyType {
    /**
     * Least recently used (LRU).
     */
    LRU,

    /**
     * Least frequently used (LFU).
     */
    LFU,

    /**
     * Random.
     */
    RANDOM,

    /**
     * Helper thread aware least recently used (LRU) policy 3.
     */
    HELPER_THREAD_AWARE_LRU_3,

    /**
     * Reuse distance prediction.
     */
    REUSE_DISTANCE_PREDICTION,

    /**
     * Rereference interval prediction (RRIP).
     */
    REREFERENCE_INTERVAL_PREDICTION,

    /**
     * Static cache partitioning based least recently used (LRU).
     */
    STATIC_CACHE_PARTITIONING_LRU,

    /**
     * Simple static cache partitioning based least recently used (LRU).
     */
    SIMPLE_STATIC_CACHE_PARTITIONING_LRU,

    /**
     * CPI based cache partitioning based least recently used (LRU).
     */
    CPI_BASED_CACHE_PARTITIONING_LRU,

    /**
     * Min-miss cache partitioning based least recently used (LRU).
     */
    MIN_MISS_CACHE_PARTITIONING_LRU,

    /**
     * MLP-aware cache partitioning based least recently used (LRU).
     */
    MLP_AWARE_CACHE_PARTITIONING_LRU,

    /**
     * Set dueling cache partitioning based least recently used (LRU).
     */
    SET_DUELING_CACHE_PARTITIONING_LRU,

    /**
     * Set dueling static cache partitioning based least recently used (LRU).
     */
    SET_DUELING_STATIC_CACHE_PARTITIONING_LRU,

    /**
     * Helper thread and MLP-aware cache partitioning based least recently used (LRU).
     */
    HELPER_THREAD_AND_MLP_AWARE_CACHE_PARTITIONING_LRU,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 0.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_0,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 1.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_1,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 2.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_2,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 3.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_3,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 4.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_4,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 5.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_5,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 6.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_6,

    /**
     * Linear MLP-aware least recently used (LRU), with lambda being 7.
     */
    LINEAR_MLP_AWARE_LRU_LAMBDA_7,

    /**
     * MLP-sensitive least recently used (LRU).
     */
    MLP_SENSITIVE_LRU,

    /**
     * Helper thread sensitive least recently used (LRU).
     */
    HELPER_THREAD_SENSITIVE_LRU,

    /**
     * Prefetch aware R least recently used (LRU).
     */
    PREFETCH_AWARE_R_LRU,

    /**
     * Prefetch aware H least recently used (LRU).
     */
    PREFETCH_AWARE_H_LRU,

    /**
     * Prefetch aware M least recently used (LRU).
     */
    PREFETCH_AWARE_M_LRU,

    /**
     * Prefetch aware RH least recently used (LRU).
     */
    PREFETCH_AWARE_RH_LRU,

    /**
     * Prefetch aware HM least recently used (LRU).
     */
    PREFETCH_AWARE_HM_LRU,

    /**
     * Prefetch aware RM least recently used (LRU).
     */
    PREFETCH_AWARE_RM_LRU,

    /**
     * Prefetch aware RHM least recently used (LRU).
     */
    PREFETCH_AWARE_RHM_LRU,

    /**
     * Prefetch aware set dueling based HM least recently used (LRU).
     */
    PREFETCH_AWARE_SET_DUELING_HM_LRU,

    /**
     * Helper thread prefetch accuracy based prefetch aware and reuse distance prediction set dueling.
     */
    HT_PREF_ACC_BASED_PREF_AND_RDP_SET_DUELING,

    /**
     * Helper thread useful prefetch based prefetch aware and reuse distance prediction set dueling).
     */
    HT_USEFUL_PREF_BASED_PREF_AND_RDP_SET_DUELING,

    /**
     * Dead block prediction based least recently used (LRU).
     */
    DEAD_BLOCK_PREDICTION_LRU
}
