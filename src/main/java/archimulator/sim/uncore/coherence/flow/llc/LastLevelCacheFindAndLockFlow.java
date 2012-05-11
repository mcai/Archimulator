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
package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class LastLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public LastLevelCacheFindAndLockFlow(LockingFlow lockingFlow, LastLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(lockingFlow, cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(MemoryHierarchyAccess access, final Action onSuccessCallback, Action onFailureCallback) {
        if (getCacheAccess().getLine().getState() == MESIState.MODIFIED) {
            getCache().sendRequest(getCache().getNext(), getCache().getCache().getLineSize() + 8, new Action() {
                @Override
                public void apply() {
                    getCache().getNext().memWriteRequestReceive(getCache(), getCacheAccess().getLine().getTag(), new Action() {
                        @Override
                        public void apply() {
                            getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), getCacheAccess().getLine()));
                            onSuccessCallback.apply();
                        }
                    });
                }
            });
        } else {
            getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                @Override
                public void apply() {
                    getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), getCacheAccess().getLine()));
                    onSuccessCallback.apply();
                }
            }, 0);
        }
    }

    @Override
    public LastLevelCache getCache() {
        return (LastLevelCache) super.getCache();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s%s: LastLevelCacheFindAndLockFlow#%d {state=%s} ", getBeginCycle(), getCache().getName(), getCacheAccess().getLine(), getId(), getState());
    }
}
