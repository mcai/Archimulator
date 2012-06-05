package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class DataFromOwnerEvent extends CacheControllerEvent {
    private final Controller sender;
    private final int tag;

    public DataFromOwnerEvent(CacheController generator, CacheCoherenceFlow producerFlow, Controller sender, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, CacheControllerEventType.DATA_FROM_OWNER, access);
        this.sender = sender;
        this.tag = tag;
    }

    public Controller getSender() {
        return sender;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: DataFromOwnerEvent{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
