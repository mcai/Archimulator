package archimulator.uncore.net.noc.tests;

import archimulator.uncore.net.noc.NoCExperiment;
import org.junit.Test;

/**
 * Experiment test.
 *
 * @author Min Cai
 */
public class NoCExperimentTest {
    private double dataPacketInjectionRate = 0.060;

    private double antPacketInjectionRate = 0.001;

    private String traffic = "transpose";

    @Test
    public void testBufferLevel() {
        NoCExperiment experiment = new NoCExperiment();
        experiment.getConfig().setResultDir("test_results/buffer_level/");
        experiment.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experiment.getConfig().setTraffic(traffic);
        experiment.getConfig().setRouting("oddEven");
        experiment.getConfig().setSelection("bufferLevel");
        experiment.run();
    }

    @Test
    public void testAco() {
        NoCExperiment experiment = new NoCExperiment();
        experiment.getConfig().setResultDir("test_results/aco/");
        experiment.getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
        experiment.getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
        experiment.getConfig().setTraffic(traffic);
        experiment.getConfig().setRouting("oddEven");
        experiment.getConfig().setSelection("aco");
        experiment.getConfig().setAcoSelectionAlpha(0.4);
        experiment.getConfig().setReinforcementFactor(0.016);
        experiment.run();
    }
}
