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
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;

import java.io.Serializable;

public class EvictableCache<StateT extends Serializable> extends Cache<StateT> {
    protected EvictionPolicy<StateT> evictionPolicy;

    public EvictableCache(SimulationObject parent, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent, name, geometry, cacheLineStateProviderFactory);

        this.evictionPolicy = EvictionPolicyFactory.createEvictionPolicy(evictionPolicyClz, this);
    }

    public CacheAccess<StateT> newAccess(MemoryHierarchyAccess access, int address) {
        CacheLine<StateT> line = this.findLine(address);

        int set = this.getSet(address);

        if (line != null) {
            return this.newHit(access, set, address, line.getWay());
        } else {
            return this.newMiss(access, set, address);
        }
    }

    private CacheAccess<StateT> newHit(MemoryHierarchyAccess access, int set, int address, int way) {
        return new CacheAccess<StateT>(this, access, set, way, this.getTag(address));
    }

    private CacheAccess<StateT> newMiss(MemoryHierarchyAccess access, int set, int address) {
        int tag = this.getTag(address);

        for (int way = 0; way < this.getAssociativity(); way++) {
            CacheLine<StateT> line = this.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<StateT>(this, access, set, way, tag);
            }
        }

        return this.evictionPolicy.handleReplacement(access, set, tag);
    }

    public EvictionPolicy<StateT> getEvictionPolicy() {
        return evictionPolicy;
    }
}
