package archimulator.incubator.noc.tests;

import archimulator.incubator.noc.Experiment;
import org.junit.Test;

/**
 * Experiment test.
 *
 * @author Min Cai
 */
public class ExperimentTest {
    private double dataPacketInjectionRate = 0.060;

    private double antPacketInjectionRate = 0.001;

    private String traffic = "transpose";

    @Test
    public void testBufferLevel() {
        new Experiment() {{
            getConfig().setResultDir("test_results/buffer_level/");
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("oddEven");
            getConfig().setSelection("bufferLevel");
        }}.run();
    }

    @Test
    public void testAco() {
        new Experiment() {{
            getConfig().setResultDir("test_results/aco/");
            getConfig().setDataPacketInjectionRate(dataPacketInjectionRate);
            getConfig().setAntPacketInjectionRate(antPacketInjectionRate);
            getConfig().setTraffic(traffic);
            getConfig().setRouting("oddEven");
            getConfig().setSelection("aco");
            getConfig().setAcoSelectionAlpha(0.4);
            getConfig().setReinforcementFactor(0.016);
        }}.run();
    }
}
