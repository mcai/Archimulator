package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;

/**
 * Run.
 *
 * @author Min Cai
 */
public class Run {
    public static void main(String[] args) {
        for(String traffic : Common.trafficsAndDataPacketInjectionRates.keySet()) {
            Experiment.runExperiments(Common.trafficsAndDataPacketInjectionRates.get(traffic), true);
        }

        Experiment.runExperiments(Common.antPacketInjectionRates, true);

        Experiment.runExperiments(Common.acoSelectionAlphasAndReinforcementFactors, true);
    }
}
