package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class FwdGetSMessage extends CoherenceMessage {
    private CacheController req;
    private int tag;

    public FwdGetSMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.FWD_GETS, access);
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
        return String.format("[%d] %s: FwdGetSMessage{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
