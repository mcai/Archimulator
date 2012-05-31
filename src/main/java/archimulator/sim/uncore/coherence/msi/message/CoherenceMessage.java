package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public abstract class CoherenceMessage extends CacheCoherenceFlow {
    private CoherenceMessageType type;

    public CoherenceMessage(Controller generator, CacheCoherenceFlow producerFlow, CoherenceMessageType type) {
        super(generator, producerFlow);
        this.type = type;
    }

    public CoherenceMessageType getType() {
        return type;
    }
}
