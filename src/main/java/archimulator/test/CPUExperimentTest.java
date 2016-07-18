/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.test;

import archimulator.common.CPUExperiment;
import archimulator.common.ContextMapping;
import archimulator.common.ExperimentType;
import archimulator.util.StorageUnitHelper;
import org.junit.Test;

/**
 * CPU experiment test.
 *
 * @author Min Cai
 */
public class CPUExperimentTest {
    @Test
    public void test_mst_ht_100_xy_detailed_l2_128KB() {
        CPUExperiment experiment = test(
                ExperimentType.DETAILED,
                -1,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "100",
                "test_results/mst_ht_100_xy_detailed_l2_128KB",
                "xy",
                "random"
        );

        experiment.getConfig().setL2Size((int) StorageUnitHelper.displaySizeToByteCount("128 KB"));

        experiment.run();
    }

    @Test
    public void test_mst_ht_100_buffer_level_detailed_l2_128KB() {
        CPUExperiment experiment = test(
                ExperimentType.DETAILED,
                -1,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "100",
                "test_results/mst_ht_100_buffer_level_detailed_l2_128KB",
                "oddEven",
                "bufferLevel"
        );

        experiment.getConfig().setL2Size((int) StorageUnitHelper.displaySizeToByteCount("128 KB"));

        experiment.run();
    }

    @Test
    public void test_mst_ht_100_aco_detailed_l2_128KB() {
        CPUExperiment experiment = test(
                ExperimentType.DETAILED,
                -1,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "100",
                "test_results/mst_ht_100_aco_detailed_l2_128KB",
                "oddEven",
                "aco"
        );

        experiment.getConfig().setL2Size((int) StorageUnitHelper.displaySizeToByteCount("128 KB"));

        experiment.run();
    }

    @Test
    public void test_mst_ht_1000_aco_two_phase() {
        CPUExperiment experiment = test(
                ExperimentType.TWO_PHASE,
                1000000,
                "benchmarks/Olden_Custom1/mst/ht/mst.mips",
                "1000",
                "test_results/mst_ht_1000_aco_two_phase",
                "oddEven",
                "aco"
        );

        experiment.run();
    }

    private CPUExperiment test(
            ExperimentType experimentType,
            long numMaxInstructions,
            String executable,
            String arguments,
            String outputDirectory,
            String routing,
            String selection
    ) {
        CPUExperiment experiment = new CPUExperiment();

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

        return experiment;
    }
}
