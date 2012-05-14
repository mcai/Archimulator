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
import archimulator.sim.uncore.coherence.common.FirstLevelCache;
import archimulator.sim.uncore.coherence.common.FirstLevelCacheLine;
import archimulator.sim.uncore.coherence.common.LastLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCacheLineState;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardWriteFlow;
import archimulator.util.Reference;
import archimulator.util.action.Action;

public class L1DownwardWriteFlow extends Flow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;

    public L1DownwardWriteFlow(Flow producerFlow, final LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        final LastLevelCacheFindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.DOWNWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        invalidateSharers(findAndLockFlow, onSuccessCallback);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        findAndLockFlow.getCacheAccess().abort();
//                        findAndLockFlow.getCacheAccess().getLine().unlock();
//
                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }
        );
    }

    private void invalidateSharers(final LastLevelCacheFindAndLockFlow findAndLockFlow, final Action onSuccessCallback) {
        final Reference<Integer> pending = new Reference<Integer>(0);

        for (final FirstLevelCache sharer : findAndLockFlow.getCache().getSharers(tag)) {
            if (sharer != source) {
//                if(sharer.getCache().findLine(findAndLockFlow.getCacheAccess().getReference().getTag()) == null) {
//                    throw new IllegalArgumentException(); //TODO: to be uncommented or ensured, temp workaround
//                }

                FindCacheLineResult<FirstLevelCacheLine> findCacheLineResult = sharer.getCache().findLine(findAndLockFlow.getCacheAccess().getReference().getTag());
                if(findCacheLineResult.getType() == FindCacheLineResultType.CACHE_HIT) {
                    getCache().sendRequest(sharer, 8, new Action() {
                        @Override
                        public void apply() {
                            L2UpwardWriteFlow l2UpwardWriteFlow = new L2UpwardWriteFlow(L1DownwardWriteFlow.this, sharer, getCache(), access, tag);
                            l2UpwardWriteFlow.start(
                                    new Action() {
                                        @Override
                                        public void apply() {
                                            pending.set(pending.get() - 1);

                                            if (pending.get() == 0) {
                                                getData(findAndLockFlow, onSuccessCallback);
                                            }
                                        }
                                    }
                            );
                        }
                    });
                    pending.set(pending.get() + 1);
                }
            }
        }

        if (pending.get() == 0) {
            getData(findAndLockFlow, onSuccessCallback);
        }
    }

    private void getData(final LastLevelCacheFindAndLockFlow findAndLockFlow, final Action onSuccessCallback) {
        if (!findAndLockFlow.getCacheAccess().isHitInCache() && !findAndLockFlow.getCache().isOwnedOrShared(tag)) {
            getCache().sendRequest(getCache().getNext(), 8, new Action() {
                @Override
                public void apply() {
                    getCache().getNext().memReadRequestReceive(getCache(), tag, new Action() {
                        @Override
                        public void apply() {
                            endGetData(findAndLockFlow, onSuccessCallback);
                        }
                    });
                }
            });
        }
        else {
            endGetData(findAndLockFlow, onSuccessCallback);
        }
    }

    private void endGetData(LastLevelCacheFindAndLockFlow findAndLockFlow, Action onSuccessCallback) {
        if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
            findAndLockFlow.getCacheAccess().getLine().setNonInitialState(findAndLockFlow.getCacheAccess().getLine().getState() == LastLevelCacheLineState.DIRTY ? LastLevelCacheLineState.DIRTY : LastLevelCacheLineState.CLEAN);
        }

        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

        getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);
        onDestroy();
    }

    public LastLevelCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L1DownwardWriteFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
