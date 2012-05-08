package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.message.DownwardWriteMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class DownwardWriteFlow{
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public DownwardWriteFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(getCache().getNext(), 8, new DownwardWriteMessage(access, tag, new Action1<DownwardWriteMessage>() {
            public void apply(DownwardWriteMessage writeMessage) {
                if (!writeMessage.isError()) {
                    onSuccessCallback.apply();
                } else {
                    onFailureCallback.apply();
                }
            }
        }));
    }

    public FirstLevelCache getCache() {
        return cache;
    }
}
