package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutSNotLastEvent extends DirectoryControllerEvent {
    private final CacheController req;
    private final int tag;

    public PutSNotLastEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        super(generator, producerFlow, DirectoryControllerEventType.PUTS_NOT_LAST);
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
        return String.format("[%d] %s: PutSNotLastEvent{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
