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
package archimulator.sim.common;

import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.service.ServiceManager;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.isa.Memory;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.BasicCacheHierarchy;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import archimulator.util.JaxenHelper;
import net.pickapack.StorageUnit;
import net.pickapack.action.Predicate;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.io.file.FileHelper;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Simulation implements SimulationObject {
    private String title;

    private long beginTime;

    private long endTime;

    private SimulationType type;

    private Processor processor;

    private Experiment experiment;

    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper;

    public long currentDynamicInstructionId;
    public long currentReorderBufferEntryId;
    public long currentDecodeBufferEntryId;
    public long currentMemoryHierarchyAccessId;
    public long currentNetMessageId;
    public long currentCacheCoherenceFlowId;

    public Simulation(String title, SimulationType type, Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.experiment = experiment;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.title = title;
        this.type = type;

        File cwdFile = new File(this.getWorkingDirectory());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        Kernel kernel = this.prepareKernel();

        if (!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.experiment, this, this.blockingEventDispatcher, this.cycleAccurateEventQueue, kernel, this.prepareCacheHierarchy());

        if (getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled()) {
            this.helperThreadL2CacheRequestProfilingHelper = new HelperThreadL2CacheRequestProfilingHelper(this);
        }
    }

    public Kernel createKernel() {
        Kernel kernel = new Kernel(this);

        for (final ContextMapping contextMapping : this.getExperiment().getContextMappings()) {
            final Context context = Context.load(kernel, this.getWorkingDirectory(), contextMapping);

            if (!kernel.map(context, new Predicate<Integer>() {
                @Override
                public boolean apply(Integer candidateThreadId) {
                    return candidateThreadId == contextMapping.getThreadId();
                }
            })) {
                throw new RuntimeException();
            }

            kernel.getContexts().add(context);
        }
        return kernel;
    }

    public void simulate() {
        Logger.infof(Logger.SIMULATOR, "begin simulation: %s", this.cycleAccurateEventQueue.getCurrentCycle(), this.getTitle());

        Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

        Logger.infof(Logger.SIMULATION, "  architecture: %s", this.cycleAccurateEventQueue.getCurrentCycle(), getExperiment().getArchitecture());

        Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

        this.beginTime = DateHelper.toTick(new Date());

        this.beginSimulation();

        try {
            if (this.getType() == SimulationType.FAST_FORWARD) {
                this.doFastForward();
            } else if (this.getType() == SimulationType.CACHE_WARMUP) {
                this.doCacheWarmup();
            } else if (this.getType() == SimulationType.MEASUREMENT) {
                this.doMeasurement();
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.endTime = DateHelper.toTick(new Date());
            this.collectStats(true);

            this.experiment.setState(ExperimentState.ABORTED);
            this.experiment.setFailedReason(e.toString());

            ServiceManager.getExperimentService().updateExperiment(this.experiment);
            ServiceManager.getExperimentService().dumpExperiment(this.experiment);

            System.err.println("Simulation aborted with errors");
            System.exit(-1);
        }

        this.endTime = DateHelper.toTick(new Date());
        this.collectStats(true);

        this.endSimulation();

        System.out.println();
    }

    private void collectStats(boolean endOfSimulation) {
        Map<String, String> stats = new LinkedHashMap<String, String>();

        JaxenHelper.dumpValueFromXPath(stats, this, "title");
        stats.put("running", "" + !endOfSimulation);

        JaxenHelper.dumpValueFromXPath(stats, this, "beginTimeAsString");
        JaxenHelper.dumpValueFromXPath(stats, this, "endTimeAsString");
        JaxenHelper.dumpValueFromXPath(stats, this, "duration");
        JaxenHelper.dumpValueFromXPath(stats, this, "durationInSeconds");

        stats.put("runtime/maxMemory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
        stats.put("runtime/totalMemory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
        stats.put("runtime/usedMemory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));

        JaxenHelper.dumpValueFromXPath(stats, this, "cycleAccurateEventQueue/currentCycle");
        JaxenHelper.dumpValueFromXPath(stats, this, "totalInstructions");

        JaxenHelper.dumpValueFromXPath(stats, this, "instructionsPerCycle");
        JaxenHelper.dumpValueFromXPath(stats, this, "cyclesPerSecond");
        JaxenHelper.dumpValueFromXPath(stats, this, "instructionsPerSecond");

        for (Memory memory : JaxenHelper.<Memory>selectNodes(this, "processor/kernel/processes/memory")) {
            JaxenHelper.dumpValueFromXPath(stats, this, "processor/kernel/processes/memory[id='" + memory.getId() + "']/numPages");
        }

        for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInstructions>0]")) {
            JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/totalInstructions");
            JaxenHelper.dumpValuesFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/executedMnemonics");
            JaxenHelper.dumpValuesFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/executedSystemCalls");
        }

        if (this.getType() == SimulationType.MEASUREMENT) {
            for (Core core : JaxenHelper.<Core>selectNodes(this, "processor/cores[threads[totalInstructions>0]]")) {
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores[name='" + core.getName() + "']/functionalUnitPool/noFreeFunctionalUnit");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores[name='" + core.getName() + "']/functionalUnitPool/acquireFailedOnNoFreeFunctionalUnit");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInstructions>0]")) {
                for (TranslationLookasideBuffer tlb : JaxenHelper.<TranslationLookasideBuffer>selectNodes(this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs")) {
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/hitRatio");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/numAccesses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/numHits");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/numMisses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/numEvictions");
                }
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT) {
            for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInstructions>0]")) {
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/branchPredictor/type");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/branchPredictor/hitRatio");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/branchPredictor/numAccesses");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/branchPredictor/numHits");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/branchPredictor/numMisses");

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/decodeBufferFull");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/reorderBufferFull");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/loadStoreQueueFull");

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/intPhysicalRegisterFileFull");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/fpPhysicalRegisterFileFull");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/miscPhysicalRegisterFileFull");

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/fetchStallsOnDecodeBufferIsFull");

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/registerRenameStallsOnDecodeBufferIsEmpty");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/registerRenameStallsOnReorderBufferIsFull");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/registerRenameStallsOnLoadStoreQueueFull");

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/selectionStallOnCanNotLoad");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/selectionStallOnCanNotStore");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/selectionStallOnNoFreeFunctionalUnit");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            if ((Long) JaxenHelper.selectSingleNode(this, "processor/cacheHierarchy/memoryController/numAccesses") > 0) {
                for (GeneralCacheController cacheController : JaxenHelper.<GeneralCacheController>selectNodes(this, "processor/cacheHierarchy/cacheControllers[numDownwardAccesses>0]")) {
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/hitRatio");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardAccesses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardHits");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardMisses");

                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardReadHits");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardReadMisses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardWriteHits");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numDownwardWriteMisses");

                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/numEvictions");

                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/cacheControllers[name='" + cacheController.getName() + "']/occupancyRatio");
                }

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/numAccesses");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/numReads");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/numWrites");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            if ((Long) JaxenHelper.selectSingleNode(this, "helperThreadL2CacheRequestProfilingHelper/numTotalHelperThreadL2CacheRequests") > 0) {
                if (endOfSimulation) {
                    JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/summedUpUnstableHelperThreadL2CacheRequests");
                }

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheHits");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numMainThreadL2CacheMisses");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheHits");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numHelperThreadL2CacheMisses");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numTotalHelperThreadL2CacheRequests");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToTransientTagHelperThreadL2CacheRequests");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numRedundantHitToCacheHelperThreadL2CacheRequests");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numUsefulHelperThreadL2CacheRequests");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numTimelyHelperThreadL2CacheRequests");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numLateHelperThreadL2CacheRequests");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numBadHelperThreadL2CacheRequests");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/numUglyHelperThreadL2CacheRequests");

                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestCoverage");
                JaxenHelper.dumpValueFromXPath(stats, this, "helperThreadL2CacheRequestProfilingHelper/helperThreadL2CacheRequestAccuracy");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            JaxenHelper.<CacheHierarchy>selectSingleNode(this, "processor/cacheHierarchy").dumpCacheControllerFsmStats(stats);
        }

        stats = this.getStatsWithSimulationPrefix(stats);

        this.getExperiment().getStats().putAll(stats);
    }

    private Map<String, String> getStatsWithSimulationPrefix(Map<String, String> stats) {
        String title = this.getTitle();
        String simulationPrefix = title.substring(title.indexOf("/") + 1);

        Map<String, String> result = new LinkedHashMap<String, String>();
        for (String key : stats.keySet()) {
            result.put(simulationPrefix + "/" + key, stats.get(key));
        }

        return result;
    }

    protected abstract boolean canDoFastForwardOneCycle();

    protected abstract boolean canDoCacheWarmupOneCycle();

    protected abstract boolean canDoMeasurementOneCycle();

    protected abstract void beginSimulation();

    protected abstract void endSimulation();

    public void doFastForward() {
        Logger.info(Logger.SIMULATION, "Switched to fast forward mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoFastForwardOneCycle()) {
            for (Core core : this.getProcessor().getCores()) {
                core.doFastForwardOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    public void doCacheWarmup() {
        Logger.info(Logger.SIMULATION, "Switched to cache warmup mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoCacheWarmupOneCycle()) {
            for (Core core : this.getProcessor().getCores()) {
                core.doCacheWarmupOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    public void doMeasurement() {
        Logger.info(Logger.SIMULATION, "Switched to measurement mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoMeasurementOneCycle()) {
            for (Core core : this.getProcessor().getCores()) {
                core.doMeasurementOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    private void advanceOneCycle() {
        this.doHouseKeeping();
        this.getCycleAccurateEventQueue().advanceOneCycle();

        if (this.getCycleAccurateEventQueue().getCurrentCycle() % (this.type == SimulationType.FAST_FORWARD ? 100000000 : 10000000) == 0) {
            this.endTime = DateHelper.toTick(new Date());
            this.collectStats(false);
            ServiceManager.getExperimentService().updateExperiment(this.experiment);
        }
    }

    public void doHouseKeeping() {
        this.getProcessor().getKernel().advanceOneCycle();
        this.getProcessor().updateContextToThreadAssignments();
    }

    public Kernel prepareKernel() {
        return this.createKernel();
    }

    public CacheHierarchy prepareCacheHierarchy() {
        return new BasicCacheHierarchy(this.getExperiment(), this, this.getBlockingEventDispatcher(), this.getCycleAccurateEventQueue());
    }

    public SimulationType getType() {
        return type;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getBeginTimeAsString() {
        return DateHelper.toString(beginTime);
    }

    public String getEndTimeAsString() {
        return DateHelper.toString(endTime);
    }

    public long getDurationInSeconds() {
        return (this.getEndTime() - this.getBeginTime()) / 1000;
    }

    public String getDuration() {
        return DurationFormatUtils.formatDurationHMS(this.getEndTime() - this.getBeginTime());
    }

    public long getTotalInstructions() {
        long totalInstructions = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                totalInstructions += thread.getTotalInstructions();
            }
        }

        return totalInstructions;
    }

    public double getInstructionsPerCycle() {
        return (double) this.getTotalInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    public double getCyclesPerSecond() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getDurationInSeconds();
    }

    public double getInstructionsPerSecond() {
        return (double) this.getTotalInstructions() / this.getDurationInSeconds();
    }

    public String getTitle() {
        return title;
    }

    public String getWorkingDirectory() {
        return "experiments" + File.separator + this.title;
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    @Override
    public Simulation getSimulation() {
        return this;
    }

    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    public HelperThreadL2CacheRequestProfilingHelper getHelperThreadL2CacheRequestProfilingHelper() {
        return helperThreadL2CacheRequestProfilingHelper;
    }
}
