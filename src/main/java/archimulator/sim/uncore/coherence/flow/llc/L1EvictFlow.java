package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.util.action.Action;

public class L1EvictFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean hasData;

    public L1EvictFlow(LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag, boolean hasData) {
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
        this.hasData = hasData;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this.cache, this.access, this.tag, CacheAccessType.EVICT);

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

                        endFillOrEvict(findAndLockFlow);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        afterFlowEnd(findAndLockFlow);

                        getCache().sendReply(source, 8, onSuccessCallback);
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

    public LastLevelCache getCache() {
        return cache;
    }
}
