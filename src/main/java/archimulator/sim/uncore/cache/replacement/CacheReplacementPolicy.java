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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

public abstract class CacheReplacementPolicy<StateT extends Serializable> {
    private EvictableCache<StateT> cache;

    public CacheReplacementPolicy(EvictableCache<StateT> cache) {
        this.cache = cache;
    }

    public abstract CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag);

    public abstract void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way);

    public abstract void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way);

    public EvictableCache<StateT> getCache() {
        return cache;
    }
}
