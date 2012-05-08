package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.BasicFiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;
import archimulator.util.fsm.event.EnterStateEvent;

public class StoreFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private BasicFiniteStateMachine<StoreFlowState, StoreFlowCondition> fsm;

    public StoreFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
        this.fsm = new BasicFiniteStateMachine<StoreFlowState, StoreFlowCondition>(fsmFactory, "loadFlow", StoreFlowState.IDLE);
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new FindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.STORE);

        this.fsm.addListener(EnterStateEvent.class, new Action1<EnterStateEvent>() {
            @Override
            public void apply(EnterStateEvent event) {
                switch (fsm.getState()) {
                    case IDLE:
                        break;
                    case LOCKING:
                        break;
                    case LOCKED:
                        break;
                    case FAILED_TO_LOCK:
                        break;
                    case FAILED_TO_EVICT:
                        break;
                    case DOWNWARD_WRITING:
                        break;
                    case DOWNWARD_WRITE_COMPLETED:
                        break;
                    case UNLOCKED_WITHOUT_ERROR:
                        findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();
                        onSuccessCallback.apply();
                        break;
                    case UNLOCKED_WITH_ERROR:
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();
                        onFailureCallback.apply();
                        break;
                }
            }
        });

        findAndLockFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(StoreFlowCondition.LOCK_SUCCESS);

                        if (findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.SHARED || findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.INVALID) {
                            final DownwardWriteFlow downwardWriteFlow = new DownwardWriteFlow(getCache(), access, access.getPhysicalTag());

                            downwardWriteFlow.run(
                                    new Action() {
                                        @Override
                                        public void apply() {
                                            fsm.fireTransition(StoreFlowCondition.END_DOWNWARD_WRITING_ON_MISS);
                                            fsm.fireTransition(StoreFlowCondition.UNLOCK);
                                        }
                                    }, new Action() {
                                        @Override
                                        public void apply() {
                                            throw new UnsupportedOperationException(); //TODO
                                        }
                                    }
                            );

                            fsm.fireTransition(StoreFlowCondition.BEGIN_DOWNWARD_WRITING_ON_MISS);
                        } else {
                            fsm.fireTransition(StoreFlowCondition.UNLOCK);
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(StoreFlowCondition.LOCK_FAILURE);
                        fsm.fireTransition(StoreFlowCondition.UNLOCK);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(StoreFlowCondition.EVICT_FAILURE);
                        fsm.fireTransition(StoreFlowCondition.UNLOCK);
                    }
                }
        );
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    public static enum StoreFlowState {
        IDLE,
        LOCKING,
        LOCKED,
        FAILED_TO_LOCK,
        FAILED_TO_EVICT,
        DOWNWARD_WRITING,
        DOWNWARD_WRITE_COMPLETED,
        UNLOCKED_WITHOUT_ERROR,
        UNLOCKED_WITH_ERROR
    }

    public static enum StoreFlowCondition {
        BEGIN_LOCK,
        LOCK_SUCCESS,
        LOCK_FAILURE,
        EVICT_FAILURE,
        BEGIN_DOWNWARD_WRITING_ON_MISS,
        END_DOWNWARD_WRITING_ON_MISS,
        UNLOCK
    }

    private static FiniteStateMachineFactory<StoreFlowState, StoreFlowCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<StoreFlowState, StoreFlowCondition>();

        fsmFactory.inState(StoreFlowState.IDLE)
                .onCondition(StoreFlowCondition.BEGIN_LOCK, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.LOCKING;
                    }
                });

        fsmFactory.inState(StoreFlowState.LOCKING)
                .onCondition(StoreFlowCondition.LOCK_SUCCESS, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.LOCKED;
                    }
                })
                .onCondition(StoreFlowCondition.LOCK_FAILURE, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.FAILED_TO_LOCK;
                    }
                })
                .onCondition(StoreFlowCondition.EVICT_FAILURE, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.FAILED_TO_EVICT;
                    }
                });

        fsmFactory.inState(StoreFlowState.LOCKED)
                .onCondition(StoreFlowCondition.BEGIN_DOWNWARD_WRITING_ON_MISS, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.DOWNWARD_WRITING;
                    }
                })
                .onCondition(StoreFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.UNLOCKED_WITHOUT_ERROR;
                    }
                });

        fsmFactory.inState(StoreFlowState.FAILED_TO_LOCK)
                .onCondition(StoreFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.UNLOCKED_WITH_ERROR;
                    }
                });

        fsmFactory.inState(StoreFlowState.FAILED_TO_EVICT)
                .onCondition(StoreFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.UNLOCKED_WITH_ERROR;
                    }
                });

        fsmFactory.inState(StoreFlowState.DOWNWARD_WRITING)
                .onCondition(StoreFlowCondition.END_DOWNWARD_WRITING_ON_MISS, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.DOWNWARD_WRITE_COMPLETED;
                    }
                });

        fsmFactory.inState(StoreFlowState.DOWNWARD_WRITE_COMPLETED)
                .onCondition(StoreFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<StoreFlowState, StoreFlowCondition>, StoreFlowState>() {
                    @Override
                    public StoreFlowState apply(FiniteStateMachine<StoreFlowState, StoreFlowCondition> param1, Object... otherParams) {
                        return StoreFlowState.UNLOCKED_WITHOUT_ERROR;
                    }
                });
    }
}
