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
package archimulator.model.metric;

import archimulator.model.Experiment;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.service.ServiceManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.WithCreateTime;
import net.pickapack.model.WithId;
import net.pickapack.model.WithParentId;
import net.pickapack.model.WithTitle;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Experiment statistic.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentStat")
public class ExperimentStat implements WithId, WithParentId, WithTitle, WithCreateTime {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long parentId;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String prefix;

    @DatabaseField
    private long gaugeId;

    @DatabaseField
    private String nodeKey;

    @DatabaseField(dataType = DataType.LONG_STRING)
    private String value;

    /**
     * Create an experiment statistic. Reserved for ORM only.
     */
    public ExperimentStat() {
    }

    /**
     * Create an experiment statistic.
     *
     * @param parent  the parent experiment object
     * @param prefix  the prefix
     * @param gauge   the gauge
     * @param nodeKey the node key
     * @param value   the value
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
     * Create an experiment statistic.
     *
     * @param parent the parent experiment object
     * @param prefix the prefix
     * @param key    the key
     * @param value  the value
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
     * Get the experiment statistic's ID.
     *
     * @return the experiment statistic's ID
     */
    @Override
    public long getId() {
        return id;
    }

    /**
     * Get the parent experiment object's ID.
     *
     * @return the parent experiment object's ID
     */
    @Override
    public long getParentId() {
        return parentId;
    }

    /**
     * Get the experiment statistic's title.
     *
     * @return the experiment statistic's title
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Get the time in ticks when the experiment statistic is created.
     *
     * @return the time in ticks when the experiment statistic is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the gauge ID.
     *
     * @return the gauge ID
     */
    public long getGaugeId() {
        return gaugeId;
    }

    /**
     * Get the node key.
     *
     * @return the node key
     */
    public String getNodeKey() {
        return nodeKey;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the parent experiment object.
     *
     * @return the parent experiment object
     */
    public Experiment getParent() {
        return ServiceManager.getExperimentService().getExperimentById(parentId);
    }

    /**
     * Get the gauge object.
     *
     * @return the gauge object
     */
    public ExperimentGauge getGauge() {
        return ServiceManager.getExperimentMetricService().getGaugeById(gaugeId);
    }

    @Override
    public String toString() {
        return String.format("ExperimentStat{id=%d, title='%s', createTime=%d, parentId=%d, prefix='%s', gaugeId=%d, nodeKey='%s', value='%s'}", id, title, createTime, parentId, prefix, gaugeId, nodeKey, value);
    }

    /**
     * Convert a list of experiment statistics into a map of experiment statistics.
     *
     * @param stats a list of experiment statistics
     * @return a map of experiment statistics
     */
    public static Map<String, ExperimentStat> toMap(List<ExperimentStat> stats) {
        final Map<String, ExperimentStat> statsMap = new LinkedHashMap<String, ExperimentStat>();
        for (ExperimentStat stat : stats) {
            statsMap.put(stat.getTitle(), stat);
        }
        return statsMap;
    }
}
