/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the PickaPack library.
 * <p>
 * PickaPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * PickaPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with PickaPack. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.util.cmd;

import archimulator.common.*;
import archimulator.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.uncore.dram.MemoryControllerType;
import archimulator.util.serialization.JsonSerializationHelper;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static archimulator.util.StorageUnitHelper.displaySizeToByteCount;

/**
 * Simulate command.
 *
 * @author Min Cai
 */
@Parameters(commandNames = "simulate", separators = "=")
public class SimulateCommand {
    @Parameter(names = "-t", description = "Experiment type")
    private ExperimentType experimentType = ExperimentType.TWO_PHASE;

    @Parameter(required = true, names = "-d", description = "Output directory for all generated files")
    private String outputDirectory = null;

    @Parameter(required = true, names = "-e", description = "Executable to be simulated")
    private String executable = null;

    @Parameter(names = "-a", description = "Arguments passed to the executable")
    private String arguments = "";

    @Parameter(names = "-numMaxInsts", description = "Number of maximum instructions executed")
    private long numMaxInstructions = -1;

    @Parameter(names = {"-n", "-numCores"}, description = "Number of cores")
    private int numCores = 2;

    @Parameter(names = "-numThreadsPerCore", description = "Number of threads per core")
    private int numThreadsPerCore = 2;

    @Parameter(names = "-l1ISize", description = "L1 instruction cache size(B,KB,MB,GB)")
    private String l1ISize = "64 KB";

    @Parameter(names = "-l1IAssoc", description = "L1 instruction cache associativity")
    private int l1IAssociativity = 4;

    @Parameter(names = "-l1DSize", description = "L1 data cache size(B,KB,MB,GB)")
    private String l1DSize = "64 KB";

    @Parameter(names = "-l1DAssoc", description = "L1 data cache associativity")
    private int l1DAssociativity = 4;

    @Parameter(names = "-l2Size", description = "L2 cache size(B,KB,MB,GB)")
    private String l2Size = "512 KB";

    @Parameter(names = "-l2Assoc", description = "L2 cache associativity")
    private int l2Associativity = 16;

    @Parameter(names = "-l2Repl", description = "L2 cache replacement policy type")
    private CacheReplacementPolicyType l2ReplacementPolicyType = CacheReplacementPolicyType.LRU;

    @Parameter(names = "-mcType", description = "Memory controller type")
    private MemoryControllerType memoryControllerType = MemoryControllerType.FIXED_LATENCY;

    /**
     * Run the simulate command.
     */
    public void run() {
        ExperimentConfig config = new ExperimentConfig();

        config.setType(experimentType);
        config.setOutputDirectory(outputDirectory);

        config.setNumCores(numCores);
        config.setNumThreadsPerCore(numThreadsPerCore);

        config.setL1ISize((int) displaySizeToByteCount(l1ISize));
        config.setL1IAssociativity(l1IAssociativity);

        config.setL1DSize((int) displaySizeToByteCount(l1DSize));
        config.setL1DAssociativity(l1DAssociativity);

        config.setL2Size((int) displaySizeToByteCount(l2Size));
        config.setL2Associativity(l2Associativity);
        config.setL2ReplacementPolicyType(l2ReplacementPolicyType);

        config.setMemoryControllerType(memoryControllerType);

        config.setNumMaxInstructions(numMaxInstructions);

        List<ContextMapping> contextMappings = new ArrayList<>();
        contextMappings.add(new ContextMapping(0, executable, arguments));

        Experiment experiment = new Experiment(
                config, contextMappings
        );

        experiment.run();

        if (experiment.getState() == ExperimentState.COMPLETED) {
            File resultDirFile = new File(config.getOutputDirectory());

            if (!resultDirFile.exists()) {
                if (!resultDirFile.mkdirs()) {
                    throw new RuntimeException();
                }
            }

            List<ExperimentStat> stats = experiment.getStats();

            Map<String, Object> stats1 = new LinkedHashMap<>();
            for(ExperimentStat stat : stats) {
                stats1.put(stat.getPrefix() + "/" + stat.getKey(), stat.getValue());
            }

            JsonSerializationHelper.writeJsonFile(config, config.getOutputDirectory(), "config.json");
            JsonSerializationHelper.writeJsonFile(stats1, config.getOutputDirectory(), "stats.json");
        }
    }
}
