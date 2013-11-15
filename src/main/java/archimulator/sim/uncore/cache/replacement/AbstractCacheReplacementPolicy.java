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

import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

/**
 * Abstract cache replacement policy.
 *
 * @param <StateT>
 * @author Min Cai
 */
public abstract class AbstractCacheReplacementPolicy<StateT extends Serializable> implements CacheReplacementPolicy<StateT> {
    private EvictableCache<StateT> cache;

    /**
     * Create an abstract cache replacement policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public AbstractCacheReplacementPolicy(EvictableCache<StateT> cache) {
        this.cache = cache;
    }

    /**
     * Get the parent evictable cache.
     *
     * @return the parent evictable cache
     */
    public EvictableCache<StateT> getCache() {
        return cache;
    }
}
