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
import archimulator.model.ExperimentPack;
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
     * @param stats
     */
    void addStats(List<ExperimentStat> stats);

    /**
     * @param parent
     */
    void clearStatsByParent(Experiment parent);

    /**
     * @param parent
     * @return
     */
    List<ExperimentStat> getStatsByParent(Experiment parent);

    /**
     * @param parent
     * @param title
     * @return
     */
    ExperimentStat getStatByParentAndTitle(Experiment parent, String title);

    /**
     * @param parent
     * @param titleLike
     * @return
     */
    List<ExperimentStat> getStatsByParentAndTitleLike(Experiment parent, String titleLike);

    /**
     * @param parent
     * @param gauge
     * @return
     */
    List<ExperimentStat> getStatsByParentAndGauge(Experiment parent, ExperimentGauge gauge);

    /**
     * @param title
     * @param baselineExperiment
     * @param experiments
     */
    Table tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * @param title
     * @param baselineExperiment
     * @param experiments
     */
    Table tableSummary2(String title, Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * @param experiments
     * @param keysFunction
     * @return
     */
    List<Map<String, Double>> getBreakdowns(List<Experiment> experiments, Function1<Experiment, List<String>> keysFunction);

    /**
     * @param experiment
     * @param keysFunction
     * @return
     */
    Map<String, Double> getBreakdown(Experiment experiment, Function1<Experiment, List<String>> keysFunction);

    /**
     * @param baselineExperiment
     * @param experiments
     * @return
     */
    List<Double> getSpeedups(Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * @param baselineExperiment
     * @param experiment
     * @return
     */
    double getSpeedup(Experiment baselineExperiment, Experiment experiment);

    /**
     * @param experimentPack
     * @param baselineExperiment
     * @param experiments
     */
    MultiBarPlot plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getTotalInstructions(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedTotalInstructions(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getTotalCycles(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedTotalCycles(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    MultiBarPlot plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);
}
