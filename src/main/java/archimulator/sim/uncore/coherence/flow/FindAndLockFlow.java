package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.message.EvictMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.BasicFiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;
import archimulator.util.fsm.event.EnterStateEvent;

public class FindAndLockFlow {
    private CoherentCache cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private CacheAccessType cacheAccessType;
    private CacheAccess<MESIState, LockableCacheLine> cacheAccess;
    private BasicFiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition> fsm;

    public FindAndLockFlow(CoherentCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
        this.cacheAccessType = cacheAccessType;
        this.fsm = new BasicFiniteStateMachine<FindAndLockFlowState, FindAndLockFlowCondition>(fsmFactory, "findAndLockFlow", FindAndLockFlowState.IDLE);
    }

    public void run(final Action onSuccessCallback, final Action onLockFailureCallback, final Action onEvictFailureCallback) {
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
                        break;
                    case EVICTING:
                        break;
                    case LOCKED:
                        onSuccessCallback.apply();
                        break;
                    case FAILED_TO_EVICT:
                        onEvictFailureCallback.apply();
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
                this.fsm.fireTransition(FindAndLockFlowCondition.FAILED_TO_LOCK);
            } else {
                Action action = new Action() {
                    public void apply() {
                        doLockingProcess();
                    }
                };

                boolean result;
                if (this.cacheAccess.isHitInCache()) {
                    result = this.cacheAccess.getLine().beginHit(action, tag);
                }
                else if (this.cacheAccess.isEviction()) {
                    result = this.cacheAccess.getLine().beginEvict(action, tag);
                }
                else {
                    result = this.cacheAccess.getLine().beginFill(action, tag);
                }

                if(result) {
                    if(this.cacheAccess.isEviction()) {
                        this.evict(access);
                        this.fsm.fireTransition(FindAndLockFlowCondition.BEGIN_EVICT);
                    }
                    else {
                        this.fsm.fireTransition(FindAndLockFlowCondition.NO_EVICT);
                    }
                }
                else {
                    this.fsm.fireTransition(FindAndLockFlowCondition.WAIT_FOR_UNLOCK);
                }
            }
        }
        else {
            this.fsm.fireTransition(FindAndLockFlowCondition.BYPASS);
        }
    }

    private void evict(MemoryHierarchyAccess access) { //TODO: this is modeled after EvictProcess, please consider both FLC and LLC cases
        final boolean hasData = cacheAccess.getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;

        getCache().sendRequest(getCache().getNext(), new EvictMessage(access, cacheAccess.getLine().getTag(), hasData, new Action1<EvictMessage>() {
            public void apply(EvictMessage evictMessage) {
                if (evictMessage.isError()) {
                    cacheAccess.getLine().endEvict();
                    fsm.fireTransition(FindAndLockFlowCondition.FAILED_TO_EVICT);
                } else {
                    cacheAccess.getLine().endEvict();
                    fsm.fireTransition(FindAndLockFlowCondition.EVICTED);
                }
            }
        }), size);
    }

    public CoherentCache getCache() {
        return cache;
    }

    public CacheAccess<MESIState, LockableCacheLine> getCacheAccess() {
        return cacheAccess;
    }

    public static enum FindAndLockFlowState {
        IDLE,
        WAITING,
        FAILED_TO_LOCK,
        EVICTING,
        LOCKED,
        FAILED_TO_EVICT
    }

    public static enum FindAndLockFlowCondition {
        WAIT_FOR_UNLOCK,
        FAILED_TO_LOCK,
        UNLOCKED,
        BYPASS,
        NO_EVICT,
        BEGIN_EVICT,
        EVICTED,
        FAILED_TO_EVICT
    }

    private static FiniteStateMachineFactory<FindAndLockFlowState, FindAndLockFlowCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<FindAndLockFlowState, FindAndLockFlowCondition>();

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