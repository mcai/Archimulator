package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public class DataFromDirAckEq0Event extends CacheControllerEvent {
    private final Controller sender;
    private final int tag;

    public DataFromDirAckEq0Event(CacheController generator, CacheCoherenceFlow producerFlow, Controller sender, int tag) {
        super(generator, producerFlow, CacheControllerEventType.DATA_FROM_DIR_ACK_EQ_0);
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
        return String.format("[%d] %s: DataFromDirAckEq0Event{id=%d, sender=%s, tag=0x%08x}", getBeginCycle(), getGenerator(), getId(), sender, tag);
    }
}
