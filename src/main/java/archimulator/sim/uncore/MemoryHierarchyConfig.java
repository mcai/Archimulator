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

import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.LastLevelCacheConfig;
import archimulator.sim.uncore.dram.FixedLatencyMainMemoryConfig;
import archimulator.sim.uncore.dram.MainMemoryConfig;

import java.io.Serializable;

public class MemoryHierarchyConfig implements Serializable {
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

    public static MemoryHierarchyConfig createDefaultMemoryHierarchyConfig(int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz) {
        FirstLevelCacheConfig l1ICacheConfig = new FirstLevelCacheConfig(32768, 4, LRUPolicy.class);
        FirstLevelCacheConfig l1DCacheConfig = new FirstLevelCacheConfig(32768, 8, LRUPolicy.class);

        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(l2Size, l2Associativity, l2EvictionPolicyClz);

        MainMemoryConfig mainMemoryConfig = new FixedLatencyMainMemoryConfig();

        return new MemoryHierarchyConfig(l1ICacheConfig, l1DCacheConfig, l2CacheConfig, mainMemoryConfig);
    }
}
