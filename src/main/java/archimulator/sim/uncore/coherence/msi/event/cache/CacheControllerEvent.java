package archimulator.sim.uncore.coherence.msi.event.cache;

import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.event.ControllerEvent;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public abstract class CacheControllerEvent extends ControllerEvent {
    private CacheControllerEventType type;

    public CacheControllerEvent(CacheController generator, CacheCoherenceFlow producerFlow, CacheControllerEventType type) {
        super(generator, producerFlow);
        this.type = type;
    }

    public CacheControllerEventType getType() {
        return type;
    }
}
