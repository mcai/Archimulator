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
package archimulator.mem.cache;

import archimulator.mem.cache.event.CacheLineInvalidatedEvent;
import archimulator.mem.cache.event.CacheLineStateChangedToNonInitialStateEvent;
import archimulator.mem.cache.event.CacheLineTagChangedEvent;
import archimulator.mem.cache.event.CacheLineValidatedEvent;

import java.io.Serializable;

public class CacheLine<StateT extends Serializable> implements Serializable {
    private Cache<?, ?> cache;
    private int set;
    private int way;
    private StateT initialState;

    private int tag;
    private StateT state;

    public CacheLine(Cache<?, ?> cache, int set, int way, StateT initialState) {
        this.cache = cache;
        this.set = set;
        this.way = way;
        this.state = initialState;

        this.tag = -1;
        this.initialState = initialState;
    }

    public void invalidate() { //TODO: should notify cache's eviction policy
        assert (this.state != this.initialState);
        
        int previousTag = this.tag;
        StateT previousState = this.state;

        this.tag = -1;
        this.state = this.initialState;

        this.cache.getBlockingEventDispatcher().dispatch(new CacheLineInvalidatedEvent(this, previousTag, previousState));
    }

    public void setNonInitialState(StateT state) {
        if (state == this.initialState) {
            throw new IllegalArgumentException();
        }
        
        if(!this.state.equals(state)) {
            StateT previousState = this.state;
            this.state = state; //TODO: should notify cache's eviction policy

            this.cache.getBlockingEventDispatcher().dispatch(new CacheLineStateChangedToNonInitialStateEvent(this, previousState));
        }
    }

    public Cache<?, ?> getCache() {
        return cache;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public StateT getInitialState() {
        return initialState;
    }

    public int getTag() {
        return tag;
    }

    protected void setTag(int tag) {
        if(this.tag != tag) {
            int previousTag = this.tag;
            this.tag = tag;

            this.cache.getBlockingEventDispatcher().dispatch(new CacheLineTagChangedEvent(this, previousTag));

            if(previousTag == -1) {
                this.cache.getBlockingEventDispatcher().dispatch(new CacheLineValidatedEvent(this));
            }
        }
    }

    public StateT getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("CacheLine{set=%d, way=%d, tag=%s, state=%s}", set, way, tag == -1 ? "<INVALID>" : String.format("0x%08x", tag), state);
    }
}
