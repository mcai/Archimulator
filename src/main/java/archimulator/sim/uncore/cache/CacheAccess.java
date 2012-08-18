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

import archimulator.sim.uncore.MemoryHierarchyAccess;

import java.io.Serializable;

public class CacheAccess<StateT extends Serializable> {
    private MemoryHierarchyAccess access;
    private int set;
    private int way;

    private CacheLine<StateT> line;

    private boolean hitInCache;
    private boolean replacement;

    public CacheAccess(EvictableCache<StateT> cache, MemoryHierarchyAccess access, int set, int way, int tag) {
        this.access = access;
        this.set = set;
        this.way = way;

        if (this.way == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        this.line = cache.getLine(this.set, this.way);

        this.hitInCache = this.line.getTag() == tag;
        this.replacement = this.line.isValid();
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public CacheLine<StateT> getLine() {
        return line;
    }

    public boolean isHitInCache() {
        return hitInCache;
    }

    public boolean isReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d] %s {hitInCache=%s, replacement=%s}", set, way, access.getType(), isHitInCache(), isReplacement());
    }
}
