package archimulator.util.cmd;

import archimulator.model.*;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MemoryControllerType;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import net.pickapack.io.serialization.JsonSerializationHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simulate command.
 *
 * @author Min Cai
 */
@Parameters(commandNames = "simulate", separators = "=")
public class SimulateCommand {
    @Parameter(required = true, names = "-p", description = "Output directory for all generated files")
    private String outputDirectory = null;

    @Parameter(required = true, names = "-b", description = "Benchmark title")
    private String benchmarkTitle = null;

    @Parameter(names = "-numMaxInsts", description = "Number of maximum instructions executed")
    private long numMaxInstructions = -1;

    @Parameter(names = "-numCores", description = "Number of cores")
    private int numCores = 2;

    @Parameter(names = "-numThreadsPerCore", description = "Number of threads per core")
    private int numThreadsPerCore = 2;

    @Parameter(names = "-l1ISize", description = "L1 instruction cache size(B,KB,MB,GB)")
    private String l1ISize = "64KB";

    @Parameter(names = "-l1IAssoc", description = "L1 instruction cache associativity")
    private int l1IAssociativity = 4;

    @Parameter(names = "-l1DSize", description = "L1 data cache size(B,KB,MB,GB)")
    private String l1DSize = "64KB";

    @Parameter(names = "-l1DAssoc", description = "L1 data cache associativity")
    private int l1DAssociativity = 4;

    @Parameter(names = "-l2Size", description = "L2 cache size(B,KB,MB,GB)")
    private String l2Size = "512KB";

    @Parameter(names = "-l2Assoc", description = "L2 cache associativity")
    private int l2Associativity = 16;

    @Parameter(names = "-l2Repl", description = "L2 cache replacement policy type")
    private CacheReplacementPolicyType l2ReplacementPolicyType = CacheReplacementPolicyType.LRU;

    @Parameter(names = "-mcType", description = "Memory controller type")
    private MemoryControllerType memoryControllerType = MemoryControllerType.FIXED_LATENCY;

    public String getBenchmarkTitle() {
        return benchmarkTitle;
    }

    public long getNumMaxInstructions() {
        return numMaxInstructions;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public String getL1ISize() {
        return l1ISize;
    }

    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    public String getL1DSize() {
        return l1DSize;
    }

    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    public String getL2Size() {
        return l2Size;
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public MemoryControllerType getMemoryControllerType() {
        return memoryControllerType;
    }

    public void run() {
        Benchmark benchmark = ServiceManager.getBenchmarkService().getBenchmarkByTitle(benchmarkTitle);

        if (benchmark == null) {
            throw new IllegalArgumentException(benchmarkTitle);
        }

        List<ContextMapping> contextMappings = new ArrayList<>();
        contextMappings.add(new ContextMapping(0, benchmark, benchmark.getDefaultArguments()));

        Experiment experiment = new Experiment(ExperimentType.TWO_PHASE, outputDirectory, false, -1, numCores, numThreadsPerCore, (int) displaySizeToByteCount(l1ISize), l1IAssociativity, (int) displaySizeToByteCount(l1DSize), l1DAssociativity, (int) displaySizeToByteCount(l2Size), l2Associativity, l2ReplacementPolicyType, memoryControllerType, numMaxInstructions, contextMappings);
        experiment.run();

        if (experiment.getState() == ExperimentState.COMPLETED) {
            File file = new File(outputDirectory, "result.json");

            if (!file.exists()) {
                List<ExperimentStat> stats = experiment.getStats();

                String json = JsonSerializationHelper.toJson(stats, true);

                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new RuntimeException();
                    }
                }

                try {
                    FileUtils.writeStringToFile(file, json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("Experiment statistics has been written to " + file.getPath());
            }
        }
    }

    private long displaySizeToByteCount(String displaySize) {
        displaySize = displaySize.trim();
        displaySize = displaySize.replaceAll(",", ".");
        try {
            return Long.parseLong(displaySize);
        } catch (NumberFormatException ignored) {
        }
        final Matcher m = Pattern.compile("([\\d.,]+)\\s*(\\w)").matcher(displaySize);
        //noinspection ResultOfMethodCallIgnored
        m.find();
        int scale = 1;
        switch (m.group(2).charAt(0)) {
            case 'G':
                scale *= 1024;
            case 'M':
                scale *= 1024;
            case 'K':
                scale *= 1024;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return Math.round(Double.parseDouble(m.group(1)) * scale);
    }
}
