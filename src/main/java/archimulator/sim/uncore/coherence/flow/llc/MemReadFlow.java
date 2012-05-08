package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemReadMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class MemReadFlow {
    private LastLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public MemReadFlow(LastLevelCache cache, MemoryHierarchyAccess access, final int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(getCache().getNext(), new MemReadMessage(access, tag, new Action1<MemReadMessage>() {
            public void apply(MemReadMessage memReadMessage) {
                onSuccessCallback.apply();
            }
        }), 8);
    }

    public LastLevelCache getCache() {
        return cache;
    }
}
