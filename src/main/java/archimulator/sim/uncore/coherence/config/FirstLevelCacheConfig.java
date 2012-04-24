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

public class FirstLevelCacheConfig extends CoherentCacheConfig {
    private int readPorts; //TODO: to be clarified, should not be confused with MSHRs
    private int writePorts; //TODO: to be clarified, should not be confused with MSHRs

    public FirstLevelCacheConfig(int size, int associativity, Class<? extends EvictionPolicy> evictionPolicyClz) {
//        this(CoherentCacheLevelType.FIRST_LEVEL_CACHE, new CacheGeometry(size, associativity, 64), 1, 2, 1, evictionPolicyClz);
        this(CoherentCacheLevelType.FIRST_LEVEL_CACHE, new CacheGeometry(size, associativity, 64), 1, 128, 128, evictionPolicyClz); //TODO: should simulate MSHRs in all levels of cache explicitly
    }

    public FirstLevelCacheConfig(CoherentCacheLevelType levelType, CacheGeometry geometry, int hitLatency, int readPorts, int writePorts, Class<? extends EvictionPolicy> evictionPolicyClz) {
        super(levelType, geometry, hitLatency, evictionPolicyClz);
        this.readPorts = readPorts;
        this.writePorts = writePorts;
    }

    public int getReadPorts() {
        return readPorts;
    }

    public int getWritePorts() {
        return writePorts;
    }
}
