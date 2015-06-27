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
import archimulator.util.Params;
import archimulator.util.ValueProvider;

import java.io.Serializable;

/**
 * Cache line.
 *
 * @param <StateT> state
 * @author Min Cai
 */
public class CacheLine<StateT extends Serializable> extends Params {
    private Cache<StateT> cache;
    private int set;
    private int way;

    private int tag;
    private MemoryHierarchyAccess access;
    private ValueProvider<StateT> stateProvider;

    /**
     * Create a cache line.
     *
     * @param cache         the parent cache
     * @param set           the set index
     * @param way           the way
     * @param stateProvider the state provider
     */
    public CacheLine(Cache<StateT> cache, int set, int way, ValueProvider<StateT> stateProvider) {
        this.cache = cache;
        this.set = set;
        this.way = way;
        this.stateProvider = stateProvider;

        this.tag = -1;
    }

    /**
     * Get the parent cache.
     *
     * @return the parent cache
     */
    public Cache<StateT> getCache() {
        return cache;
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
     * Get the state provider.
     *
     * @return the state provider
     */
    public ValueProvider<StateT> getStateProvider() {
        return stateProvider;
    }

    /**
     * Get the initial state.
     *
     * @return the initial state
     */
    public StateT getInitialState() {
        return getStateProvider().getInitialValue();
    }

    /**
     * Get the tag.
     *
     * @return the tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * Set the tag.
     *
     * @param tag the tag
     */
    public void setTag(int tag) {
        if (tag != INVALID_TAG) {
            for (CacheLine<StateT> line : this.cache.getLines(this.set)) {
                if (line.getTag() == tag) {
                    throw new IllegalArgumentException();
                }
            }
        }

        if (this.tag == INVALID_TAG && tag != INVALID_TAG) {
            this.cache.setNumTagsInUse(this.cache.getNumTagsInUse() + 1);
        } else if (this.tag != INVALID_TAG && tag == INVALID_TAG) {
            this.cache.setNumTagsInUse(this.cache.getNumTagsInUse() - 1);
        }

        this.tag = tag;
    }

    /**
     * Get the associated memory hierarchy access.
     *
     * @return the associated memory hierarchy access if any exist; otherwise null
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     * Set the associated memory hierarchy access.
     *
     * @param access the associated memory hierarchy access
     */
    public void setAccess(MemoryHierarchyAccess access) {
        this.access = access;
    }

    /**
     * Get the state.
     *
     * @return the state
     */
    public StateT getState() {
        return getStateProvider().get();
    }

    /**
     * Get a value indicating whether the cache line is valid or not.
     *
     * @return a value indicating whether the cache line is valid or not
     */
    public boolean isValid() {
        return this.getState() != this.getInitialState();
    }

    @Override
    public String toString() {
        return String.format("%s [%d,%d] {%s} %s", getCache().getName(), getSet(), getWay(), getState(), tag == INVALID_TAG ? "N/A" : String.format("0x%08x", tag));
    }

    /**
     * Invalid tag constant.
     */
    public static final int INVALID_TAG = -1;
}
