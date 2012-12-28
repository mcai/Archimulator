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
package archimulator.sim.uncore.cache;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyFactory;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import net.pickapack.util.ValueProvider;
import net.pickapack.util.ValueProviderFactory;

import java.io.Serializable;

/**
 * Cache which supports eviction (replacement).
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class EvictableCache<StateT extends Serializable> extends Cache<StateT> {
    /**
     * The replacement policy.
     */
    protected CacheReplacementPolicy<StateT> replacementPolicy;

    /**
     * Create an evictable cache.
     *
     * @param parent                        the parent simulation object.
     * @param name                          the name
     * @param geometry                      the geometry
     * @param replacementPolicyType         the replacement policy type
     * @param cacheLineStateProviderFactory the cache line state provider factory
     */
    public EvictableCache(SimulationObject parent, String name, CacheGeometry geometry, CacheReplacementPolicyType replacementPolicyType, ValueProviderFactory<StateT, ValueProvider<StateT>> cacheLineStateProviderFactory) {
        super(parent, name, geometry, cacheLineStateProviderFactory);

        this.replacementPolicy = CacheReplacementPolicyFactory.createCacheReplacementPolicy(replacementPolicyType, this);
    }

    /**
     * Create a new cache access.
     *
     * @param access  the memory hierarchy access
     * @param address the address
     * @return the newly created cache access
     */
    public CacheAccess<StateT> newAccess(MemoryHierarchyAccess access, int address) {
        CacheLine<StateT> line = this.findLine(address);

        int set = this.getSet(address);

        if (line != null) {
            return this.newHit(access, set, address, line.getWay());
        } else {
            return this.newMiss(access, set, address);
        }
    }

    /**
     * Create a new cache hit.
     *
     * @param access  the memory hierarchy access
     * @param set     the set index
     * @param address the address
     * @param way     the way
     * @return the newly created cache hit object
     */
    private CacheAccess<StateT> newHit(MemoryHierarchyAccess access, int set, int address, int way) {
        return new CacheAccess<StateT>(this, access, set, way, this.getTag(address));
    }

    /**
     * Create a new cache miss.
     *
     * @param access  the memory hierarchy access
     * @param set     the set index
     * @param address the address
     * @return the newly created cache miss object
     */
    private CacheAccess<StateT> newMiss(MemoryHierarchyAccess access, int set, int address) {
        int tag = this.getTag(address);

        for (int way = 0; way < this.getAssociativity(); way++) {
            CacheLine<StateT> line = this.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<StateT>(this, access, set, way, tag);
            }
        }

        return this.replacementPolicy.handleReplacement(access, set, tag);
    }

    /**
     * Get the replacement policy.
     *
     * @return the replacement policy
     */
    public CacheReplacementPolicy<StateT> getReplacementPolicy() {
        return replacementPolicy;
    }
}
