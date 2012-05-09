package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemWriteMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class LastLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public LastLevelCacheFindAndLockFlow(LastLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(MemoryHierarchyAccess access, final Action onSuccessCallback, Action onFailureCallback) {
        if (getCacheAccess().getLine().getState() == MESIState.MODIFIED) {
            getCache().sendRequest(getCache().getNext(), getCache().getCache().getLineSize() + 8, new MemWriteMessage(access, getCacheAccess().getLine().getTag(), new Action1<MemWriteMessage>() {
                public void apply(MemWriteMessage memWriteMessage) {
                    getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), getCacheAccess().getLine()));
                    onSuccessCallback.apply();
                }
            }));
        } else {
            getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), getCacheAccess().getLine()));
            onSuccessCallback.apply();
        }
    }
}
