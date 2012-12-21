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
package archimulator.service.impl;

import archimulator.model.Experiment;
import archimulator.model.ExperimentSummary;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.util.plot.MultiBarPlot;
import archimulator.util.plot.Table;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import net.pickapack.Pair;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;
import net.pickapack.util.CollectionHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.sql.SQLException;
import java.util.*;

import static net.pickapack.util.CollectionHelper.toMap;
import static net.pickapack.util.CollectionHelper.transform;

/**
 * @author Min Cai
 */
public class ExperimentStatServiceImpl extends AbstractService implements ExperimentStatService {
    private static Map<String, String> variablePropertyNameDescriptions;

    static {
        variablePropertyNameDescriptions = new LinkedHashMap<String, String>();

        variablePropertyNameDescriptions.put("benchmarkTitle", "Benchmark Title");
        variablePropertyNameDescriptions.put("benchmarkArguments", "Benchmark Arguments");
        variablePropertyNameDescriptions.put("helperThreadLookahead", "Helper Thread Lookahead");
        variablePropertyNameDescriptions.put("helperThreadStride", "Helper Thread Stride");
        variablePropertyNameDescriptions.put("numCores", "Number of Cores");
        variablePropertyNameDescriptions.put("numThreadsPerCore", "Number of Threads per Core");
        variablePropertyNameDescriptions.put("l1ISize", "L1I Size");
        variablePropertyNameDescriptions.put("l1IAssociativity", "L1I Associativity");
        variablePropertyNameDescriptions.put("l1DSize", "L1D Size");
        variablePropertyNameDescriptions.put("l1DAssociativity", "L1D Associativity");
        variablePropertyNameDescriptions.put("l2Size", "L2 Size");
        variablePropertyNameDescriptions.put("l2Associativity", "L2 Associativity");
        variablePropertyNameDescriptions.put("l2ReplacementPolicyType", "L2 Replacement Policy Type");
    }

