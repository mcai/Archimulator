package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardWriteMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class UpwardWriteFlow {
    private LastLevelCache cache;
    private FirstLevelCache except;
    private MemoryHierarchyAccess access;
    private int tag;
    private int pending;

    public UpwardWriteFlow(LastLevelCache cache, final FirstLevelCache except, final MemoryHierarchyAccess access, final int tag) {
        this.cache = cache;
        this.except = except;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        for (final FirstLevelCache sharer : getCache().getSharers(tag)) {
            if (sharer != except) {
                getCache().sendRequest(sharer, 8, new UpwardWriteMessage(access, tag, new Action1<UpwardWriteMessage>() {
                    public void apply(UpwardWriteMessage upwardWriteMessage) {
                        if (!upwardWriteMessage.isError()) {
                            pending--;

                            if (pending == 0) {
                                onSuccessCallback.apply();
                            }
                        } else {
                            onFailureCallback.apply();
                        }
                    }
                }));
                pending++;
            }
        }
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
