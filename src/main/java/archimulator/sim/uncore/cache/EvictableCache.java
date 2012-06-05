package archimulator.sim.uncore.cache; /*******************************************************************************
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

import archimulator.sim.base.simulation.SimulationObject;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;

public class EvictableCache<StateT extends Serializable> extends Cache<StateT> {
    protected EvictionPolicy<StateT> evictionPolicy;

    public EvictableCache(SimulationObject parent, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent, name, geometry, cacheLineStateProviderFactory);

        this.evictionPolicy = EvictionPolicyFactory.createEvictionPolicy(evictionPolicyClz, this);
    }

    public CacheAccess<StateT> newAccess(GeneralCacheController cacheController, MemoryHierarchyAccess access, int address, CacheAccessType accessType) {
        CacheLine<StateT> line = this.findLine(address);

        int set = this.getSet(address);

        if (line != null) {
            CacheHit<StateT> cacheHit = this.newHit(cacheController, access, set, address, accessType, line.getWay());
            line.setCacheAccess(cacheHit);
            return cacheHit;
        } else {
            CacheMiss<StateT> cacheMiss = this.newMiss(cacheController, access, set, address, accessType);
            line = this.getLine(set, cacheMiss.getWay());
            line.setCacheAccess(cacheMiss);
            return cacheMiss;
        }
    }

    private CacheHit<StateT> newHit(GeneralCacheController coherentCache, MemoryHierarchyAccess access, int set, int address, CacheAccessType accessType, int way) {
        return new CacheHit<StateT>(this, new CacheReference(coherentCache, access, address, this.getTag(address), accessType, set), way);
    }

    private CacheMiss<StateT> newMiss(GeneralCacheController coherentCache, MemoryHierarchyAccess access, int set, int address, CacheAccessType accessType) {
        CacheReference reference = new CacheReference(coherentCache, access, address, this.getTag(address), accessType, set);

        for (int way = 0; way < this.getAssociativity(); way++) {
            CacheLine<StateT> line = this.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheMiss<StateT>(this, reference, way);
            }
        }

        return this.evictionPolicy.handleReplacement(reference);
    }

    public EvictionPolicy<StateT> getEvictionPolicy() {
        return evictionPolicy;
    }
}
