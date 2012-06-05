package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class DataEvent extends DirectoryControllerEvent {
    private final CacheController sender;
    private final int tag;

    public DataEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, CacheController sender, int tag, MemoryHierarchyAccess access) {
        super(generator, producerFlow, DirectoryControllerEventType.DATA, access);
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
        return String.format("[%d] %s: DataEvent{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
