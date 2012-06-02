package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import net.pickapack.action.Action;

public class ReplacementEvent extends DirectoryControllerEvent {
    private int tag;
    private int set;
    private int way;
    private Action onCompletedCallback;
    private Action onStalledCallback;

    public ReplacementEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, int tag, int set, int way, Action onCompletedCallback, Action onStalledCallback) {
        super(generator, producerFlow, DirectoryControllerEventType.REPLACEMENT);
        this.tag = tag;
        this.set = set;
        this.way = way;
        this.onCompletedCallback = onCompletedCallback;
        this.onStalledCallback = onStalledCallback;
    }

    public int getTag() {
        return tag;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }

    public Action getOnStalledCallback() {
        return onStalledCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: ReplacementEvent{id=%d, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), tag, set, way);
    }
}