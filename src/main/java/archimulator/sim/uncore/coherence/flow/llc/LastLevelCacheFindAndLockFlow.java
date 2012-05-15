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
import archimulator.sim.uncore.cache.FindCacheLineResult;
import archimulator.sim.uncore.cache.FindCacheLineResultType;
import archimulator.sim.uncore.coherence.common.*;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardWriteFlow;
import archimulator.util.Reference;
import archimulator.util.action.Action;

public class LastLevelCacheFindAndLockFlow extends FindAndLockFlow<LastLevelCacheLineState, LastLevelCacheLine> {
    public LastLevelCacheFindAndLockFlow(LockingFlow lockingFlow, LastLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(lockingFlow, cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(final MemoryHierarchyAccess access, final Action onSuccessCallback, Action onFailureCallback) {
        final Reference<Integer> pending = new Reference<Integer>(0);

        for (final FirstLevelCache sharer : getCacheAccess().getLine().getDirectoryEntry().getSharers()) {
//            if(sharer.getCache().findLine(getCacheAccess().getReference().getTag()) == null) {
//                throw new IllegalArgumentException(); //TODO: to be uncommented or ensured, temp workaround
//            }

            FindCacheLineResult<FirstLevelCacheLine> findCacheLineResult = sharer.getCache().findLine(getCacheAccess().getReference().getTag());
            if(findCacheLineResult.getType() == FindCacheLineResultType.CACHE_HIT) {
                getCache().sendRequest(sharer, 8, new Action() {
                    @Override
                    public void apply() {
                        L2UpwardWriteFlow l2UpwardWriteFlow = new L2UpwardWriteFlow(LastLevelCacheFindAndLockFlow.this, sharer, getCache(), access, getCacheAccess().getReference().getTag());
                        l2UpwardWriteFlow.start(
                                new Action() {
                                    @Override
                                    public void apply() {
                                        pending.set(pending.get() - 1);

                                        if (pending.get() == 0) {
                                            doEvict(onSuccessCallback);
                                        }
                                    }
                                }
                        );
                    }
                });
                pending.set(pending.get() + 1);
            }
        }

        if (pending.get() == 0) {
            doEvict(onSuccessCallback);
        }
    }

    private void doEvict(final Action onSuccessCallback) {
        if (getCacheAccess().getLine().getState() == LastLevelCacheLineState.DIRTY) {
            getCache().sendRequest(getCache().getNext(), getCache().getCache().getLineSize() + 8, new Action() {
                @Override
                public void apply() {
                    getCache().getNext().memWriteRequestReceive(getCache(), getCacheAccess().getLine().getTag(), new Action() {
                        @Override
                        public void apply() {
                            getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), getCacheAccess().getLine()));
                            getCacheAccess().getLine().getDirectoryEntry().reset();
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
                    getCacheAccess().getLine().getDirectoryEntry().reset();
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
