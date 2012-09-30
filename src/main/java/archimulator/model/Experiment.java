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

import archimulator.service.ServiceManager;
import archimulator.util.ArrayListOfContextMappingsJsonSerializableType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.action.Function1;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.util.CollectionHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;

@DatabaseTable(tableName = "Experiment")
public class Experiment implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private ExperimentType type;

    @DatabaseField
    private ExperimentState state;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String failedReason;

    @DatabaseField
    private long architectureId;

    @DatabaseField
    private int numMaxInstructions;

    @DatabaseField(persisterClass = ArrayListOfContextMappingsJsonSerializableType.class)
    private ArrayList<ContextMapping> contextMappings;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private LinkedHashMap<String, String> stats;

    private transient Architecture architecture;

    public transient int currentMemoryPageId;
    public transient int currentProcessId;

    public Experiment() {
    }

    public Experiment(ExperimentType type, Architecture architecture, int numMaxInstructions, List<ContextMapping> contextMappings) {
        this.type = type;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.numMaxInstructions = numMaxInstructions;
        this.contextMappings = new ArrayList<ContextMapping>(contextMappings);
        this.architectureId = architecture.getId();
        this.createTime = DateHelper.toTick(new Date());
    }

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

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    public String getTitle() {
        if(title == null) {
            updateTitle();
        }

        return title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    public ExperimentType getType() {
        return type;
    }

    public ExperimentState getState() {
        return state;
    }

    public void setState(ExperimentState state) {
        this.state = state;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public long getArchitectureId() {
        return architectureId;
    }

    public int getNumMaxInstructions() {
        return numMaxInstructions;
    }

    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    public Map<String, String> getStats() {
        if(stats == null) {
            stats = new LinkedHashMap<String, String>();
        }

        return stats;
    }

    public String getStatValue(String key) {
        return getStats().containsKey(key) ? getStats().get(key).replaceAll(",", "") : null;
    }

    public List<String> getStatValues(List<String> keys) {
        return CollectionHelper.transform(keys, new Function1<String, String>() {
            @Override
            public String apply(String key) {
                return getStatValue(key);
            }
        });
    }

    public Architecture getArchitecture() {
        if (architecture == null) {
            architecture = ServiceManager.getArchitectureService().getArchitectureById(this.architectureId);
        }

        return architecture;
    }

    public boolean isStopped() {
        return this.state == ExperimentState.COMPLETED || this.state == ExperimentState.ABORTED;
    }

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
