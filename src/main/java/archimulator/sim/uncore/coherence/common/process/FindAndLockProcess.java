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
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.LockableCacheLineReplacementState;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.util.action.Action;

public abstract class FindAndLockProcess extends CoherentCacheProcess {
    protected CacheAccess<MESIState, LockableCacheLine> cacheAccess;

    public FindAndLockProcess(CoherentCache cache, final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        super(cache);

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                if (cacheAccess.isEviction()) {
                    evict(access, new Action() {
                        public void apply() {
                            cacheAccess.getLine().endEvict();
                        }
                    });
                }

                return true;
            }
        });

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() throws CoherentCacheException {
                return doLockingProcess(access, tag, cacheAccessType);
            }
        });
    }

    private boolean doLockingProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        if (this.cacheAccess == null) {
            this.cacheAccess = this.getCache().getCache().newAccess(this.getCache(), access, tag, cacheAccessType);
        }

        if (this.cacheAccess.isHitInCache() || !this.cacheAccess.isBypass()) {
            if (this.cacheAccess.getLine().isLocked() && !cacheAccessType.isUpward()) {
                if (this.cacheAccess.isHitInCache()) {
                    this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(this.getCache(), tag, access, this.cacheAccess.getLine()));
                }
            }

            if (this.cacheAccess.getLine().isLocked() && cacheAccessType.isDownward()) {
//                throw new CoherentCacheException();
                return false;
            }
            else {
                Action action = new Action() {
                    public void apply() {
                        doLockingProcess(access, tag, cacheAccessType);
                    }
                };

                boolean result = this.cacheAccess.isHitInCache() ? this.cacheAccess.getLine().beginHit(action, tag) :
                        (this.cacheAccess.isEviction() ? this.cacheAccess.getLine().beginEvict(action, tag) : this.cacheAccess.getLine().beginFill(action, tag));

                if (result) {
                    if (!cacheAccessType.isUpward()) {
                        this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCache(), tag, access, this.cacheAccess.getLine(), this.cacheAccess.isHitInCache(), this.cacheAccess.isEviction(), this.cacheAccess.getReference().getAccessType()));
                    }

                    if (this.cacheAccess.isEviction()) {
                        getCache().incEvictions();
                    }
                }
            }
        }

        if(this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.HITTING || this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.FILLING || this.cacheAccess.isBypass()) {
            this.getCache().updateStats(cacheAccessType, this.cacheAccess);
            this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getCache(), access, this.cacheAccess));
        }

        return true;
    }

    public CacheAccess<MESIState, LockableCacheLine> getCacheAccess() {
        return cacheAccess;
    }

    protected abstract void evict(MemoryHierarchyAccess access, Action onCompletedCallback);

    @Override
    public String toString() {
        return String.format("%s %s", this.cacheAccess != null ? this.cacheAccess.getLine().getReplacementState() : "<INVALID>", this.cacheAccess != null ? this.cacheAccess : "<INVALID>");
    }
}
