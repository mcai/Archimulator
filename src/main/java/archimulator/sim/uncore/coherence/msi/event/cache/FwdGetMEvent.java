package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class FwdGetMEvent extends CacheControllerEvent {
    private final CacheController req;
    private final int tag;

    public FwdGetMEvent(CacheController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        super(generator, producerFlow, CacheControllerEventType.FWD_GETM);
        this.req = req;
        this.tag = tag;
    }

    public CacheController getReq() {
        return req;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: FwdGetMEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
