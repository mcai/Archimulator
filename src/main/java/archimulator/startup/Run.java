package archimulator.startup;

import archimulator.common.CPUExperiment;

/**
 * Run.
 *
 * @author Min Cai
 */
public class Run {
    public static void main(String[] args) {
        CPUExperiment.runExperiments(Experiments.antPacketInjectionRates, true);

        CPUExperiment.runExperiments(Experiments.acoSelectionAlphasAndReinforcementFactors, true);
    }
}
