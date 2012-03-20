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

public class LRUPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends StackBasedEvictionPolicy<StateT, LineT> {
    public LRUPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);
    }

    @Override
    public CacheMiss<StateT, LineT> handleReplacement(CacheReference reference) {
        return new CacheMiss<StateT, LineT>(this.getCache(), reference, this.getLRU(reference.getSet()));
    }

    @Override
    public void handlePromotionOnHit(CacheHit<StateT, LineT> hit) {
        this.setMRU(hit.getReference().getSet(), hit.getWay());
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        this.setMRU(miss.getReference().getSet(), miss.getWay());
    }
}
