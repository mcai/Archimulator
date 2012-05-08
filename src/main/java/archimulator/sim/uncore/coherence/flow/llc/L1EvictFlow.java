package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.EvictMessage;
import archimulator.util.action.Action;

public class L1EvictFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private EvictMessage message;

    public L1EvictFlow(LastLevelCache cache, final FirstLevelCache source, final EvictMessage message) {
        this.cache = cache;
        this.source = source;
        this.message = message;
        this.access = message.getAccess();
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this.cache, this.message.getAccess(), this.message.getTag(), CacheAccessType.EVICT);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (message.isDirty()) {
                            if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                            }
                        } else {
                            if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                                findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
                            }
                        }

                        getCache().getShadowTagDirectories().get(source).removeTag(message.getTag());
                        getCache().sendReply(source, 8, message);

                        endFillOrEvict(findAndLockFlow);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        afterFlowEnd(findAndLockFlow);

                        onSuccessCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();
                        getCache().sendReply(source, 8, message);
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();
                        getCache().sendReply(source, 8, message);
                    }
                }
        );
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
