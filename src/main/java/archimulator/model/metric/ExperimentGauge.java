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
import net.pickapack.model.metric.Gauge;

/**
 *
 * @author Min Cai
 */
@DatabaseTable(tableName = "ExperimentGauge")
public class ExperimentGauge extends Gauge {
    @DatabaseField
    private long typeId;

    /**
     *
     */
    public ExperimentGauge() {
    }

    /**
     *
     * @param type
     * @param valueExpression
     *
     */
    public ExperimentGauge(ExperimentGaugeType type, String valueExpression) {
        this(type, valueExpression, valueExpression);
    }

    /**
     *
     * @param type
     * @param title
     * @param valueExpression
     */
    public ExperimentGauge(ExperimentGaugeType type, String title, String valueExpression) {
        super(title, valueExpression);
        this.typeId = type != null ? type.getId() : -1;
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public ExperimentGaugeType getType() {
        return ServiceManager.getExperimentMetricService().getGaugeTypeById(typeId);
    }
}
