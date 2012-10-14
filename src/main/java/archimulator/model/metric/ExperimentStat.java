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
package archimulator.model.metric;

import archimulator.model.Experiment;
import archimulator.service.ServiceManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentStat")
public class ExperimentStat implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long parentId;

    @DatabaseField
    private String prefix;

    @DatabaseField
    private long gaugeId;

    @DatabaseField
    private String nodeKey;

    @DatabaseField
    private String value;

    /**
     *
     */
    public ExperimentStat() {
    }

    /**
     *
     * @param parent
     * @param prefix
     * @param gauge
     * @param nodeKey
     * @param value
     */
    public ExperimentStat(Experiment parent, String prefix, ExperimentGauge gauge, String nodeKey, String value) {
        this.title = prefix + "/" + nodeKey + "/" + gauge.getValueExpression();
        this.parentId = parent.getId();
        this.prefix = prefix;
        this.gaugeId = gauge.getId();
        this.nodeKey = nodeKey;
        this.value = value;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @param parent
     * @param prefix
     * @param key
     * @param value
     */
    public ExperimentStat(Experiment parent, String prefix, String key, String value) {
        this.title = prefix + "/" + key;
        this.parentId = parent.getId();
        this.prefix = prefix;
        this.gaugeId = -1;
        this.nodeKey = null;
        this.value = value;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     *
     * @return
     */
    @Override
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
    @Override
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     *
     * @return
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     *
     * @return
     */
    public long getGaugeId() {
        return gaugeId;
    }

    /**
     *
     * @return
     */
    public String getNodeKey() {
        return nodeKey;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }

    /**
     *
     * @return
     */
    public ExperimentGauge getGauge() {
        return ServiceManager.getExperimentMetricService().getGaugeById(gaugeId);
    }

    @Override
    public String toString() {
        return String.format("ExperimentStat{id=%d, title='%s', createTime=%d, parentId=%d, prefix='%s', gaugeId=%d, nodeKey='%s', value='%s'}", id, title, createTime, parentId, prefix, gaugeId, nodeKey, value);
    }
}
