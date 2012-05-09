package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L1EvictFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean hasData;

    public L1EvictFlow(Flow producerFlow, LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag, boolean hasData) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
        this.hasData = hasData;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.EVICT);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (hasData) {
                            if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                            }
                        } else {
                            if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
                            }
                        }

                        getCache().getShadowTagDirectories().get(source).removeTag(tag);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        getCache().sendReply(source, 8, onSuccessCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }
        );
    }

    public LastLevelCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L1EvictFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
