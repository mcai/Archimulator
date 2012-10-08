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
package archimulator.service.impl;

import archimulator.model.metric.ExperimentGauge;
import archimulator.service.ExperimentMetricService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class ExperimentMetricServiceImpl extends AbstractService implements ExperimentMetricService {
    private Dao<ExperimentGauge, Long> gauges;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public ExperimentMetricServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends ModelElement>>asList(ExperimentGauge.class));

        this.gauges = createDao(ExperimentGauge.class);

        if(this.getFirstGauge() == null) {
            //TODO: add initial gauges
        }
    }

    /**
     *
     * @return
     */
    @Override
    public List<ExperimentGauge> getAllGauges() {
        return this.getAllItems(this.gauges);
    }

    /**
     *
     * @param id
     * @return
     */
    @Override
    public ExperimentGauge getGaugeById(long id) {
        return this.getItemById(this.gauges, id);
    }

    /**
     *
     * @param title
     * @return
     */
    @Override
    public ExperimentGauge getGaugeByTitle(String title) {
        return this.getFirstItemByTitle(this.gauges, title);
    }

    /**
     *
     * @return
     */
    @Override
    public ExperimentGauge getFirstGauge() {
        return this.getFirstItem(this.gauges);
    }

    /**
     *
     * @param gauge
     */
    @Override
    public void addGauge(ExperimentGauge gauge) {
        this.addItem(this.gauges, ExperimentGauge.class, gauge);
    }

    /**
     *
     * @param id
     */
    @Override
    public void removeGaugeById(long id) {
        this.removeItemById(this.gauges, ExperimentGauge.class, id);
    }

    /**
     *
     * @param gauge
     */
    @Override
    public void updateGauge(ExperimentGauge gauge) {
        this.updateItem(this.gauges, ExperimentGauge.class, gauge);
    }
}
