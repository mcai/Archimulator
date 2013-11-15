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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

/**
 * Cache replacement policy.
 *
 * @param <StateT>
 * @author Min Cai
 */
public interface CacheReplacementPolicy<StateT extends Serializable> extends Reportable {
    /**
     * Create a new cache miss.
     *
     * @param access  the memory hierarchy access
     * @param set     the set index
     * @param address the address
     * @return the newly created cache miss object
     */
    default CacheAccess<StateT> newMiss(MemoryHierarchyAccess access, int set, int address) {
        int tag = this.getCache().getTag(address);

        for (int way = 0; way < this.getCache().getAssociativity(); way++) {
            CacheLine<StateT> line = this.getCache().getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<>(this.getCache(), access, set, way, tag);
            }
        }

        return this.handleReplacement(access, set, tag);
    }

    /**
     * Handle a cache replacement.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param tag    the tag
     * @return the newly created cache access object
     */
    CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag);

    /**
     * Handle promotion on a cache hit.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way);

    /**
     * Handle insertion on a cache miss.
     *
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     */
    void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way);

    /**
     * Get the parent evictable cache.
     *
     * @return the parent evictable cache
     */
    EvictableCache<StateT> getCache();
}
