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
package archimulator.service.impl;

import archimulator.model.Experiment;
import archimulator.model.ExperimentSummary;
import archimulator.model.ExperimentStat;
import archimulator.service.ExperimentStatService;
import archimulator.service.ServiceManager;
import archimulator.util.plot.Table;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import net.pickapack.action.Function1;
import net.pickapack.collection.CollectionHelper;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Experiment stat service implementation.
 *
 * @author Min Cai
 */
public class ExperimentStatServiceImpl extends AbstractService implements ExperimentStatService {
    private Dao<ExperimentStat, Long> stats;
    private Dao<ExperimentSummary, Long> summaries;

    /**
     * Create an experiment stat service implementation.
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

        System.out.println("Cleaned up experiment stats and summaries.");
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
    public List<ExperimentStat> getStatsByParentAndPrefix(Experiment parent, String prefix) {
        try {
            PreparedQuery<ExperimentStat> query = this.stats.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .and()
                    .eq("prefix", prefix)
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

    /**
     * Create the summary for the specified experiment if necessary.
     *
     * @param parent the parent experiment
     */
    private void CreateSummaryIfNotExistsByParent(Experiment parent) {
        if (this.getFirstItemByParent(this.summaries, parent) == null) {
            System.out.println("Creating summary for experiment #" + parent.getId() + "..");

            Map<String, ExperimentStat> statsMap = ExperimentStat.toMap(ServiceManager.getExperimentStatService().getStatsByParent(parent));

            boolean helperThreadEnabled = parent.getContextMappings().get(0).getBenchmark().getHelperThreadEnabled();

            ExperimentSummary summary = new ExperimentSummary(parent);

            summary.setType(parent.getType());
            summary.setState(parent.getState());

            summary.setBeginTimeAsString(parent.getStatValue(statsMap, parent.getMeasurementTitlePrefix() + "simulation/beginTimeAsString"));
            summary.setEndTimeAsString(parent.getStatValue(statsMap, parent.getMeasurementTitlePrefix() + "simulation/endTimeAsString"));
            summary.setDuration(parent.getStatValue(statsMap, parent.getMeasurementTitlePrefix() + "simulation/duration"));
            summary.setDurationInSeconds(parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/durationInSeconds", 0));

            summary.setL2Size(parent.getArchitecture().getL2Size());
            summary.setL2Associativity(parent.getArchitecture().getL2Associativity());
            summary.setL2ReplacementPolicyType(parent.getArchitecture().getL2ReplacementPolicyType());

            summary.setHelperThreadLookahead(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadLookahead() : -1);
            summary.setHelperThreadStride(helperThreadEnabled ? parent.getContextMappings().get(0).getHelperThreadStride() : -1);

            summary.setNumMainThreadWaysInStaticPartitionedLRUPolicy(parent.getArchitecture().getNumMainThreadWaysInStaticPartitionedLRUPolicy());

            summary.setNumInstructions(parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/numInstructions", 0));
            summary.setC0t0NumInstructions(parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/c0t0NumInstructions", 0));
            summary.setC1t0NumInstructions(parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/c1t0NumInstructions", 0));

            summary.setNumCycles(
                    parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() + "simulation/cycleAccurateEventQueue/currentCycle", 0)
            );

            summary.setIpc(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "simulation/instructionsPerCycle", 0)
            );

            summary.setC0t0Ipc(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "simulation/c0t0InstructionsPerCycle", 0)
            );

            summary.setC1t0Ipc(
                    parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() + "simulation/c1t0InstructionsPerCycle", 0)
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

            summary.setHelperThreadL2CacheRequestLateness(
                    helperThreadEnabled ? parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestLateness", 0.0f) : 0.0f
            );

            summary.setHelperThreadL2CacheRequestPollution(
                    helperThreadEnabled ? parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestPollution", 0.0f) : 0.0f
            );

            summary.setHelperThreadL2CacheRequestRedundancy(
                    helperThreadEnabled ? parent.getStatValueAsDouble(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestRedundancy", 0.0f) : 0.0f
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

            summary.setNumEarlyHelperThreadL2CacheRequests(
                    helperThreadEnabled ? parent.getStatValueAsLong(statsMap, parent.getMeasurementTitlePrefix() +
                            "helperThreadL2CacheRequestProfilingHelper/numEarlyHelperThreadL2CacheRequests", 0) : 0
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
    public Table tableSummary(final List<Experiment> experiments) {
        return new Table(Arrays.asList(
                "Id",

                "Type",
                "State",

                "Begin Time",
                "End Time",
                "Duration",
                "Duration in Seconds",

                "L2_Size",
                "L2_Associativity",
                "L2_Replacement",

                "Lookahead",
                "Stride",

                "MT_Ways_In_Partitioned_L2",

                "Num_Instructions",
                "C0T0.Num_Instructions",
                "C1T0.Num_Instructions",
                "Num_Cycles",

                "IPC",
                "C0T0.IPC",
                "C1T0.IPC",
                "CPI",

                "MT.Hits",
                "MT.Misses",

                "HT.Hits",
                "HT.Misses",

                "L2.Evictions",
                "L2.Hit_Ratio",
                "L2.Occupancy_Ratio",

                "HT.Coverage",
                "HT.Accuracy",
                "HT.Lateness",
                "HT.Pollution",
                "HT.Redundancy",

                "Late",
                "Timely",
                "Bad",
                "Early",
                "Ugly",
                "Redundant_MSHR",
                "Redundant_Cache"
        ), new ArrayList<List<String>>() {{
            for (Experiment experiment : experiments) {
                add(getSummaryByParent(experiment).tableSummary2Row());
            }
        }});
    }
}
