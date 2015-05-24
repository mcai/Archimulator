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
package archimulator.uncore.cache.replacement.prefetchAware;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.CacheAccess;
import archimulator.uncore.cache.setDueling.AbstractSetDuelingUnit;
import archimulator.uncore.cache.replacement.CacheReplacementPolicy;

import java.io.Serializable;

/**
 * Set dueling based policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public interface SetDuelingCacheReplacementPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    default CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        return this.getPolicy(access, set).handleReplacement(access, set, tag);
    }

    default void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.getPolicy(access, set).handlePromotionOnHit(access, set, way);
    }

    default void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.getPolicy(access, set).handleInsertionOnMiss(access, set, way);
    }

    /**
     * Get the cache replacement policy for the specified access and set index.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @return the cache replacement policy for the specified access and set index
     */
     CacheReplacementPolicy<StateT> getPolicy(MemoryHierarchyAccess access, int set);

    /**
     * Get the set dueling unit.
     *
     * @return the set dueling unit
     */
    public AbstractSetDuelingUnit getSetDuelingUnit();
}
