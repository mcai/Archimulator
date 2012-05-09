package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.llc.L1DownwardReadFlow;
import archimulator.util.action.Action;

public class LoadFlow extends LockingFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public LoadFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        pendings++;

        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.LOAD);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            downwardRead(findAndLockFlow, onSuccessCallback, onFailureCallback);
                        } else {
                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                            endFillOrEvict(findAndLockFlow);

                            afterFlowEnd(findAndLockFlow);

                            onSuccessCallback.apply();

                            pendings--;
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        findAndLockFlow.getCacheAccess().abort();
//                        findAndLockFlow.getCacheAccess().getLine().unlock();
//
//                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();

                        pendings--;
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();

                        pendings--;
                    }
                }
        );
    }

    private void downwardRead(final FindAndLockFlow findAndLockFlow, final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(getCache().getNext(), 8, new Action() {
            @Override
            public void apply() {
                final L1DownwardReadFlow l1DownwardReadFlow = new L1DownwardReadFlow(getCache().getNext(), getCache(), access, tag);
                l1DownwardReadFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(l1DownwardReadFlow.isShared() ? MESIState.SHARED : MESIState.EXCLUSIVE);

                                findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                endFillOrEvict(findAndLockFlow);

                                afterFlowEnd(findAndLockFlow);

                                onSuccessCallback.apply();

                                pendings--;
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                getCache().getCycleAccurateEventQueue().schedule(new Action() {
                                    public void apply() {
                                        downwardRead(findAndLockFlow, onSuccessCallback, onFailureCallback);
                                    }
                                }, getCache().getRetryLatency());

//                                            findAndLockFlow.getCacheAccess().abort();
//                                            findAndLockFlow.getCacheAccess().getLine().unlock();
//
//                                            abort(findAndLockFlow);
//
//                                            afterFlowEnd(findAndLockFlow);
//
//                                            onFailureCallback.apply();
                            }
                        }
                );
            }
        });
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    private static int pendings = 0;
}
