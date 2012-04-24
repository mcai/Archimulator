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
package archimulator.sim.uncore.cache.eviction;

import archimulator.sim.uncore.cache.*;

import java.io.Serializable;

public abstract class EvictionPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> {
    private EvictableCache<StateT, LineT> cache;

    public EvictionPolicy(EvictableCache<StateT, LineT> cache) {
        this.cache = cache;
    }

    public abstract CacheMiss<StateT, LineT> handleReplacement(CacheReference reference); // victim selection

    public abstract void handlePromotionOnHit(CacheHit<StateT, LineT> hit); // => promotion of referenced line

    public abstract void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss); // => insertion of fetched line

    public EvictableCache<StateT, LineT> getCache() {
        return cache;
    }
}
