/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore;

import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.uncore.coherence.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.LastLevelCacheConfig;
import archimulator.sim.uncore.dram.FixedLatencyMainMemoryConfig;
import archimulator.sim.uncore.dram.MainMemoryConfig;

public class MemoryHierarchyConfig {
    private FirstLevelCacheConfig instructionCache;
    private FirstLevelCacheConfig dataCache;
    private LastLevelCacheConfig l2Cache;
    private MainMemoryConfig mainMemory;

    public MemoryHierarchyConfig(FirstLevelCacheConfig instructionCache, FirstLevelCacheConfig dataCache, LastLevelCacheConfig l2Cache, MainMemoryConfig mainMemory) {
        this.instructionCache = instructionCache;
        this.dataCache = dataCache;
        this.l2Cache = l2Cache;
        this.mainMemory = mainMemory;
    }

    public FirstLevelCacheConfig getInstructionCache() {
        return instructionCache;
    }

    public FirstLevelCacheConfig getDataCache() {
        return dataCache;
    }

    public LastLevelCacheConfig getL2Cache() {
        return l2Cache;
    }

    public MainMemoryConfig getMainMemory() {
        return mainMemory;
    }

    public static MemoryHierarchyConfig createDefaultMemoryHierarchyConfig(EvictionPolicyFactory l2EvictionPolicyFactory) {
        FirstLevelCacheConfig l1ICacheConfig = new FirstLevelCacheConfig(32768, 4, LeastRecentlyUsedEvictionPolicy.FACTORY);
        FirstLevelCacheConfig l1DCacheConfig = new FirstLevelCacheConfig(32768, 8, LeastRecentlyUsedEvictionPolicy.FACTORY);

//        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(4194304, 8, l2EvictionPolicyFactory);
//        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(524288 * 2, 8, l2EvictionPolicyFactory);
//        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(524288 * 4, 8, l2EvictionPolicyFactory);
        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(524288 * 8, 8, l2EvictionPolicyFactory);
//        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(32768 * 2, 8, l2EvictionPolicyFactory);

        MainMemoryConfig mainMemoryConfig = new FixedLatencyMainMemoryConfig();

        return new MemoryHierarchyConfig(l1ICacheConfig, l1DCacheConfig, l2CacheConfig, mainMemoryConfig);
    }
}