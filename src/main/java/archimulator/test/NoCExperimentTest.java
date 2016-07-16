package archimulator.test;

import archimulator.uncore.noc.NoCExperiment;
import org.junit.Test;

/**
 * NoC experiment test.
 *
 * @author Min Cai
 */
public class NoCExperimentTest {
    @Test
    public void test() {
        NoCExperiment experiment = new NoCExperiment(
                "test_results/synthetic/test",
                64,
                -1,
                10000,
                false
        );

        experiment.getConfig().setRouting("xy");
        experiment.getConfig().setSelection("random");
        experiment.getConfig().setAntPacketInjectionRate(0.001);
        experiment.getConfig().setAcoSelectionAlpha(0.45);
        experiment.getConfig().setReinforcementFactor(0.001);

        experiment.run();
    }
}
