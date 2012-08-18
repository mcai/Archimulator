package archimulator.sim.uncore.cache; /*******************************************************************************
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

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.util.ValueProvider;
import net.pickapack.Params;

import java.io.Serializable;

public class CacheLine<StateT extends Serializable> extends Params {
    private Cache<StateT> cache;
    private int set;
    private int way;

    private int tag;
    private MemoryHierarchyAccess access;
    private ValueProvider<StateT> stateProvider;

    public CacheLine(Cache<StateT> cache, int set, int way, ValueProvider<StateT> stateProvider) {
        this.cache = cache;
        this.set = set;
        this.way = way;
        this.stateProvider = stateProvider;

        this.tag = -1;
    }

    public Cache<StateT> getCache() {
        return cache;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public ValueProvider<StateT> getStateProvider() {
        return stateProvider;
    }

    public StateT getInitialState() {
        return getStateProvider().getInitialValue();
    }

    public int getTag() {
        return tag;
    }

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

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public void setAccess(MemoryHierarchyAccess access) {
        this.access = access;
    }

    public StateT getState() {
        return getStateProvider().get();
    }

    public boolean isValid() {
        return this.getState() != this.getInitialState();
    }

    @Override
    public String toString() {
        return String.format("%s [%d,%d] {%s} %s", getCache().getName(), getSet(), getWay(), getState(), tag == INVALID_TAG ? "N/A" : String.format("0x%08x", tag));
    }

    public static final int INVALID_TAG = -1;
}
