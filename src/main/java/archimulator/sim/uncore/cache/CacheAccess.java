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
package archimulator.sim.uncore.cache;

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheEndCacheAccessEvent;

import java.io.Serializable;

public abstract class CacheAccess<StateT extends Serializable> {
    private long id;
    private EvictableCache<StateT> cache;
    private CacheReference reference;
    private int way;

    private CacheLine<StateT> line;
    private boolean completed;

    public CacheAccess(EvictableCache<StateT> cache, CacheReference reference, int way) {
        this.id = Simulation.currentCacheAccessId++;
        this.cache = cache;
        this.reference = reference;
        this.way = way;

        if (this.way != -1) {
            this.line = this.cache.getLine(this.reference.getSet(), this.way);
        }
    }

    public long getId() {
        return id;
    }

    public EvictableCache<StateT> getCache() {
        return cache;
    }

    public CacheReference getReference() {
        return reference;
    }

    public int getWay() {
        return way;
    }

    public CacheLine<StateT> getLine() {
        return line;
    }

    public abstract boolean isHitInCache();

    public abstract boolean isEviction();

    public CacheAccess<StateT> commit() {
        if(this.completed) {
           throw new IllegalArgumentException();
        }

        if (this.reference.getCacheController() != null && this.reference.getAccess() != null) {
            this.reference.getCacheController().getBlockingEventDispatcher().dispatch(new CoherentCacheEndCacheAccessEvent(this.reference.getCacheController(), this.reference.getAccess(), this));
        }

        this.completed = true;

        return this;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d] %s {id=%d, hitInCache=%s, eviction=%s, completed=%s}", reference.getSet(), way, reference.getAccessType(), id, isHitInCache(), isEviction(), isCompleted());
    }
}
