package archimulator.incubator.noc.startup;

import archimulator.incubator.noc.Experiment;
import org.junit.Test;

/**
 * Experiment test.
 *
 * @author Min Cai
 */
public class ExperimentTest {
    @Test
    public void test() {
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
