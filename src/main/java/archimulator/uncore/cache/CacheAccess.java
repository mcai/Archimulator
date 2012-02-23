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
package archimulator.uncore.cache;

import archimulator.uncore.coherence.event.CoherentCacheEndCacheAccessEvent;

import java.io.Serializable;

public abstract class CacheAccess<StateT extends Serializable, LineT extends CacheLine<StateT>> {
    private long id;
    private EvictableCache<StateT, LineT> cache;
    private CacheReference reference;
    private int way;

    private LineT line;

    private boolean committed;

    public CacheAccess(EvictableCache<StateT, LineT> cache, CacheReference reference, int way) {
        this.id = currentId++;
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

    public EvictableCache<StateT, LineT> getCache() {
        return cache;
    }

    public CacheReference getReference() {
        return reference;
    }

    public int getWay() {
        return way;
    }

    public LineT getLine() {
        return line;
    }

    public abstract boolean isHitInCache();

    public abstract boolean isBypass();

    public abstract boolean isEviction();

    public void abort() {
    }

    public CacheAccess<StateT, LineT> commit() {
        assert (!this.committed);
        this.committed = true;

        if (this.reference.getCoherentCache() != null && this.reference.getAccess() != null) {
            this.reference.getCoherentCache().getBlockingEventDispatcher().dispatch(new CoherentCacheEndCacheAccessEvent(this.reference.getCoherentCache(), this.reference.getAccess(), this, false));
        }

        return this;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d] {hitInCache=%s, bypass=%s, eviction=%s}", reference.getSet(), way, isHitInCache(), isBypass(), isEviction());
    }

    private static long currentId = 0;
}
