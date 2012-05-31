package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import net.pickapack.action.Action;

public class GetSEvent extends DirectoryControllerEvent {
    private final CacheController req;
    private int tag;
    private int set;
    private int way;
    private Action onStalledCallback;

    public GetSEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, int set, int way, Action onStalledCallback) {
        super(generator, producerFlow, DirectoryControllerEventType.GETS);
        this.req = req;
        this.tag = tag;
        this.set = set;
        this.way = way;
        this.onStalledCallback = onStalledCallback;
    }

    public CacheController getReq() {
        return req;
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

    public Action getOnStalledCallback() {
        return onStalledCallback;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: GetSEvent{id=%d, req=%s, tag=0x%08x, set=%d, way=%d}", getBeginCycle(), getGenerator(), getId(), req, tag, set, way);
    }
}
