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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.*;

@DatabaseTable(tableName = "Experiment")
public class Experiment implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true)
    private long parentId;

    @DatabaseField(index = true)
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private ExperimentType type;

    @DatabaseField
    private ExperimentState state;

    @DatabaseField
    private String failedReason;

    @DatabaseField
    private long architectureId;

    @DatabaseField
    private int numMaxInsts;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayList<ContextMapping> contextMappings;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private LinkedHashMap<String, String> stats;

    private transient Architecture architecture;

    private transient ExperimentPack parent;

    public transient int currentMemoryPageId;
    public transient int currentProcessId;

    public Experiment() {
    }

    public Experiment(ExperimentPack parent, String title, ExperimentType type, Architecture architecture, int numMaxInsts, List<ContextMapping> contextMappings) {
        this.parentId = parent == null ? -1 : parent.getId();
        this.title = title;
        this.type = type;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.numMaxInsts = numMaxInsts;
        this.contextMappings = new ArrayList<ContextMapping>(contextMappings);
        this.architectureId = architecture.getId();
        this.stats = new LinkedHashMap<String, String>();
        this.createTime = DateHelper.toTick(new Date());
    }

    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return parentId;
    }

    public String getTitle() {
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

    public int getNumMaxInsts() {
        return numMaxInsts;
    }

    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    public Map<String, String> getStats() {
        return stats;
    }

    public ExperimentPack getParent() {
        if(parent == null) {
            parent = ServiceManager.getExperimentService().getExperimentPackById(this.parentId);
        }

        return parent;
    }

    public Architecture getArchitecture() {
        if(architecture == null) {
            architecture = ServiceManager.getArchitectureService().getArchitectureById(this.architectureId);
        }

        return architecture;
    }

    public boolean isStopped() {
        return this.state == ExperimentState.COMPLETED || this.state == ExperimentState.ABORTED;
    }

    public void dump() {
        ServiceManager.getExperimentService().dumpExperiment(this);
    }

    @Override
    public String toString() {
        return title;
    }
}
