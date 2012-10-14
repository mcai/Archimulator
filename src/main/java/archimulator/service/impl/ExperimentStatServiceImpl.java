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
import archimulator.model.ExperimentType;
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.model.metric.Table;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import net.pickapack.JsonSerializationHelper;
import net.pickapack.Pair;
import net.pickapack.StorageUnit;
import net.pickapack.action.Function1;
import net.pickapack.action.Function2;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.io.cmd.CommandLineHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;
import net.pickapack.util.IndentedPrintWriter;
import net.pickapack.util.JaxenHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.StatUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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


    /**
     * @param title
     * @param baselineExperiment
     * @param experiments
     */
    @Override
    public Table tableSummary(String title, Experiment baselineExperiment, List<Experiment> experiments) {
        boolean helperThreadEnabled = baselineExperiment.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

        List<String> columns = helperThreadEnabled ? Arrays.asList(
                "L2 Size", "L2 Assoc", "L2 Repl",
                "Lookahead", "Stride",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio", "Helper Thread Hit", "Helper Thread Miss", "Helper Thread Coverage", "Helper Thread Accuracy", "Redundant MSHR", "Redundant Cache", "Timely", "Late", "Bad", "Ugly"
        ) : Arrays.asList(
                "L2 Size", "L2 Assoc", "L2 Repl",
                "Total Cycles", "Speedup", "IPC", "CPI",
                "Main Thread Hit", "Main Thread Miss", "L2 Hit Ratio", "L2 Evictions", "L2 Occupancy Ratio"
        );

        List<List<String>> rows = new ArrayList<List<String>>();

        for (Experiment experiment : experiments) {
            List<String> row = new ArrayList<String>();

            row.add(StorageUnit.toString(experiment.getArchitecture().getL2Size()));
//            row.add(experiment.getArchitecture().getL2Size() + "");

            row.add(experiment.getArchitecture().getL2Associativity() + "");
            row.add(experiment.getArchitecture().getL2ReplacementPolicyType() + "");

            if (helperThreadEnabled) {
                row.add(experiment.getContextMappings().get(0).getHelperThreadLookahead() + "");
                row.add(experiment.getContextMappings().get(0).getHelperThreadStride() + "");
            }

            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle"));

            row.add(String.format("%.4f", getSpeedup(baselineExperiment, experiment)));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "simulation/instructionsPerCycle"))));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "simulation/cyclesPerInstruction"))));

            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits"));
            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses"));

            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "l2/hitRatio"))));
            row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "l2/numEvictions"));
            row.add(String.format("%.4f", Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                    "l2/occupancyRatio"))));

            if (helperThreadEnabled) {
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses"));

                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy"));

                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests"));
                row.add(experiment.getStatValue(experiment.getMeasurementTitlePrefix() +
                        "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests"));
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
    public void plotSpeedups(ExperimentPack experimentPack, Experiment baselineExperiment, List<Experiment> experiments) {
        plot(experimentPack, "speedups", "Speedups", getSpeedups(baselineExperiment, experiments));
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
    public void plotTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalInstructions", "Total Instructions", getTotalCycles(experiments));
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
    public void plotNormalizedTotalInstructions(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalInstructions_normalized", "Normalized Total Instructions", getNormalizedTotalCycles(experiments));
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
    public void plotTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalCycles", "Total Cycles", getTotalCycles(experiments));
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
    public void plotNormalizedTotalCycles(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "totalCycles_normalized", "Normalized Total Cycles", getNormalizedTotalCycles(experiments));
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
    public void plotNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numL2DownwardReadMisses", "# L2 Downward Read Misses", getNumL2DownwardReadMisses(experiments));
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
    public void plotNormalizedNumL2DownwardReadMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numL2DownwardReadMisses_normalized", "# Normalized L2 Downward Read Misses", getNormalizedNumL2DownwardReadMisses(experiments));
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
    public void plotNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheHits", "# Main Thread L2 Cache Hits", getNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheMisses", "# Main Thread L2 Cache Misses", getNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheHits", "# Helper Thread L2 Cache Hits", getNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheMisses", "# Helper Thread L2 Cache Misses", getNumHelperThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedNumMainThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheHits_normalized", "# Normalized Main Thread L2 Cache Hits", getNormalizedNumMainThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedNumMainThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numMainThreadL2CacheMisses_normalized", "# Normalized Main Thread L2 Cache Misses", getNormalizedNumMainThreadL2CacheMisses(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedNumHelperThreadL2CacheHits(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheHits_normalized", "# Normalized Helper Thread L2 Cache Hits", getNormalizedNumHelperThreadL2CacheHits(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedNumHelperThreadL2CacheMisses(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "numHelperThreadL2CacheMisses_normalized", "# Normalized Helper Thread L2 Cache Misses", getNormalizedNumHelperThreadL2CacheMisses(experimentPack, experiments));
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
    public void plotHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestCoverage", "Helper Thread L2 Cache Request Coverage", getHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestAccuracy", "HelperT hread L2 Cache Request Accuracy", getHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedHelperThreadL2CacheRequestCoverage(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestCoverage_normalized", "Normalized Helper Thread L2 Cache Request Coverage", getNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experiments));
    }

    /**
     * @param experimentPack
     * @param experiments
     */
    @Override
    public void plotNormalizedHelperThreadL2CacheRequestAccuracy(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "helperThreadL2CacheRequestAccuracy_normalized", "Normalized Helper Thread L2 Cache Request Accuracy", getNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experiments));
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
    public void plotL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "l2DownwardReadMPKIs", "# L2 Downward Read MPKIs", getL2DownwardReadMPKIs(experiments));
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
    public void plotNormalizedL2DownwardReadMPKIs(ExperimentPack experimentPack, List<Experiment> experiments) {
        plot(experimentPack, "l2DownwardReadMPKIs_normalized", "# Normalized L2 Downward Read MPKIs", getNormalizedL2DownwardReadMPKIs(experiments));
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
    public void plotHelperThreadL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments) {
        List<Map<String, Double>> breakdowns = getHelperThreadL2CacheRequestBreakdowns(experiments);
        List<Map<String, Double>> transformedBreakdowns = transform(breakdowns, new Function1<Map<String, Double>, Map<String, Double>>() {
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
        plot(experimentPack, "helperThreadL2CacheRequestBreakdowns", "# Helper Thread L2 Request Breakdowns", transformedBreakdowns);
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
    public void plotL2CacheRequestBreakdowns(ExperimentPack experimentPack, List<Experiment> experiments) {
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
        plot(experimentPack, "l2CacheRequestBreakdowns", "# L2 Request Breakdowns", transformedBreakdowns);
    }

    /**
     * @param experimentPack
     * @param detailed
     * @param stoppedExperimentsOnly
     */
    @Override
    public void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, boolean stoppedExperimentsOnly) {
        dumpExperimentPack(experimentPack, detailed, new IndentedPrintWriter(new PrintWriter(System.out), true), stoppedExperimentsOnly);
    }

    /**
     * @param experimentPack
     * @param detailed
     * @param writer
     * @param stoppedExperimentsOnly
     */
    @Override
    public void dumpExperimentPack(ExperimentPack experimentPack, boolean detailed, IndentedPrintWriter writer, boolean stoppedExperimentsOnly) {
        writer.printf("experiment pack %s\n", experimentPack.getTitle());
        writer.println();

        writer.incrementIndentation();

        List<Experiment> experimentsByExperimentPack = stoppedExperimentsOnly ? ServiceManager.getExperimentService().getStoppedExperimentsByExperimentPack(experimentPack) : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack);
        Experiment firstExperimentByExperimentPack = stoppedExperimentsOnly ? ServiceManager.getExperimentService().getFirstStoppedExperimentByExperimentPack(experimentPack) : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack).get(0);

        if (firstExperimentByExperimentPack != null) {
            writer.println("experiment titles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(transform(experimentsByExperimentPack, new Function1<Experiment, String>() {
                @Override
                public String apply(Experiment experiment) {
                    return experiment.getTitle();
                }
            }), true));
            writer.println();
            writer.decrementIndentation();

            tableSummary(experimentPack.getTitle(), experimentsByExperimentPack.get(0), experimentsByExperimentPack); //TODO: to be refactored out

            writer.println("simulation times in seconds: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(transform(experimentsByExperimentPack, new Function1<Experiment, Double>() {
                @Override
                public Double apply(Experiment experiment) {
                    return Double.parseDouble(experiment.getStatValue(experiment.getMeasurementTitlePrefix() + "simulation/durationInSeconds"));
                }
            }), true));
            writer.println();
            writer.decrementIndentation();

            writer.println("speedups: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(getSpeedups(firstExperimentByExperimentPack, experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            plotSpeedups(experimentPack, firstExperimentByExperimentPack, experimentsByExperimentPack);

            writer.println("total instructions: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(getTotalInstructions(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            plotTotalInstructions(experimentPack, experimentsByExperimentPack);

            writer.println("normalized total instructions: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(getNormalizedTotalInstructions(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            plotNormalizedTotalInstructions(experimentPack, experimentsByExperimentPack);

            writer.println("total cycles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(getTotalCycles(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            plotTotalCycles(experimentPack, experimentsByExperimentPack);

            writer.println("normalized total cycles: ");
            writer.incrementIndentation();
            writer.println(JsonSerializationHelper.toJson(getNormalizedTotalCycles(experimentsByExperimentPack), true));
            writer.println();
            writer.decrementIndentation();

            plotNormalizedTotalCycles(experimentPack, experimentsByExperimentPack);

            if (firstExperimentByExperimentPack.getType() != ExperimentType.FUNCTIONAL) {
                writer.println("l2 request breakdowns: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getL2CacheRequestBreakdowns(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotL2CacheRequestBreakdowns(experimentPack, experimentsByExperimentPack);

                writer.println("# l2 downward read MPKIs: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getL2DownwardReadMPKIs(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotL2DownwardReadMPKIs(experimentPack, experimentsByExperimentPack);

                writer.println("# normalized l2 downward read MPKIs: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getNormalizedL2DownwardReadMPKIs(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotNormalizedL2DownwardReadMPKIs(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request breakdowns: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getHelperThreadL2CacheRequestBreakdowns(experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotHelperThreadL2CacheRequestBreakdowns(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request coverage: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack), true));
                writer.decrementIndentation();

                plotHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack);

                writer.println("helper thread L2 request accuracy: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack);

                writer.println("normalized helper thread L2 request coverage: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack), true));
                writer.decrementIndentation();

                plotNormalizedHelperThreadL2CacheRequestCoverage(experimentPack, experimentsByExperimentPack);

                writer.println("normalized helper thread L2 request accuracy: ");
                writer.incrementIndentation();
                writer.println(JsonSerializationHelper.toJson(getNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack), true));
                writer.println();
                writer.decrementIndentation();

                plotNormalizedHelperThreadL2CacheRequestAccuracy(experimentPack, experimentsByExperimentPack);

                //TODO: dump and plot 3C misses/cache request/miss latencies and mlp based costs (and breakdowns)!!!
            }
        }

        writer.decrementIndentation();

        if (detailed) {
            writer.incrementIndentation();
            for (Experiment experiment : ServiceManager.getExperimentService().getExperimentsByExperimentPack(experimentPack)) {
                dumpExperiment(experiment, writer);
            }
            writer.decrementIndentation();
        }
    }

    /**
     * @param experiment
     */
    @Override
    public void dumpExperiment(Experiment experiment) {
        dumpExperiment(experiment, new IndentedPrintWriter(new PrintWriter(System.out), true));
    }

    /**
     * @param experiment
     * @param writer
     */
    @Override
    public void dumpExperiment(Experiment experiment, IndentedPrintWriter writer) {
        writer.printf("[%s] experiment %s\n", DateHelper.toString(experiment.getCreateTime()), experiment);

        Map<String, String> configs = new LinkedHashMap<String, String>();

        JaxenHelper.dumpValueFromXPath(configs, experiment, "title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "type");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "state");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "numMaxInstructions");
        JaxenHelper.dumpValuesFromXPath(configs, experiment, "contextMappings");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/title");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/createTimeAsString");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/helperThreadPthreadSpawnIndex");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/helperThreadL2CacheRequestProfilingEnabled");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numCores");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/numThreadsPerCore");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/physicalRegisterFileCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/issueWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/commitWidth");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/decodeBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/reorderBufferCapacity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/loadStoreQueueCapacity");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/branchPredictorType");
        switch (experiment.getArchitecture().getBranchPredictorType()) {
            case PERFECT:
                break;
            case TAKEN:
                break;
            case NOT_TAKEN:
                break;
            case TWO_BIT:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoBitBranchPredictorReturnAddressStackSize");
                break;
            case TWO_LEVEL:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/twoLevelBranchPredictorReturnAddressStackSize");
                break;
            case COMBINED:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBimodSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorL1Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorL2Size");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorMetaSize");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorShiftWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorXor");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBranchTargetBufferNumSets");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorBranchTargetBufferAssociativity");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/combinedBranchPredictorReturnAddressStackSize");
                break;
        }

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/tlbMissLatency");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ISize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1ILineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1INumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1IReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DAssociativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DLineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DHitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumReadPorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DNumWritePorts");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l1DReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Size");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2Associativity");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2LineSize");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2HitLatency");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/l2ReplacementPolicyType");

        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/memoryControllerType");
        JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/memoryControllerLineSize");
        switch (experiment.getArchitecture().getMemoryControllerType()) {
            case FIXED_LATENCY:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/fixedLatencyMemoryControllerLatency");
                break;
            case SIMPLE:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMemoryControllerMemoryLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMemoryControllerMemoryTrunkLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/simpleMemoryControllerBusWidth");
                break;
            case BASIC:
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerToDramLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerFromDramLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerPrechargeLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerClosedLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerConflictLatency");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerBusWidth");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerNumBanks");
                JaxenHelper.dumpValueFromXPath(configs, experiment, "architecture/basicMemoryControllerRowSize");
                break;
        }

        writer.incrementIndentation();
        writer.println("  configs:");
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            writer.incrementIndentation();
            writer.printf("%s: %s\n", entry.getKey(), entry.getValue());
            writer.decrementIndentation();
        }

        writer.println();
        writer.decrementIndentation();

        writer.incrementIndentation();
        writer.println("  stats:");
        for (ExperimentStat stat : ServiceManager.getExperimentStatService().getStatsByParent(experiment)) {
            writer.incrementIndentation();
            writer.printf("%s: %s\n", stat.getTitle(), stat.getValue());
            writer.decrementIndentation();
        }

        writer.println();
        writer.decrementIndentation();
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

    private void plot(final ExperimentPack experimentPack, String plotFileNameSuffix, String yLabel, List<?> rows) {
        try {
            String fileNamePdf = "experiment_plots" + File.separator + experimentPack.getTitle() + "_" + plotFileNameSuffix + ".pdf";
            new File(fileNamePdf).getParentFile().mkdirs();

            File fileInput = File.createTempFile("archimulator", "bargraph_input.txt");

            PrintWriter pw = new PrintWriter(fileInput);

            String variablePropertyDescription = CollectionUtils.isEmpty(experimentPack.getVariablePropertyNames()) ? "" : StringUtils.join(transform(experimentPack.getVariablePropertyNames(), new Function1<String, Object>() {
                @Override
                public Object apply(String variablePropertyName) {
                    return getDescriptionOfVariablePropertyName(variablePropertyName);
                }
            }), "_");

            pw.println("yformat=%g");
            pw.println("=nogridy");
            pw.println("=norotate");
//            pw.println("=patterns");

            pw.println("xscale=1.2");

//            pw.println("legendx=right");
//            pw.println("legendy=top");
//            pw.println("=nolegoutline");
//            pw.println("legendfill=");
//            pw.println("legendfontsz=13");

            if (rows.get(0) instanceof Map) {
                pw.println("=stacked;" + StringUtils.join(((Map) (rows.get(0))).keySet(), ';'));
                pw.println("=table");
            }

            pw.println("xlabel=" + variablePropertyDescription);
            pw.println("ylabel=" + yLabel);

            int i = 0;
            for (Object row : rows) {
                String title = CollectionUtils.isEmpty(experimentPack.getVariablePropertyValues()) ? "" : experimentPack.getVariablePropertyValues().get(i++);
                pw.println(title.replaceAll(" ", "_") + "\t" + ((row instanceof Map) ? StringUtils.join(((Map) row).values(), '\t') : row));
            }

            pw.close();

            System.err.println(StringUtils.join(CommandLineHelper.invokeShellCommandAndGetResult("tools/bargraph.pl" + " -pdf " + fileInput + " > " + fileNamePdf), IOUtils.LINE_SEPARATOR));

            if (!fileInput.delete()) {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
