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
import archimulator.sim.uncore.coherence.config.L1CacheControllerConfig;
import archimulator.sim.uncore.coherence.config.LastLevelCacheConfig;
import archimulator.sim.uncore.dram.FixedLatencyMainMemoryConfig;
import archimulator.sim.uncore.dram.MainMemoryConfig;

public class MemoryHierarchyConfig {
    private L1CacheControllerConfig l1ICacheController;
    private L1CacheControllerConfig l1DCacheController;
    private LastLevelCacheConfig l2CacheController;
    private MainMemoryConfig memoryController;

    public MemoryHierarchyConfig(L1CacheControllerConfig l1ICacheController, L1CacheControllerConfig l1DCacheController, LastLevelCacheConfig l2CacheController, MainMemoryConfig memoryController) {
        this.l1ICacheController = l1ICacheController;
        this.l1DCacheController = l1DCacheController;
        this.l2CacheController = l2CacheController;
        this.memoryController = memoryController;
    }

    public L1CacheControllerConfig getL1ICacheController() {
        return l1ICacheController;
    }

    public L1CacheControllerConfig getL1DCacheController() {
        return l1DCacheController;
    }

    public LastLevelCacheConfig getL2CacheController() {
        return l2CacheController;
    }

    public MainMemoryConfig getMemoryController() {
        return memoryController;
    }

    public static MemoryHierarchyConfig createDefaultMemoryHierarchyConfig(int l1ISize, int l1IAssociativity, int l1DSize, int l1DAssociativity, int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz) {
        L1CacheControllerConfig l1ICacheControllerConfig = new L1CacheControllerConfig(l1ISize, l1IAssociativity, LRUPolicy.class);
        L1CacheControllerConfig l1DCacheControllerConfig = new L1CacheControllerConfig(l1DSize, l1DAssociativity, LRUPolicy.class);

        LastLevelCacheConfig l2CacheConfig = new LastLevelCacheConfig(l2Size, l2Associativity, l2EvictionPolicyClz);

        MainMemoryConfig mainMemoryConfig = new FixedLatencyMainMemoryConfig();

        return new MemoryHierarchyConfig(l1ICacheControllerConfig, l1DCacheControllerConfig, l2CacheConfig, mainMemoryConfig);
    }
}
