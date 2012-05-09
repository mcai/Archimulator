package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L2UpwardReadFlow extends LockingFlow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean copyBack;

    public L2UpwardReadFlow(FirstLevelCache cache, final LastLevelCache source, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.UPWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        copyBack = findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED;

                        findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.SHARED);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        endFillOrEvict(findAndLockFlow);

                        afterFlowEnd(findAndLockFlow);

                        getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        afterFlowEnd(findAndLockFlow);

                        getCache().sendReply(source, 8, onFailureCallback);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        afterFlowEnd(findAndLockFlow);

                        getCache().sendReply(source, 8, onFailureCallback);
                    }
                }
        );

    }

    public FirstLevelCache getCache() {
        return cache;
    }

    public boolean isCopyBack() {
        return copyBack;
    }
}
