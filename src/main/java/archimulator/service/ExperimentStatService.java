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
import archimulator.model.ExperimentSummary;
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.model.metric.MultiBarPlot;
import archimulator.model.metric.Table;
import net.pickapack.action.Function1;
import net.pickapack.service.Service;

import java.util.List;
import java.util.Map;

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
     * Get the list of statistics under the specified parent experiment object and matching the specified title prefix and gauge.
     *
     * @param parent the parent experiment object
     * @param prefix the title prefix
     * @param gauge  the gauge
     * @return a list of statistics under the specified parent experiment object and matching the specified title prefix and gauge if any exist; otherwise an empty list
     */
    List<ExperimentStat> getStatsByParentAndPrefixAndGauge(Experiment parent, String prefix, ExperimentGauge gauge);

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
     * Generate a table of summaries for the specified title, baseline experiment and a list of experiments.
     *
     * @param title              the title
     * @param baselineExperiment the baseline experiment
     * @param experiments        a list of experiments
     * @return a table of summaries for the specified title, baseline experiment and a list of experiments
     */
    @Deprecated
    Table tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * Generate a table of summaries for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a table of summaries for the specified list of experiments
     */
    Table tableSummary2(List<Experiment> experiments);

    /**
     * Get the list of breakdowns for the specified list of experiments and the keys function.
     *
     * @param experiments  a list of experiments
     * @param keysFunction the keys function
     * @return a list of breakdowns for the specified list of experiments and the keys function
     */
    List<Map<String, Double>> getBreakdowns(List<Experiment> experiments, Function1<Experiment, List<String>> keysFunction);

    /**
     * Get the breakdown for the specified experiment object and the keys function.
     *
     * @param experiment   the experiment object
     * @param keysFunction the keys function
     * @return the breakdown for the specified experiment object and the keys function
     */
    Map<String, Double> getBreakdown(Experiment experiment, Function1<Experiment, List<String>> keysFunction);

    /**
     * Get the list of speedups for the specified baseline experiment and a list of experiments.
     *
     * @param baselineExperiment the baseline experiment
     * @param experiments        a list of experiments
     * @return the list of speedups for the specified baseline experiment and a list of experiments
     */
    List<Double> getSpeedups(Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * Get the speedup for the specified baseline experiment and the other experiment.
     *
     * @param baselineExperiment the baseline experiment
     * @param experiment         the other experiment
     * @return the speedup for the specified baseline experiment and the other experiment
     */
    double getSpeedup(Experiment baselineExperiment, Experiment experiment);

    /**
     * Generate a multi bar plot object for showing the speedups for the specified baseline experiment and a list of experiments.
     *
     * @param baselineExperiment the baseline experiment
     * @param experiments        a list of experiments
     * @return a multi bar plot object for showing the speedups for the specified baseline experiment and a list of experiments
     */
    MultiBarPlot plotSpeedups(Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * Get the list of the number of total instructions for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of total instructions for the specified list of experiments
     */
    List<Double> getTotalInstructions(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the total instructions for the specified list of experiments.
     *
     * @param experiments    a list of experiments
     * @return a multi bar plot object for showing the total instructions for the specified list of experiments
     */
    MultiBarPlot plotTotalInstructions(List<Experiment> experiments);

    /**
     * Get a list of normalized number of total instructions for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of normalized number of total instructions for the specified list of experiments
     */
    List<Double> getNormalizedTotalInstructions(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of normalized number of total instructions for the specified list of experiments.
     *
     * @param experiments    a list of experiments
     * @return a multi bar plot object for showing the list of normalized number of total instructions for the specified list of experiments
     */
    MultiBarPlot plotNormalizedTotalInstructions(List<Experiment> experiments);

    /**
     * Get a list of the number of total cycles for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of total cycles for the specified list of experiments
     */
    List<Double> getTotalCycles(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of total cycles for the specified list of experiments.
     *
     * @param experiments    a list of experiments
     * @return a multi bar plot object for showing the list of the number of total cycles for the specified list of experiments
     */
    MultiBarPlot plotTotalCycles(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of total cycles for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of total cycles for the specified list of experiments
     */
    List<Double> getNormalizedTotalCycles(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized number of total cycles for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of total cycles for the specified list of experiments
     */
    MultiBarPlot plotNormalizedTotalCycles(List<Experiment> experiments);

    /**
     * Get a list of the number of L2 downward read misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of L2 downward read misses for the specified list of experiments
     */
    List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of L2 downward read misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the number of L2 downward read misses for the specified experiment list of experiments
     */
    MultiBarPlot plotNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of L2 downward read misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of L2 downward read misses for the specified list of experiments
     */
    List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized number of L2 downward read misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of L2 downward read misses for the specified list of experiments
     */
    MultiBarPlot plotNormalizedNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * Get a list of the number of main thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of main thread L2 cache hits for the specified list of experiments
     */
    List<Double> getNumMainThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of main thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the number of main thread L2 cache hits for the specified list of experiments
     */
    MultiBarPlot plotNumMainThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Get a list of the number of main thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of main thread L2 cache misses for the specified list of experiments
     */
    List<Double> getNumMainThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of main thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the number of main thread L2 cache misses for the specified list of experiments
     */
    MultiBarPlot plotNumMainThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Get a list of the number of helper thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of helper thread L2 cache hits for the specified list of experiments
     */
    List<Double> getNumHelperThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of helper thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the number of helper thread L2 cache hits for the specified list of experiments
     */
    MultiBarPlot plotNumHelperThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Get a list of the number of helper thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the number of helper thread L2 cache misses for the specified list of experiments
     */
    List<Double> getNumHelperThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the number of helper thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the number of helper thread L2 cache misses for the specified list of experiments
     */
    MultiBarPlot plotNumHelperThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of main thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of main thread L2 cache hits for the specified list of experiments
     */
    List<Double> getNormalizedNumMainThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized number of main thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of main thread L2 cache hits for the specified list of experiments
     */
    MultiBarPlot plotNormalizedNumMainThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of main thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of main thread L2 cache hits for the specified list of experiments
     */
    List<Double> getNormalizedNumMainThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized number of main thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of main thread L2 cache misses for the specified list of experiments
     */
    MultiBarPlot plotNormalizedNumMainThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of helper thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of helper thread L2 cache hits for the specified list of experiments
     */
    List<Double> getNormalizedNumHelperThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Generate multi bar plot object for showing the list of the normalized number of helper thread L2 cache hits for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of helper thread L2 cache hits for the specified list of experiments
     */
    MultiBarPlot plotNormalizedNumHelperThreadL2CacheHits(List<Experiment> experiments);

    /**
     * Get a list of the normalized number of helper thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized number of helper thread L2 cache misses for the specified list of experiments
     */
    List<Double> getNormalizedNumHelperThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized number of helper thread L2 cache misses for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized number of helper thread L2 cache misses for the specified list of experiments
     */
    MultiBarPlot plotNormalizedNumHelperThreadL2CacheMisses(List<Experiment> experiments);

    /**
     * Get a list of the helper thread L2 cache request coverage for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the helper thread L2 cache request coverage for the specified list of experiments
     */
    List<Double> getHelperThreadL2CacheRequestCoverage(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the helper thread L2 cache request coverage for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the helper thread L2 cache request coverage for the specified list of experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestCoverage(List<Experiment> experiments);

    /**
     * Get a list of the helper thread L2 cache request accuracy for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the helper thread L2 cache request accuracy for the specified list of experiments
     */
    List<Double> getHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the helper thread L2 cache request accuracy for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot for showing the list of the helper thread L2 cache request accuracy for the specified list of experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments);

    /**
     * Get a list of the normalized helper thread L2 cache request coverage for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized helper thread L2 cache request coverage for the specified list of experiments
     */
    List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized helper thread L2 cache request coverage for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized helper thread L2 cache request coverage for the specified list of experiments
     */
    MultiBarPlot plotNormalizedHelperThreadL2CacheRequestCoverage(List<Experiment> experiments);

    /**
     * Get a list of the normalized helper thread L2 cache request accuracy for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized helper thread L2 cache request accuracy for the specified list of experiments
     */
    List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized helper thread L2 cache request accuracy for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized helper thread L2 cache request accuracy for the specified list of experiments
     */
    MultiBarPlot plotNormalizedHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments);

    /**
     * Get a list of the L2 cache downward read MPKI for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the L2 cache downward read MPKI for the specified list of experiments
     */
    List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the L2 cache downward read MPKI for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the L2 cache downward read MPKI for the specified list of experiments
     */
    MultiBarPlot plotL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * Get a list of the normalized L2 cache downward read MPKI for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of the normalized L2 cache downward read MPKI for the specified list of experiments
     */
    List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of the normalized L2 cache downward read MPKI for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of the normalized L2 cache downward read MPKI for the specified list of experiments
     */
    MultiBarPlot plotNormalizedL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * Get a list of helper thread L2 cache request breakdown for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of helper thread L2 cache request breakdown for the specified list of experiments
     */
    List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * Generate multi bar plot object for showing the list of helper thread L2 cache request breakdown for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of helper thread L2 cache request breakdown for the specified list of experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * Get a list of L2 cache request breakdown for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a list of L2 cache request breakdown for the specified list of experiments
     */
    List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * Generate a multi bar plot object for showing the list of L2 cache request breakdown for the specified list of experiments.
     *
     * @param experiments a list of experiments
     * @return a multi bar plot object for showing the list of L2 cache request breakdown for the specified list of experiments
     */
    MultiBarPlot plotL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * Initialize the service.
     */
    void initialize();
}
