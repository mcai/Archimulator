package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardReadMessage;
import archimulator.util.action.Action;

public class L2UpwardReadFlow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private UpwardReadMessage message;

    public L2UpwardReadFlow(FirstLevelCache cache, final LastLevelCache source, final UpwardReadMessage message) {
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
                        message.setHasCopyback(findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED);
                        getCache().sendReply(source, message, source.getCache().getLineSize() + 8);

                        findAndLockFlow.getCacheAccess().getLine().setNonInitialState(MESIState.SHARED);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        onSuccessCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        onFailureCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        onFailureCallback.apply();
                    }
                }
        );

    }

    public FirstLevelCache getCache() {
        return cache;
    }

}
