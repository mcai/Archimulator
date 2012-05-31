package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class InvAckEvent extends CacheControllerEvent {
    private final CacheController sender;
    private final int tag;

    public InvAckEvent(CacheController generator, CacheCoherenceFlow producerFlow, CacheController sender, int tag) {
        super(generator, producerFlow, CacheControllerEventType.INV_ACK);
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
        return String.format("[%d] %s: InvAckEvent{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
