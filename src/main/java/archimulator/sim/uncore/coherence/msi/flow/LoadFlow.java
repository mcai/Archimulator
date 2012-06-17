package archimulator.sim.uncore.coherence.msi.flow;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;

public class LoadFlow extends CacheCoherenceFlow {
    private Action onCompletedCallback;
    private Action onCompletedCallback2;

    public LoadFlow(final CacheController generator, int tag, final Action onCompletedCallback, MemoryHierarchyAccess access) {
        super(generator, null, access, tag);
        this.onCompletedCallback = onCompletedCallback;

        this.onCompletedCallback2 = new Action() {
            @Override
            public void apply() {
                onCompletedCallback.apply();
                onCompleted();
            }
        };
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public Action getOnCompletedCallback2() {
        return onCompletedCallback2;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: LoadFlow{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), getTag());
    }
}
