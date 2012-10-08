package archimulator.sim.common;

import net.pickapack.event.BlockingEvent;

/**
 *
 * @author Min Cai
 */
public abstract class SimulationEvent implements BlockingEvent {
    private SimulationObject sender;

    /**
     *
     * @param sender
     */
    public SimulationEvent(SimulationObject sender) {
        this.sender = sender;
    }

    /**
     *
     * @return
     */
    public SimulationObject getSender() {
        return sender;
    }
}
