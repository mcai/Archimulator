package archimulator.sim.uncore.coherence.flow;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.message.DownwardWriteMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class DownwardWriteFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public DownwardWriteFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(getCache().getNext(), new DownwardWriteMessage(access, tag, new Action1<DownwardWriteMessage>() {
            public void apply(DownwardWriteMessage writeMessage) {
                if (!writeMessage.isError()) {
                    onSuccessCallback.apply();
                } else {
                    onFailureCallback.apply();
                }
            }
        }), 8);
    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
