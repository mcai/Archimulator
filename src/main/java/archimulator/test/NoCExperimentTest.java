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
    public void testXy() {
        NoCExperiment experimentXy = new NoCExperiment(
                "test_results/synthetic/xy",
                64,
                20000,
                -1,
                false
        );

        experimentXy.getConfig().setRouting("xy");
        experimentXy.getConfig().setSelection("random");

        experimentXy.getConfig().setDataPacketTraffic("transpose");
        experimentXy.getConfig().setDataPacketInjectionRate(0.06);

        experimentXy.run();
    }

    @Test
    public void testBufferLevel() {
        NoCExperiment experimentBufferLevel = new NoCExperiment(
                "test_results/synthetic/bufferLevel",
                64,
                20000,
                -1,
                false
        );

        experimentBufferLevel.getConfig().setRouting("oddEven");
        experimentBufferLevel.getConfig().setSelection("bufferLevel");

        experimentBufferLevel.getConfig().setDataPacketTraffic("transpose");
        experimentBufferLevel.getConfig().setDataPacketInjectionRate(0.06);

        experimentBufferLevel.run();
    }

    @Test
    public void testAco() {
        NoCExperiment experimentAco = new NoCExperiment(
                "test_results/synthetic/aco",
                64,
                20000,
                -1,
                false
        );

        experimentAco.getConfig().setRouting("oddEven");
        experimentAco.getConfig().setSelection("aco");

        experimentAco.getConfig().setDataPacketTraffic("transpose");
        experimentAco.getConfig().setDataPacketInjectionRate(0.06);

        experimentAco.getConfig().setAntPacketTraffic("uniform");
        experimentAco.getConfig().setAntPacketInjectionRate(0.0002);
        experimentAco.getConfig().setAcoSelectionAlpha(0.45);
        experimentAco.getConfig().setReinforcementFactor(0.001);

        experimentAco.run();
    }
}
