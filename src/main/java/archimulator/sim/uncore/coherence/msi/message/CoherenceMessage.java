package archimulator.sim.uncore.coherence.msi.message;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public abstract class CoherenceMessage extends CacheCoherenceFlow {
    private CoherenceMessageType type;
    private boolean destinationArrived;

    public CoherenceMessage(Controller generator, CacheCoherenceFlow producerFlow, CoherenceMessageType type, MemoryHierarchyAccess access, int tag) {
        super(generator, producerFlow, access, tag);
        this.type = type;
    }

    public void onDestinationArrived() {
        this.destinationArrived = true;
    }

    public CoherenceMessageType getType() {
        return type;
    }

    public boolean isDestinationArrived() {
        return destinationArrived;
    }
}
