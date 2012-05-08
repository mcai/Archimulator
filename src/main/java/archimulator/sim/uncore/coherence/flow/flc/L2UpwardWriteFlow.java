package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardWriteMessage;
import archimulator.util.action.Action;

public class L2UpwardWriteFlow extends LockingFlow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private UpwardWriteMessage message;

    public L2UpwardWriteFlow(FirstLevelCache cache, final LastLevelCache source, final UpwardWriteMessage message) {
        this.cache = cache;
        this.source = source;
        this.message = message;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        final FindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this.cache, this.message.getAccess(), this.message.getTag(), CacheAccessType.UPWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().getLine().invalidate();

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        getCache().getBlockingEventDispatcher().dispatch(new FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent(getCache(), findAndLockFlow.getCacheAccess().getLine()));

                        int size = findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? getCache().getCache().getLineSize() + 8 : 8;
                        getCache().sendReply(source, size, message);

                        endFillOrEvict(findAndLockFlow);

                        afterFlowEnd(findAndLockFlow);

                        onSuccessCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
//                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        afterFlowEnd(findAndLockFlow);

                        onFailureCallback.apply();
                    }
                }
        );

    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
