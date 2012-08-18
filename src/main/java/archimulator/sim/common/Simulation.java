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
import archimulator.sim.uncore.ht.HTLLCRequestProfilingHelper;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import archimulator.util.JaxenHelper;
import net.pickapack.StorageUnit;
import net.pickapack.action.Predicate;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.io.file.FileHelper;
import net.pickapack.io.serialization.MapHelper;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.File;
import java.sql.SQLException;
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

    private HTLLCRequestProfilingHelper htllcRequestProfilingHelper;

    public Simulation(String title, SimulationType type, Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.experiment = experiment;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.title = title;
        this.type = type;

        File cwdFile = new File(this.getCwd());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        Kernel kernel = this.prepareKernel();

        if (!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.experiment, this.blockingEventDispatcher, this.cycleAccurateEventQueue, kernel, this.prepareCacheHierarchy());

        if (getExperiment().getArchitecture().getHtLLCRequestProfilingEnabled()) {
            this.htllcRequestProfilingHelper = new HTLLCRequestProfilingHelper(this);
        }
    }

    public Kernel createKernel() {
        Kernel kernel = new Kernel(this, getExperiment().getArchitecture().getNumCores(), getExperiment().getArchitecture().getNumThreadsPerCore());

        for (final ContextMapping contextMapping : this.getExperiment().getContextMappings()) {
            final Context context = Context.load(kernel, this.getCwd(), contextMapping);

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

            try {
                ServiceManager.getExperimentService().updateExperiment(this.experiment);
                ServiceManager.getExperimentService().dumpExperiment(experiment);
            } catch (SQLException e1) {
                throw new RuntimeException(e);
            }

            System.err.println("Simulation aborted with errors");
            System.exit(-1);
        }

        this.endTime = DateHelper.toTick(new Date());
        this.collectStats(true);

        this.endSimulation();

        resetIdCounters();

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
        JaxenHelper.dumpValueFromXPath(stats, this, "totalInsts");

        JaxenHelper.dumpValueFromXPath(stats, this, "instsPerCycle");
        JaxenHelper.dumpValueFromXPath(stats, this, "cyclesPerSecond");
        JaxenHelper.dumpValueFromXPath(stats, this, "instsPerSecond");

        for (Memory memory : JaxenHelper.<Memory>selectNodes(this, "processor/kernel/processes/memory")) {
            JaxenHelper.dumpValueFromXPath(stats, this, "processor/kernel/processes/memory[id='" + memory.getId() + "']/numPages");
        }

        for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInsts>0]")) {
            JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/totalInsts");
            JaxenHelper.dumpValuesFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/executedMnemonics");
            JaxenHelper.dumpValuesFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/executedSystemCalls");
        }

        if (this.getType() == SimulationType.MEASUREMENT) {
            for (Core core : JaxenHelper.<Core>selectNodes(this, "processor/cores[threads[totalInsts>0]]")) {
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores[name='" + core.getName() + "']/fuPool/noFreeFu");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores[name='" + core.getName() + "']/fuPool/acquireFailedOnNoFreeFu");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInsts>0]")) {
                for (TranslationLookasideBuffer tlb : JaxenHelper.<TranslationLookasideBuffer>selectNodes(this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs")) {
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/hitRatio");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/accesses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/hits");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/misses");
                    JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/tlbs[name='" + tlb.getName() + "']/evictions");
                }
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT) {
            for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInsts>0]")) {
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/bpred/type");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/bpred/accesses");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/bpred/hits");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/bpred/misses");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cores/threads[name='" + thread.getName() + "']/bpred/hitRatio");

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
            if ((Long) JaxenHelper.selectSingleNode(this, "processor/cacheHierarchy/memoryController/accesses") > 0) {
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

                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/accesses");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/reads");
                JaxenHelper.dumpValueFromXPath(stats, this, "processor/cacheHierarchy/memoryController/writes");
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            if(endOfSimulation) {
                JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/summedUpUnstableHTLLCRequests");
            }

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numMTLLCHits");
            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numMTLLCMisses");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numTotalHTLLCRequests");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numUsefulHTLLCRequests");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numRedundantHitToTransientTagHTLLCRequests");
            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numRedundantHitToCacheHTLLCRequests");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numTimelyHTLLCRequests");
            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numLateHTLLCRequests");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numBadHTLLCRequests");
            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/numUglyHTLLCRequests");

            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/htLLCRequestAccuracy");
            JaxenHelper.dumpValueFromXPath(stats, this, "htllcRequestProfilingHelper/htLLCRequestCoverage");
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            Map<String, String> statsCacheControllerFsms = new LinkedHashMap<String, String>();
            JaxenHelper.<CacheHierarchy>selectSingleNode(this, "processor/cacheHierarchy").dumpCacheControllerFsmStats(statsCacheControllerFsms);
            MapHelper.save(statsCacheControllerFsms, this.getCwd() + "/stats_cacheControllerFsms.txt");
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

        if(this.getCycleAccurateEventQueue().getCurrentCycle() % (this.type == SimulationType.FAST_FORWARD ? 100000000 : 10000000) == 0) {
            this.endTime = DateHelper.toTick(new Date());
            this.collectStats(false);
            try {
                ServiceManager.getExperimentService().updateExperiment(this.experiment);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
        return new BasicCacheHierarchy(this.getExperiment(), this.getBlockingEventDispatcher(), this.getCycleAccurateEventQueue());
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

    public long getTotalInsts() {
        long totalInsts = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                totalInsts += thread.getTotalInsts();
            }
        }

        return totalInsts;
    }

    public double getInstsPerCycle() {
        return (double) this.getTotalInsts() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    public double getCyclesPerSecond() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getDurationInSeconds();
    }

    public double getInstsPerSecond() {
        return (double) this.getTotalInsts() / this.getDurationInSeconds();
    }

    public String getTitle() {
        return title;
    }

    public String getCwd() {
        return "experiments" + File.separator + this.title;
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    public HTLLCRequestProfilingHelper getHtllcRequestProfilingHelper() {
        return htllcRequestProfilingHelper;
    }

    public static int currentMemoryPageId = 0;
    public static int currentProcessId = 0;

    public static long currentDynamicInstructionId = 0;
    public static long currentReorderBufferEntryId = 0;
    public static long currentDecodeBufferEntryId = 0;
    public static long currentMemoryHierarchyAccessId = 0;

    public static long currentCacheAccessId = 0;
    public static long currentNetMessageId;
    public static long currentCoherentCacheProcessId = 0;

    private static void resetIdCounters() {
        currentMemoryPageId = 0;
        currentProcessId = 0;

        currentDynamicInstructionId = 0;
        currentReorderBufferEntryId = 0;
        currentDecodeBufferEntryId = 0;
        currentMemoryHierarchyAccessId = 0;

        currentCacheAccessId = 0;
        currentNetMessageId = 0;
        currentCoherentCacheProcessId = 0;
    }
}
