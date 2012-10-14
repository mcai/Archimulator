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
package archimulator.service;

import archimulator.model.Experiment;
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentGaugeType;
import net.pickapack.service.Service;

import java.util.List;

/**
 *
 * Service for managing experiment metrics.
 *
 * @author Min Cai
 */
public interface ExperimentMetricService extends Service {
    /**
     *
     * @return
     */
    List<ExperimentGaugeType> getAllGaugeTypes();

    /**
     *
     * @param id
     * @return
     */
    ExperimentGaugeType getGaugeTypeById(long id);

    /**
     *
     * @param title
     * @return
     */
    ExperimentGaugeType getGaugeTypeByTitle(String title);

    /**
     *
     * @return
     */
    ExperimentGaugeType getFirstGaugeType();

    /**
     *
     * @param gaugeType
     */
    void addGaugeType(ExperimentGaugeType gaugeType);

    /**
     *
     * @param id
     */
    void removeGaugeTypeById(long id);

    /**
     *
     * @param gaugeType
     */
    void updateGaugeType(ExperimentGaugeType gaugeType);

    /**
     *
     * @return
     */
    List<ExperimentGauge> getAllGauges();

    /**
     *
     * @param id
     * @return
     */
    ExperimentGauge getGaugeById(long id);

    /**
     *
     * @param title
     * @return
     */
    ExperimentGauge getGaugeByTitle(String title);

    /**
     *
     * @param type
     * @return
     */
    List<ExperimentGauge> getGaugesByType(ExperimentGaugeType type);

    /**
     *
     * @param experiment
     * @return
     */
    List<ExperimentGauge> getGaugesByExperiment(Experiment experiment);

    /**
     *
     * @param experiment
     * @param type
     * @return
     */
    List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, ExperimentGaugeType type);

    /**
     *
     * @param experiment
     * @param typeTitle
     * @return
     */
    List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, String typeTitle);

    /**
     *
     * @return
     */
    ExperimentGauge getFirstGauge();

    /**
     *
     * @param gauge
     */
    void addGauge(ExperimentGauge gauge);

    /**
     *
     * @param id
     */
    void removeGaugeById(long id);

    /**
     *
     * @param gauge
     */
    void updateGauge(ExperimentGauge gauge);
}
