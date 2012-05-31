package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class RecallEvent extends CacheControllerEvent {
    private final int tag;

    public RecallEvent(CacheController generator, CacheCoherenceFlow producerFlow, int tag) {
        super(generator, producerFlow, CacheControllerEventType.RECALL);
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s: RecallEvent{id=%d, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), tag);
    }
}
