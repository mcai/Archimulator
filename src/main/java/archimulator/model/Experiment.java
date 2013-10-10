/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.model;

import archimulator.service.ServiceManager;
import archimulator.sim.common.*;
import archimulator.sim.os.Kernel;
import archimulator.util.serialization.ContextMappingArrayListJsonSerializableType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.collection.CollectionHelper;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;
import net.pickapack.model.WithTitle;
import net.pickapack.util.Reference;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Experiment.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "Experiment")
public class Experiment implements WithId, WithParentId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private ExperimentType type;

    @DatabaseField
    private ExperimentState state;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String failedReason;

    @DatabaseField
    private long architectureId;

    @DatabaseField
    private long numMaxInstructions;

    @DatabaseField(persisterClass = ContextMappingArrayListJsonSerializableType.class)
    private ArrayList<ContextMapping> contextMappings;

    private transient List<ExperimentStat> stats;

    private transient ExperimentSummary summary;

    private transient Architecture architecture;

    /**
     * Current (max) memory page ID.
     */
    public transient int currentMemoryPageId;

    /**
     * Current (max) process ID.
     */
    public transient int currentProcessId;

    /**
     * Create an experiment. Reserved for ORM only.
     */
    public Experiment() {
    }

    /**
     * Create an experiment.
     *
     * @param parent             the parent experiment pack
     * @param type               the experiment type
     * @param architecture       the architecture
     * @param numMaxInstructions the upper limit of the number of instructions executed on the first hardware thread
     * @param contextMappings    the context mappings
     */
    public Experiment(ExperimentPack parent, ExperimentType type, Architecture architecture, long numMaxInstructions, List<ContextMapping> contextMappings) {
        this.parentId = parent != null ? parent.getId() : -1;
        this.type = type;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.numMaxInstructions = numMaxInstructions;
        this.contextMappings = new ArrayList<ContextMapping>(contextMappings);
        this.architectureId = architecture.getId();
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Update the experiment title.
     */
    public void updateTitle() {
        ContextMapping contextMapping = this.contextMappings.get(0);
        Benchmark benchmark = contextMapping.getBenchmark();

        this.title = benchmark.getTitle().replaceAll(" ", "_") + (StringUtils.isEmpty(contextMapping.getArguments()) ? "" : "_" + contextMapping.getArguments().replaceAll(" ", "_"));

        if (this.type == ExperimentType.FUNCTIONAL) {
            this.title += "-functional";
        } else {
            if (this.type == ExperimentType.TWO_PHASE) {
                this.title += "-two_phase";
            }

            if (contextMapping.getBenchmark().getHelperThreadEnabled()) {
                this.title += "-lookahead_" + contextMapping.getHelperThreadLookahead() + "-stride_" + contextMapping.getHelperThreadStride();
            }

            this.title += "-" + this.getArchitecture().getTitle();
        }
    }

    /**
     * Run.
     */
    public void run() {
        ServiceManager.getBlockingEventDispatcher().dispatch(new ExperimentStartedEvent(this));

        try {
            CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

            if (getType() == ExperimentType.FUNCTIONAL) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new FunctionalSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (getType() == ExperimentType.DETAILED) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new DetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (getType() == ExperimentType.TWO_PHASE) {
                Reference<Kernel> kernelRef = new Reference<Kernel>();

                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();

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

        ServiceManager.getBlockingEventDispatcher().dispatch(new ExperimentStoppedEvent(this));
    }

    /**
     * Get the experiment ID.
     *
     * @return the experiment ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the parent experiment pack's ID.
     *
     * @return the parent experiment pack's ID
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     * Get the experiment title.
     *
     * @return the experiment title
     */
    @Override
    public String getTitle() {
        if (title == null) {
            updateTitle();
        }

        return title;
    }

    /**
     * Get the time in ticks when the experiment is created.
     *
     * @return the time in ticks when the experiment is created
     */
    @Override
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
     * Get the experiment type.
     *
     * @return the experiment type
     */
    public ExperimentType getType() {
        return type;
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
     * Get the architecture ID.
     *
     * @return the architecture ID
     */
    public long getArchitectureId() {
        return architectureId;
    }

    /**
     * Get the upper limit of the number of instructions executed in the first hardware thread.
     *
     * @return the upper limit of the number of instructions executed in the first hardware thread
     */
    public long getNumMaxInstructions() {
        return numMaxInstructions;
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
     * Get a statistic value by key.
     *
     * @param prefix the prefix
     * @param key    the key
     * @return the statistic value
     */
    public String getStatValue(String prefix, String key) {
        return getStatValue(prefix, key, null);
    }

    /**
     * Get a statistic value by key and default value.
     *
     * @param prefix       the prefix
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistic value
     */
    public String getStatValue(String prefix, String key, String defaultValue) {
        return getStatValue(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(this, prefix, key), defaultValue);
    }

    /**
     * Get a statistic value as long by key and default value.
     *
     * @param prefix       the prefix
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistic value as long
     */
    public long getStatValueAsLong(String prefix, String key, long defaultValue) {
        String val = getStatValue(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(this, prefix, key), defaultValue + "");

        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Get a statistic value as double by key and default value.
     *
     * @param prefix       the prefix
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistic value as double
     */
    public double getStatValueAsDouble(String prefix, String key, double defaultValue) {
        String val = getStatValue(ServiceManager.getExperimentStatService().getStatByParentAndPrefixAndKey(this, prefix, key), defaultValue + "");

        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Get a statistic value from the map and by key.
     *
     * @param statsMap the statistic map
     * @param key      the key
     * @return the statistic value
     */
    public String getStatValue(Map<String, ExperimentStat> statsMap, String key) {
        return getStatValue(statsMap.containsKey(key) ? statsMap.get(key) : null);
    }

    /**
     * Get a statistic value from the map and by key and default value.
     *
     * @param statsMap     the statistics map
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistic value
     */
    public String getStatValue(Map<String, ExperimentStat> statsMap, String key, String defaultValue) {
        return getStatValue(statsMap.containsKey(key) ? statsMap.get(key) : null, defaultValue);
    }

    /**
     * Get a statistics value as long from the map and by key and default value.
     *
     * @param statsMap     the statistics map
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistics value
     */
    public long getStatValueAsLong(Map<String, ExperimentStat> statsMap, String key, long defaultValue) {
        String val = getStatValue(statsMap.containsKey(key) ? statsMap.get(key) : null, defaultValue + "");
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Get a statistics value as double from the map and by key and default value.
     *
     * @param statsMap     the statistics map
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistics value
     */
    public double getStatValueAsDouble(Map<String, ExperimentStat> statsMap, String key, double defaultValue) {
        String val = getStatValue(statsMap.containsKey(key) ? statsMap.get(key) : null, defaultValue + "");
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * Get the statistic value from the experiment stat object.
     *
     * @param stat the experiment stat object
     * @return the statistic value
     */
    public String getStatValue(ExperimentStat stat) {
        return getStatValue(stat, null);
    }

    /**
     * Get the statistic value from the experiment stat object and the default value.
     *
     * @param stat         the experiment stat object
     * @param defaultValue the default value
     * @return the statistic value
     */
    public String getStatValue(ExperimentStat stat, String defaultValue) {
        if (stat != null) {
            if (stat.getValue() != null) {
                return stat.getValue().replaceAll(",", "");
            }
            else return null;
        }
        else {
            return defaultValue;
        }
    }

    /**
     * Get the statistic values from keys.
     *
     * @param keys   the keys
     * @param prefix the prefix
     * @return the statistic values
     */
    public List<String> getStatValues(final String prefix, List<String> keys) {
        return CollectionHelper.transform(keys, new Function1<String, String>() {
            @Override
            public String apply(String key) {
                return getStatValue(prefix, key);
            }
        });
    }

    /**
     * Get the architecture object.
     *
     * @return the architecture object
     */
    public Architecture getArchitecture() {
        if (architecture == null) {
            architecture = ServiceManager.getArchitectureService().getArchitectureById(this.architectureId);
        }

        return architecture;
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
     * Set the in-memory list of statistics.
     *
     * @param stats the in-memory list of statistics
     */
    public void setStats(List<ExperimentStat> stats) {
        this.stats = stats;
    }

    /**
     * Get the in-memory summary.
     *
     * @return the in-memory summary
     */
    public ExperimentSummary getSummary() {
        return summary;
    }

    /**
     * Set the in-memory summary.
     *
     * @param summary the in-memory summary
     */
    public void setSummary(ExperimentSummary summary) {
        this.summary = summary;
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
     * Get the measurement simulation title prefix.
     *
     * @return the measurement simulation title prefix
     */
    public String getMeasurementTitlePrefix() {
        return getMeasurementTitlePrefix(type);
    }

    /**
     * Get the parent experiment pack.
     *
     * @return the parent experiment pack
     */
    public ExperimentPack getParent() {
        return ServiceManager.getExperimentService().getParent(this);
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Get the measurement simulation title prefix from the specified experiment type.
     *
     * @param type the experiment type
     * @return the measurement simulation title prefix from the specified experiment type
     */
    public static String getMeasurementTitlePrefix(ExperimentType type) {
        switch (type) {
            case TWO_PHASE:
                return "twoPhase/phase1";
            case FUNCTIONAL:
                return "functional";
            case DETAILED:
                return "detailed";
            default:
                throw new IllegalArgumentException();
        }
    }
}
