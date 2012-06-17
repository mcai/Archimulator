package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class FwdGetMEvent extends CacheControllerEvent {
    private CacheController req;

    public FwdGetMEvent(CacheController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.FWD_GETM, access, tag);
        this.req = req;
    }

    public CacheController getReq() {
        return req;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: FwdGetMEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, getTag());
    }
}