    private Dao<ExperimentStat, Long> stats;
    private Dao<ExperimentSummary, Long> summaries;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public ExperimentStatServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(ExperimentStat.class, ExperimentSummary.class));

        this.stats = createDao(ExperimentStat.class);
        this.summaries = createDao(ExperimentSummary.class);
    }

    @Override
    public void initialize() {
        System.out.println("Cleaning up experiment stats and summaries..");

        List<Experiment> experiments = ServiceManager.getExperimentService().getAllExperiments();

        List<Long> experimentIds = CollectionHelper.transform(experiments, new Function1<Experiment, Long>() {
            @Override
            public Long apply(Experiment experiment) {
                return experiment.getId();
            }
        });

        try {
            DeleteBuilder<ExperimentStat, Long> deleteBuilder = this.stats.deleteBuilder();
            deleteBuilder.where().notIn("parentId", experimentIds);
            PreparedDelete<ExperimentStat> delete = deleteBuilder.prepare();
            this.stats.delete(delete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            DeleteBuilder<ExperimentSummary, Long> deleteBuilder2 = this.summaries.deleteBuilder();
            deleteBuilder2.where().notIn("parentId", experimentIds);
            PreparedDelete<ExperimentSummary> delete = deleteBuilder2.prepare();
            this.summaries.delete(delete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("There are " + experiments.size() + " experiments.");

        for(Experiment experiment : experiments) {
            this.CreateSummaryIfNotExistsByParent(experiment);
        }

        System.out.println("Cleaned up experiment stats and summaries.");
    }

    /**
     * @param variablePropertyName
     * @return
     */
    public static String getDescriptionOfVariablePropertyName(String variablePropertyName) {
        return variablePropertyNameDescriptions.containsKey(variablePropertyName) ? variablePropertyNameDescriptions.get(variablePropertyName) : variablePropertyName;
    }

    @Override
    public void addStatsByParent(Experiment parent, List<ExperimentStat> stats) {
        if (stats.isEmpty()) {
            throw new IllegalArgumentException();
        }

        long parentId = stats.get(0).getParentId();
        String prefix = stats.get(0).getPrefix();

        try {
            DeleteBuilder<ExperimentStat, Long> deleteBuilder = this.stats.deleteBuilder();
            deleteBuilder.where().eq("parentId", parentId).and().eq("prefix", prefix);
            PreparedDelete<ExperimentStat> delete = deleteBuilder.prepare();
            this.stats.delete(delete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        addItems(this.stats, stats);

        invalidateSummaryByParent(parent);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void clearStatsByParent(Experiment parent) {
        try {
            DeleteBuilder<ExperimentStat, Long> deleteBuilder = this.stats.deleteBuilder();
            deleteBuilder.where().eq("parentId", parent.getId());
            PreparedDelete<ExperimentStat> delete = deleteBuilder.prepare();
            this.stats.delete(delete);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        invalidateSummaryByParent(parent);
    }

    @Override
    public List<ExperimentStat> getStatsByParent(Experiment parent) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .prepare();
            return this.stats.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExperimentStat getStatByParentAndTitle(Experiment parent, String title) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .and()
                    .eq("title", title)
                    .prepare();
            return this.stats.queryForFirst(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentStat> getStatsByParentAndTitleLike(Experiment parent, String titleLike) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .and()
                    .like("title", "%" + titleLike + "%")
                    .prepare();
            return this.stats.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentStat> getStatsByParentAndPrefixAndGauge(Experiment parent, String prefix, ExperimentGauge gauge) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .and()
                    .eq("prefix", prefix)
                    .and()
                    .eq("gaugeId", gauge.getId())
                    .prepare();
            return this.stats.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getStatPrefixesByParent(Experiment parent) {
        try {
            QueryBuilder<ExperimentStat, Long> queryBuilder = this.stats.queryBuilder();
            queryBuilder.where().eq("parentId", parent.getId());
            PreparedQuery<ExperimentStat> query = queryBuilder.distinct().selectColumns("prefix").prepare();
            return CollectionHelper.transform(this.stats.query(query), new Function1<ExperimentStat, String>() {
                @Override
                public String apply(ExperimentStat stat) {
                    return stat.getPrefix();
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ExperimentSummary getSummaryByParent(Experiment parent) {
        this.CreateSummaryIfNotExistsByParent(parent);
        return this.getFirstItemByParent(this.summaries, parent);
    }

    private void CreateSummaryIfNotExistsByParent(Experiment parent) {
        if(this.getFirstItemByParent(this.summaries, parent) == null) {
            System.out.println("Creating summary for experiment #" + parent.getId() + "..");

            Map<String, ExperimentStat> statsMap = ExperimentStat.toMap(ServiceManager.getExperimentStatService().getStatsByParent(parent));

            boolean helperThreadEnabled = parent.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

            ExperimentSummary summary = new ExperimentSummary(parent);

            summary.setType(parent.getType());
            summary.setState(parent.getState());

            summary.setL2Size(parent.getArchitecture().getL2Size());
            summary.setL2Associativity(parent.getArchitecture().getL2Associativity());
            summary.setL2ReplacementPolicyType(parent.getArchitecture().getL2ReplacementPolicyType());

            summary.setHelperThreadLookahead(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadLookahead() : -1);
            summary.setHelperThreadStride(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadStride() : -1);

            summary.setTotalInstructions(parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/totalInstructions", 0));

            summary.setTotalCycles(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle", 0)
            );

            summary.setIpc(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "simulation/instructionsPerCycle", 0)
            );

            summary.setCpi(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "simulation/cyclesPerInstruction", 0)
            );

            summary.setNumMainThreadL2CacheHits(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits", 0)
            );
            summary.setNumMainThreadL2CacheMisses(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses", 0)
            );

            summary.setNumHelperThreadL2CacheHits(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits", 0)
            );
            summary.setNumHelperThreadL2CacheMisses(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses", 0)
            );

            summary.setNumL2CacheEvictions(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "l2/numEvictions", 0)
            );

            summary.setL2CacheHitRatio(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "l2/hitRatio", 0)
            );

            summary.setL2CacheOccupancyRatio(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "l2/occupancyRatio", 0)
            );

            summary.setHelperThreadL2CacheRequestCoverage(
                    helperThreadEnabled ? parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage", 0.0f) : 0.0f
            );

            summary.setHelperThreadL2CacheRequestAccuracy(
                    helperThreadEnabled ? parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy", 0.0f) : 0.0f
            );

            summary.setNumLateHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests", 0) : 0
            );

            summary.setNumTimelyHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests", 0) : 0
            );

            summary.setNumBadHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests", 0) : 0
            );

            summary.setNumUglyHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests", 0) : 0
            );

            summary.setNumRedundantHitToTransientTagHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests", 0) : 0
            );

            summary.setNumRedundantHitToCacheHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests", 0) : 0
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
    @Deprecated
    public Table tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments) {
        boolean helperThreadEnabled = baselineExperiment != null && baselineExperiment.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

        List<String> columns = helperThreadEnabled ? Arrays.asList(
                "Experiment", "L2 Size", "L2 Assoc", "L2 Repl",
                "Lookahead", "Stride",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "L2 Downward Read MPKI", "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio", "Helper Thread Hit", "Helper Thread Miss", "Helper Thread Coverage", "Helper Thread Accuracy", "Redundant MSHR", "Redundant Cache", "Timely", "Late", "Bad", "Ugly"
        ) : Arrays.asList(
                "Experiment", "L2 Size", "L2 Assoc", "L2 Repl",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "L2 Downward Read MPKI", "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio"
        );

        List<List<String>> rows = new ArrayList<List<String>>();

        for (Experiment experiment : experiments) {
            Map<String, ExperimentStat> statsMap = ExperimentStat.toMap(ServiceManager.getExperimentStatService().getStatsByParent(experiment));

            List<String> row = new ArrayList<String>();

            row.add("exp#" + experiment.getId());

            row.add(StorageUnit.toString(experiment.getArchitecture().getL2Size()));
//            row.add(experiment.getArchitecture().getL2Size() + "");

            row.add(experiment.getArchitecture().getL2Associativity() + "");
            row.add(experiment.getArchitecture().getL2ReplacementPolicyType() + "");

            if (helperThreadEnabled) {
                row.add(experiment.getContextMappings().get(0).getHelperThreadLookahead() + "");
                row.add(experiment.getContextMappings().get(0).getHelperThreadStride() + "");
            }

            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));

            row.add(String.format("%.4f", getSpeedup(baselineExperiment, experiment)));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "simulation/instructionsPerCycle"))));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "simulation/cyclesPerInstruction"))));

            long numL2DownwardReadMisses = Long.parseLong(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() + "l2/numDownwardReadMisses"));
            long totalInstructions = Long.parseLong(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() + "simulation/totalInstructions"));

            row.add(String.format("%.4f", (double) numL2DownwardReadMisses / (totalInstructions / FileUtils.ONE_KB)));

            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "l2/hitRatio"))));
            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "l2/numEvictions"));
            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "l2/occupancyRatio"))));

            if (helperThreadEnabled) {
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));

                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));

                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests"));
            }

            rows.add(row);
        }

        return new Table(columns, rows);
    }

    @Override
    public Table tableSummary2(final List<Experiment> experiments) {
        return new Table(Arrays.asList(
                "Id",

                "Type",
                "State",

                "L2 Size",
                "L2 Associativity",
                "L2 Replacement",

                "Lookahead",
                "Stride",

                "Total Instructions",
                "Total Cycles",

                "IPC",
                "CPI",

                "MT Hits",
                "MT Misses",

                "HT Hits",
                "HT Misses",

                "L2 Evictions",
                "L2 Hit Ratio",
                "L2 Occupancy Ratio",

                "HT Coverage",
                "HT Accuracy",

                "Late",
                "Timely",
                "Bad",
                "Ugly",
                "Redundant MSHR",
                "Redundant Cache"
        ), new ArrayList<List<String>>() {{
            for (Experiment experiment : experiments) {
                add(getSummaryByParent(experiment).tableSummary2Row());
            }
        }});
    }

    /**
     * @param experiments
     * @param keysFunction
     * @return
     */
    @Override
    public List<Map<String, Double>> getBreakdowns(List<Experiment> experiments, final Function1<Experiment, List<String>> keysFunction) {
        return transform(experiments, new Function1<Experiment, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Experiment experiment) {
                return getBreakdown(experiment, keysFunction);
            }
        });
    }

    /**
     * @param experiment
     * @param keysFunction
     * @return
     */
    @Override
    public Map<String, Double> getBreakdown(Experiment experiment, Function1<Experiment, List<String>> keysFunction) {
        final List<String> keys = keysFunction.apply(experiment);
        return toMap(transform(experiment.getStatValues(keys), new Function2<Integer, String, Pair<String, Double>>() {
            @Override
            public Pair<String, Double> apply(Integer index, String param) {
                return new Pair<String, Double>(keys.get(index), Double.parseDouble(param));
            }
        }));
    }

    /**
     * @param baselineExperiment
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getSpeedups(Experiment baselineExperiment, List<Experiment> experiments) {
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStatValue(baselineExperiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));

        List<Double> speedups = new ArrayList<Double>();

        for (Experiment experiment : experiments) {
            long totalCycles = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));
            speedups.add((double) baselineTotalCycles / totalCycles);
        }

        return speedups;
    }

    /**
     * @param baselineExperiment
     * @param experiment
     * @return
     */
    @Override
    public double getSpeedup(Experiment baselineExperiment, Experiment experiment) {
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStatValue(baselineExperiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle", "0"));
        long totalCycles = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle", "0"));
        return totalCycles == 0L ? 0.0f : (double) baselineTotalCycles / totalCycles;
    }

    /**
     * @param baselineExperiment
     * @param experiments
     */
    @Override
    public MultiBarPlot plotSpeedups(Experiment baselineExperiment, List<Experiment> experiments) {
        return singleBarPlot("Speedups", experiments, getSpeedups(baselineExperiment, experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getTotalInstructions(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/totalInstructions"));
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotTotalInstructions(List<Experiment> experiments) {
        return singleBarPlot("Total Instructions", experiments, getTotalCycles(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedTotalInstructions(List<Experiment> experiments) {
        return normalize(getTotalInstructions(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedTotalInstructions(List<Experiment> experiments) {
        return singleBarPlot("Normalized Total Instructions", experiments, getNormalizedTotalCycles(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getTotalCycles(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotTotalCycles(List<Experiment> experiments) {
        return singleBarPlot("Total Cycles", experiments, getTotalCycles(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedTotalCycles(List<Experiment> experiments) {
        return normalize(getTotalCycles(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedTotalCycles(List<Experiment> experiments) {
        return singleBarPlot("Normalized Total Cycles", experiments, getNormalizedTotalCycles(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumL2DownwardReadMisses(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "l2/numDownwardReadMisses"));
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumL2DownwardReadMisses(List<Experiment> experiments) {
        return singleBarPlot("# L2 Downward Read Misses", experiments, getNumL2DownwardReadMisses(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumL2DownwardReadMisses(List<Experiment> experiments) {
        return normalize(getNumL2DownwardReadMisses(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumL2DownwardReadMisses(List<Experiment> experiments) {
        return singleBarPlot("# Normalized L2 Downward Read Misses", experiments, getNormalizedNumL2DownwardReadMisses(experiments));
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumMainThreadL2CacheHits(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            }
        });
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumMainThreadL2CacheMisses(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));
            }
        });
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumHelperThreadL2CacheHits(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
            }
        });
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumHelperThreadL2CacheMisses(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));
            }
        });
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheHits(List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheHits(experiments));
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheMisses(List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheMisses(experiments));
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheHits(List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheHits(experiments));
    }

    /**
     *
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheMisses(List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheMisses(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumMainThreadL2CacheHits(List<Experiment> experiments) {
        return singleBarPlot("# Main Thread L2 Cache Hits", experiments, getNumMainThreadL2CacheHits(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumMainThreadL2CacheMisses(List<Experiment> experiments) {
        return singleBarPlot("# Main Thread L2 Cache Misses", experiments, getNumMainThreadL2CacheMisses(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumHelperThreadL2CacheHits(List<Experiment> experiments) {
        return singleBarPlot("# Helper Thread L2 Cache Hits", experiments, getNumHelperThreadL2CacheHits(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumHelperThreadL2CacheMisses(List<Experiment> experiments) {
        return singleBarPlot("# Helper Thread L2 Cache Misses", experiments, getNumHelperThreadL2CacheMisses(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumMainThreadL2CacheHits(List<Experiment> experiments) {
        return singleBarPlot("# Normalized Main Thread L2 Cache Hits", experiments, getNormalizedNumMainThreadL2CacheHits(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumMainThreadL2CacheMisses(List<Experiment> experiments) {
        return singleBarPlot("# Normalized Main Thread L2 Cache Misses", experiments, getNormalizedNumMainThreadL2CacheMisses(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumHelperThreadL2CacheHits(List<Experiment> experiments) {
        return singleBarPlot("# Normalized Helper Thread L2 Cache Hits", experiments, getNormalizedNumHelperThreadL2CacheHits(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumHelperThreadL2CacheMisses(List<Experiment> experiments) {
        return singleBarPlot("# Normalized Helper Thread L2 Cache Misses", experiments, getNormalizedNumHelperThreadL2CacheMisses(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getHelperThreadL2CacheRequestCoverage(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
            }
        });
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));
            }
        });
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestCoverage(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestAccuracy(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestCoverage(List<Experiment> experiments) {
        return singleBarPlot("Helper Thread L2 Cache Request Coverage", experiments, getHelperThreadL2CacheRequestCoverage(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments) {
        return singleBarPlot("Helper Thread L2 Cache Request Accuracy", experiments, getHelperThreadL2CacheRequestAccuracy(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedHelperThreadL2CacheRequestCoverage(List<Experiment> experiments) {
        return singleBarPlot("Normalized Helper Thread L2 Cache Request Coverage", experiments, getNormalizedHelperThreadL2CacheRequestCoverage(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedHelperThreadL2CacheRequestAccuracy(List<Experiment> experiments) {
        return singleBarPlot("Normalized Helper Thread L2 Cache Request Accuracy", experiments, getNormalizedHelperThreadL2CacheRequestAccuracy(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getL2DownwardReadMPKIs(List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                long numL2DownwardReadMisses = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "l2/numDownwardReadMisses"));
                long totalInstructions = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/totalInstructions"));
                return (double) numL2DownwardReadMisses / (totalInstructions / FileUtils.ONE_KB);
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotL2DownwardReadMPKIs(List<Experiment> experiments) {
        return singleBarPlot("# L2 Downward Read MPKIs", experiments, getL2DownwardReadMPKIs(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedL2DownwardReadMPKIs(List<Experiment> experiments) {
        return normalize(getL2DownwardReadMPKIs(experiments));
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedL2DownwardReadMPKIs(List<Experiment> experiments) {
        return singleBarPlot("# Normalized L2 Downward Read MPKIs", experiments, getNormalizedL2DownwardReadMPKIs(experiments));
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Map<String, Double>> getHelperThreadL2CacheRequestBreakdowns(List<Experiment> experiments) {
        return getBreakdowns(experiments, new Function1<Experiment, List<String>>() {
            @Override
            public List<String> apply(Experiment experiment) {
                return Arrays.asList(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests");
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestBreakdowns(final List<Experiment> experiments) {
        List<Map<String, Double>> breakdowns = getHelperThreadL2CacheRequestBreakdowns(experiments);
        final List<Map<String, Double>> transformedBreakdowns = transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Map<String, Double> input) {
                Map<String, Double> output = new LinkedHashMap<String, Double>();

                int i = 0;
                for (String key : input.keySet()) {
                    switch (i++) {
                        case 0:
                            output.put("Redundant MSHR", input.get(key));
                            break;
                        case 1:
                            output.put("Redundant Cache", input.get(key));
                            break;
                        case 2:
                            output.put("Timely", input.get(key));
                            break;
                        case 3:
                            output.put("Late", input.get(key));
                            break;
                        case 4:
                            output.put("Bad", input.get(key));
                            break;
                        case 5:
                            output.put("Ugly", input.get(key));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                return output;
            }
        });
        return multiBarPlot("# Helper Thread L2 Request Breakdowns", experiments, transformedBreakdowns);
    }

    /**
     * @param experiments
     * @return
     */
    @Override
    public List<Map<String, Double>> getL2CacheRequestBreakdowns(List<Experiment> experiments) {
        return getBreakdowns(experiments, new Function1<Experiment, List<String>>() {
            @Override
            public List<String> apply(Experiment experiment) {
                return Arrays.asList(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits",
                        experiment.getMeasurementTitlePrefix() +
                                "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses");
            }
        });
    }

    /**
     * @param experiments
     */
    @Override
    public MultiBarPlot plotL2CacheRequestBreakdowns(List<Experiment> experiments) {
        List<Map<String, Double>> breakdowns = getL2CacheRequestBreakdowns(experiments);
        List<Map<String, Double>> transformedBreakdowns = transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
            @Override
            public Map<String, Double> apply(Map<String, Double> input) {
                Map<String, Double> output = new LinkedHashMap<String, Double>();

                int i = 0;
                for (String key : input.keySet()) {
                    switch (i++) {
                        case 0:
                            output.put("Main Thread L2 Hits", input.get(key));
                            break;
                        case 1:
                            output.put("Main Thread L2 Misses", input.get(key));
                            break;
                        case 2:
                            output.put("Helper Thread L2 Hits", input.get(key));
                            break;
                        case 3:
                            output.put("Helper Thread L2 Misses", input.get(key));
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

                return output;
            }
        });
        return multiBarPlot("# L2 Request Breakdowns", experiments, transformedBreakdowns);
    }

    private MultiBarPlot multiBarPlot(String title, List<Experiment> experiments, final List<Map<String, Double>> transformedBreakdowns) {
        final List<String> titles = new ArrayList<String>(transformedBreakdowns.get(0).keySet());

        List<MultiBarPlot.Bar> subCharts = new ArrayList<MultiBarPlot.Bar>();

        for (int k = 0; k < titles.size(); k++) {
            List<MultiBarPlot.XY> values = new ArrayList<MultiBarPlot.XY>();

            for (int i = 0; i < experiments.size(); i++) {
                values.add(new MultiBarPlot.XY("exp#" + experiments.get(i).getId(), new ArrayList<List<Double>>() {{
                    for (String key : titles) {
                        List<Double> rows = new ArrayList<Double>();

                        for (Map<String, Double> transformedBreakdown : transformedBreakdowns) {
                            rows.add(transformedBreakdown.get(key));
                        }

                        add(rows);
                    }
                }}.get(k).get(i)));
            }

            subCharts.add(new MultiBarPlot.Bar(titles.get(k), values.toArray(new MultiBarPlot.XY[values.size()])));
        }

        return new MultiBarPlot(title, subCharts.toArray(new MultiBarPlot.Bar[subCharts.size()]));
    }

    private MultiBarPlot singleBarPlot(String title, List<Experiment> experiments, List<Double> rows) {
        List<MultiBarPlot.Bar> subCharts = new ArrayList<MultiBarPlot.Bar>();

        List<MultiBarPlot.XY> values = new ArrayList<MultiBarPlot.XY>();

        for (int i = 0; i < experiments.size(); i++) {
            values.add(new MultiBarPlot.XY("exp#" + experiments.get(i).getId(), rows.get(i)));
        }

        subCharts.add(new MultiBarPlot.Bar(title, values.toArray(new MultiBarPlot.XY[values.size()])));

        return new MultiBarPlot(title, subCharts.toArray(new MultiBarPlot.Bar[subCharts.size()]));
    }

    private List<Double> normalize(List<Double> input) {
        double[] resultArray = StatUtils.normalize(ArrayUtils.toPrimitive(input.toArray(new Double[input.size()])));
        List<Double> result = new ArrayList<Double>();
        for (double d : resultArray) {
            result.add(d);
        }

        return result;
    }
}
