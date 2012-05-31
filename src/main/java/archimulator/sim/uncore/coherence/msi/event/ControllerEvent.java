package archimulator.sim.uncore.coherence.msi.event;

import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public abstract class ControllerEvent extends CacheCoherenceFlow {
    public ControllerEvent(Controller generator, CacheCoherenceFlow producerFlow) {
        super(generator, producerFlow);
    }
}
