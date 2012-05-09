package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L2UpwardWriteFlow extends LockingFlow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;

    public L2UpwardWriteFlow(FirstLevelCache cache, final LastLevelCache source, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback) {
        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this.cache, access, tag, CacheAccessType.UPWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().getLine().invalidate();

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        getCache().getBlockingEventDispatcher().dispatch(new FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent(getCache(), findAndLockFlow.getCacheAccess().getLine()));

                        int size = findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? getCache().getCache().getLineSize() + 8 : 8;

                        getCache().sendReply(source, size, onSuccessCallback);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        throw new IllegalArgumentException();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        throw new IllegalArgumentException();
                    }
                }
        );

    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
