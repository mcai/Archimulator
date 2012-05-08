package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.DownwardReadMessage;
import archimulator.util.action.Action;

public class L1DownwardReadFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    protected MemoryHierarchyAccess access;
    protected int tag;
    private DownwardReadMessage message;
    private boolean copyBack;

    public L1DownwardReadFlow(final LastLevelCache cache, final FirstLevelCache source, final DownwardReadMessage message) {
        this.cache = cache;
        this.source = source;
        this.message = message;
        this.access = message.getAccess();
        this.tag = message.getTag();
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.DOWNWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            if (getCache().isOwnedOrShared(message.getTag())) {
                                final UpwardReadFlow upwardReadFlow = new UpwardReadFlow(getCache(), getCache().getOwnerOrFirstSharer(message.getTag()), message.getAccess(), message.getTag());
                                upwardReadFlow.start(
                                        new Action() {
                                            @Override
                                            public void apply() {
                                                if (upwardReadFlow.isCopyBack()) {
                                                    copyBack = true;
//                                                  findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                                                }

                                                reply(findAndLockFlow);

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
                            } else {
                                MemReadFlow memReadFlow = new MemReadFlow(getCache(), message.getAccess(), message.getTag());
                                memReadFlow.start(
                                        new Action() {
                                            @Override
                                            public void apply() {
//                                    if (!findAndLockProcess.getCacheAccess().isHitInCache() && !findAndLockProcess.getCacheAccess().isBypass()) {
//                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
//                                    }

                                                reply(findAndLockFlow);

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
                        }
                        else {
                            reply(findAndLockFlow);

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

                        onFailureCallback.apply();
                        getCache().sendReply(source, 8, message);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();
                        getCache().sendReply(source, 8, message);
                    }
                }
        );
    }

    private void reply(FindAndLockFlow findAndLockFlow) {
        getCache().getShadowTagDirectories().get(source).addTag(message.getTag());
        message.setShared(getCache().isShared(message.getTag()));

        getCache().sendReply(source, source.getCache().getLineSize() + 8, message);

        if (!findAndLockFlow.getCacheAccess().isHitInCache() && !findAndLockFlow.getCacheAccess().isBypass()) {
            if (copyBack) {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
            } else {
                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
            }
        }

        findAndLockFlow.getCacheAccess().commit().getLine().unlock();
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
