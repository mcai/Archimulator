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
import archimulator.sim.common.report.ReportNode;
import net.pickapack.service.Service;

/**
 * Service for managing experiment reports.
 *
 * @author Min Cai
 */
public interface ExperimentReportService extends Service {
    /**
     * Initialize.
     */
    void initialize();

    /**
     * Set the report node for the specified parent experiment.
     *
     * @param parent the parent experiment
     * @param reportNode the report node
     */
    void setReportNodeByParent(Experiment parent, ReportNode reportNode);

    /**
     * Get the report node for the specified parent experiment.
     *
     * @param parent the parent experiment
     * @return the report node for the specified parent experiment
     */
    ReportNode getReportNodeByParent(Experiment parent);

    /**
     * Get the report node for the specified parent experiment and path.
     *
     * @param parent the parent experiment
     * @param path the path
     * @return the report node for the specified parent experiment and path
     */
    ReportNode getReportNodeByParent(Experiment parent, String... path);
}
