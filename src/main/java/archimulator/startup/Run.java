package archimulator.startup;

import archimulator.common.Experiment;

/**
 * Run.
 *
 * @author Min Cai
 */
public class Run {
    public static void main(String[] args) {
        Experiment.runExperiments(Experiments.antPacketInjectionRates, true);

        Experiment.runExperiments(Experiments.acoSelectionAlphasAndReinforcementFactors, true);
    }
}
