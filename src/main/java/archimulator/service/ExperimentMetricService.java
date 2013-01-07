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
package archimulator.service;

import archimulator.model.Experiment;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.model.metric.gauge.ExperimentGaugeType;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing experiment metrics.
 *
 * @author Min Cai
 */
public interface ExperimentMetricService extends Service {
    /**
     * Get all the gauge types.
     *
     * @return all the gauge types
     */
    List<ExperimentGaugeType> getAllGaugeTypes();

    /**
     * Get a gauge type by ID.
     *
     * @param id the gauge type's ID
     * @return the gauge type matching the ID if any exists; otherwise null
     */
    ExperimentGaugeType getGaugeTypeById(long id);

    /**
     * Get a gauge type by title.
     *
     * @param title the gauge type's title
     * @return the gauge type matching the title if any exists; otherwise null
     */
    ExperimentGaugeType getGaugeTypeByTitle(String title);

    /**
     * Get the first gauge type.
     *
     * @return the first gauge type
     */
    ExperimentGaugeType getFirstGaugeType();

    /**
     * Add a gauge type.
     *
     * @param gaugeType the gauge type that is to be added
     */
    void addGaugeType(ExperimentGaugeType gaugeType);

    /**
     * Remove a gauge type by ID.
     *
     * @param id the ID of the gauge type that is to be removed
     */
    void removeGaugeTypeById(long id);

    /**
     * Update a gauge type.
     *
     * @param gaugeType the gauge type that is to be updated
     */
    void updateGaugeType(ExperimentGaugeType gaugeType);

    /**
     * Get all the gauges.
     *
     * @return all the gauges
     */
    List<ExperimentGauge> getAllGauges();

    /**
     * Get a gauge by ID.
     *
     * @param id the gauge's ID
     * @return the gauge matching the ID if any exists; otherwise null
     */
    ExperimentGauge getGaugeById(long id);

    /**
     * Get a gauge by title.
     *
     * @param title the gauge's title
     * @return the gauge matching the title if any exists; otherwise null
     */
    ExperimentGauge getGaugeByTitle(String title);

    /**
     * Get the gauges by type.
     *
     * @param type the gauge type to be searched
     * @return the gauges matching the type if any exist; otherwise an empty list
     */
    List<ExperimentGauge> getGaugesByType(ExperimentGaugeType type);

    /**
     * Get the gauges by experiment.
     *
     * @param experiment the experiment to be searched
     * @return the gauges under the experiment if any exist; otherwise an empty list
     */
    List<ExperimentGauge> getGaugesByExperiment(Experiment experiment);

    /**
     * Get the gauges by experiment and type.
     *
     * @param experiment the experiment to be searched
     * @param type       the gauge type to be searched
     * @return the gauges under the experiment matching the type if any exist; otherwise an empty list
     */
    List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, ExperimentGaugeType type);

    /**
     * Get the gauges by experiment and type title.
     *
     * @param experiment the experiment to be searched
     * @param typeTitle  the title of the gauge type to be searched
     * @return the gauges under the experiment matching the type if any exist; otherwise an empty list
     */
    List<ExperimentGauge> getGaugesByExperimentAndType(Experiment experiment, String typeTitle);

    /**
     * Get the first gauge.
     *
     * @return the first gauge
     */
    ExperimentGauge getFirstGauge();

    /**
     * Add a gauge.
     *
     * @param gauge the gauge to be added
     */
    void addGauge(ExperimentGauge gauge);

    /**
     * Remove a gauge by ID.
     *
     * @param id the ID of the gauge to be removed
     */
    void removeGaugeById(long id);

    /**
     * Update a gauge.
     *
     * @param gauge the gauge to be updated
     */
    void updateGauge(ExperimentGauge gauge);

    /**
     * Initialize the service.
     */
    void initialize();
}
