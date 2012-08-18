package archimulator.sim.common;

import net.pickapack.event.BlockingEvent;

public abstract class SimulationEvent implements BlockingEvent {
    private SimulationObject sender;

    public SimulationEvent(SimulationObject sender) {
        this.sender = sender;
    }

    public SimulationObject getSender() {
        return sender;
    }
}
