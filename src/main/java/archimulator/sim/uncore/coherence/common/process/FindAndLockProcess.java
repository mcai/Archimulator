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
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.exception.CacheLineLockFailedException;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.util.action.Action;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

public abstract class FindAndLockProcess extends CoherentCacheProcess {
    protected CacheAccess<MESIState, CoherentCache.LockableCacheLine> cacheAccess;
    protected FiniteStateMachine<FindAndLockState, FindAndLockCondition> fsm;

    public FindAndLockProcess(CoherentCache cache, final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        super(cache);
        this.fsm = new FiniteStateMachine<FindAndLockState, FindAndLockCondition>(fsmFactory, "findAndLock", FindAndLockState.READY);
        this.fsm.put("findAndLockProcess", this);

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                if (cacheAccess.isEviction()) {
                    evict(access, new Action() {
                        public void apply() {
                            fsm.fireTransition(FindAndLockCondition.EVICTED);
//                                state = FindAndLockState.ACQUIRED;
                        }
                    });
                }

                return true;
            }
        });

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() throws CoherentCacheException {
                if (fsm.getState() == FindAndLockState.READY) {
                    doLockingProcess(access, tag, cacheAccessType);
                }

                return fsm.getState() != FindAndLockState.WAITING;
            }
        });
    }

    private void doLockingProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
        if (this.fsm.getState() == FindAndLockState.READY) {
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
                    this.fsm.fireTransition(FindAndLockCondition.FAILED_TO_LOCK);
//                        this.state = FindAndLockState.FAILED;
//
//                        System.out.printf("access: %s%n", access);
//                        System.out.printf("access.getPhysicalTag(): 0x%08x%n", access.getPhysicalTag());
//                        System.out.printf("this.cacheAccess.getLine().getAccess(): %s%n", this.cacheAccess.getLine().getAccess());
//                        System.out.printf("this.cacheAccess.getLine().getAccess().getPhysicalTag(): 0x%08x%n", this.cacheAccess.getLine().getAccess().getPhysicalTag());
                }
                else {
                    if (this.cacheAccess.getLine().lock(new Action() {
                        public void apply() {
                            fsm.fireTransition(FindAndLockCondition.UNLOCKED);
//                                state = FindAndLockState.READY;
                            doLockingProcess(access, tag, cacheAccessType);
                        }
                    }, tag)) {
                        if (!cacheAccessType.isUpward()) {
                            this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCache(), tag, access, this.cacheAccess.getLine(), this.cacheAccess.isHitInCache(), this.cacheAccess.isEviction(), this.cacheAccess.getReference().getAccessType()));
                        }

                        if (this.cacheAccess.isEviction()) {
                            getCache().incEvictions();
                            this.fsm.fireTransition(FindAndLockCondition.BEGIN_EVICTING);
//                                state = FindAndLockState.EVICTING;
                        } else {
                            this.fsm.fireTransition(FindAndLockCondition.NO_EVICTION);
//                                state = FindAndLockState.ACQUIRED;
                        }
                    } else {
                        this.fsm.fireTransition(FindAndLockCondition.WAIT_FOR_UNLOCK);
//                            state = FindAndLockState.WAITING;
                    }
                }
            } else {
                this.fsm.fireTransition(FindAndLockCondition.BYPASS);
//                    state = FindAndLockState.BYPASSED;
            }

            if (this.fsm.getState() == FindAndLockState.ACQUIRED || this.fsm.getState() == FindAndLockState.EVICTING || this.fsm.getState() == FindAndLockState.BYPASSED) {
                this.getCache().updateStats(cacheAccessType, this.cacheAccess);
                this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getCache(), access, this.cacheAccess));
            }
        }
    }

    public CacheAccess<MESIState, CoherentCache.LockableCacheLine> getCacheAccess() {
        return cacheAccess;
    }

    protected abstract void evict(MemoryHierarchyAccess access, Action onCompletedCallback);

    @Override
    public String toString() {
        return String.format("%s %s", this.fsm.getState(), this.fsm.getState() == FindAndLockState.READY ? "" : cacheAccess);
    }

    private static FiniteStateMachineFactory<FindAndLockState, FindAndLockCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<FindAndLockState, FindAndLockCondition>();

        fsmFactory.inState(FindAndLockState.READY)
                .onCondition(FindAndLockCondition.BYPASS, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.BYPASSED;
                    }
                })
                .onCondition(FindAndLockCondition.NO_EVICTION, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.ACQUIRED;
                    }
                })
                .onCondition(FindAndLockCondition.BEGIN_EVICTING, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.EVICTING;
                    }
                })
                .onCondition(FindAndLockCondition.WAIT_FOR_UNLOCK, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.WAITING;
                    }
                })
                .onCondition(FindAndLockCondition.FAILED_TO_LOCK, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        throw new CacheLineLockFailedException();
//                        return FindAndLockState.FAILED;
                    }
                });

        fsmFactory.inState(FindAndLockState.EVICTING)
                .onCondition(FindAndLockCondition.EVICTED, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.ACQUIRED;
                    }
                });

        fsmFactory.inState(FindAndLockState.WAITING)
                .onCondition(FindAndLockCondition.UNLOCKED, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.READY;
                    }
                });

        fsmFactory.inState(FindAndLockState.ACQUIRED)
                .onCondition(FindAndLockCondition.COMPLETED, new Function1X<FiniteStateMachine<FindAndLockState, FindAndLockCondition>, FindAndLockState>() {
                    @Override
                    public FindAndLockState apply(FiniteStateMachine<FindAndLockState, FindAndLockCondition> param1, Object... otherParams) {
                        return FindAndLockState.RELEASED;
                    }
                });
    }
}
