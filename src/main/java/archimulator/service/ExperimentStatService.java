/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.model.ExperimentPack;
import archimulator.model.ExperimentStat;
import archimulator.model.ExperimentSummary;
import archimulator.util.plot.Table;
import archimulator.util.plot.TableFilterCriteria;
import net.pickapack.service.Service;

import java.util.List;

/**
 * Service for managing experiment stats.
 *
 * @author Min Cai
 */
public interface ExperimentStatService extends Service {
    static final TableFilterCriteria SUMMARY_PDF_FILTER_CRITERIA = new TableFilterCriteria(
            "Id",
            "Benchmark",

            "Stride",
            "Lookahead",

            "L2_Size",
            "L2_Associativity",
            "L2_Replacement",
            "MT_Ways_In_Partitioned_L2",

            "Num_Cycles",

            "Late",
            "Timely",
            "Bad",
            "Early",
            "Ugly",
            "Redundant_MSHR",
            "Redundant_Cache"
    );

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
     * Get the statistics under the specified parent experiment object and matching the specified prefix and key.
     *
     * @param parent the parent experiment object
     * @param prefix the prefix
     * @param key    the key
     * @return the statistics under the specified parent experiment object and matching the specified prefix and key if any exist; otherwise an empty list
     */
    ExperimentStat getStatByParentAndPrefixAndKey(Experiment parent, String prefix, String key);

    /**
     * Get the list of statistics under the specified parent experiment object and matching the specified prefix and key pattern.
     *
     * @param parent    the parent experiment object
     * @param prefix the prefix
     * @param keyLike the key pattern
     * @return a list of statistics under the specified parent experiment object and matching the specified prefix and key pattern if any exist; otherwise an empty list
     */
    List<ExperimentStat> getStatsByParentAndPrefixAndKeyLike(Experiment parent, String prefix, String keyLike);

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
     * @param experimentPack the experiment pack
     * @param experiments a list of experiments
     * @return a table of summaries for the specified list of experiments
     */
    Table tableSummary(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * Initialize the service.
     */
    void initialize();
}
