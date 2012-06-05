package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class RecallAckMessage extends CoherenceMessage {
    private CacheController sender;
    private int tag;

    public RecallAckMessage(Controller generator, CacheCoherenceFlow producerFlow, CacheController sender, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.RECALL_ACK, access);
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
        return String.format("[%d] %s: RecallAckMessage{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
