package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutSMessage extends CoherenceMessage {
    private CacheController req;
    private int tag;

    public PutSMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.PUTS, access);
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
        return String.format("[%d] %s: PutSMessage{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
