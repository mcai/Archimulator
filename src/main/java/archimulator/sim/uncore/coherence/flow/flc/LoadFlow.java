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
        super(null);
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        findAndLock(onSuccessCallback);
    }

    private void findAndLock(final Action onSuccessCallback) {
        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.LOAD);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            downwardRead(findAndLockFlow, onSuccessCallback);
                        } else {
                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                            onSuccessCallback.apply();
                            onDestroy();
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                            public void apply() {
                                findAndLock(onSuccessCallback);
                            }
                        }, getCache().getRetryLatency());
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                            public void apply() {
                                findAndLock(onSuccessCallback);
                            }
                        }, getCache().getRetryLatency());
                    }
                }
        );
    }

    private void downwardRead(final FindAndLockFlow findAndLockFlow, final Action onSuccessCallback) {
        getCache().sendRequest(getCache().getNext(), 8, new Action() {
            @Override
            public void apply() {
                final L1DownwardReadFlow l1DownwardReadFlow = new L1DownwardReadFlow(LoadFlow.this, getCache().getNext(), getCache(), access, tag);
                l1DownwardReadFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(l1DownwardReadFlow.isShared() ? MESIState.SHARED : MESIState.EXCLUSIVE);

                                findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                onSuccessCallback.apply();
                                onDestroy();
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                numDownwardReadRetries++;

                                findAndLockFlow.getCacheAccess().abort();
                                findAndLockFlow.getCacheAccess().getLine().unlock();

                                getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                                    public void apply() {
                                        findAndLock(onSuccessCallback);
                                    }
                                }, getCache().getRetryLatency());
                            }
                        }
                );
            }
        });
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    private int numDownwardReadRetries = 0;

    public int getNumDownwardReadRetries() {
        return numDownwardReadRetries;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s {tag=0x%08x}: LoadFlow#%d", getBeginCycle(), getCache().getName(), access.getPhysicalTag(), getId());
    }
}
