/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.experiment;

import archimulator.core.*;
import archimulator.sim.capability.ProcessorCapability;
import archimulator.sim.capability.ProcessorCapabilityFactory;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.mem.MemoryHierarchyConfig;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.isa.NativeMipsIsaEmulatorCapability;
import archimulator.os.KernelCapability;
import archimulator.os.KernelCapabilityFactory;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.sim.ContextConfig;
import archimulator.sim.SimulatedProgram;
import archimulator.sim.Simulation;
import archimulator.sim.SimulationConfig;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PauseSimulationEvent;
import archimulator.sim.event.StopSimulationEvent;
import archimulator.sim.strategy.SimulationStrategy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public abstract class Experiment {
    private Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> simulationCapabilityFactories = new HashMap<Class<? extends SimulationCapability>, SimulationCapabilityFactory>();
    private Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories = new HashMap<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>();
    private Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories = new HashMap<Class<? extends KernelCapability>, KernelCapabilityFactory>();

    private long id;

    private String title;
    private int numCores;
    private int numThreadsPerCore;
    private ContextConfig contextConfig;
    private List<ContextConfig> contextConfigs;
    private EvictionPolicyFactory l2EvictionPolicyFactory;
    private ProcessorConfig processorConfig;

    private String beginTime;

    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private CyclicBarrier phaser;

    private FiniteStateMachine<ExperimentState> fsm;

    private Thread threadStartExperiment;

    private Simulation simulation;

    public Experiment(String title, int numCores, int numThreadsPerCore, SimulatedProgram simulatedProgram) { //TODO: add support for multi simulatedProgram configuration at startup
        this.id = currentId++;

        this.title = title;
        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

        this.contextConfig = new ContextConfig(simulatedProgram, "ctx0.out", 0);

        this.contextConfigs = new ArrayList<ContextConfig>();
        this.contextConfigs.add(this.contextConfig);

        this.l2EvictionPolicyFactory = LeastRecentlyUsedEvictionPolicy.FACTORY;

        this.processorConfig = ProcessorConfig.createDefaultProcessorConfig(MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(this.l2EvictionPolicyFactory), this.processorCapabilityFactories, this.kernelCapabilityFactories, this.numCores, this.numThreadsPerCore);

        this.beginTime = new SimpleDateFormat("yyyyMMdd_HH-mm-ss").format(new Date());

        this.blockingEventDispatcher = new BlockingEventDispatcher<BlockingEvent>();
        this.cycleAccurateEventQueue = new CycleAccurateEventQueue();

        final int slacks = 100;

        this.phaser = new CyclicBarrier(2);

        this.fsm = new FiniteStateMachine<ExperimentState>(title, ExperimentState.NOT_STARTED);

        this.fsm.in(ExperimentState.NOT_STARTED)
                .ignore(FSM_CONDITION_STOP)
                .on(FSM_CONDITION_START, new Function1X<ExperimentState, ExperimentState>() {
                    public ExperimentState apply(ExperimentState state, Object... params) {
                        threadStartExperiment = new Thread() {
                            @Override
                            public void run() {
                                doStart();

                                fsm.fireTransition("stop");

                                System.gc();
                            }
                        };

                        threadStartExperiment.setDaemon(true);
                        threadStartExperiment.start();

                        return ExperimentState.RUNNING;
                    }
                });

        this.fsm.in(ExperimentState.RUNNING)
                .ignore(FSM_CONDITION_START)
                .on(FSM_CONDITION_PAUSE, new Function1X<ExperimentState, ExperimentState>() {
                    public ExperimentState apply(ExperimentState param1, Object... otherParams) {
                        getBlockingEventDispatcher().dispatch(new PauseSimulationEvent());
                        return ExperimentState.PAUSED;
                    }
                })
                .on(FSM_CONDITION_STOP, new Function1X<ExperimentState, ExperimentState>() {
                    public ExperimentState apply(ExperimentState param1, Object... otherParams) {
                        getBlockingEventDispatcher().dispatch(new StopSimulationEvent());
                        return ExperimentState.STOPPED;
                    }
                });

        this.fsm.in(ExperimentState.PAUSED)
                .ignore(FSM_CONDITION_PAUSE)
                .on(FSM_CONDITION_RESUME, new Function1X<ExperimentState, ExperimentState>() {
                    public ExperimentState apply(ExperimentState param1, Object... otherParams) {
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
                .on(FSM_CONDITION_STOP, new Function1X<ExperimentState, ExperimentState>() {
                    public ExperimentState apply(ExperimentState param1, Object... otherParams) {
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

        this.fsm.in(ExperimentState.STOPPED)
                .ignore("stop");
    }

    private void dumpStats(Map<String, Object> stats, boolean detailedSimulation) {
        stats.put("experiment.beginTime", this.beginTime);
        stats.put("experiment.workload", this.contextConfig.getSimulatedProgram().getSetTitle() + "." + this.contextConfig.getSimulatedProgram().getTitle() + "(" + this.contextConfig.getSimulatedProgram().getArgs() + ")");

        if (detailedSimulation) {
            stats.put("experiment.numCores", this.numCores);
            stats.put("experiment.numThreadsPerCore", this.numThreadsPerCore);
            stats.put("experiment.l2EvictionPolicy", this.l2EvictionPolicyFactory.getName());
        }
    }

    public Experiment addSimulationCapabilityFactory(Class<? extends SimulationCapability> clz, SimulationCapabilityFactory factory) {
        this.simulationCapabilityFactories.put(clz, factory);
        return this;
    }

    public Experiment addProcessorCapabilityFactory(Class<? extends ProcessorCapability> clz, ProcessorCapabilityFactory factory) {
        this.processorCapabilityFactories.put(clz, factory);
        return this;
    }

    public Experiment addKernelCapabilityFactory(Class<? extends KernelCapability> clz, KernelCapabilityFactory factory) {
        this.kernelCapabilityFactories.put(clz, factory);
        return this;
    }

    public Experiment setL2EvictionPolicyFactory(EvictionPolicyFactory l2EvictionPolicyFactory) {
        this.l2EvictionPolicyFactory = l2EvictionPolicyFactory;
        return this;
    }

    public void start() {
        this.fsm.fireTransition(FSM_CONDITION_START);
    }

    protected abstract void doStart();

    public void pause() {
        this.fsm.fireTransition(FSM_CONDITION_PAUSE);
    }

    public void resume() {
        this.fsm.fireTransition(FSM_CONDITION_RESUME);
    }

    public void stop() {
        this.fsm.fireTransition(FSM_CONDITION_STOP);
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

    protected void doSimulation(String title, SimulationStrategy strategy, BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.simulation = new Simulation(new SimulationConfig(title, this.processorConfig, this.contextConfigs), strategy, this.simulationCapabilityFactories, blockingEventDispatcher, cycleAccurateEventQueue);

        this.simulation.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                dumpStats(event.getStats(), event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION);
            }
        });

        this.simulation.simulate();
    }

    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    protected BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
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

    private static final String FSM_CONDITION_START = "start";
    private static final String FSM_CONDITION_STOP = "stop";
    private static final String FSM_CONDITION_PAUSE = "pause";
    private static final String FSM_CONDITION_RESUME = "resume";

    private static long currentId = 0;

    static {
        CopyEmbeddedResourceIntoTempDirectory("lib" + NativeMipsIsaEmulatorCapability.LIBRARY_NAME + ".so");
        System.setProperty("jna.library.path", System.getProperty("java.io.tmpdir"));
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.printf("\nExperiment has completed %s.\n", !Simulation.hasError ? "successfully" : "with error(s)");
            }
        });

        System.out.println("Archimulator - A Flexible Multicore Architectural Simulator Written in Java.\n");
        System.out.println("Version: 2.0.\n");
        System.out.println("Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).\n");
    }

    public static void CopyEmbeddedResourceIntoTempDirectory(String name) {
        try {
            InputStream in = Experiment.class.getResourceAsStream("/resources/" + name);
            byte[] buffer = new byte[1024];
            int read;

            FileOutputStream fos = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/" + name);

            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
