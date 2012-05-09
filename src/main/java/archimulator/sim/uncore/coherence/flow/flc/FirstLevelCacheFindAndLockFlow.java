package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.message.EvictMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class FirstLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public FirstLevelCacheFindAndLockFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(MemoryHierarchyAccess access, final Action onSuccessCallback, final Action onFailureCallback) {
        final boolean hasData = getCacheAccess().getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;

        getCache().sendRequest(getCache().getNext(), size, new EvictMessage(access, getCacheAccess().getLine().getTag(), hasData, new Action1<EvictMessage>() {
            public void apply(EvictMessage evictMessage) {
                if (!evictMessage.isError()) {
                    getCacheAccess().getLine().endEvict();
                    onSuccessCallback.apply();
                } else {
                    getCacheAccess().getLine().endEvict();
                    onFailureCallback.apply();
                }
            }
        }));
    }
}
