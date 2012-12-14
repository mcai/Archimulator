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

import archimulator.service.ServiceManager;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

/**
 * Experiment gauge.
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentGauge")
public class ExperimentGauge implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private long typeId;

    @DatabaseField
    private String valueExpression;

    @DatabaseField
    private String description;

    /**
     * Create an experiment gauge. Reserved for ORM only.
     */
    public ExperimentGauge() {
    }

    /**
     * Create an experiment gauge.
     *
     * @param type the type of the gauge
     * @param valueExpression the value expression
     *
     */
    public ExperimentGauge(ExperimentGaugeType type, String valueExpression) {
        this(type, valueExpression, valueExpression);
    }

    /**
     * Create an experiment gauge.
     *
     * @param type the type of the gauge
     * @param title the title of the gauge
     * @param valueExpression the value expression
     */
    public ExperimentGauge(ExperimentGaugeType type, String title, String valueExpression) {
        this.title = title;
        this.valueExpression = valueExpression;
        this.typeId = type != null ? type.getId() : -1;
        this.createTime = DateHelper.toTick(new Date());
    }

    /**
     * Get the gauge's ID.
     *
     * @return the gauge's ID
     */
    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    /**
     * Get the title of the gauge.
     *
     * @return the title of the gauge.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the time in ticks when the gauge is created.
     *
     * @return the time in ticks when the gauge is created
     */
    @Override
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the value expression.
     *
     * @return the value expression
     */
    public String getValueExpression() {
        return valueExpression;
    }

    /**
     * Set the value expression.
     *
     * @param valueExpression the value expression
     */
    public void setValueExpression(String valueExpression) {
        this.valueExpression = valueExpression;
    }

    /**
     * Get the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the type ID.
     *
     * @return the type ID
     */
    public long getTypeId() {
        return typeId;
    }

    /**
     * Set the type ID.
     *
     * @param typeId the type ID
     */
    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    /**
     * Get the type of the gauge.
     *
     * @return  the type of the gauge
     */
    public ExperimentGaugeType getType() {
        return ServiceManager.getExperimentMetricService().getGaugeTypeById(typeId);
    }

    @Override
    public String toString() {
        return String.format("ExperimentGauge{id=%d, title='%s', createTime=%d, valueExpression='%s', typeId='%d'}", getId(), getTitle(), getCreateTime(), getValueExpression(), typeId);
    }
}
