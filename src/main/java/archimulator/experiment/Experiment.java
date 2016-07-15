package archimulator.experiment;

import archimulator.common.ExperimentStat;
import archimulator.common.ExperimentState;
import archimulator.util.dateTime.DateHelper;
import archimulator.util.serialization.JsonSerializationHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public abstract class Experiment<ExperimentConfigT> {
    private long createTime;

    private ExperimentConfigT config;

    private ExperimentState state;

    private String failedReason;

    private List<ExperimentStat> stats;

    private Map<String, Object> statsMap;

    /**
     * Create an experiment.
     */
    public Experiment(ExperimentConfigT config) {
        this.config = config;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";

        this.createTime = DateHelper.toTick(new Date());
        this.stats = new ArrayList<>();
    }

    /**
     * Run.
     */
    public void run() {
        try {
            simulate();

            this.setState(ExperimentState.COMPLETED);
            this.setFailedReason("");
        } catch (Exception e) {
            this.setState(ExperimentState.ABORTED);
            this.setFailedReason(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }

        if (this.getState() == ExperimentState.COMPLETED) {
            File resultDirFile = new File(this.getOutputDirectory());

            if (!resultDirFile.exists()) {
                if (!resultDirFile.mkdirs()) {
                    throw new RuntimeException();
                }
            }

            JsonSerializationHelper.writeJsonFile(this.getConfig(), this.getOutputDirectory(), "config.json");
            JsonSerializationHelper.writeJsonFile(this.getStatsMap(), this.getOutputDirectory(), "stats.json");
        }
    }

    /**
     * Simulate.
     */
    protected abstract void simulate();

    /**
     * Get the time in ticks when the experiment is created.
     *
     * @return the time in ticks when the experiment is created
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the string representation of the time when the experiment is created.
     *
     * @return the string representation of the time when the experiment is created
     */
    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    /**
     * Get the experiment config.
     *
     * @return the experiment config
     */
    public ExperimentConfigT getConfig() {
        return config;
    }

    /**
     * Get the experiment state.
     *
     * @return the experiment state
     */
    public ExperimentState getState() {
        return state;
    }

    /**
     * Set the experiment state.
     *
     * @param state the experiment state
     */
    public void setState(ExperimentState state) {
        this.state = state;
    }

    /**
     * Get the failed reason, being empty if the experiment is not failed at all.
     *
     * @return the failed reason
     */
    public String getFailedReason() {
        return failedReason;
    }

    /**
     * Set the failed reason, being empty if the experiment is not failed at all.
     *
     * @param failedReason the failed reason
     */
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    /**
     * Get the in-memory list of statistics.
     *
     * @return the in-memory list of statistics
     */
    public List<ExperimentStat> getStats() {
        return stats;
    }

    /**
     * Get the map of statistics.
     *
     * @return the map of statistics
     */
    public Map<String, Object> getStatsMap() {
        if (statsMap != null) {
            return statsMap;
        } else {
            Map<String, Object> result = new LinkedHashMap<>();

            for(ExperimentStat stat : stats) {
                result.put(stat.getPrefix() + "/" + stat.getKey(), stat.getValue());
            }

            return result;
        }
    }

    /**
     * Set the in-memory list of statistics.
     *
     * @param stats the in-memory list of statistics
     */
    public void setStats(List<ExperimentStat> stats) {
        this.stats = stats;
    }

    /**
     * Get a value indicating whether the experiment is stopped or not.
     *
     * @return a value indicating whether the experiment is stopped or not
     */
    public boolean isStopped() {
        return this.state == ExperimentState.COMPLETED || this.state == ExperimentState.ABORTED;
    }

    /**
     * Get the output directory.
     *
     * @return the output directory
     */
    protected abstract String getOutputDirectory();

    /**
     * Load statistics.
     */
    @SuppressWarnings("unchecked")
    public void loadStats() {
        File file = new File(getOutputDirectory(), "stats.json");

        try {
            String json = FileUtils.readFileToString(file);

            statsMap = JsonSerializationHelper.fromJson(Map.class, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the specified list of experiments.
     *
     * @param experiments the list of experiments
     * @param parallel a boolean value indicating whether runs the experiments in parallel or not
     */
    public static void runExperiments(List<? extends Experiment> experiments, boolean parallel) {
        if(parallel) {
            experiments.parallelStream().forEach(Experiment::run);
        } else {
            experiments.forEach(Experiment::run);
        }
    }
}
