package archimulator.sim.uncore.coherence.msi.flow;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;

public class LoadFlow extends CacheCoherenceFlow {
    private int tag;
    private Action onCompletedCallback;
    private final Action onCompletedCallback2;

    public LoadFlow(final CacheController generator, int tag, final Action onCompletedCallback) {
        super(generator, null);
        this.tag = tag;
        this.onCompletedCallback = onCompletedCallback;

        this.onCompletedCallback2 = new Action() {
            @Override
            public void apply() {
                onCompletedCallback.apply();
                onCompleted();
            }
        };
    }

    public int getTag() {
        return tag;
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public Action getOnCompletedCallback2() {
        return onCompletedCallback2;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: LoadFlow{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), tag);
    }
}
