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
package archimulator.model.experiment;

import archimulator.model.capability.*;
import archimulator.model.event.DumpStatEvent;
import archimulator.model.event.PauseSimulationEvent;
import archimulator.model.event.StopSimulationEvent;
import archimulator.model.simulation.ContextConfig;
import archimulator.model.simulation.Simulation;
import archimulator.model.simulation.SimulationConfig;
import archimulator.model.strategy.SimulationStrategy;
import archimulator.sim.core.ProcessorConfig;
import archimulator.model.capability.KernelCapability;
import archimulator.sim.uncore.MemoryHierarchyConfig;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public abstract class Experiment {
    private List<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>> ();

    private long id;

    private String title;
    private int numCores;
    private int numThreadsPerCore;
    private List<ContextConfig> contextConfigs;
    private ProcessorConfig processorConfig;

    private String beginTime;

    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private CyclicBarrier phaser;

    private FiniteStateMachineFactory<ExperimentState, ExperimentCondition> fsmFactory;
    private FiniteStateMachine<ExperimentState, ExperimentCondition> fsm;

    private Thread threadStartExperiment;

    private Simulation simulation;

    public Experiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz, List<Class<? extends SimulationCapability>> simulationCapabilityClasses, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
        this.id = currentId++;

        this.title = title;
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;
        this.contextConfigs = contextConfigs;

        this.processorConfig = ProcessorConfig.createDefaultProcessorConfig(MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(l2Size, l2Associativity, l2EvictionPolicyClz), processorCapabilityClasses, kernelCapabilityClasses, this.numCores, this.numThreadsPerCore);

        this.simulationCapabilityClasses = simulationCapabilityClasses;
        
        this.beginTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        this.blockingEventDispatcher = new BlockingEventDispatcher<BlockingEvent>();
        this.cycleAccurateEventQueue = new CycleAccurateEventQueue();

        this.blockingEventDispatcher.addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                dumpStats(event.getStats(), event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION);
            }
        });

        this.phaser = new CyclicBarrier(2);

        this.fsmFactory = new FiniteStateMachineFactory<ExperimentState, ExperimentCondition>();

        this.fsmFactory.inState(ExperimentState.NOT_STARTED)
                .ignoreCondition(ExperimentCondition.STOP)
                .onCondition(ExperimentCondition.START, new Function1X<FiniteStateMachine<ExperimentState, ExperimentCondition>, ExperimentState>() {
                    public ExperimentState apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object... params) {
                        threadStartExperiment = new Thread() {
                            @Override
                            public void run() {
                                doStart();

                                fsm.fireTransition(ExperimentCondition.STOP);

                                System.gc();
                            }
                        };

                        threadStartExperiment.setDaemon(true);
                        threadStartExperiment.start();

                        return ExperimentState.RUNNING;
                    }
                });

        this.fsmFactory.inState(ExperimentState.RUNNING)
                .ignoreCondition(ExperimentCondition.START)
                .onCondition(ExperimentCondition.PAUSE, new Function1X<FiniteStateMachine<ExperimentState, ExperimentCondition>, ExperimentState>() {
                    public ExperimentState apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object... params) {
                        getBlockingEventDispatcher().dispatch(new PauseSimulationEvent());
                        return ExperimentState.PAUSED;
                    }
                })
                .onCondition(ExperimentCondition.STOP, new Function1X<FiniteStateMachine<ExperimentState, ExperimentCondition>, ExperimentState>() {
                    public ExperimentState apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object... params) {
                        getBlockingEventDispatcher().dispatch(new StopSimulationEvent());
                        return ExperimentState.STOPPED;
                    }
                });

        this.fsmFactory.inState(ExperimentState.PAUSED)
                .ignoreCondition(ExperimentCondition.PAUSE)
                .onCondition(ExperimentCondition.RESUME, new Function1X<FiniteStateMachine<ExperimentState, ExperimentCondition>, ExperimentState>() {
                    public ExperimentState apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object... params) {
                        try {
                            getPhaser().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                        return ExperimentState.RUNNING;
                    }
                })
                .onCondition(ExperimentCondition.STOP, new Function1X<FiniteStateMachine<ExperimentState, ExperimentCondition>, ExperimentState>() {
                    public ExperimentState apply(FiniteStateMachine<ExperimentState, ExperimentCondition> from, Object... params) {
                        try {
                            getPhaser().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (BrokenBarrierException e) {
                            throw new RuntimeException(e);
                        }
                        getBlockingEventDispatcher().dispatch(new StopSimulationEvent());
                        return ExperimentState.STOPPED;
                    }
                });

        this.fsmFactory.inState(ExperimentState.STOPPED)
                .ignoreCondition(ExperimentCondition.STOP);

        this.fsm = new FiniteStateMachine<ExperimentState, ExperimentCondition>(this.fsmFactory, title, ExperimentState.NOT_STARTED);
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
        this.fsm.fireTransition(ExperimentCondition.START);
    }

    protected abstract void doStart();

    public void pause() {
        this.fsm.fireTransition(ExperimentCondition.PAUSE);
    }

    public void resume() {
        this.fsm.fireTransition(ExperimentCondition.RESUME);
    }

    public void stop() {
        this.fsm.fireTransition(ExperimentCondition.STOP);
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

    public void runToEnd() {
        this.start();
        this.join();
    }

    protected void doSimulation(String title, SimulationStrategy strategy, BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.simulation = new Simulation(new SimulationConfig(title, this.processorConfig, this.contextConfigs), strategy, this.simulationCapabilityClasses, blockingEventDispatcher, cycleAccurateEventQueue);
        this.simulation.simulate();
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
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

    static {
        System.out.println("Archimulator - A Cloud Enabled Multicore Architectural Simulator Written in Java.\n");
        System.out.println("Version: 3.0.\n");
        System.out.println("Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).\n");
    }
}
