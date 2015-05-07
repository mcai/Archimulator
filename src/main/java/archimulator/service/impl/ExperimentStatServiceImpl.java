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
package archimulator.service.impl;

import archimulator.model.*;
import archimulator.service.ExperimentService;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import archimulator.util.ExperimentStatHelper;
import archimulator.util.ExperimentTableHelper;
import archimulator.util.plot.Table;
import com.Ostermiller.util.SignificantFigures;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import net.pickapack.io.serialization.JsonSerializationHelper;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;
import net.pickapack.util.StorageUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Experiment stat service implementation.
 *
 * @author Min Cai
 */
public class ExperimentStatServiceImpl extends AbstractService implements ExperimentStatService {
    public static final String FILE_NAME_EXPERIMENT_STATS = "experiment_stats";

    public static final int significantFigures = 4;

    private Dao<ExperimentSummary, Long> summaries;

    private transient List<ExperimentStat> lastExperimentStats;

    /**
     * Create an experiment stat service implementation.
     */
    @SuppressWarnings("unchecked")
    public ExperimentStatServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(ExperimentSummary.class));

        this.summaries = createDao(ExperimentSummary.class);

        this.lastExperimentStats = null;
    }

    @Override
    public void initialize() {
        System.out.println("Cleaning up experiment stats and summaries..");

        ExperimentStatHelper.main(null);

        List<Experiment> experiments = ServiceManager.getExperimentService().getAllExperiments();

        List<Long> experimentIds = experiments.stream().map(Experiment::getId).collect(Collectors.toList());

        if (!experimentIds.isEmpty()) {
            File directoryExperimentStats = new File(FILE_NAME_EXPERIMENT_STATS);

            if (directoryExperimentStats.exists()) {
                Collection<File> files = FileUtils.listFiles(directoryExperimentStats, new String[]{"json"}, true);

                for (File file : files) {
                    String baseName = FilenameUtils.getBaseName(file.getAbsolutePath());
                    long storedExperimentId = Long.parseLong(baseName);

                    if (!experimentIds.contains(storedExperimentId)) {
                        clearStatsByParentId(storedExperimentId);
                    }
                }
            }

            try {
                DeleteBuilder<ExperimentSummary, Long> deleteBuilder2 = this.summaries.deleteBuilder();
                deleteBuilder2.where().notIn("parentId", experimentIds);
                PreparedDelete<ExperimentSummary> delete = deleteBuilder2.prepare();
                this.summaries.delete(delete);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Cleaned up experiment stats and summaries.");
    }

    @Override
    public void addStatsByParent(Experiment parent, List<ExperimentStat> stats) {
        if (stats.isEmpty()) {
            throw new IllegalArgumentException();
        }

        List<ExperimentStat> result = new ArrayList<>();
        result.addAll(stats);
        result.addAll(getStatsByParent(parent));

        String json = JsonSerializationHelper.serialize(
                new ExperimentStat.ExperimentStatListContainer(
                        parent.getParent().getTitle(), parent.getTitle(), result
                ));

        File file = new File(FILE_NAME_EXPERIMENT_STATS + "/" + parent.getId() + ".json");

        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException();
            }
        }

        try {
            FileUtils.writeStringToFile(file, json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        invalidateSummaryByParent(parent);

        if (lastExperimentStats != null && !lastExperimentStats.isEmpty() && lastExperimentStats.get(0).getParentId() == parent.getId()) {
            this.lastExperimentStats = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearStatsByParent(Experiment parent) {
        clearStatsByParentId(parent.getId());

        invalidateSummaryByParent(parent);
    }

    private void clearStatsByParentId(Long parentId) {
        File file = new File(FILE_NAME_EXPERIMENT_STATS + "/" + parentId + ".json");

        if (file.exists()) {
            file.delete();
        }

        if (lastExperimentStats != null && !lastExperimentStats.isEmpty() && lastExperimentStats.get(0).getParentId() == parentId) {
            this.lastExperimentStats = null;
        }
    }

    @Override
    public ExperimentStat getStatByParentAndPrefixAndKey(Experiment parent, String prefix, String key) {
        List<ExperimentStat> stats = getStatsByParent(parent);

        for (ExperimentStat stat : stats) {
            if (stat.getPrefix().equals(prefix) && stat.getKey().equals(key)) {
                return stat;
            }
        }

        return null;
    }

    @Override
    public List<ExperimentStat> getStatsByParentAndPrefixAndKeyLike(Experiment parent, String prefix, String keyLike) {
        List<ExperimentStat> stats = getStatsByParent(parent);

        List<ExperimentStat> result = new ArrayList<>();

        for (ExperimentStat stat : stats) {
            if (stat.getPrefix().equals(prefix) && stat.getKey().contains(keyLike)) {
                result.add(stat);
            }
        }

        return result;
    }

    @Override
    public List<ExperimentStat> getStatsByParentAndPrefix(Experiment parent, String prefix) {
        List<ExperimentStat> stats = getStatsByParent(parent);

        List<ExperimentStat> result = new ArrayList<>();

        for (ExperimentStat stat : stats) {
            if (stat.getPrefix().equals(prefix)) {
                result.add(stat);
            }
        }

        return result;
    }

    @Override
    public List<String> getStatPrefixesByParent(Experiment parent) {
        List<ExperimentStat> stats = getStatsByParent(parent);

        List<String> prefixes = new ArrayList<>();

        for (ExperimentStat stat : stats) {
            if (!prefixes.contains(stat.getPrefix())) {
                prefixes.add(stat.getPrefix());
            }
        }

        return prefixes;
    }

    @SuppressWarnings("unchecked")
    private List<ExperimentStat> getStatsByParent(Experiment parent) {
        if (lastExperimentStats != null && !lastExperimentStats.isEmpty() && lastExperimentStats.get(0).getParentId() == parent.getId()) {
            return this.lastExperimentStats;
        }

        File file = new File(FILE_NAME_EXPERIMENT_STATS + "/" + parent.getId() + ".json");

        if (file.exists()) {
            try {
                String json = FileUtils.readFileToString(file);
                List<ExperimentStat> stats = JsonSerializationHelper.deserialize(ExperimentStat.ExperimentStatListContainer.class, json).getStats();
                lastExperimentStats = stats;
                return stats;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new ArrayList<>();
    }

    @Override
    public ExperimentSummary getSummaryByParent(Experiment parent) {
        this.createSummaryIfNotExistsByParent(parent);
        return this.getFirstItemByParent(this.summaries, parent);
    }

    /**
     * Create the summary for the specified experiment if necessary.
     *
     * @param parent the parent experiment
     */
    private void createSummaryIfNotExistsByParent(Experiment parent) {
        if (this.getFirstItemByParent(this.summaries, parent) == null) {
            System.out.println("Creating summary for experiment #" + parent.getId() + "..");

            boolean helperThreadEnabled = parent.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

            ExperimentSummary summary = new ExperimentSummary(parent);

            ContextMapping contextMapping = parent.getContextMappings().get(0);
            Benchmark benchmark = contextMapping.getBenchmark();

            summary.setBenchmarkTitle(benchmark.getTitle());

            summary.setType(parent.getType());
            summary.setState(parent.getState());

            summary.setBeginTimeAsString(parent.getStatValue(parent.getMeasurementTitlePrefix(), "simulation/beginTimeAsString"));
            summary.setEndTimeAsString(parent.getStatValue(parent.getMeasurementTitlePrefix(), "simulation/endTimeAsString"));
            summary.setDuration(parent.getStatValue(parent.getMeasurementTitlePrefix(), "simulation/duration"));
            summary.setDurationInSeconds(parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "simulation/durationInSeconds", 0));

            summary.setL2Size(parent.getArchitecture().getL2Size());
            summary.setL2Associativity(parent.getArchitecture().getL2Associativity());
            summary.setL2ReplacementPolicyType(parent.getArchitecture().getL2ReplacementPolicyType());

            summary.setHelperThreadLookahead(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadLookahead() : -1);
            summary.setHelperThreadStride(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadStride() : -1);

            summary.setNumMainThreadWaysInStaticPartitionedLRUPolicy(parent.getArchitecture().getNumMainThreadWaysInStaticPartitionedLRUPolicy());

            summary.setNumInstructions(parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "simulation/numInstructions", 0));
            summary.setC0t0NumInstructions(parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "simulation/c0t0NumInstructions", 0));
            summary.setC1t0NumInstructions(parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "simulation/c1t0NumInstructions", 0));

            summary.setNumCycles(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "simulation/cycleAccurateEventQueue/currentCycle", 0)
            );

            summary.setIpc(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "simulation/instructionsPerCycle", 0)
            );

            summary.setC0t0Ipc(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "simulation/c0t0InstructionsPerCycle", 0)
            );

            summary.setC1t0Ipc(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "simulation/c1t0InstructionsPerCycle", 0)
            );

            summary.setCpi(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "simulation/cyclesPerInstruction", 0)
            );

            summary.setC0t0Cpi(
                    summary.getC0t0NumInstructions() == 0 ? 0 : (double) summary.getNumCycles() / summary.getC0t0NumInstructions()
            );

            summary.setC1t0Cpi(
                    summary.getC1t0NumInstructions() == 0 ? 0 : (double) summary.getNumCycles() / summary.getC1t0NumInstructions()
            );

            summary.setNumMainThreadL2Hits(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "helperThreadL2RequestProfilingHelper/numMainThreadL2Hits", 0)
            );
            summary.setNumMainThreadL2Misses(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "helperThreadL2RequestProfilingHelper/numMainThreadL2Misses", 0)
            );

            summary.setNumHelperThreadL2Hits(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "helperThreadL2RequestProfilingHelper/numHelperThreadL2Hits", 0)
            );
            summary.setNumHelperThreadL2Misses(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "helperThreadL2RequestProfilingHelper/numHelperThreadL2Misses", 0)
            );

            summary.setNumL2Evictions(
                    parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "l2/numEvictions", 0)
            );

            summary.setL2HitRatio(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "l2/hitRatio", 0)
            );

            summary.setL2OccupancyRatio(
                    parent.getStatValueAsDouble(parent.getMeasurementTitlePrefix(), "l2/occupancyRatio", 0)
            );

            summary.setL2Mpki(
                    summary.getNumInstructions() == 0 ? 0 : (double) parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(), "l2/numDownwardMisses", 0) / ((double) summary.getNumInstructions() / 1000)
            );

            summary.setC0t0L2Mpki(
                    summary.getC0t0NumInstructions() == 0 ? 0 : (double) summary.getNumMainThreadL2Misses() / ((double) summary.getC0t0NumInstructions() / 1000)
            );

            summary.setC1t0L2Mpki(
                    summary.getC1t0NumInstructions() == 0 ? 0 : (double) summary.getNumHelperThreadL2Misses() / ((double) summary.getC1t0NumInstructions() / 1000)
            );

            summary.setNumLateHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numLateHelperThreadL2Requests", 0) : 0
            );

            summary.setNumTimelyHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numTimelyHelperThreadL2Requests", 0) : 0
            );

            summary.setNumBadHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numBadHelperThreadL2Requests", 0) : 0
            );

            summary.setNumEarlyHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numEarlyHelperThreadL2Requests", 0) : 0
            );

            summary.setNumUglyHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numUglyHelperThreadL2Requests", 0) : 0
            );

            summary.setNumRedundantHitToTransientTagHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2Requests", 0) : 0
            );

            summary.setNumRedundantHitToCacheHelperThreadL2Requests(
                    helperThreadEnabled ? parent.getStatValueAsLong(parent.getMeasurementTitlePrefix(),
                            "helperThreadL2RequestProfilingHelper/numRedundantHitToCacheHelperThreadL2Requests", 0) : 0
            );

            this.addItem(this.summaries, summary);
        }
    }

    @Override
    public void invalidateSummaryByParent(Experiment parent) {
        try {
            DeleteBuilder<ExperimentSummary, Long> delete = this.summaries.deleteBuilder();
            delete.where().eq("parentId", parent.getId());
            this.summaries.delete(delete.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Table tableSummary(ExperimentPack experimentPack, final List<Experiment> experiments) {
        ExperimentTableHelper.sort(experiments);

        return new Table(Arrays.asList(
                "Id",
                "Benchmark",

                "Type",
                "State",

                "Begin Time",
                "End Time",
                "Duration",
                "Duration in Seconds",

                "Stride",
                "Lookahead",

                "L2_Size",
                "L2_Associativity",
                "L2_Replacement",
                "MT_Ways_In_Partitioned_L2",

                "Num_Instructions",
                "C0T0.Num_Instructions",
                "C1T0.Num_Instructions",

                ///////////////////////////////////////////

                "IPC",

                "CPI",
                "C0T0.CPI",
                "C1T0.CPI",

                "L2.Occupancy_Ratio",

                "L2_MPKI",

                ///////////////////////////////////////////

                "Num_Cycles",
                "Speedup",
                "C0T0.IPC",
                "C1T0.IPC",

                "MT.Hits",
                "MT.Misses",

                "HT.Hits",
                "HT.Misses",

                "L2.Evictions",
                "L2.Hit_Ratio",

                "C0T0.L2_MPKI",
                "C1T0.L2_MPKI",

                "Late",
                "Timely",
                "Bad",
                "Early",
                "Ugly",
                "Redundant_MSHR",
                "Redundant_Cache",

                "HT.Coverage",
                "HT.Accuracy",
                "HT.Lateness",
                "HT.Pollution"
        ), new ArrayList<List<String>>() {{
            for (Experiment experiment : experiments) {
                add(new ArrayList<String>() {{
                    Experiment speedupBaselineExperiment = ExperimentService.getSpeedupBaselineExperiment(experimentPack, experiment);
                    Experiment helperThreadPrefetchCoverageBaselineExperiment = ExperimentService.getHelperThreadPrefetchCoverageBaselineExperiment(experiment);

                    ExperimentSummary summary = getSummaryByParent(experiment);

                    boolean helperThreadEnabled = summary.getHelperThreadLookahead() != -1;

                    add(summary.getParentId() + "");
                    add(summary.getBenchmarkTitle());

                    add(summary.getType() + "");
                    add(summary.getState() + "");

                    add(summary.getBeginTimeAsString());
                    add(summary.getEndTimeAsString());
                    add(summary.getDuration());
                    add(summary.getDurationInSeconds() + "");

                    add(helperThreadEnabled ? "S=" + summary.getHelperThreadStride() + "" : "");
                    add(helperThreadEnabled ? "L=" + summary.getHelperThreadLookahead() + "" : "");

                    add(StorageUnit.toString(summary.getL2Size()).replaceAll(" ", ""));
                    add(summary.getL2Associativity() + "way");
                    add(summary.getL2ReplacementPolicyType() + "");
                    add("P=" + summary.getNumMainThreadWaysInStaticPartitionedLRUPolicy() + "");

                    add(summary.getNumInstructions() + "");
                    add(summary.getC0t0NumInstructions() + "");
                    add(summary.getC1t0NumInstructions() + "");

                    ///////////////////////////////////////////

                    add(SignificantFigures.format(summary.getIpc(), significantFigures));

                    add(SignificantFigures.format(summary.getCpi(), significantFigures));
                    add(SignificantFigures.format(summary.getC0t0Cpi(), significantFigures));
                    add(SignificantFigures.format(summary.getC1t0Cpi(), significantFigures));

                    add(SignificantFigures.format(summary.getL2OccupancyRatio(), significantFigures));

                    add(SignificantFigures.format(summary.getL2Mpki(), significantFigures));

                    ///////////////////////////////////////////

                    add(summary.getNumCycles() + "");

                    long numCyclesInBaselineExperiment = speedupBaselineExperiment == null ? 0 : getSummaryByParent(speedupBaselineExperiment).getNumCycles();
                    double speedup = numCyclesInBaselineExperiment == 0 ? 0 : (double) numCyclesInBaselineExperiment / summary.getNumCycles();
                    add(SignificantFigures.format(speedup, significantFigures));

                    add(SignificantFigures.format(summary.getC0t0Ipc(), significantFigures));
                    add(SignificantFigures.format(summary.getC1t0Ipc(), significantFigures));

                    add(summary.getNumMainThreadL2Hits() + "");
                    add(summary.getNumMainThreadL2Misses() + "");

                    add(summary.getNumHelperThreadL2Hits() + "");
                    add(summary.getNumHelperThreadL2Misses() + "");

                    add(summary.getNumL2Evictions() + "");
                    add(SignificantFigures.format(summary.getL2HitRatio(), significantFigures));

                    add(SignificantFigures.format(summary.getC0t0L2Mpki(), significantFigures));
                    add(SignificantFigures.format(summary.getC1t0L2Mpki(), significantFigures));

                    add(summary.getNumLateHelperThreadL2Requests() + "");
                    add(summary.getNumTimelyHelperThreadL2Requests() + "");
                    add(summary.getNumBadHelperThreadL2Requests() + "");
                    add(summary.getNumEarlyHelperThreadL2Requests() + "");
                    add(summary.getNumUglyHelperThreadL2Requests() + "");
                    add(summary.getNumRedundantHitToTransientTagHelperThreadL2Requests() + "");
                    add(summary.getNumRedundantHitToCacheHelperThreadL2Requests() + "");

                    long numMainThreadL2MissesInBaselineExperiment = helperThreadPrefetchCoverageBaselineExperiment == null ? 0 : getSummaryByParent(helperThreadPrefetchCoverageBaselineExperiment).getNumMainThreadL2Misses();

                    add(SignificantFigures.format(summary.getHelperThreadL2RequestCoverage(numMainThreadL2MissesInBaselineExperiment), significantFigures));
                    add(SignificantFigures.format(summary.getHelperThreadL2RequestAccuracy(), significantFigures));
                    add(SignificantFigures.format(summary.getHelperThreadL2RequestLateness(), significantFigures));
                    add(SignificantFigures.format(summary.getHelperThreadL2RequestPollution(), significantFigures));
                }});
            }
        }});
    }
}
