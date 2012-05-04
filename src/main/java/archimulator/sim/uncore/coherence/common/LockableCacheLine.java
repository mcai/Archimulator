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
package archimulator.sim.uncore.coherence.common;

import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

public class LockableCacheLine extends CacheLine<MESIState> {
    private int transientTag = -1;
    private List<Action> suspendedActions;

    public LockableCacheLine(Cache<?, ?> cache, int set, int way, MESIState initialState) {
        super(cache, set, way, initialState);

        this.suspendedActions = new ArrayList<Action>();
    }

    public boolean lock(Action action, int transientTag) {
        if (this.isLocked()) {
            this.suspendedActions.add(action);
            return false;
        } else {
            this.transientTag = transientTag;
            return true;
        }
    }

    public LockableCacheLine unlock() {
        assert (this.isLocked());

        this.transientTag = -1;

        for (Action action : this.suspendedActions) {
            getCache().getCycleAccurateEventQueue().schedule(action, 0);
        }

        this.suspendedActions.clear();

        return this;
    }

    public int getTransientTag() {
        return transientTag;
    }

    public boolean isLocked() {
        return transientTag != -1;
    }

    @Override
    public String toString() {
        return String.format("LockableCacheLine{set=%d, way=%d, tag=%s, transientTag=%s, state=%s}", getSet(), getWay(), getTag() == -1 ? "<INVALID>" : String.format("0x%08x", getTag()), transientTag == -1 ? "<INVALID>" : String.format("0x%08x", transientTag), getState());
    }
}
