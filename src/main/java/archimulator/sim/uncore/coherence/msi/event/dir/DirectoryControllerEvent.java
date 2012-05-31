package archimulator.sim.uncore.coherence.msi.event.dir;

import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.event.ControllerEvent;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;

public abstract class DirectoryControllerEvent extends ControllerEvent {
    private DirectoryControllerEventType type;

    public DirectoryControllerEvent(DirectoryController generator, CacheCoherenceFlow producerFlow, DirectoryControllerEventType type) {
        super(generator, producerFlow);
        this.type = type;
    }

    public DirectoryControllerEventType getType() {
        return type;
    }
}
