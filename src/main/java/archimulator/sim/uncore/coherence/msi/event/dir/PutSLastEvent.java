package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutSLastEvent extends DirectoryControllerEvent {
    private final CacheController req;
    private final int tag;

    public PutSLastEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.PUTS_LAST, access);
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
        return String.format("[%d] %s: PutSLastEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
