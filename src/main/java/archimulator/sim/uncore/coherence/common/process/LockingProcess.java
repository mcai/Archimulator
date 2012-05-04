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

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.flc.process.EvictProcess;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public abstract class LockingProcess extends CoherentCacheProcess {
    protected FindAndLockProcess findAndLockProcess;
    protected MemoryHierarchyAccess access;
    protected int tag;

    public LockingProcess(CoherentCache cache, MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        super(cache);
        this.access = access;
        this.tag = tag;
        this.findAndLockProcess = new FindAndLockProcess(getCache(), this.access, this.tag, cacheAccessType) {
            @Override
            protected void evict(MemoryHierarchyAccess access, final Action onCompletedCallback) {
                getPendingActions().push(newEvictProcess(access, cacheAccess).addOnCompletedCallback(new Action1<EvictProcess>() {
                    public void apply(EvictProcess evictProcess) {
                        onCompletedCallback.apply();
                    }
                }));
            }
        };
    }

    @Override
    protected void complete() {
        if (this.findAndLockProcess.fsm.getState() == FindAndLockState.ACQUIRED) {
            this.findAndLockProcess.fsm.fireTransition(FindAndLockCondition.COMPLETE);
//                this.findAndLockProcess.state = FindAndLockState.RELEASED;
        } else if (this.findAndLockProcess.fsm.getState() == FindAndLockState.FAILED) {
            this.findAndLockProcess.cacheAccess.abort();
        }

        super.complete();
    }

    @Override
    public boolean processPendingActions() throws CoherentCacheException {
        if (this.findAndLockProcess.fsm.getState() == FindAndLockState.READY || this.findAndLockProcess.fsm.getState() == FindAndLockState.EVICTING) {
            this.findAndLockProcess.processPendingActions();
        }

        return (this.findAndLockProcess.fsm.getState() == FindAndLockState.ACQUIRED || this.findAndLockProcess.fsm.getState() == FindAndLockState.BYPASSED) && super.processPendingActions();
    }

    protected abstract CoherentCacheProcess newEvictProcess(MemoryHierarchyAccess access, CacheAccess<MESIState, LockableCacheLine> cacheAccess);

    @Override
    public String toString() {
        return String.format("%s: %s {id=%d} @ 0x%08x %s", access, findAndLockProcess.getCacheAccess() != null ? findAndLockProcess.getCacheAccess().getReference().getAccessType() : "<NOT_INITIALIZED>", id, tag, findAndLockProcess);
    }
}
