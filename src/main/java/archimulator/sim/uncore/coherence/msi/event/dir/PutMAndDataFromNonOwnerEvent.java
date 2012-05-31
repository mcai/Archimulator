package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutMAndDataFromNonOwnerEvent extends DirectoryControllerEvent {
    private final CacheController req;
    private final int tag;

    public PutMAndDataFromNonOwnerEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        super(generator, producerFlow, DirectoryControllerEventType.PUTM_AND_DATA_FROM_NONOWNER);
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
        return String.format("[%d] %s: PutMAndDataFromNonOwnerEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
