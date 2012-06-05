package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class PutAckMessage extends CoherenceMessage {
    private int tag;

    public PutAckMessage(Controller generator, CacheCoherenceFlow producerFlow, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CoherenceMessageType.PUT_ACK, access);
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: PutAckMessage{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), tag);
    }
}
