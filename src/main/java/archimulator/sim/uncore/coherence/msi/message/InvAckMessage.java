package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class InvAckMessage extends CoherenceMessage {
    private CacheController sender;
    private int tag;

    public InvAckMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        super(generator, producerFlow, CoherenceMessageType.INV_ACK);
        this.sender = sender;
        this.tag = tag;
    }

    public CacheController getSender() {
        return sender;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: InvAckMessage{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
