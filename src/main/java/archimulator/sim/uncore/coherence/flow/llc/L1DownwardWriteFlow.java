package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L1DownwardWriteFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;

    public L1DownwardWriteFlow(final LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.DOWNWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        UpwardWriteFlow upwardWriteFlow = new UpwardWriteFlow(getCache(), source, access, tag);
                        upwardWriteFlow.start(
                                new Action() {
                                    @Override
                                    public void apply() {
                                        if (!findAndLockFlow.getCacheAccess().isHitInCache() && !getCache().isOwnedOrShared(tag)) {
                                            MemReadFlow memReadFlow = new MemReadFlow(getCache(), access, tag);
                                            memReadFlow.start(
                                                    new Action() {
                                                        @Override
                                                        public void apply() {
                                                            for (final FirstLevelCache sharer : getCache().getSharers(tag)) {
                                                                getCache().getShadowTagDirectories().get(sharer).removeTag(tag);
                                                            }

                                                            getCache().getShadowTagDirectories().get(source).addTag(tag);

                                                            if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                                                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? MESIState.MODIFIED : MESIState.EXCLUSIVE);
                                                            }

                                                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                                            getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);

                                                            endFillOrEvict(findAndLockFlow);

                                                            afterFlowEnd(findAndLockFlow);
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
                                }, new Action() {
                                    @Override
                                    public void apply() {
                                        throw new UnsupportedOperationException();
                                    }
                                }
                        );
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

    public LastLevelCache getCache() {
        return cache;
    }
}
