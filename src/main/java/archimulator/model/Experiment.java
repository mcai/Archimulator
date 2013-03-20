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

import archimulator.model.metric.ExperimentStat;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.service.ServiceManager;
import archimulator.util.serialization.ContextMappingArrayListJsonSerializableType;
import archimulator.util.serialization.LongArrayListJsonSerializableType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;
import net.pickapack.model.WithTitle;
import net.pickapack.collection.CollectionHelper;
import org.apache.commons.lang.StringUtils;

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

    @DatabaseField(persisterClass = LongArrayListJsonSerializableType.class)
    private ArrayList<Long> gaugeIds;

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
     * @param gauges             the experiment gauges
     */
    public Experiment(ExperimentPack parent, ExperimentType type, Architecture architecture, long numMaxInstructions, List<ContextMapping> contextMappings, List<ExperimentGauge> gauges) {
        this.parentId = parent != null ? parent.getId() : -1;
        this.type = type;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.numMaxInstructions = numMaxInstructions;
        this.contextMappings = new ArrayList<ContextMapping>(contextMappings);
        this.gaugeIds = new ArrayList<Long>(CollectionHelper.transform(gauges, new Function1<ExperimentGauge, Long>() {
            @Override
            public Long apply(ExperimentGauge gauge) {
                return gauge.getId();
            }
        }));
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
     * Get the gauge IDs.
     *
     * @return the gauge IDs
     */
    public List<Long> getGaugeIds() {
        return gaugeIds;
    }

    /**
     * Get a statistic value by key.
     *
     * @param key the key
     * @return the statistic value
     */
    public String getStatValue(String key) {
        return getStatValue(key, null);
    }

    /**
     * Get a statistic value by key and default value.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the statistic value
     */
    public String getStatValue(String key, String defaultValue) {
        return getStatValue(ServiceManager.getExperimentStatService().getStatByParentAndTitle(this, key), defaultValue);
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
        return stat != null ? stat.getValue().replaceAll(",", "") : defaultValue;
    }

    /**
     * Get the statistic values from keys.
     *
     * @param keys the keys
     * @return the statistic values
     */
    public List<String> getStatValues(List<String> keys) {
        return CollectionHelper.transform(keys, new Function1<String, String>() {
            @Override
            public String apply(String key) {
                return getStatValue(key);
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
        switch (type) {
            case TWO_PHASE:
                return "twoPhase/phase1/";
            case FUNCTIONAL:
                return "functional/";
            case DETAILED:
                return "detailed/";
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return title;
    }
}
