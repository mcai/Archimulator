/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.replacement.CacheReplacementPolicy;

import java.io.Serializable;

/**
 * Cache which supports eviction (replacement).
 *
 * @param <StateT> state
 * @author Min Cai
 */
public interface EvictableCache<StateT extends Serializable> extends Cache<StateT> {
    /**
     * Create a new cache access.
     *
     * @param access  the memory hierarchy access
     * @param address the address
     * @return the newly created cache access
     */
    default CacheAccess<StateT> newAccess(MemoryHierarchyAccess access, int address) {
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
    default CacheAccess<StateT> newHit(MemoryHierarchyAccess access, int set, int address, int way) {
        return new CacheAccess<>(this, access, set, way, this.getTag(address));
    }

    /**
     * Create a new cache miss.
     *
     * @param access  the memory hierarchy access
     * @param set     the set index
     * @param address the address
     * @return the newly created cache miss object
     */
    default CacheAccess<StateT> newMiss(MemoryHierarchyAccess access, int set, int address) {
        return this.getReplacementPolicy().newMiss(access, set, address);
    }

    /**
     * Get the replacement policy.
     *
     * @return the replacement policy
     */
    public CacheReplacementPolicy<StateT> getReplacementPolicy();
}
