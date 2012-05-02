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
package archimulator.sim.uncore.coherence.common.process;

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.uncore.coherence.action.PendingActionOwner;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.util.action.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class CoherentCacheProcess implements PendingActionOwner {
    protected long id;
    private List<Action1<? extends PendingActionOwner>> onCompletedCallbacks;
    private Stack<PendingActionOwner> pendingActions;
    private CoherentCache cache;

    public CoherentCacheProcess(CoherentCache cache) {
        this.cache = cache;
        this.id = Simulation.currentCoherentCacheProcessId++;
        this.onCompletedCallbacks = new ArrayList<Action1<? extends PendingActionOwner>>();
        this.pendingActions = new Stack<PendingActionOwner>();
    }

    @SuppressWarnings("unchecked")
    protected void complete() {
        for (Action1<? extends PendingActionOwner> onCompletedCallback : this.onCompletedCallbacks) {
            ((Action1<PendingActionOwner>) onCompletedCallback).apply(this);
        }

        this.onCompletedCallbacks.clear();
    }

    public CoherentCacheProcess addOnCompletedCallback(Action1<? extends PendingActionOwner> onCompletedCallback) {
        this.onCompletedCallbacks.add(onCompletedCallback);
        return this;
    }

    public boolean processPendingActions() throws CoherentCacheException {
        if (this.pendingActions.empty()) {
            this.complete();
            return true;
        } else {
            PendingActionOwner peek = this.pendingActions.peek();
            if (peek.processPendingActions()) {
                this.pendingActions.remove(peek);
            }

            if (this.pendingActions.empty()) {
                this.complete();
                return true;
            }

            return false;
        }
    }

    protected Stack<PendingActionOwner> getPendingActions() {
        return pendingActions;
    }

    public CoherentCache getCache() {
        return cache;
    }
}
