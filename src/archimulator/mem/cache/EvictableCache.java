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
package archimulator.mem.cache;

import archimulator.mem.CacheAccessType;
import archimulator.mem.MemoryHierarchyAccess;
import archimulator.mem.cache.eviction.EvictionPolicy;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.coherence.CoherentCache;
import archimulator.sim.SimulationObject;
import archimulator.util.action.Function2;

import java.io.Serializable;

public class EvictableCache<StateT extends Serializable, LineT extends CacheLine<StateT>> extends Cache<StateT, LineT> {
    protected EvictionPolicy<StateT, LineT> evictionPolicy;

    public EvictableCache(SimulationObject parent, String name, CacheGeometry geometry, EvictionPolicyFactory evictionPolicyFactory, Function2<Integer, Integer, LineT> createLine) {
        super(parent, name, geometry, createLine);

        this.evictionPolicy = evictionPolicyFactory.create(this);
    }

    public CacheAccess<StateT, LineT> newAccess(int address, CacheAccessType accessType) {
        return this.newAccess(null, null, address, accessType);
    }

    public CacheAccess<StateT, LineT> newAccess(MemoryHierarchyAccess access, int address, CacheAccessType accessType) {
        return this.newAccess(null, access, address, accessType);
    }

    public CacheAccess<StateT, LineT> newAccess(CoherentCache<StateT> coherentCache, MemoryHierarchyAccess access, int address, CacheAccessType accessType) {
        LineT line = this.findLine(address);

        int set = this.getSet(address);

        if (line != null) {
            return this.newHit(coherentCache, access, set, address, accessType, line.getWay());
        } else {
            return this.newMiss(coherentCache, access, set, address, accessType);
        }
    }

    private CacheHit<StateT, LineT> newHit(CoherentCache<StateT> coherentCache, MemoryHierarchyAccess access, int set, int address, CacheAccessType accessType, int way) {
        return new CacheHit<StateT, LineT>(this, new CacheReference(coherentCache, access, address, this.getTag(address), accessType, set), way);
    }

    private CacheMiss<StateT, LineT> newMiss(CoherentCache<StateT> coherentCache, MemoryHierarchyAccess access, int set, int address, CacheAccessType accessType) {
        CacheReference reference = new CacheReference(coherentCache, access, address, this.getTag(address), accessType, set);

        for (int way = 0; way < this.getAssociativity(); way++) {
            LineT line = this.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheMiss<StateT, LineT>(this, reference, way);
            }
        }

        return this.evictionPolicy.handleReplacement(reference);
    }

    public EvictionPolicy<StateT, LineT> getEvictionPolicy() {
        return evictionPolicy;
    }
}
