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

import archimulator.model.*;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.service.ExperimentService;
import archimulator.service.ServiceManager;
import archimulator.sim.common.*;
import archimulator.sim.os.Kernel;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.*;
import net.pickapack.JsonSerializationHelper;
import net.pickapack.Reference;
import net.pickapack.action.Function1;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;
import net.pickapack.util.CollectionHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Experiment service implementation.
 *
 * @author Min Cai
 */
public class ExperimentServiceImpl extends AbstractService implements ExperimentService {
    private Dao<Experiment, Long> experiments;
    private Dao<ExperimentPack, Long> experimentPacks;

    private boolean running = false;

    private Lock lockGetFirstExperimentToRun = new ReentrantLock();

    /**
     * Create an experiment service implementation.
     */
    @SuppressWarnings("unchecked")
    public ExperimentServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(Experiment.class, ExperimentPack.class));

        this.experiments = createDao(Experiment.class);
        this.experimentPacks = createDao(ExperimentPack.class);
    }

    @Override
    public void initialize() {
        this.cleanUpExperiments();

        //TODO: to be exposed as import/upload experiment pack via web UI

        try {
            TransactionManager.callInTransaction(getConnectionSource(),
                    new Callable<Void>() {
                        public Void call() throws Exception {
                            try {
                                for (File file : FileUtils.listFiles(new File("experiment_inputs"), null, true)) {
                                    String text = FileUtils.readFileToString(file);

                                    ExperimentPack experimentPack = JsonSerializationHelper.deserialize(ExperimentPack.class, text);

                                    if (experimentPack != null && getExperimentPackByTitle(experimentPack.getTitle()) == null) {
                                        addExperimentPack(experimentPack);

                                        for (ExperimentSpec experimentSpec : experimentPack.getExperimentSpecs()) {
                                            ExperimentType experimentType = experimentPack.getExperimentType();
                                            Benchmark benchmark = experimentSpec.getBenchmark();
                                            Architecture architecture = experimentSpec.getArchitecture();
                                            String arguments = experimentSpec.getBenchmark().getDefaultArguments();

                                            List<ContextMapping> contextMappings = new ArrayList<ContextMapping>();

                                            ContextMapping contextMapping = new ContextMapping(0, benchmark, arguments);
                                            contextMapping.setHelperThreadLookahead(experimentSpec.getHelperThreadLookahead());
                                            contextMapping.setHelperThreadStride(experimentSpec.getHelperThreadStride());
                                            contextMappings.add(contextMapping);

                                            List<ExperimentGauge> gauges = ServiceManager.getExperimentMetricService().getAllGauges(); //TODO: should use basic gauges only.

                                            addExperiment(new Experiment(experimentPack, experimentType, architecture, experimentSpec.getNumMaxInstructions(), contextMappings, gauges));
                                        }

                                        updateExperimentPack(experimentPack);
                                    }
                                }

                                return null;
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    });
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Clean up experiments.
     */
    private void cleanUpExperiments() {
        System.out.println("Cleaning up experiments..");

        List<Long> experimentPackIds = CollectionHelper.transform(getAllExperimentPacks(), new Function1<ExperimentPack, Long>() {
            @Override
            public Long apply(ExperimentPack experimentPack) {
                return experimentPack.getId();
            }
        });

        if (!experimentPackIds.isEmpty()) {
            try {
                DeleteBuilder<Experiment, Long> deleteBuilder = this.experiments.deleteBuilder();
                deleteBuilder.where().notIn("parentId", experimentPackIds);
                PreparedDelete<Experiment> delete = deleteBuilder.prepare();
                this.experiments.delete(delete);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();
            updateBuilder.where().eq("state", ExperimentState.READY_TO_RUN).or().eq("state", ExperimentState.RUNNING);
            updateBuilder.updateColumnValue("state", ExperimentState.PENDING);
            updateBuilder.updateColumnValue("failedReason", "");

            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Cleaned up experiments.");
    }

    @Override
    public void start() {
        running = true;

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        for (; running; ) {
                            Experiment experiment;
                            while ((experiment = getFirstExperimentToRun()) == null) {
                                synchronized (this) {
                                    wait(500L);
                                }
                            }

                            runExperiment(experiment);
                        }
                    } catch (InterruptedException ignored) {
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Run the specified experiment.
     *
     * @param experiment the experiment to run
     */
    private void runExperiment(Experiment experiment) {
        try {
            CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

            if (experiment.getType() == ExperimentType.FUNCTIONAL) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new FunctionalSimulation(experiment, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (experiment.getType() == ExperimentType.DETAILED) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new DetailedSimulation(experiment, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (experiment.getType() == ExperimentType.TWO_PHASE) {
                Reference<Kernel> kernelRef = new Reference<Kernel>();

                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();

                new ToRoiFastForwardSimulation(experiment, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();

                blockingEventDispatcher.clearListeners();

                cycleAccurateEventQueue.resetCurrentCycle();

                new FromRoiDetailedSimulation(experiment, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();
            }

            experiment.setState(ExperimentState.COMPLETED);
            experiment.setFailedReason("");
        } catch (Exception e) {
            experiment.setState(ExperimentState.ABORTED);
            experiment.setFailedReason(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        } finally {
            ServiceManager.getExperimentService().updateExperiment(experiment);
        }
    }

    @Override
    public void stop() {
        running = false;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        super.stop();
    }

    @Override
    public List<Experiment> getAllExperiments() {
        return this.getItems(this.experiments);
    }

    @Override
    public List<Experiment> getAllExperiments(long first, long count) {
        return this.getItems(this.experiments, first, count);
    }

    @Override
    public long getNumAllExperiments() {
        return this.getNumItems(this.experiments);
    }

    @Override
    public long getNumAllExperimentsByState(ExperimentState experimentState) {
        try {
            PreparedQuery<Experiment> query = this.experiments.queryBuilder().setCountOf(true).where()
                    .eq("state", experimentState)
                    .prepare();
            return this.experiments.countOf(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Experiment getExperimentById(long id) {
        return this.getItemById(this.experiments, id);
    }

    @Override
    public List<Experiment> getExperimentsByTitle(String title) {
        return this.getItemsByTitle(this.experiments, title);
    }

    @Override
    public Experiment getFirstExperimentByTitle(String title) {
        return this.getFirstItemByTitle(this.experiments, title);
    }

    @Override
    public Experiment getFirstExperimentByParent(ExperimentPack parent) {
        return this.getFirstItemByParent(this.experiments, parent);
    }

    @Override
    public Experiment getLatestExperimentByTitle(String title) {
        return this.getLatestItemByTitle(this.experiments, title);
    }

    @Override
    public List<Experiment> getExperimentsByBenchmark(Benchmark benchmark) { //TODO: to be optimized
        List<Experiment> result = new ArrayList<Experiment>();

        for (Experiment experiment : getAllExperiments()) {
            for (ContextMapping contextMapping : experiment.getContextMappings()) {
                if (contextMapping.getBenchmarkId() == benchmark.getId()) {
                    result.add(experiment);
                    break;
                }
            }
        }

        return result;
    }

    @Override
    public List<Experiment> getExperimentsByArchitecture(Architecture architecture) {
        try {
            PreparedQuery<Experiment> query = this.experiments.queryBuilder().where().eq("architectureId", architecture.getId()).prepare();
            return this.experiments.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Experiment> getExperimentsByParent(ExperimentPack parent) {
        try {
            PreparedQuery<Experiment> query = this.experiments.queryBuilder().where()
                    .eq("parentId", parent.getId())
                    .prepare();
            return this.experiments.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Experiment> getExperimentsByParent(ExperimentPack parent, long first, long count) {
        try {
            QueryBuilder<Experiment, Long> queryBuilder = this.experiments.queryBuilder();
            queryBuilder.offset(first).limit(count);
            queryBuilder.where().eq("parentId", parent.getId());
            PreparedQuery<Experiment> query = queryBuilder.prepare();
            return this.experiments.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addExperiment(Experiment experiment) {
        this.addItem(this.experiments, experiment);
    }

    @Override
    public void removeExperimentById(long id) {
        this.removeItemById(this.experiments, id);
    }

    @Override
    public void updateExperiment(Experiment experiment) {
        this.updateItem(this.experiments, experiment);
    }

    @Override
    public Experiment getFirstExperimentToRun() {
        lockGetFirstExperimentToRun.lock();
        try {
            try {
                PreparedQuery<Experiment> query = experiments.queryBuilder().where().eq("state", ExperimentState.READY_TO_RUN).prepare();
                Experiment experiment = experiments.queryForFirst(query);

                if (experiment != null) {
                    experiment.setState(ExperimentState.RUNNING);
                    ServiceManager.getExperimentService().updateExperiment(experiment);
                    ServiceManager.getExperimentStatService().clearStatsByParent(experiment);
                }

                return experiment;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } finally {
            lockGetFirstExperimentToRun.unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Experiment> getStoppedExperimentsByParent(ExperimentPack parent) {
        try {
            QueryBuilder<Experiment, Long> queryBuilder = this.experiments.queryBuilder();

            Where<Experiment, Long> where = queryBuilder.where();
            where.and(
                    where.eq("parentId", parent.getId()),
                    where.eq("state", ExperimentState.COMPLETED).or().eq("state", ExperimentState.ABORTED)
            );

            PreparedQuery<Experiment> query = queryBuilder.prepare();
            return this.experiments.query(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Experiment getFirstStoppedExperimentByParent(ExperimentPack parent) {
        try {
            QueryBuilder<Experiment, Long> queryBuilder = this.experiments.queryBuilder();

            Where<Experiment, Long> where = queryBuilder.where();
            where.and(
                    where.eq("parentId", parent.getId()),
                    where.eq("state", ExperimentState.COMPLETED).or().eq("state", ExperimentState.ABORTED)
            );

            PreparedQuery<Experiment> query = queryBuilder.prepare();
            return this.experiments.queryForFirst(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExperimentPack> getAllExperimentPacks() {
        return this.getItems(this.experimentPacks);
    }

    @Override
    public List<ExperimentPack> getAllExperimentPacks(long first, long count) {
        return this.getItems(this.experimentPacks, first, count);
    }

    @Override
    public ExperimentPack getExperimentPackById(long id) {
        return this.getItemById(this.experimentPacks, id);
    }

    @Override
    public ExperimentPack getExperimentPackByTitle(String title) {
        return this.getFirstItemByTitle(this.experimentPacks, title);
    }

    @Override
    public ExperimentPack getFirstExperimentPack() {
        return this.getFirstItem(this.experimentPacks);
    }

    @Override
    public void addExperimentPack(ExperimentPack experimentPack) {
        this.addItem(this.experimentPacks, experimentPack);
    }

    @Override
    public void removeExperimentPackById(long id) {
        this.removeItemById(this.experimentPacks, id);
    }

    @Override
    public void updateExperimentPack(ExperimentPack experimentPack) {
        this.updateItem(this.experimentPacks, experimentPack);
    }

    @Override
    public long getNumExperimentsByParent(ExperimentPack parent) {
        try {
            PreparedQuery<Experiment> query = this.experiments.queryBuilder().setCountOf(true).where()
                    .eq("parentId", parent.getId())
                    .prepare();
            return this.experiments.countOf(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getNumExperimentsByParentAndState(ExperimentPack parent, ExperimentState experimentState) {
        try {
            PreparedQuery<Experiment> query = this.experiments.queryBuilder().setCountOf(true).where()
                    .eq("parentId", parent.getId())
                    .and()
                    .eq("state", experimentState)
                    .prepare();
            return this.experiments.countOf(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startExperimentPack(ExperimentPack experimentPack) {
        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();
            updateBuilder.where().eq("parentId", experimentPack.getId()).and().eq("state", ExperimentState.PENDING);
            updateBuilder.updateColumnValue("state", ExperimentState.READY_TO_RUN);
            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stopExperimentPack(ExperimentPack experimentPack) {
        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();
            updateBuilder.where().eq("parentId", experimentPack.getId()).and().eq("state", ExperimentState.READY_TO_RUN);
            updateBuilder.updateColumnValue("state", ExperimentState.PENDING);
            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resetCompletedExperimentsByParent(ExperimentPack parent) {
        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();

            Where<Experiment, Long> where = updateBuilder.where();
            where.and(
                    where.eq("parentId", parent.getId()),
                    where.eq("state", ExperimentState.COMPLETED)
            );

            updateBuilder.updateColumnValue("state", ExperimentState.PENDING);

            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resetAbortedExperimentsByParent(ExperimentPack parent) {
        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();

            Where<Experiment, Long> where = updateBuilder.where();
            where.and(
                    where.eq("parentId", parent.getId()),
                    where.eq("state", ExperimentState.ABORTED)
            );

            updateBuilder.updateColumnValue("state", ExperimentState.PENDING);

            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void startExperiment(Experiment experiment) {
        try {
            UpdateBuilder<Experiment, Long> updateBuilder = this.experiments.updateBuilder();
            updateBuilder.where().eq("id", experiment.getId()).and().eq("state", ExperimentState.PENDING);
            updateBuilder.updateColumnValue("state", ExperimentState.READY_TO_RUN);
            PreparedUpdate<Experiment> update = updateBuilder.prepare();
            this.experiments.update(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
