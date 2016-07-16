package archimulator.startup;

import archimulator.common.Experiment;

/**
 * Run.
 *
 * @author Min Cai
 */
public class Run {
    public static void main(String[] args) {
        for(String traffic : Experiments.trafficsAndDataPacketInjectionRates.keySet()) {
            Experiment.runExperiments(Experiments.trafficsAndDataPacketInjectionRates.get(traffic), true);
        }

        Experiment.runExperiments(Experiments.antPacketInjectionRates, true);

        Experiment.runExperiments(Experiments.acoSelectionAlphasAndReinforcementFactors, true);
    }
}
