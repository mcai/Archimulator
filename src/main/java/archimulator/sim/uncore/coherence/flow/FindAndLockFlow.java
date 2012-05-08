package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.LockableCacheLineReplacementState;
import archimulator.sim.uncore.coherence.common.MESIState;
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

import java.io.FileWriter;
import java.io.IOException;

public abstract class FindAndLockFlow extends Flow {
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

    public void start(final Action onSuccessCallback, final Action onLockFailureCallback, final Action onEvictFailureCallback) {
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

    public static FileWriter fwLocks;

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
                try {
                    fwLocks.write(String.format("[%d] %s: %s failed to lock, locked by %s\n", this.getCache().getCycleAccurateEventQueue().getCurrentCycle(), this.getCache().getName(), this.cacheAccess, this.cacheAccess.getLine().getCacheAccess()));
                    fwLocks.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                this.fsm.fireTransition(FindAndLockFlowCondition.FAILED_TO_LOCK);
            } else {
                Action action = new Action() {
                    public void apply() {
                        doLockingProcess();
                    }
                };

                boolean result = this.cacheAccess.isHitInCache() ? this.cacheAccess.getLine().beginHit(action, tag, this.cacheAccess) :
                        (this.cacheAccess.isEviction() ? this.cacheAccess.getLine().beginEvict(action, tag, this.cacheAccess) : this.cacheAccess.getLine().beginFill(action, tag, this.cacheAccess));

                if (result) {
                    try {
                        fwLocks.write(String.format("[%d] %s: %s lock\n", this.getCache().getCycleAccurateEventQueue().getCurrentCycle(), this.getCache().getName(), this.cacheAccess));
                        fwLocks.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (!cacheAccessType.isUpward()) {
                        this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(this.getCache(), tag, access, this.cacheAccess.getLine(), this.cacheAccess.isHitInCache(), this.cacheAccess.isEviction(), this.cacheAccess.getReference().getAccessType()));
                    }

                    if (this.cacheAccess.isEviction()) {
                        getCache().incEvictions();
                    }
                }

                if (result) {
                    if (this.cacheAccess.isEviction()) {
                        this.evict(access);
                        this.fsm.fireTransition(FindAndLockFlowCondition.BEGIN_EVICT);
                    } else {
                        this.fsm.fireTransition(FindAndLockFlowCondition.NO_EVICT);
                    }
                } else {
                    this.fsm.fireTransition(FindAndLockFlowCondition.WAIT_FOR_UNLOCK);
                }
            }
        } else {
            this.fsm.fireTransition(FindAndLockFlowCondition.BYPASS);
        }

        if(this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.HITTING || this.cacheAccess.getLine().getReplacementState() == LockableCacheLineReplacementState.FILLING || this.cacheAccess.isBypass()) {
            this.getCache().updateStats(cacheAccessType, this.cacheAccess);
            this.getCache().getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(this.getCache(), access, this.cacheAccess));
        }
    }

    private void evict(MemoryHierarchyAccess access) {
        AbstractEvictFlow evictFlow = this.createEvictFlow(access);
        evictFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(FindAndLockFlowCondition.EVICTED);
                        cacheAccess.getLine().beginFillAfterEvict();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(FindAndLockFlowCondition.FAILED_TO_EVICT);
                    }
                }
        );
    }

    protected abstract AbstractEvictFlow createEvictFlow(MemoryHierarchyAccess access);

    public CoherentCache getCache() {
        return cache;
    }

    public CacheAccess<MESIState, LockableCacheLine> getCacheAccess() {
        return cacheAccess;
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

        try {
            fwLocks = new FileWriter("/home/itecgo/Desktop/failedToLock.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
