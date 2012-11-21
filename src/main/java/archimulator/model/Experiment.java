/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.service.ServiceManager;
import archimulator.util.serialization.ContextMappingArrayListJsonSerializableType;
import archimulator.util.serialization.LongArrayListJsonSerializableType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.util.CollectionHelper;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "Experiment")
public class Experiment implements ModelElement {
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
     *
     */
    public transient int currentMemoryPageId;

    /**
     *
     */
    public transient int currentProcessId;

    /**
     *
     */
    public Experiment() {
    }

    /**
     *
     * @param type
     * @param architecture
     * @param numMaxInstructions
     * @param contextMappings
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
     *
     */
    public void updateTitle() {
        ContextMapping contextMapping = this.contextMappings.get(0);
        Benchmark benchmark = contextMapping.getBenchmark();

        this.title = benchmark.getTitle().replaceAll(" ", "_") + (StringUtils.isEmpty(contextMapping.getArguments()) ? "" : "_" + contextMapping.getArguments().replaceAll(" ", "_"));

        if (this.type == ExperimentType.FUNCTIONAL) {
            this.title += "-functional";
        }
        else {
            if(this.type == ExperimentType.TWO_PHASE) {
                this.title += "-two_phase";
            }

            if(contextMapping.getBenchmark().getHelperThreadEnabled()) {
                this.title += "-lookahead_" + contextMapping.getHelperThreadLookahead() + "-stride_" + contextMapping.getHelperThreadStride();
            }

            this.title += "-" + this.getArchitecture().getTitle();
        }
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     *
     * @return
     */
    public String getTitle() {
        if(title == null) {
            updateTitle();
        }

        return title;
    }

    /**
     *
     * @return
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    /**
     *
     * @return
     */
    public ExperimentType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public ExperimentState getState() {
        return state;
    }

    /**
     *
     * @param state
     */
    public void setState(ExperimentState state) {
        this.state = state;
    }

    /**
     *
     * @return
     */
    public String getFailedReason() {
        return failedReason;
    }

    /**
     *
     * @param failedReason
     */
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    /**
     *
     * @return
     */
    public long getArchitectureId() {
        return architectureId;
    }

    /**
     *
     * @return
     */
    public long getNumMaxInstructions() {
        return numMaxInstructions;
    }

    /**
     *
     * @return
     */
    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    public List<Long> getGaugeIds() {
        return gaugeIds;
    }

    /**
     *
     * @param key
     * @return
     */
    public String getStatValue(String key) {
        return getStatValue(ServiceManager.getExperimentStatService().getStatByParentAndTitle(this, key));
    }

    /**
     *
     * @param statsMap
     * @param key
     * @return
     */
    public String getStatValue(Map<String, ExperimentStat> statsMap, String key) {
        return getStatValue(statsMap.containsKey(key) ? statsMap.get(key) : null);
    }

    /**
     *
     * @param stat
     * @return
     */
    public String getStatValue(ExperimentStat stat) {
        return stat != null ? stat.getValue().replaceAll(",", "") : null;
    }

    /**
     *
     * @param keys
     * @return
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
     *
     * @return
     */
    public Architecture getArchitecture() {
        if (architecture == null) {
            architecture = ServiceManager.getArchitectureService().getArchitectureById(this.architectureId);
        }

        return architecture;
    }

    /**
     *
     * @return
     */
    public boolean isStopped() {
        return this.state == ExperimentState.COMPLETED || this.state == ExperimentState.ABORTED;
    }

    /**
     *
     * @return
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
