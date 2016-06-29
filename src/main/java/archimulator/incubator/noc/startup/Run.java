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
            Experiment.runExperiments(Common.trafficsAndDataPacketInjectionRates.get(traffic));
        }

        Experiment.runExperiments(Common.antPacketInjectionRates);

        Experiment.runExperiments(Common.acoSelectionAlphasAndReinforcementFactors);
    }
}
