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
package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.BasicFiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;
import archimulator.util.fsm.event.EnterStateEvent;

import java.io.Serializable;

public abstract class FindAndLockFlow<StateT extends Serializable, LineT extends LockableCacheLine<StateT>> extends Flow {
    private CoherentCache<StateT, LineT> cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private CacheAccessType cacheAccessType;
    private CacheAccess<StateT, LineT> cacheAccess;
    private BasicFiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> fsm;

    public FindAndLockFlow(Flow producerFlow, CoherentCache<StateT, LineT> cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(producerFlow);
        this.cache = cache;
        this.access = access;
        this.tag = tag;
        this.cacheAccessType = cacheAccessType;
        this.fsm = new BasicFiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>("findAndLockFlow", FindAndLockFlowState.IDLE);
    }

    public void start(final Action onSuccessCallback, final Action onLockFailureCallback, final Action onEvictFailureCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        this.fsm.addListener(EnterStateEvent.class, new Action1<EnterStateEvent>() {
            @Override
            public void apply(EnterStateEvent event) {
                FindAndLockFlowState state = fsm.getState();
                switch (state) {
                    case IDLE:
                        break;
                    case WAITING:
                        break;
                    case FAILED_TO_LOCK:
                        onLockFailureCallback.apply();
                        onDestroy();
                        break;
                    case EVICTING:
                        getCache().incNumEvictions();
                        break;
                    case LOCKED:
                        if(!cacheAccess.isBypass()) {
                            getCache().updateStats(cacheAccessType, cacheAccess);
                            getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(getCache(), access, cacheAccess));
                        }
                        onSuccessCallback.apply();
                        onDestroy();
                        break;
                    case FAILED_TO_EVICT:
                        onEvictFailureCallback.apply();
                        onDestroy();
                        break;
                }
            }
        });
        this.doLockingProcess();
    }

    private void doLockingProcess() {
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
                fsmFactory.fireTransition(this.fsm, FindAndLockFlowCondition.FAILED_TO_LOCK);
            } else {
                if (this.cacheAccess.getLine().lock(new Action() {
                    public void apply() {
                        fsmFactory.fireTransition(fsm, FindAndLockFlowCondition.UNLOCKED);
                        doLockingProcess();
                    }
                }, tag, getProducerFlow())) {
                    if (!cacheAccessType.isUpward()) {
                        this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCache(), tag, access, this.cacheAccess.getLine(), this.cacheAccess.isHitInCache(), this.cacheAccess.isEviction(), this.cacheAccess.getReference().getAccessType()));
                    }

                    if (this.cacheAccess.isEviction()) {
                        this.evict();
                        fsmFactory.fireTransition(this.fsm, FindAndLockFlowCondition.BEGIN_EVICT);
                    } else {
                        fsmFactory.fireTransition(this.fsm, FindAndLockFlowCondition.NO_EVICT);
                    }
                }
                else {
                    fsmFactory.fireTransition(this.fsm, FindAndLockFlowCondition.WAIT_FOR_UNLOCK);
                }
            }
        } else {
            fsmFactory.fireTransition(this.fsm, FindAndLockFlowCondition.BYPASS);
        }
    }

    private void evict() {
        this.evict(access,
                new Action() {
                    @Override
                    public void apply() {
                        fsmFactory.fireTransition(fsm, FindAndLockFlowCondition.EVICTED);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsmFactory.fireTransition(fsm, FindAndLockFlowCondition.FAILED_TO_EVICT);
                    }
                }
        );
    }

    protected abstract void evict(MemoryHierarchyAccess access, Action onSuccessCallback, Action onFailureCallback);

    public CoherentCache<StateT, LineT> getCache() {
        return cache;
    }

    public CacheAccess<StateT, LineT> getCacheAccess() {
        return cacheAccess;
    }

    @Override
    public Flow getProducerFlow() {
        return super.getProducerFlow();
    }

    public FindAndLockFlowState getState() {
        return this.fsm.getState();
    }

    private enum FindAndLockFlowState {
        IDLE,
        WAITING,
        FAILED_TO_LOCK,
        EVICTING,
        LOCKED,
        FAILED_TO_EVICT
    }

    private enum FindAndLockFlowCondition {
        WAIT_FOR_UNLOCK,
        FAILED_TO_LOCK,
        UNLOCKED,
        BYPASS,
        NO_EVICT,
        BEGIN_EVICT,
        EVICTED,
        FAILED_TO_EVICT
    }

    private static FiniteStateMachineFactory<FindAndLockFlowState, FindAndLockFlowCondition, FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<FindAndLockFlowState, FindAndLockFlowCondition, FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>>();

        fsmFactory.inState(FindAndLockFlowState.IDLE)
                .onCondition(FindAndLockFlowCondition.WAIT_FOR_UNLOCK, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.WAITING;
                    }
                })
                .onCondition(FindAndLockFlowCondition.FAILED_TO_LOCK, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.FAILED_TO_LOCK;
                    }
                })
                .onCondition(FindAndLockFlowCondition.BYPASS, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.LOCKED;
                    }
                })
                .onCondition(FindAndLockFlowCondition.NO_EVICT, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.LOCKED;
                    }
                })
                .onCondition(FindAndLockFlowCondition.BEGIN_EVICT, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.EVICTING;
                    }
                });

        fsmFactory.inState(FindAndLockFlowState.WAITING)
                .onCondition(FindAndLockFlowCondition.UNLOCKED, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.IDLE;
                    }
                });

        fsmFactory.inState(FindAndLockFlowState.EVICTING)
                .onCondition(FindAndLockFlowCondition.EVICTED, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.LOCKED;
                    }
                })
                .onCondition(FindAndLockFlowCondition.FAILED_TO_EVICT, new Function1X<FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>, FindAndLockFlowState>() {
                    @Override
                    public FindAndLockFlowState apply(FiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> param1, Object... otherParams) {
                        return FindAndLockFlowState.FAILED_TO_EVICT;
                    }
                });
    }
}
