package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardReadMessage;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

public class UpwardReadFlow {
    private LastLevelCache cache;
    private FirstLevelCache target;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean copyBack;

    public UpwardReadFlow(LastLevelCache cache, final FirstLevelCache target, final MemoryHierarchyAccess access, final int tag) {
        this.cache = cache;
        this.target = target;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        getCache().sendRequest(target, new UpwardReadMessage(access, tag, new Action1<UpwardReadMessage>() {
            public void apply(UpwardReadMessage upwardReadMessage) {
                if (upwardReadMessage.isError()) {
                    onFailureCallback.apply();
                } else {
                    copyBack = upwardReadMessage.isHasCopyback();
                    onSuccessCallback.apply();
                }
            }
        }), 8);
    }

    public LastLevelCache getCache() {
        return cache;
    }

    public boolean isCopyBack() {
        return copyBack;
    }
}
