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
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;

public abstract class LockingProcess extends CoherentCacheProcess {
    protected FindAndLockProcess findAndLockProcess;
    protected MemoryHierarchyAccess access;
    protected int tag;

    public LockingProcess(CoherentCache cache, MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        super(cache);
        this.access = access;
        this.tag = tag;
        this.findAndLockProcess = this.newFindAndLockProcess(cacheAccessType);
    }

    @Override
    protected void complete() {
        if (this.findAndLockProcess.fsm.getState() == FindAndLockState.ACQUIRED) {
            this.findAndLockProcess.fsm.fireTransition(FindAndLockCondition.COMPLETED);
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

    public abstract FindAndLockProcess newFindAndLockProcess(CacheAccessType cacheAccessType);

    @Override
    public String toString() {
        return String.format("%s: %s {id=%d} @ 0x%08x %s", access, findAndLockProcess.getCacheAccess() != null ? findAndLockProcess.getCacheAccess().getReference().getAccessType() : "<NOT_INITIALIZED>", id, tag, findAndLockProcess);
    }
}
