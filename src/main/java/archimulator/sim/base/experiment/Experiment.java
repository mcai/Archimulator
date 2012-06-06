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
package archimulator.sim.base.experiment;

import archimulator.sim.base.event.*;
import archimulator.sim.base.experiment.capability.KernelCapability;
import archimulator.sim.base.experiment.capability.ProcessorCapability;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.Logger;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.base.simulation.SimulationConfig;
import archimulator.sim.base.simulation.strategy.SimulationStrategy;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.uncore.MemoryHierarchyConfig;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import net.pickapack.Params;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Action4;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.fsm.BasicFiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachineFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public abstract class Experiment {
    private List<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();

    private long id;

    private String title;
    private int numCores;
    private int numThreadsPerCore;
    private List<ContextConfig> contextConfigs;
    private ProcessorConfig processorConfig;

    private String beginTime;

    private BlockingEventDispatcher<ExperimentEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private CyclicBarrier phaser;

    private BasicFiniteStateMachine<ExperimentState, ExperimentCondition> fsm;

    private Thread threadStartExperiment;

    private ScheduledExecutorService scheduledExecutorPollState;

    private Simulation simulation;

    public Experiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz, List<Class<? extends SimulationCapability>> simulationCapabilityClasses, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
        this.id = currentId++;

        this.title = title;
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;
        this.contextConfigs = contextConfigs;

        this.processorConfig = ProcessorConfig.createDefaultProcessorConfig(MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(32768, 4, 32768, 8, l2Size, l2Associativity, l2EvictionPolicyClz), processorCapabilityClasses, kernelCapabilityClasses, this.numCores, this.numThreadsPerCore);

        this.simulationCapabilityClasses = simulationCapabilityClasses;

        this.beginTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        this.blockingEventDispatcher = new BlockingEventDispatcher<ExperimentEvent>();
        this.cycleAccurateEventQueue = new CycleAccurateEventQueue();

        this.phaser = new CyclicBarrier(2);

        this.fsm = new BasicFiniteStateMachine<ExperimentState, ExperimentCondition>(title, ExperimentState.NOT_STARTED);
        this.fsm.put("experiment", this);

        this.scheduledExecutorPollState = Executors.newSingleThreadScheduledExecutor();
    }

    private void pollSimulationState() {
        if (this.simulation == null) {
            return;
        }

        Logger.infof(Logger.SIMULATION, "------ Simulation %s: BEGIN DUMP STATE at %s ------", this.cycleAccurateEventQueue.getCurrentCycle(), this.simulation.getConfig().getTitle(), new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

        Map<String, Object> polledStats = new LinkedHashMap<String, Object>();

        this.simulation.getBlockingEventDispatcher().dispatch(new PollStatsEvent(this.simulation, polledStats));

        polledStats = this.simulation.getStatsWithSimulationPrefix(polledStats);

        for (Map.Entry<String, Object> entry : polledStats.entrySet()) {
            Logger.infof(Logger.SIMULATION, "\t%s: %s", this.cycleAccurateEventQueue.getCurrentCycle(), entry.getKey(), entry.getValue());
        }

        this.simulation.getBlockingEventDispatcher().dispatch(new PollStatsCompletedEvent(this.simulation, polledStats));

        Logger.info(Logger.SIMULATION, "------ END DUMP STATE ------\n", this.cycleAccurateEventQueue.getCurrentCycle());
    }

    private void dumpStats(Map<String, Object> stats, boolean detailedSimulation) {
        stats.put("experiment.beginTime", this.beginTime);
        stats.put("experiment.workloads.size", this.contextConfigs.size());

        for (ContextConfig contextConfig : this.contextConfigs) {
            stats.put("experiment.workload." + contextConfig.getThreadId(), contextConfig.getSimulatedProgram().getExe() + "(" + contextConfig.getSimulatedProgram().getArgs() + ")");
        }

        if (detailedSimulation) {
            stats.put("experiment.numCores", this.numCores);
            stats.put("experiment.numThreadsPerCore", this.numThreadsPerCore);
            stats.put("experiment.l2EvictionPolicy", this.processorConfig.getMemoryHierarchyConfig().getL2Cache().getEvictionPolicyClz().getName());
        }
    }

    public void start() {
        fsmFactory.fireTransition(this.fsm, this, ExperimentCondition.START, new Params());
    }

    protected abstract void doStart();

    public void pause() {
        fsmFactory.fireTransition(this.fsm, this, ExperimentCondition.PAUSE, new Params());
    }

    public void resume() {
        fsmFactory.fireTransition(this.fsm, this, ExperimentCondition.RESUME, new Params());
    }

    public void stop() {
        fsmFactory.fireTransition(this.fsm, this, ExperimentCondition.STOP, new Params());
    }

    public void join() {
        if (this.fsm.getState() != ExperimentState.NOT_STARTED) {
            try {
                this.threadStartExperiment.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void doSimulation(String title, SimulationStrategy strategy, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.simulation = new Simulation(new SimulationConfig(title, this.processorConfig, this.contextConfigs), strategy, this.simulationCapabilityClasses, blockingEventDispatcher, cycleAccurateEventQueue);

        this.blockingEventDispatcher.dispatch(new SimulationCreatedEvent(this));

        this.simulation.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                pollSimulationState();
                dumpStats(event.getStats(), event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION);
            }
        });

        this.simulation.simulate(this);
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public BlockingEventDispatcher<ExperimentEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    protected CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    protected CyclicBarrier getPhaser() {
        return this.phaser;
    }

    public ExperimentState getState() {
        return this.fsm.getState();
    }

    public Simulation getSimulation() {
        return simulation;
    }

    private enum ExperimentCondition {
        START,
        STOP,
        PAUSE,
        RESUME
    }

    private static long currentId = 0;

    private static FiniteStateMachineFactory<ExperimentState, ExperimentCondition, FiniteStateMachine<ExperimentState, ExperimentCondition>> fsmFactory;

    static {
        fsmFactory = new FiniteStateMachineFactory<ExperimentState, ExperimentCondition, FiniteStateMachine<ExperimentState, ExperimentCondition>>();

        fsmFactory.inState(ExperimentState.NOT_STARTED)
                .ignoreCondition(ExperimentCondition.STOP)
                .onCondition(ExperimentCondition.START, new Action4<FiniteStateMachine<ExperimentState, ExperimentCondition>, Object, ExperimentCondition, Params>() {
                    @Override
                    public void apply(final FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object param2, ExperimentCondition param3, Params param4) {
                        final Experiment experiment = ((BasicFiniteStateMachine) from).get(Experiment.class, "experiment");

                        Thread threadStartExperiment = new Thread() {
                            @Override
                            public void run() {
                                experiment.scheduledExecutorPollState.scheduleWithFixedDelay(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Semaphore semaphore = new Semaphore(0);

                                        experiment.getCycleAccurateEventQueue().schedule(this, new Action() {
                                            public void apply() {
                                                experiment.pollSimulationState();
                                                semaphore.release();
                                            }
                                        }, 0);

                                        try {
                                            semaphore.acquire();
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }, 2, 10, TimeUnit.SECONDS);

                                experiment.doStart();
                                experiment.scheduledExecutorPollState.shutdown();
                                try {
                                    experiment.scheduledExecutorPollState.awaitTermination(0, TimeUnit.SECONDS);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                fsmFactory.fireTransition(from, from, ExperimentCondition.STOP, new Params());

                                System.gc();
                            }
                        };

                        threadStartExperiment.setDaemon(true);
                        threadStartExperiment.start();

                        experiment.threadStartExperiment = threadStartExperiment;
                    }
                }, ExperimentState.RUNNING);

        fsmFactory.inState(ExperimentState.RUNNING)
                .ignoreCondition(ExperimentCondition.START)
                .onCondition(ExperimentCondition.PAUSE, new Action4<FiniteStateMachine<ExperimentState, ExperimentCondition>, Object, ExperimentCondition, Params>() {
                    @Override
                    public void apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object param2, ExperimentCondition param3, Params param4) {
                        final Experiment experiment = ((BasicFiniteStateMachine) from).get(Experiment.class, "experiment");
                        experiment.getBlockingEventDispatcher().dispatch(new PauseExperimentEvent(experiment));
                    }
                }, ExperimentState.PAUSED)
                .onCondition(ExperimentCondition.STOP, new Action4<FiniteStateMachine<ExperimentState, ExperimentCondition>, Object, ExperimentCondition, Params>() {
                    @Override
                    public void apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object param2, ExperimentCondition param3, Params param4) {
                        final Experiment experiment = ((BasicFiniteStateMachine) from).get(Experiment.class, "experiment");
                        experiment.getBlockingEventDispatcher().dispatch(new StopExperimentEvent(experiment));
                        experiment.getBlockingEventDispatcher().clearListeners();
                    }
                }, ExperimentState.STOPPED);

        fsmFactory.inState(ExperimentState.PAUSED)
                .ignoreCondition(ExperimentCondition.PAUSE)
                .onCondition(ExperimentCondition.RESUME, new Action4<FiniteStateMachine<ExperimentState, ExperimentCondition>, Object, ExperimentCondition, Params>() {
                    @Override
                    public void apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object param2, ExperimentCondition param3, Params param4) {
                        try {
                            final Experiment experiment = ((BasicFiniteStateMachine) from).get(Experiment.class, "experiment");
                            experiment.getPhaser().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, ExperimentState.RUNNING)
                .onCondition(ExperimentCondition.STOP, new Action4<FiniteStateMachine<ExperimentState, ExperimentCondition>, Object, ExperimentCondition, Params>() {
                    @Override
                    public void apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object param2, ExperimentCondition param3, Params param4) {
                        final Experiment experiment = (Experiment) ((BasicFiniteStateMachine) from).get(Experiment.class, "experiment");
                        try {
                            experiment.getPhaser().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                        experiment.scheduledExecutorPollState.shutdown();
                        try {
                            experiment.scheduledExecutorPollState.awaitTermination(0, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        experiment.getBlockingEventDispatcher().dispatch(new StopExperimentEvent(experiment));
                        experiment.getBlockingEventDispatcher().clearListeners();
                    }
                }, ExperimentState.STOPPED);

        fsmFactory.inState(ExperimentState.STOPPED)
                .ignoreCondition(ExperimentCondition.STOP);

        System.out.println("Archimulator - A Cloud Enabled Multicore Architectural Simulator Written in Java.\n");
        System.out.println("Version: 3.0.\n");
        System.out.println("Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).\n");
    }
}
