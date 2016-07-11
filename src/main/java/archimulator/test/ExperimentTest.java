package archimulator.test;

import archimulator.common.ContextMapping;
import archimulator.common.Experiment;
import archimulator.common.ExperimentType;
import org.junit.Test;

/**
 * Experiment test.
 *
 * @author Min Cai
 */
public class ExperimentTest {
    @Test
    public void test() {
        Experiment experiment = new Experiment();

        experiment.getConfig().setType(ExperimentType.DETAILED);

        experiment.getConfig().setNumCores(16);
        experiment.getConfig().setNumThreadsPerCore(1);

        String executable = "benchmarks/Olden_Custom1/mst/ht/mst.mips";
        String arguments = "100";

        experiment.getConfig().getContextMappings().add(new ContextMapping(0, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(4, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(8, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(12, executable, arguments));

        experiment.getConfig().setOutputDirectory("test_result/mst_ht/aco");
        experiment.getConfig().setRouting("oddEven");
        experiment.getConfig().setSelection("aco");
        experiment.getConfig().setAntPacketInjectionRate(0.001);
        experiment.getConfig().setAcoSelectionAlpha(0.45);
        experiment.getConfig().setReinforcementFactor(0.001);

        experiment.run();
    }
}
