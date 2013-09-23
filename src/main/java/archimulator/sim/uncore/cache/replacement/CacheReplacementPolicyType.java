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
     * Helper thread aware least recently used (LRU).
     */
    HELPER_THREAD_AWARE_LRU,

    /**
     * Helper thread interval aware least recently used (LRU).
     */
    HELPER_THREAD_INTERVAL_AWARE_LRU,

    /**
     * Helper thread request breakdown enhanced least recently used (LRU).
     */
    HELPER_THREAD_AWARE_BREAKDOWN_LRU,

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
     * Linear MLP-aware least recently used (LRU).
     */
    LINEAR_MLP_AWARE_LRU
}
