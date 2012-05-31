package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutMAndDataMessage extends CoherenceMessage {
    private CacheController req;
    private int tag;

    public PutMAndDataMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController req, int tag) {
        super(generator, producerFlow, CoherenceMessageType.PUTM_AND_DATA);

        if (req == null || tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

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
        return String.format("[%d] %s: PutMAndDataMessage{id=%d, req=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), req, tag);
    }
}
