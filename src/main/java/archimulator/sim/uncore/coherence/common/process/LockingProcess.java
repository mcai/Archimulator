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
import archimulator.sim.uncore.coherence.common.LockableCacheLineReplacementState;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
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
                getPendingActions().push(newEvictProcess(access, cacheAccess).addOnCompletedCallback(new Action1<CoherentCacheProcess>() {
                    public void apply(CoherentCacheProcess evictProcess) {
                        onCompletedCallback.apply();
                    }
                }));
            }
        };
    }

    @Override
    protected void complete() {
        LockableCacheLineReplacementState replacementState = this.findAndLockProcess.getCacheAccess().getLine().getReplacementState();
        if (replacementState == LockableCacheLineReplacementState.HITTING) {
            this.findAndLockProcess.getCacheAccess().getLine().endHit();
        } else if (replacementState == LockableCacheLineReplacementState.EVICTED) {
        } else if (replacementState == LockableCacheLineReplacementState.FILLING) {
            this.findAndLockProcess.getCacheAccess().getLine().endFill();
        } else {
            throw new IllegalArgumentException();
        }

        super.complete();
    }

    @Override
    public boolean processPendingActions() throws CoherentCacheException {
        LockableCacheLineReplacementState replacementState = this.findAndLockProcess.getCacheAccess() != null ? this.findAndLockProcess.getCacheAccess().getLine().getReplacementState() : null;
        if (replacementState == null) {
            this.findAndLockProcess.processPendingActions();
            return false;
        }
        if (!this.findAndLockProcess.getCacheAccess().isBypass() && replacementState == LockableCacheLineReplacementState.INVALID) {
            this.findAndLockProcess.processPendingActions();
        } else if (!this.findAndLockProcess.getCacheAccess().isBypass() && replacementState == LockableCacheLineReplacementState.VALID) {
            this.findAndLockProcess.processPendingActions();
        } else if (replacementState == LockableCacheLineReplacementState.EVICTING) {
            this.findAndLockProcess.processPendingActions();
        }

        LockableCacheLineReplacementState replacementState2 = this.findAndLockProcess.getCacheAccess() != null ? this.findAndLockProcess.getCacheAccess().getLine().getReplacementState() : null;

        return (replacementState2 == LockableCacheLineReplacementState.HITTING || replacementState2 == LockableCacheLineReplacementState.EVICTED || replacementState2 == LockableCacheLineReplacementState.FILLING || this.findAndLockProcess.getCacheAccess().isBypass()) && super.processPendingActions();
    }

    protected abstract CoherentCacheProcess newEvictProcess(MemoryHierarchyAccess access, CacheAccess<MESIState, LockableCacheLine> cacheAccess);

    @Override
    public String toString() {
        return String.format("%s: %s {id=%d} @ 0x%08x %s", access, findAndLockProcess.getCacheAccess() != null ? findAndLockProcess.getCacheAccess().getReference().getAccessType() : "<NOT_INITIALIZED>", id, tag, findAndLockProcess);
    }
}
