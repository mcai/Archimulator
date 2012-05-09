package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.llc.L1EvictFlow;
import archimulator.util.action.Action;

public class FirstLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public FirstLevelCacheFindAndLockFlow(LockingFlow producerFlow, FirstLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(producerFlow, cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(final MemoryHierarchyAccess access, final Action onSuccessCallback, final Action onFailureCallback) {
        final boolean hasData = getCacheAccess().getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;

        getCache().sendRequest(getCache().getNext(), size, new Action() {
            @Override
            public void apply() {
                L1EvictFlow l1EvictFlow = new L1EvictFlow(FirstLevelCacheFindAndLockFlow.this, getCache().getNext(), getCache(), access, getCacheAccess().getLine().getTag(), hasData);
                l1EvictFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                                onSuccessCallback.apply();
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                onFailureCallback.apply();
                            }
                        }
                );
            }
        });
    }

    @Override
    public FirstLevelCache getCache() {
        return (FirstLevelCache) super.getCache();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s%s: FirstLevelCacheFindAndLockFlow#%d {state=%s} ", getBeginCycle(), getCache().getName(), getCacheAccess().getLine(), getId(), getState());
    }
}
