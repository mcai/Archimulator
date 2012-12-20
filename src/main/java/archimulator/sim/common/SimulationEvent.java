package archimulator.sim.common;

import net.pickapack.event.BlockingEvent;

/**
 * Simulation event.
 *
 * @author Min Cai
 */
public abstract class SimulationEvent implements BlockingEvent {
    private SimulationObject sender;

    /**
     * Create a simulation event.
     *
     * @param sender the sender simulation object
     */
    public SimulationEvent(SimulationObject sender) {
        this.sender = sender;
    }

    /**
     * Get the sender simulation object.
     *
     * @return the sender simulation object
     */
    public SimulationObject getSender() {
        return sender;
    }
}
