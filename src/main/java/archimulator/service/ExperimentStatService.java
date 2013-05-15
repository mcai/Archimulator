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
import archimulator.model.ExperimentSummary;
import archimulator.model.ExperimentStat;
import archimulator.util.plot.Table;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing experiment stats.
 *
 * @author Min Cai
 */
public interface ExperimentStatService extends Service {
    /**
     * Add a list of statistics under the specified parent experiment object.
     *
     * @param parent the parent experiment object
     * @param stats  a list of statistics to be added under the specified parent
     */
    void addStatsByParent(Experiment parent, List<ExperimentStat> stats);

    /**
     * Clear the list of statistics under the specified parent experiment object.
     *
     * @param parent the parent experiment object
     */
    void clearStatsByParent(Experiment parent);

    /**
     * Get the list of statistics under the specified parent experiment object.
     *
     * @param parent the parent experiment object
     * @return a list of statistics under the specified parent if any exist; otherwise an empty list
     */
    List<ExperimentStat> getStatsByParent(Experiment parent);

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified title.
     *
     * @param parent the parent experiment object
     * @param title  the title
     * @return a list of statistics under the specified parent experiment object and matching the specified title if any exist; otherwise an empty list
     */
    ExperimentStat getStatByParentAndTitle(Experiment parent, String title);

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified title pattern.
     *
     * @param parent    the parent experiment object
     * @param titleLike the title pattern
     * @return a list of statistics under the specified parent experiment object and matching the specified title pattern if any exist; otherwise an empty list
     */
    List<ExperimentStat> getStatsByParentAndTitleLike(Experiment parent, String titleLike);

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified title prefix.
     *
     * @param parent the parent experiment object
     * @param prefix the title prefix
     * @return a list of statistics under the specified parent experiment object and matching the specified title prefix if any exist; otherwise an empty list
     */
    List<ExperimentStat> getStatsByParentAndPrefix(Experiment parent, String prefix);

    /**
     * Get the list of statistic prefixes under the specified parent experiment object.
     *
     * @param parent the parent experiment object
     * @return a list of statistic prefixes under the specified parent experiment object
     */
    List<String> getStatPrefixesByParent(Experiment parent);

    /**
     * Get the summary of the specified parent experiment object.
     *
     * @param parent the parent experiment object
     * @return the summary of the specified parent experiment object
     */
    ExperimentSummary getSummaryByParent(Experiment parent);

    /**
     * Invalidate the summary of the specified parent experiment object.
     *
     * @param parent the parent experiment object
     */
    void invalidateSummaryByParent(Experiment parent);

    /**
     * Generate a table of summaries for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a table of summaries for the specified list of experiments
     */
    Table tableSummary(List<Experiment> experiments);

    /**
     * Initialize the service.
     */
    void initialize();
}
