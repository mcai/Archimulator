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

import archimulator.util.JaxenHelper;
import archimulator.sim.base.event.*;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.Logger;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.base.simulation.strategy.SimulationStrategy;
import archimulator.sim.core.Core;
import archimulator.sim.core.ProcessorConfig;
import archimulator.sim.core.Thread;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryHierarchyConfig;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.ht.LLCReuseDistanceProfilingCapability;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.Params;
import net.pickapack.action.Action;
import net.pickapack.action.Action4;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.fsm.BasicFiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachine;
import net.pickapack.fsm.FiniteStateMachineFactory;
import net.pickapack.io.serialization.MapHelper;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public abstract class Experiment {
    private List<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();

    private long id;

    private String title;
    private List<ContextConfig> contextConfigs;
    private ProcessorConfig processorConfig;

    private String beginTime;

    private BlockingEventDispatcher<ExperimentEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private CyclicBarrier phaser;

    private BasicFiniteStateMachine<ExperimentState, ExperimentCondition> fsm;

    private java.lang.Thread threadStartExperiment;

    private ScheduledExecutorService scheduledExecutorPollState;

    private Simulation simulation;

    public Experiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, int l1ISize, int l1IAssociativity, int l1DSize, int l1DAssociativity, int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz, List<Class<? extends SimulationCapability>> simulationCapabilityClasses) {
        this.id = currentId++;

        this.title = title;
        this.contextConfigs = contextConfigs;

        this.processorConfig = ProcessorConfig.createDefaultProcessorConfig(MemoryHierarchyConfig.createDefaultMemoryHierarchyConfig(l1ISize, l1IAssociativity, l1DSize, l1DAssociativity, l2Size, l2Associativity, l2EvictionPolicyClz), numCores, numThreadsPerCore);

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

        Logger.infof(Logger.SIMULATION, "------ Simulation %s: BEGIN DUMP STATE at %s ------", this.cycleAccurateEventQueue.getCurrentCycle(), this.simulation.getTitle(), new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

        Map<String, Object> polledStats = new LinkedHashMap<String, Object>();

        this.simulation.getBlockingEventDispatcher().dispatch(new PollStatsEvent(this.simulation, polledStats));

        polledStats = this.simulation.getStatsWithSimulationPrefix(polledStats);

        for (Map.Entry<String, Object> entry : polledStats.entrySet()) {
            Logger.infof(Logger.SIMULATION, "\t%s: %s", this.cycleAccurateEventQueue.getCurrentCycle(), entry.getKey(), entry.getValue());
        }

        this.simulation.getBlockingEventDispatcher().dispatch(new PollStatsCompletedEvent(this.simulation, polledStats));

        Logger.info(Logger.SIMULATION, "------ END DUMP STATE ------\n", this.cycleAccurateEventQueue.getCurrentCycle());
    }

    public void dumpStats(Map<String, Object> stats, boolean detailedSimulation) {
        pollSimulationState();

        JaxenHelper.dumpSingleStatFromXPath(stats, this, "title");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "beginTime");

        JaxenHelper.dumpStatsFromXPath(stats, this, "contextConfigs");

        if (detailedSimulation) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "processorConfig/numCores");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "processorConfig/numThreadsPerCore");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "processorConfig/memmoryHierarchyConfig/l2Cache/evictionPolicyClz/name");
        }

        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/title");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/duration");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/cycleAccurateEventQueue/currentCycle");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/totalInsts");

        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/instsPerCycle");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/cyclesPerSecond");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/instsPerSecond");

        for (Core core : JaxenHelper.<Core>selectNodes(this, "simulation/processor/cores[threads[totalInsts > 0]]")) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores[name ='" + core.getName() + "']/itlb/noFreeFu");

            if(detailedSimulation) {
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores[name ='" + core.getName() + "']/fuPool/noFreeFu");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores[name ='" + core.getName() + "']/fuPool/acquireFailedOnNoFreeFu");
            }
        }

        for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "simulation/processor/cores/threads[totalInsts > 0]")) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/totalInsts");

            if(detailedSimulation) {
                for (TranslationLookasideBuffer tlb : JaxenHelper.<TranslationLookasideBuffer>selectNodes(this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs")) {
                    JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs[name ='" + tlb.getName() + "']/hitRatio");
                    JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs[name ='" + tlb.getName() + "']/accesses");
                    JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs[name ='" + tlb.getName() + "']/hits");
                    JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs[name ='" + tlb.getName() + "']/misses");
                    JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/tlbs[name ='" + tlb.getName() + "']/evictions");
                }

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/bpred/type");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/bpred/accesses");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/bpred/hits");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/bpred/misses");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/bpred/hitRatio");

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/decodeBufferFull");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/reorderBufferFull");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/loadStoreQueueFull");

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/intPhysicalRegisterFileFull");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/fpPhysicalRegisterFileFull");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/miscPhysicalRegisterFileFull");

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/fetchStallsOnDecodeBufferIsFull");

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/registerRenameStallsOnDecodeBufferIsEmpty");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/registerRenameStallsOnReorderBufferIsFull");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/registerRenameStallsOnLoadStoreQueueFull");

                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/selectionStallOnCanNotLoad");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/selectionStallOnCanNotStore");
                JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cores/threads[name ='" + thread.getName() + "']/selectionStallOnNoFreeFunctionalUnit");
            }
        }

        for (GeneralCacheController cacheController : JaxenHelper.<GeneralCacheController>selectNodes(this, "simulation/processor/cacheHierarchy/cacheControllers[numDownwardAccesses > 0]")) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/hitRatio");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardAccesses");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardHits");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardMisses");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardReadHits");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardReadMisses");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardWriteHits");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numDownwardWriteMisses");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/numEvictions");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/cacheControllers[name ='" + cacheController.getName() + "']/occupancyRatio");
        }

        if(detailedSimulation) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/memoryController/accesses");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/memoryController/reads");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/processor/cacheHierarchy/memoryController/writes");
        }

        JaxenHelper.dumpStatsFromXPath(stats, this, "simulation/capabilities[class/simpleName='FunctionalExecutionProfilingCapability']/executedMnemonics");
        JaxenHelper.dumpStatsFromXPath(stats, this, "simulation/capabilities[class/simpleName='FunctionalExecutionProfilingCapability']/executedSyscalls");

        if(detailedSimulation) {
            //should be called only when simulation ends
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/summedUpUnstableHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numMTLLCHits");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numMTLLCMisses");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numTotalHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numUsefulHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numRedundantHitToTransientTagHTLLCRequests");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numRedundantHitToCacheHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numTimelyHTLLCRequests");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numLateHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numBadHTLLCRequests");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/numUglyHTLLCRequests");

            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/htLLCRequestAccuracy");
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "simulation/capabilities[class/simpleName='HTLLCRequestProfilingCapability']/htLLCRequestCoverage");
        }

        if(detailedSimulation) {
            Map<String, Object> statsCacheControllerFsms = new LinkedHashMap<String, Object>();
            JaxenHelper.<CacheHierarchy>selectSingleNode(this, "simulation/processor/cacheHierarchy").dumpCacheControllerFsmStats(statsCacheControllerFsms);
            MapHelper.save(statsCacheControllerFsms, simulation.getCwd() + "/stats_cacheControllerFsms.txt");
        }

        if(detailedSimulation) {
            Map<String, Object> statsLLCReuseDistanceProfilingCapability = new LinkedHashMap<String, Object>();
            JaxenHelper.<LLCReuseDistanceProfilingCapability>selectSingleNode(this, "simulation/capabilities[class/simpleName='LLCReuseDistanceProfilingCapability']").dumpStats(statsLLCReuseDistanceProfilingCapability);
            MapHelper.save(statsLLCReuseDistanceProfilingCapability, simulation.getCwd() + "/stats_llcReuseDistanceProfilingCapability.txt");
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
        this.simulation = new Simulation(this, title, strategy, this.simulationCapabilityClasses, blockingEventDispatcher, cycleAccurateEventQueue);
        this.blockingEventDispatcher.dispatch(new SimulationCreatedEvent(this));
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

    public List<ContextConfig> getContextConfigs() {
        return contextConfigs;
    }

    public ProcessorConfig getProcessorConfig() {
        return processorConfig;
    }

    public String getBeginTime() {
        return beginTime;
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

                        java.lang.Thread threadStartExperiment = new java.lang.Thread() {
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
