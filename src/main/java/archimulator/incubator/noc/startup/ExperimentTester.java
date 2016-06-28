package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;

/**
 * Experiment tester.
 *
 * @author Min Cai
 */
public class ExperimentTester {
    public static void main(String[] args) {
        double dataPacketInjectionRate = 0.060;
        double antPacketInjectionRate = 0.001;
        String traffic = "transpose";

        Experiment experiment = new Experiment();
        experiment.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experiment.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
        experiment.getConfig().setTraffic(traffic);
        experiment.getConfig().setRouting("oddEven");
        experiment.getConfig().setSelection("aco");
        experiment.getConfig().setAcoSelectionAlpha(0.45);
        experiment.getConfig().setReinforcementFactor(0.001);
        experiment.run();
    }
}
