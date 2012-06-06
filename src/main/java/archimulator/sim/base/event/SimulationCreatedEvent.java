package archimulator.sim.base.event;

import archimulator.sim.base.experiment.Experiment;

public class SimulationCreatedEvent extends ExperimentEvent {
    public SimulationCreatedEvent(Experiment experiment) {
        super(experiment);
    }
}
