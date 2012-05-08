package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flow.AbstractEvictFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemWriteMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class MemWriteFlow implements AbstractEvictFlow {
    private LastLevelCache cache;
    private MemoryHierarchyAccess access;
    private CacheAccess<MESIState, LockableCacheLine> cacheAccess;

    public MemWriteFlow(LastLevelCache cache, MemoryHierarchyAccess access, CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        this.cache = cache;
        this.access = access;
        this.cacheAccess = cacheAccess;
    }

    @Override
    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        if (cacheAccess.getLine().getState() == MESIState.MODIFIED) {
            getCache().sendRequest(getCache().getNext(), new MemWriteMessage(access, cacheAccess.getLine().getTag(), new Action1<MemWriteMessage>() {
                public void apply(MemWriteMessage memWriteMessage) {
                    onSuccessCallback.apply();
                }
            }), getCache().getCache().getLineSize() + 8);
        } else {
            onSuccessCallback.apply();
        }
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
