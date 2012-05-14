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
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.util.action.Action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LockableCacheLine<StateT extends Serializable> extends CacheLine<StateT> {
    private int transientTag = -1;
    private List<Action> suspendedActions;
    private Flow lockingFlow;

    public LockableCacheLine(Cache<?, ?> cache, int set, int way, StateT initialState) {
        super(cache, set, way, initialState);

        this.suspendedActions = new ArrayList<Action>();
    }

    public boolean lock(Action action, int transientTag, Flow lockingFlow) {
        if (this.isLocked()) {
            this.suspendedActions.add(action);
            return false;
        } else {
            this.transientTag = transientTag;
            this.lockingFlow = lockingFlow;
            return true;
        }
    }

    public LockableCacheLine unlock() {
        this.transientTag = -1;
        this.lockingFlow = null;

        for (Action action : this.suspendedActions) {
            getCache().getCycleAccurateEventQueue().schedule(this, action, 0);
        }

        this.suspendedActions.clear();

        return this;
    }

    public int getTransientTag() {
        return transientTag;
    }

    public Flow getLockingFlow() {
        return lockingFlow;
    }

    public boolean isLocked() {
        return transientTag != -1;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d] {tag=%s, transientTag=%s, lockingFlow=%s, state=%s}", getSet(), getWay(), getTag() == -1 ? "<INVALID>" : String.format("0x%08x", getTag()), transientTag == -1 ? "<INVALID>" : String.format("0x%08x", transientTag), lockingFlow, getState());
    }
}
