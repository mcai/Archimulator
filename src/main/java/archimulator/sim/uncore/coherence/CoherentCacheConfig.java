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
package archimulator.sim.uncore.coherence;

import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;

public abstract class CoherentCacheConfig {
    private CoherentCacheLevelType levelType;
    private CacheGeometry geometry;
    private int hitLatency;
    private Class<? extends EvictionPolicy> evictionPolicyClz;

    public CoherentCacheConfig(CoherentCacheLevelType levelType, CacheGeometry geometry, int hitLatency, Class<? extends EvictionPolicy> evictionPolicyClz) {
        this.levelType = levelType;
        this.geometry = geometry;
        this.hitLatency = hitLatency;
        this.evictionPolicyClz = evictionPolicyClz;
    }

    public CoherentCacheLevelType getLevelType() {
        return levelType;
    }

    public CacheGeometry getGeometry() {
        return geometry;
    }

    public int getHitLatency() {
        return hitLatency;
    }

    public Class<? extends EvictionPolicy> getEvictionPolicyClz() {
        return evictionPolicyClz;
    }
}
