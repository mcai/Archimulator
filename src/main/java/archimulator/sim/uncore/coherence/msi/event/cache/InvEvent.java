package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class InvEvent extends CacheControllerEvent {
    private final CacheController req;
    private final int tag;

    public InvEvent(CacheController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.INV, access);
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
        return String.format("[%d] %s: InvEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
