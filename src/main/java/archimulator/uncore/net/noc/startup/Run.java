package archimulator.uncore.net.noc.startup;

import archimulator.uncore.net.noc.NoCExperiment;

/**
 * Run.
 *
 * @author Min Cai
 */
public class Run {
    public static void main(String[] args) {
        for(String traffic : NoCExperiments.trafficsAndDataPacketInjectionRates.keySet()) {
            NoCExperiment.runExperiments(NoCExperiments.trafficsAndDataPacketInjectionRates.get(traffic), true);
        }

        NoCExperiment.runExperiments(NoCExperiments.antPacketInjectionRates, true);

        NoCExperiment.runExperiments(NoCExperiments.acoSelectionAlphasAndReinforcementFactors, true);
    }
}
