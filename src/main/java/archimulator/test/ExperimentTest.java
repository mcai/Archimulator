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
    public void test_mst_ht_100_aco_detailed() {
        test(
                ExperimentType.DETAILED,
                -1,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "100",
                "test_result/mst_ht_100_aco_detailed",
                "oddEven",
                "aco"
        );
    }

    @Test
    public void test_mst_ht_100_aco_two_phase() {
        test(
                ExperimentType.TWO_PHASE,
                -1,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "100",
                "test_result/test_mst_ht_100_aco_two_phase",
                "oddEven",
                "aco"
        );
    }

    @Test
    public void test_mst_ht_1000_aco_two_phase() {
        test(
                ExperimentType.TWO_PHASE,
                1000000000,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "1000",
                "test_result/test_mst_ht_1000_aco_two_phase",
                "oddEven",
                "aco"
        );
    }

    public void test(
            ExperimentType experimentType,
            long numMaxInstructions, String executable,
            String arguments,
            String outputDirectory,
            String routing,
            String selection
    ) {
        Experiment experiment = new Experiment();

        experiment.getConfig().setType(experimentType);

        experiment.getConfig().setNumMaxInstructions(numMaxInstructions);

        experiment.getConfig().setNumCores(16);
        experiment.getConfig().setNumThreadsPerCore(1);

        experiment.getConfig().getContextMappings().add(new ContextMapping(0, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(4, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(8, executable, arguments));
        experiment.getConfig().getContextMappings().add(new ContextMapping(12, executable, arguments));

        experiment.getConfig().setOutputDirectory(outputDirectory);

        experiment.getConfig().setRouting(routing);
        experiment.getConfig().setSelection(selection);

        experiment.getConfig().setAntPacketInjectionRate(0.001);
        experiment.getConfig().setAcoSelectionAlpha(0.45);
        experiment.getConfig().setReinforcementFactor(0.001);

        experiment.run();
    }
}
