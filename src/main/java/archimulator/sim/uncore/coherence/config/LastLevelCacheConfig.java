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
package archimulator.sim.uncore.coherence.config;

import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.coherence.CoherentCacheLevelType;

public class LastLevelCacheConfig extends CoherentCacheConfig {
    public LastLevelCacheConfig(int size, int associativity, Class<? extends EvictionPolicy> evictionPolicyClz) {
        this(CoherentCacheLevelType.LAST_LEVEL_CACHE, new CacheGeometry(size, associativity, 64), 10, evictionPolicyClz);
    }

    public LastLevelCacheConfig(CoherentCacheLevelType levelType, CacheGeometry geometry, int hitLatency, Class<? extends EvictionPolicy> evictionPolicyClz) {
        super(levelType, geometry, hitLatency, evictionPolicyClz);
    }
}
