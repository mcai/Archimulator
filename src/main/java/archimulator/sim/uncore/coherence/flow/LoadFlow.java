package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.BasicFiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;
import archimulator.util.fsm.event.EnterStateEvent;

public class LoadFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private BasicFiniteStateMachine<LoadFlowState, LoadFlowCondition> fsm;

    public LoadFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
        this.fsm = new BasicFiniteStateMachine<LoadFlowState, LoadFlowCondition>(fsmFactory, "loadFlow", LoadFlowState.IDLE);
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new FindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.LOAD);

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
                    case DOWNWARD_READING:
                        break;
                    case DOWNWARD_READ_COMPLETED:
                        break;
                    case UNLOCKED_WITHOUT_ERROR:
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
                        fsm.fireTransition(LoadFlowCondition.LOCK_SUCCESS);
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            final DownwardReadFlow downwardReadFlow = new DownwardReadFlow(getCache(), access, access.getPhysicalTag());

                            downwardReadFlow.run(
                                    new Action() {
                                        @Override
                                        public void apply() {
                                            fsm.fireTransition(LoadFlowCondition.END_DOWNWARD_READING_ON_MISS);

                                            findAndLockFlow.getCacheAccess().getLine().setNonInitialState(downwardReadFlow.isShared() ? MESIState.SHARED : MESIState.EXCLUSIVE);

                                            fsm.fireTransition(LoadFlowCondition.UNLOCK);
                                        }
                                    }, new Action() {
                                        @Override
                                        public void apply() {
                                            throw new UnsupportedOperationException(); //TODO
                                        }
                                    }
                            );

                            fsm.fireTransition(LoadFlowCondition.BEGIN_DOWNWARD_READING_ON_MISS);
                        } else {
                            fsm.fireTransition(LoadFlowCondition.UNLOCK);
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(LoadFlowCondition.LOCK_FAILURE);
                        fsm.fireTransition(LoadFlowCondition.UNLOCK);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        fsm.fireTransition(LoadFlowCondition.EVICT_FAILURE);
                        fsm.fireTransition(LoadFlowCondition.UNLOCK);
                    }
                }
        );
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    public static enum LoadFlowState {
        IDLE,
        LOCKING,
        LOCKED,
        FAILED_TO_LOCK,
        FAILED_TO_EVICT,
        DOWNWARD_READING,
        DOWNWARD_READ_COMPLETED,
        UNLOCKED_WITHOUT_ERROR,
        UNLOCKED_WITH_ERROR
    }

    public static enum LoadFlowCondition {
        BEGIN_LOCK,
        LOCK_SUCCESS,
        LOCK_FAILURE,
        EVICT_FAILURE,
        BEGIN_DOWNWARD_READING_ON_MISS,
        END_DOWNWARD_READING_ON_MISS,
        UNLOCK
    }

    private static FiniteStateMachineFactory<LoadFlowState, LoadFlowCondition> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<LoadFlowState, LoadFlowCondition>();

        fsmFactory.inState(LoadFlowState.IDLE)
                .onCondition(LoadFlowCondition.BEGIN_LOCK, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.LOCKING;
                    }
                });

        fsmFactory.inState(LoadFlowState.LOCKING)
                .onCondition(LoadFlowCondition.LOCK_SUCCESS, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.LOCKED;
                    }
                })
                .onCondition(LoadFlowCondition.LOCK_FAILURE, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.FAILED_TO_LOCK;
                    }
                })
                .onCondition(LoadFlowCondition.EVICT_FAILURE, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.FAILED_TO_EVICT;
                    }
                });

        fsmFactory.inState(LoadFlowState.LOCKED)
                .onCondition(LoadFlowCondition.BEGIN_DOWNWARD_READING_ON_MISS, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.DOWNWARD_READING;
                    }
                })
                .onCondition(LoadFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.UNLOCKED_WITHOUT_ERROR;
                    }
                });

        fsmFactory.inState(LoadFlowState.FAILED_TO_LOCK)
                .onCondition(LoadFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.UNLOCKED_WITH_ERROR;
                    }
                });

        fsmFactory.inState(LoadFlowState.FAILED_TO_EVICT)
                .onCondition(LoadFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.UNLOCKED_WITH_ERROR;
                    }
                });

        fsmFactory.inState(LoadFlowState.DOWNWARD_READING)
                .onCondition(LoadFlowCondition.END_DOWNWARD_READING_ON_MISS, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.DOWNWARD_READ_COMPLETED;
                    }
                });

        fsmFactory.inState(LoadFlowState.DOWNWARD_READ_COMPLETED)
                .onCondition(LoadFlowCondition.UNLOCK, new Function1X<FiniteStateMachine<LoadFlowState, LoadFlowCondition>, LoadFlowState>() {
                    @Override
                    public LoadFlowState apply(FiniteStateMachine<LoadFlowState, LoadFlowCondition> param1, Object... otherParams) {
                        return LoadFlowState.UNLOCKED_WITHOUT_ERROR;
                    }
                });
    }
}
