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
import archimulator.model.ExperimentPack;
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.model.metric.MultiBarPlot;
import archimulator.model.metric.Table;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import net.pickapack.Pair;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;
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
    private Dao<ExperimentStat, Long> stats;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public ExperimentStatServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends ModelElement>>asList(ExperimentStat.class));

        this.stats = createDao(ExperimentStat.class);
    }

    @Override
    public void addStats(final List<ExperimentStat> stats) {
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

        addItems(ExperimentStatServiceImpl.this.stats, stats);
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
    public List<ExperimentStat> getStatsByParentAndGauge(Experiment parent, ExperimentGauge gauge) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .and()
                    .eq("gaugeId", gauge.getId())
                    .prepare();
            return this.stats.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
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

        return new Table(title, columns, rows);
    }

    @Override
    public Table tableSummary2(String title, Experiment baselineExperiment, List<Experiment> experiments) {
        final boolean helperThreadEnabled = baselineExperiment != null && baselineExperiment.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

        List<String> columns = new ArrayList<String>(){{
            addAll(Arrays.asList("Experiment", "L2 Size In KB", "L2 Assoc", "L2 Repl"));

            if(helperThreadEnabled) {
                addAll(Arrays.asList("Lookahead", "Stride"));
            }

            addAll(Arrays.asList("Total Cycles", "Speedup"));

            addAll(Arrays.asList("MT Hits", "MT Misses"));

            if(helperThreadEnabled) {
                addAll(Arrays.asList("HT Hits", "HT Misses"));
            }

            addAll(Arrays.asList("L2 Evictions"));

            if(helperThreadEnabled) {
                addAll(Arrays.asList("HT Coverage", "HT Accuracy", "Late", "Timely", "Bad", "Ugly", "Redundant MSHR", "Redundant Cache"));
            }
        }};

        List<List<String>> rows = new ArrayList<List<String>>();

        for (Experiment experiment : experiments) {
            Map<String, ExperimentStat> statsMap = ExperimentStat.toMap(ServiceManager.getExperimentStatService().getStatsByParent(experiment));

            List<String> row = new ArrayList<String>();

            row.add("exp#" + experiment.getId());

            row.add(StorageUnit.KILOBYTE.getValue(experiment.getArchitecture().getL2Size()) + "KB");

            row.add(experiment.getArchitecture().getL2Associativity() + "way");
            row.add(experiment.getArchitecture().getL2ReplacementPolicyType() + "");

            if (helperThreadEnabled) {
                row.add(experiment.getContextMappings().get(0).getHelperThreadLookahead() + "");
                row.add(experiment.getContextMappings().get(0).getHelperThreadStride() + "");
            }

            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));

            row.add(String.format("%.4f", getSpeedup(baselineExperiment, experiment)));

            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));

            if (helperThreadEnabled) {
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));
            }

            row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                    "l2/numEvictions"));

            if (helperThreadEnabled) {
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));

                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(statsMap, experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests"));
            }

            rows.add(row);
        }

        return new Table(title, columns, rows);
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
        long baselineTotalCycles = Long.parseLong(baselineExperiment.getStatValue(baselineExperiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));
        long totalCycles = Long.parseLong(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));
        return (double) baselineTotalCycles / totalCycles;
    }

    /**
     * @param experimentPack
     * @param baselineExperiment
     * @param experiments
     */
    @Override
    public MultiBarPlot plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Normalized L2 Downward Read Misses", experiments, getNormalizedNumL2DownwardReadMisses(experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Main Thread L2 Cache Hits", experiments, getNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Main Thread L2 Cache Misses", experiments, getNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Helper Thread L2 Cache Hits", experiments, getNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Helper Thread L2 Cache Misses", experiments, getNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Normalized Main Thread L2 Cache Hits", experiments, getNormalizedNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Normalized Main Thread L2 Cache Misses", experiments, getNormalizedNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Normalized Helper Thread L2 Cache Hits", experiments, getNormalizedNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("# Normalized Helper Thread L2 Cache Misses", experiments, getNormalizedNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return transform(experiments, new Function1<Experiment, Double>() {
            @Override
            public Double apply(Experiment experiment) {
                return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));
            }
        });
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     * @return
     */
    @Override
    public List<Double> getNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return normalize(getHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("Helper Thread L2 Cache Request Coverage", experiments, getHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("Helper Thread L2 Cache Request Accuracy", experiments, getHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("Normalized Helper Thread L2 Cache Request Coverage", experiments, getNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        return singleBarPlot("Normalized Helper Thread L2 Cache Request Accuracy", experiments, getNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, final List<Experiment> experiments) {
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
     * @param experimentPack
     * @param experiments
     */
    @Override
    public MultiBarPlot plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments) {
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

    /**
     * @param variablePropertyName
     * @return
     */
    public static String getDescriptionOfVariablePropertyName(String variablePropertyName) {
        return variablePropertyNameDescriptions.containsKey(variablePropertyName) ? variablePropertyNameDescriptions.get(variablePropertyName) : variablePropertyName;
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
