/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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

import java.io.Serializable;

public class CacheLine<StateT extends Serializable> implements Serializable {
    private int set;
    private int way;
    private StateT initialState;

    protected int tag;
    protected StateT state;

    public CacheLine(int set, int way, StateT initialState) {
        this.set = set;
        this.way = way;
        this.state = initialState;

        this.tag = -1;
        this.initialState = initialState;
    }

    public void invalidate() { //TODO: should notify cache's eviction policy
        assert (this.state != this.initialState);

        this.tag = -1;
        this.state = this.initialState;
    }

    public void setNonInitialState(StateT state) {
        if (state == this.initialState) {
            throw new IllegalArgumentException();
        }
        this.state = state; //TODO: should notify cache's eviction policy
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

    public StateT getState() {
        return state;
    }

    @Override
    public String toString() {
        return String.format("CacheLine{set=%d, way=%d, initialState=%s, tag=%d, state=%s}", set, way, initialState, tag, state);
    }
}
