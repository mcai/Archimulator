package archimulator.sim.base.event;

import archimulator.sim.base.experiment.Experiment;

public class StopExperimentEvent extends ExperimentEvent {
    public StopExperimentEvent(Experiment experiment) {
        super(experiment);
    }
}
