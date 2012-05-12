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
import archimulator.sim.uncore.coherence.common.FirstLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCacheLineState;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardReadFlow;
import archimulator.util.action.Action;

public class L1DownwardReadFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    protected MemoryHierarchyAccess access;
    protected int tag;
    private boolean copyBack;
    private boolean shared;

    public L1DownwardReadFlow(Flow producerFlow, final LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        final LastLevelCacheFindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.DOWNWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            if (getCache().isOwned(tag)) {
                                getCache().sendRequest(getCache().getOwnerOrFirstSharer(tag), 8, new Action() {
                                    @Override
                                    public void apply() {
                                        final L2UpwardReadFlow l2UpwardReadFlow = new L2UpwardReadFlow(L1DownwardReadFlow.this, getCache().getOwnerOrFirstSharer(tag), getCache(), access, tag);
                                        l2UpwardReadFlow.start(
                                                new Action() {
                                                    @Override
                                                    public void apply() {
                                                        if (l2UpwardReadFlow.isCopyBack()) {
                                                            copyBack = true;
//                                                          findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                                                        }

                                                        reply(findAndLockFlow, onSuccessCallback);
                                                    }
                                                }
                                        );
                                    }
                                });
                            } else {
                                getCache().sendRequest(getCache().getNext(), 8, new Action() {
                                    @Override
                                    public void apply() {
                                        getCache().getNext().memReadRequestReceive(getCache(), tag, new Action() {
                                            @Override
                                            public void apply() {
                                                //                                    if (!findAndLockProcess.getCacheAccess().isHitInCache() && !findAndLockProcess.getCacheAccess().isBypass()) {
                                                //                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
                                                //                                    }

                                                reply(findAndLockFlow, onSuccessCallback);
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        else {
                            reply(findAndLockFlow, onSuccessCallback);
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        findAndLockFlow.getCacheAccess().abort();
//                        findAndLockFlow.getCacheAccess().getLine().unlock();
//
//                        afterFlowEnd(findAndLockFlow);

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

    private void reply(LastLevelCacheFindAndLockFlow findAndLockFlow, Action onSuccessCallback) {
        getCache().getShadowTagDirectories().get(source).addTag(tag);
        this.shared = getCache().isShared(tag);

        if (!findAndLockFlow.getCacheAccess().isHitInCache() && !findAndLockFlow.getCacheAccess().isBypass()) {
            if (copyBack) {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(LastLevelCacheLineState.DIRTY);
            } else {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(LastLevelCacheLineState.CLEAN);
            }
        }

        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

        getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);
        onDestroy();
    }

    public boolean isShared() {
        return shared;
    }

    public LastLevelCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L1DownwardReadFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
