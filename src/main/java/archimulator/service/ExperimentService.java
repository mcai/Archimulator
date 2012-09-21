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

import archimulator.client.ExperimentPack;
import archimulator.model.Architecture;
import archimulator.model.Experiment;
import archimulator.model.SimulatedProgram;
import net.pickapack.action.Function1;
import net.pickapack.service.Service;
import net.pickapack.util.IndentedPrintWriter;

import java.util.List;
import java.util.Map;

public interface ExperimentService extends Service {
    List<Experiment> getAllExperiments();

    List<Experiment> getAllExperiments(long first, long count);

    long getNumAllExperiments();

    Experiment getExperimentById(long id);

    List<Experiment> getExperimentsByTitle(String title);

    List<Experiment> getExperimentsByTitlePrefix(String titlePrefix, boolean stoppedExperimentsOnly);

    Experiment getFirstExperimentByTitle(String title);

    Experiment getLatestExperimentByTitle(String title);

    List<Experiment> getExperimentsBySimulatedProgram(SimulatedProgram simulatedProgram);

    List<Experiment> getExperimentsByArchitecture(Architecture architecture);

    void addExperiment(Experiment experiment);

    void removeExperimentById(long id);

    void updateExperiment(Experiment experiment);

    void dumpExperiment(Experiment experiment);

    void dumpExperiment(Experiment experiment, IndentedPrintWriter writer);

    Experiment getFirstExperimentToRun();

    List<Experiment> getStoppedExperimentsByExperimentPack(ExperimentPack experimentPack);

    Experiment getFirstStoppedExperimentByExperimentPack(ExperimentPack experimentPack);

    List<ExperimentPack> getAllExperimentPacks();

    ExperimentPack getExperimentPackByTitle(String title);

    List<ExperimentPack> getExperimentPacksByTitlePrefix(String titlePrefix);

    void runExperimentPackByTitle(String experimentPackTitle);

    void runExperimentByTitle(String experimentTitle);

    void tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments);

    List<Map<String, Double>> getBreakdowns(List<Experiment> experiments, Function1<Experiment, List<String>> keysFunction);

    Map<String, Double> getBreakdown(Experiment experiment, Function1<Experiment, List<String>> keysFunction);

    List<Double> getSpeedups(Experiment baselineExperiment, List<Experiment> experiments);

    double getSpeedup(Experiment baselineExperiment, Experiment experiment);

    void plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments);

    List<Double> getTotalInstructions(List<Experiment> experiments);

    void plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedTotalInstructions(List<Experiment> experiments);

    void plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getTotalCycles(List<Experiment> experiments);

    void plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedTotalCycles(List<Experiment> experiments);

    void plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments);

    void plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments);

    void plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments);

    void plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments);

    void plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments);

    void plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments);

    void plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);

    List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments);

    void plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments);

    void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, boolean stoppedExperimentsOnly);

    void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, IndentedPrintWriter writer, boolean stoppedExperimentsOnly);

    void start();
}
