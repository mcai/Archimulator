package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.AbstractEvictFlow;
import archimulator.sim.uncore.coherence.message.EvictMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class EvictFlow implements AbstractEvictFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private CacheAccess<MESIState, LockableCacheLine> cacheAccess;

    public EvictFlow(FirstLevelCache cache, MemoryHierarchyAccess access, CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        this.cache = cache;
        this.access = access;
        this.cacheAccess = cacheAccess;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final boolean hasData = cacheAccess.getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;

        getCache().sendRequest(getCache().getNext(), new EvictMessage(access, cacheAccess.getLine().getTag(), hasData, new Action1<EvictMessage>() {
            public void apply(EvictMessage evictMessage) {
                if (!evictMessage.isError()) {
                    cacheAccess.getLine().endEvict();
                    onSuccessCallback.apply();
                } else {
                    cacheAccess.getLine().endEvict();
                    onFailureCallback.apply();
                }
            }
        }), size);
    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
