package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.message.DownwardReadMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class DownwardReadFlow extends Flow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean shared;

    public DownwardReadFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(getCache().getNext(), new DownwardReadMessage(access, tag, new Action1<DownwardReadMessage>() {
            public void apply(DownwardReadMessage downwardReadMessage) {
                if (!downwardReadMessage.isError()) {
                    shared = downwardReadMessage.isShared();
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

    public boolean isShared() {
        return shared;
    }
}
