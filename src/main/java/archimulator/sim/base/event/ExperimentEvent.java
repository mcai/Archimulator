package archimulator.sim.base.event;

import archimulator.sim.base.experiment.Experiment;
import net.pickapack.event.BlockingEvent;

public abstract class ExperimentEvent implements BlockingEvent {
    private Experiment sender;

    public ExperimentEvent(Experiment sender) {
        this.sender = sender;
    }

    public Experiment getSender() {
        return sender;
    }
}
