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
package archimulator.common;

import archimulator.os.Kernel;
import archimulator.util.Reference;
import archimulator.util.dateTime.DateHelper;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.serialization.JsonSerializationHelper;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.util.*;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment {
    private long createTime;

    private ExperimentConfig config;

    private ExperimentState state;

    private String failedReason;

    private List<ContextMapping> contextMappings;

    private List<ExperimentStat> stats;

    /**
     * Current (max) memory page ID.
     */
    public transient int currentMemoryPageId;

    /**
     * Current (max) process ID.
     */
    public transient int currentProcessId;

    /**
     * Create an experiment.
     */
    public Experiment() {
        this.config = new ExperimentConfig();
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.contextMappings = new ArrayList<>(config.getContextMappings());

        this.createTime = DateHelper.toTick(new Date());
        this.stats = new ArrayList<>();
    }

    /**
     * Run.
     */
    public void run() {
        try {
            CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

            if (config.getType() == ExperimentType.FUNCTIONAL) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
                new FunctionalSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (config.getType() == ExperimentType.DETAILED) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
                new DetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (config.getType() == ExperimentType.TWO_PHASE) {
                Reference<Kernel> kernelRef = new Reference<>();

                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();

                new ToRoiFastForwardSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();

                blockingEventDispatcher.clearListeners();

                cycleAccurateEventQueue.resetCurrentCycle();

                new FromRoiDetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();
            }

            this.setState(ExperimentState.COMPLETED);
            this.setFailedReason("");
        } catch (Exception e) {
            this.setState(ExperimentState.ABORTED);
            this.setFailedReason(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }

        if (this.getState() == ExperimentState.COMPLETED) {
            File resultDirFile = new File(this.getConfig().getOutputDirectory());

            if (!resultDirFile.exists()) {
                if (!resultDirFile.mkdirs()) {
                    throw new RuntimeException();
                }
            }

            JsonSerializationHelper.writeJsonFile(this.getConfig(), this.getConfig().getOutputDirectory(), "config.json");
            JsonSerializationHelper.writeJsonFile(this.getStatsMap(), this.getConfig().getOutputDirectory(), "stats.json");
        }
    }

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
    public ExperimentConfig getConfig() {
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
     * Get the context mappings.
     *
     * @return the context mappings
     */
    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    /**
     * Get the in-memory list of statistics.
     *
     * @return the in-memory list of statistics
     */
    public List<ExperimentStat> getStats() {
        return stats;
    }

    public Map<String, Object> getStatsMap() {
        Map<String, Object> statsMap = new LinkedHashMap<>();

        for(ExperimentStat stat : stats) {
            statsMap.put(stat.getPrefix() + "/" + stat.getKey(), stat.getValue());
        }

        return statsMap;
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

    public void loadStats() {
        // TODO
    }

    public static void runExperiments(List<Experiment> experiments, boolean parallel) {
        if(parallel) {
            experiments.parallelStream().forEach(Experiment::run);
        } else {
            experiments.forEach(Experiment::run);
        }
    }
}
