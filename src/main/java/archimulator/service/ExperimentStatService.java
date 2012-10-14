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
import archimulator.model.metric.Table;
import net.pickapack.action.Function1;
import net.pickapack.service.Service;
import net.pickapack.util.IndentedPrintWriter;

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
    void plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getTotalInstructions(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedTotalInstructions(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getTotalCycles(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedTotalCycles(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

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
    void plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

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
    void plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experiments
     * @return
     */
    List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param experiments
     */
    void plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);

    /**
     * @param experimentPack
     * @param detailed
     * @param stoppedExperimentsOnly
     */
    void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, boolean stoppedExperimentsOnly);

    /**
     * @param experimentPack
     * @param detailed
     * @param writer
     * @param stoppedExperimentsOnly
     */
    void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, IndentedPrintWriter writer, boolean stoppedExperimentsOnly);

    /**
     * @param experiment
     */
    void dumpExperiment(Experiment experiment);

    /**
     * @param experiment
     * @param writer
     */
    void dumpExperiment(Experiment experiment, IndentedPrintWriter writer);
}
