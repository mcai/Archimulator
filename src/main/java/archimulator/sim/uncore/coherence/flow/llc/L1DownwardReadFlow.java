package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardReadFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L1DownwardReadFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    protected MemoryHierarchyAccess access;
    protected int tag;
    private boolean copyBack;
    private boolean shared;

    public L1DownwardReadFlow(final LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.DOWNWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            if (getCache().isOwnedOrShared(tag)) {
                                getCache().sendRequest(getCache().getOwnerOrFirstSharer(tag), 8, new Action() {
                                    @Override
                                    public void apply() {
                                        final L2UpwardReadFlow l2UpwardReadFlow = new L2UpwardReadFlow(getCache().getOwnerOrFirstSharer(tag), getCache(), access, tag);
                                        l2UpwardReadFlow.start(
                                                new Action() {
                                                    @Override
                                                    public void apply() {
                                                        if (l2UpwardReadFlow.isCopyBack()) {
                                                            copyBack = true;
//                                                          findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                                                        }

                                                        reply(findAndLockFlow, onSuccessCallback);

                                                        endFillOrEvict(findAndLockFlow);

                                                        afterFlowEnd(findAndLockFlow);

                                                        onSuccessCallback.apply();
                                                    }
                                                }, new Action() {
                                                    @Override
                                                    public void apply() {
                                                        throw new UnsupportedOperationException();
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

                                                endFillOrEvict(findAndLockFlow);

                                                afterFlowEnd(findAndLockFlow);

                                                onSuccessCallback.apply();
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        else {
                            reply(findAndLockFlow, onSuccessCallback);

                            endFillOrEvict(findAndLockFlow);

                            afterFlowEnd(findAndLockFlow);

                            onSuccessCallback.apply();
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
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        afterFlowEnd(findAndLockFlow);

                        getCache().sendReply(source, 8, onFailureCallback);
                    }
                }
        );
    }

    private void reply(FindAndLockFlow findAndLockFlow, Action onSuccessCallback) {
        getCache().getShadowTagDirectories().get(source).addTag(tag);
        this.shared = getCache().isShared(tag);

        getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);

        if (!findAndLockFlow.getCacheAccess().isHitInCache() && !findAndLockFlow.getCacheAccess().isBypass()) {
            if (copyBack) {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
            } else {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
            }
        }

        findAndLockFlow.getCacheAccess().commit().getLine().unlock();
    }

    public boolean isShared() {
        return shared;
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
