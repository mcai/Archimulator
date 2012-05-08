package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
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
        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.LOAD);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            final DownwardReadFlow downwardReadFlow = new DownwardReadFlow(getCache(), access, access.getPhysicalTag());

                            downwardReadFlow.start(
                                    new Action() {
                                        @Override
                                        public void apply() {
                                            findAndLockFlow.getCacheAccess().getLine().setNonInitialState(downwardReadFlow.isShared() ? MESIState.SHARED : MESIState.EXCLUSIVE);

                                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                            endFillOrEvict(findAndLockFlow);

                                            onSuccessCallback.apply();
                                        }
                                    }, new Action() {
                                        @Override
                                        public void apply() {
                                            throw new UnsupportedOperationException(); //TODO
                                        }
                                    }
                            );
                        } else {
                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                            endFillOrEvict(findAndLockFlow);

                            onSuccessCallback.apply();
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();
                        onFailureCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();
                        onFailureCallback.apply();
                    }
                }
        );
    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
