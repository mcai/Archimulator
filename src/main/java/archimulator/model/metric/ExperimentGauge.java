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
    private ExperimentGaugeType type;

    /**
     *
     */
    public ExperimentGauge() {
    }

    /**
     *
     * @param title
     * @param expression
     * @param type
     */
    public ExperimentGauge(String title, String expression, ExperimentGaugeType type) {
        super(title, expression);
        this.type = type;
    }

    /**
     *
     * @return
     */
    public ExperimentGaugeType getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(ExperimentGaugeType type) {
        this.type = type;
    }
}
