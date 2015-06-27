/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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

import java.io.Serializable;

/**
 * Cache access.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class CacheAccess<StateT extends Serializable> {
    private MemoryHierarchyAccess access;
    private int set;
    private int way;

    private CacheLine<StateT> line;

    private boolean hitInCache;
    private boolean replacement;

    /**
     * Create a cache access.
     *
     * @param cache  the access
     * @param access the memory hierarchy access
     * @param set    the set index
     * @param way    the way
     * @param tag    the tag
     */
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

    /**
     * Get the memory hierarchy access.
     *
     * @return the memory hierarchy access
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     * Get the set index.
     *
     * @return the set index
     */
    public int getSet() {
        return set;
    }

    /**
     * Get the way.
     *
     * @return the way
     */
    public int getWay() {
        return way;
    }

    /**
     * Get the cache line.
     *
     * @return the cache line
     */
    public CacheLine<StateT> getLine() {
        return line;
    }

    /**
     * Get a value indicating whether the cache access is hit in cache or not.
     *
     * @return a value indicating whether the cache access is hit in cache or not.
     */
    public boolean isHitInCache() {
        return hitInCache;
    }

    /**
     * Get a value indicating whether the cache access needs a replacement or not.
     *
     * @return a value indicating whether the cache access needs a replacement or not.
     */
    public boolean isReplacement() {
        return replacement;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d] %s {hitInCache=%s, replacement=%s}", set, way, access.getType(), isHitInCache(), isReplacement());
    }
}
