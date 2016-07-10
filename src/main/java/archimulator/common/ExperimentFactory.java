package archimulator.common;

import archimulator.util.serialization.JsonSerializationHelper;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Experiment factory.
 *
 * @author Min Cai
 */
public class ExperimentFactory {
    /**
     * Run an experiment using the specified experiment config.
     *
     * @param config the experiment config
     */
    public static void run(ExperimentConfig config) {
        Experiment experiment = new Experiment(config);

        experiment.run();

        if (experiment.getState() == ExperimentState.COMPLETED) {
            File resultDirFile = new File(config.getOutputDirectory());

            if (!resultDirFile.exists()) {
                if (!resultDirFile.mkdirs()) {
                    throw new RuntimeException();
                }
            }

            List<ExperimentStat> stats = experiment.getStats();

            Map<String, Object> statsMap = new LinkedHashMap<>();
            for(ExperimentStat stat : stats) {
                statsMap.put(stat.getPrefix() + "/" + stat.getKey(), stat.getValue());
            }

            JsonSerializationHelper.writeJsonFile(config, config.getOutputDirectory(), "config.json");
            JsonSerializationHelper.writeJsonFile(statsMap, config.getOutputDirectory(), "stats.json");
        }
    }
}
