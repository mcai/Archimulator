/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.model.ExperimentStat;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.core.speculativePrecomputation.DynamicSpeculativePrecomputationHelper;
import archimulator.sim.isa.Memory;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.BasicMemoryHierarchy;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.cache.Interval.IntervalHelper;
import archimulator.sim.uncore.cache.interference.CacheInteractionHelper;
import archimulator.sim.uncore.cache.replacement.reuseDistancePrediction.ReuseDistancePredictionHelper;
import archimulator.sim.uncore.cache.stackDistanceProfile.StackDistanceProfilingHelper;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.delinquentLoad.DelinquentLoadIdentificationHelper;
import archimulator.sim.uncore.helperThread.FeedbackDirectedHelperThreadingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadL2RequestProfilingHelper;
import archimulator.sim.uncore.helperThread.hotspot.HotspotProfilingHelper;
import archimulator.sim.uncore.mlp.BLPProfilingHelper;
import archimulator.sim.uncore.mlp.MLPProfilingHelper;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import archimulator.util.RuntimeHelper;
import net.pickapack.collection.tree.NodeHelper;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.io.file.FileHelper;
import net.pickapack.util.Reference;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simulation.
 *
 * @author Min Cai
 */
public abstract class Simulation implements SimulationObject, Reportable {
    protected Reference<Kernel> kernelRef;

    private long beginTime;

    private long endTime;

    private SimulationType type;

    private Processor processor;

    private Experiment experiment;

    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    //TODO: the following stuffs are to be refactored out!!!

    private LatencyTrackingHelper latencyTrackingHelper;

    private StackDistanceProfilingHelper stackDistanceProfilingHelper;

    private ReuseDistancePredictionHelper reuseDistancePredictionHelper;

    private HotspotProfilingHelper hotspotProfilingHelper;

    private HelperThreadL2RequestProfilingHelper helperThreadL2RequestProfilingHelper;

    private CacheInteractionHelper cacheInteractionHelper;

    private FeedbackDirectedHelperThreadingHelper feedbackDirectedHelperThreadingHelper;

    private DelinquentLoadIdentificationHelper delinquentLoadIdentificationHelper;

    private DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper;

    private MLPProfilingHelper mlpProfilingHelper;

    private BLPProfilingHelper blpProfilingHelper;

    private IntervalHelper intervalHelper;

    private RuntimeHelper runtimeHelper;

    /**
     * Current (max) dynamic instruction ID.
     */
    public long currentDynamicInstructionId;

    /**
     * Current (max) reorder buffer entry ID.
     */
    public long currentReorderBufferEntryId;

    /**
     * Current (max) decode buffer entry ID.
     */
    public long currentDecodeBufferEntryId;

    /**
     * Current (max) memory hierarchy access ID.
     */
    public long currentMemoryHierarchyAccessId;

    /**
     * Current (max) net message ID.
     */
    public long currentNetMessageId;

    /**
     * Current (max) cache coherence flow ID.
     */
    public long currentCacheCoherenceFlowId;

    /**
     * Pending cache coherence flows.
     */
    public List<CacheCoherenceFlow> pendingFlows = new ArrayList<>();

    /**
     * Create a simulation.
     *
     * @param type                    the simulation type
     * @param experiment              the experiment object
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param kernelRef               the kernel reference
     */
    public Simulation(SimulationType type, Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Reference<Kernel> kernelRef) {
        this.experiment = experiment;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.type = type;

        File cwdFile = new File(this.getExperiment().getOutputDirectory());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        this.kernelRef = kernelRef;
        Kernel kernel = this.prepareKernel();

        if (!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.experiment, this, this.blockingEventDispatcher, this.cycleAccurateEventQueue, kernel, this.prepareMemoryHierarchy());

        this.latencyTrackingHelper = new LatencyTrackingHelper(this);

        this.stackDistanceProfilingHelper = new StackDistanceProfilingHelper(this);

        this.reuseDistancePredictionHelper = new ReuseDistancePredictionHelper(this);

        this.hotspotProfilingHelper = new HotspotProfilingHelper(this);

        this.helperThreadL2RequestProfilingHelper = new HelperThreadL2RequestProfilingHelper(this);

        this.cacheInteractionHelper = new CacheInteractionHelper(this);

        this.feedbackDirectedHelperThreadingHelper = new FeedbackDirectedHelperThreadingHelper(this);

        this.delinquentLoadIdentificationHelper = new DelinquentLoadIdentificationHelper(this);

        if (getExperiment().getDynamicSpeculativePrecomputationEnabled()) {
            this.dynamicSpeculativePrecomputationHelper = new DynamicSpeculativePrecomputationHelper(this);
        }

        this.mlpProfilingHelper = new MLPProfilingHelper(this);

        this.blpProfilingHelper = new BLPProfilingHelper(this);

        this.intervalHelper = new IntervalHelper(this);
    }

    /**
     * Perform the simulation.
     */
    public void simulate() {
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
            this.collectStats();

            this.endSimulation();

            throw new RuntimeException(e);
        }

        this.endTime = DateHelper.toTick(new Date());
        this.collectStats();

        this.endSimulation();
    }

    /**
     * Collect the statistics.
     */
    private void collectStats() {
        final List<ExperimentStat> stats = new ArrayList<>();

        ReportNode rootReportNode = new ReportNode(null, "");

        this.getRuntimeHelper().dumpStats(rootReportNode);

        this.dumpStats(rootReportNode);

        this.processor.dumpStats(rootReportNode);

        for (Memory memory : this.getProcessor().getKernel().getMemories()) {
            memory.dumpStats(rootReportNode);
        }

        for (Core core : this.getProcessor().getCores()) {
            core.dumpStats(rootReportNode);
        }

        for (Thread thread : this.getProcessor().getThreads()) {
            thread.dumpStats(rootReportNode);
        }

        for (TranslationLookasideBuffer tlb : this.getProcessor().getMemoryHierarchy().getTlbs()) {
            tlb.dumpStats(rootReportNode);
        }

        for (GeneralCacheController cacheController : this.getProcessor().getMemoryHierarchy().getCacheControllers()) {
            cacheController.dumpStats(rootReportNode);
        }

        this.getProcessor().getMemoryHierarchy().getMemoryController().dumpStats(rootReportNode);

        this.getLatencyTrackingHelper().dumpStats(rootReportNode);
        this.getStackDistanceProfilingHelper().dumpStats(rootReportNode);
        this.getReuseDistancePredictionHelper().dumpStats(rootReportNode);
        this.getHotspotProfilingHelper().dumpStats(rootReportNode);
        this.getHelperThreadL2RequestProfilingHelper().dumpStats(rootReportNode);
        this.getCacheInteractionHelper().dumpStats(rootReportNode);
        this.getFeedbackDirectedHelperThreadingHelper().dumpStats(rootReportNode);
        this.getDelinquentLoadIdentificationHelper().dumpStats(rootReportNode);
        this.getMlpProfilingHelper().dumpStats(rootReportNode);
        this.getIntervalHelper().dumpStats(rootReportNode);

        this.getProcessor().getMemoryHierarchy().getL2Controller().getCache().getReplacementPolicy().dumpStats(rootReportNode);

        rootReportNode.traverse(node -> stats.add(new ExperimentStat(getPrefix(), node.getPath(), node.getValue())));

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            getProcessor().getMemoryHierarchy().dumpCacheControllerFsmStats(stats);
        }

        this.getExperiment().getStats().addAll(stats);
    }

    /**
     * Get a value indicating whether it can do fast forwarding one cycle or not.
     *
     * @return a value indicating whether it can fast forwarding one cycle or not
     */
    protected abstract boolean canDoFastForwardOneCycle();

    /**
     * Get a value indicating whether it can do cache warmup one cycle or not.
     *
     * @return a value indicating whether it can do cache warmup one cycle or not
     */
    protected abstract boolean canDoCacheWarmupOneCycle();

    /**
     * Get a value indicating whether it can do measurement one cycle or not.
     *
     * @return a value indicating whether it can do measurement one cycle or not
     */
    protected abstract boolean canDoMeasurementOneCycle();

    /**
     * Begin the simulation.
     */
    protected abstract void beginSimulation();

    /**
     * End the simulation.
     */
    protected abstract void endSimulation();

    /**
     * Do fast forwarding.
     */
    public void doFastForward() {
        Logger.info(Logger.SIMULATION, "Switched to fast forward mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoFastForwardOneCycle()) {
            this.getProcessor().getCores().forEach(Core::doFastForwardOneCycle);
            this.advanceOneCycle();
        }
    }

    /**
     * Do cache warmup.
     */
    public void doCacheWarmup() {
        Logger.info(Logger.SIMULATION, "Switched to cache warmup mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoCacheWarmupOneCycle()) {
            this.getProcessor().getCores().forEach(Core::doCacheWarmupOneCycle);
            this.advanceOneCycle();
        }
    }

    /**
     * Do measurement.
     */
    public void doMeasurement() {
        Logger.info(Logger.SIMULATION, "Switched to measurement mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoMeasurementOneCycle()) {
            this.getProcessor().getCores().forEach(Core::doMeasurementOneCycle);
            this.advanceOneCycle();
        }
    }

    /**
     * Advance one cycle.
     */
    private void advanceOneCycle() {
        this.doHouseKeeping();
        this.getCycleAccurateEventQueue().advanceOneCycle();
    }

    /**
     * Do housekeeping work.
     */
    public void doHouseKeeping() {
        this.getProcessor().getKernel().advanceOneCycle();
        this.getProcessor().updateContextToThreadAssignments();
    }

    /**
     * Prepare the kernel.
     *
     * @return the kernel that is prepared
     */
    public Kernel prepareKernel() {
        Kernel kernel = new Kernel(this);

        for (final ContextMapping contextMapping : this.getExperiment().getContextMappings()) {
            final Context context = Context.load(kernel, this.getExperiment().getOutputDirectory(), contextMapping);

            if (!kernel.map(context, candidateThreadId -> candidateThreadId == contextMapping.getThreadId())) {
                throw new RuntimeException();
            }

            kernel.getContexts().add(context);
        }
        return kernel;
    }

    /**
     * Prepare the memory hierarchy.
     *
     * @return the memory hierarchy that is prepared
     */
    public MemoryHierarchy prepareMemoryHierarchy() {
        return new BasicMemoryHierarchy(this.getExperiment(), this, this.getBlockingEventDispatcher(), this.getCycleAccurateEventQueue());
    }

    /**
     * Dump the tree of the pending cache coherence flows.
     */
    public void dumpPendingFlowTree() {
        for (CacheCoherenceFlow pendingFlow : this.pendingFlows) {
            NodeHelper.print(pendingFlow);
            System.out.println();
        }

        System.out.println();
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "simulation") {{
            getChildren().add(new ReportNode(this, "beginTimeAsString", getBeginTimeAsString()));
            getChildren().add(new ReportNode(this, "endTimeAsString", getEndTimeAsString()));
            getChildren().add(new ReportNode(this, "duration", getDuration()));
            getChildren().add(new ReportNode(this, "durationInSeconds", getDurationInSeconds() + ""));

            getChildren().add(new ReportNode(this, "cycleAccurateEventQueue/currentCycle", getCycleAccurateEventQueue().getCurrentCycle() + ""));
        }});
    }

    /**
     * Get the simulation type.
     *
     * @return the simulation type
     */
    public SimulationType getType() {
        return type;
    }

    /**
     * Get the time in ticks when the simulation begins.
     *
     * @return the time in ticks when the simulation begins
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Get the time in ticks when the simulation ends.
     *
     * @return the time in ticks when the simulation ends
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Get the string representation of the time when the simulation begins.
     *
     * @return the string representation of the time when the simulation begins
     */
    public String getBeginTimeAsString() {
        return DateHelper.toString(beginTime);
    }

    /**
     * Get the string representation of the time when the simulation ends.
     *
     * @return the string representation of the time when the simulation ends
     */
    public String getEndTimeAsString() {
        return DateHelper.toString(endTime);
    }

    /**
     * Get the duration in seconds that the simulation lasts.
     *
     * @return the duration in seconds that the simulation lasts
     */
    public long getDurationInSeconds() {
        return (this.getEndTime() - this.getBeginTime()) / 1000;
    }

    /**
     * Get the string representation of the duration that the simulation lasts.
     *
     * @return the string representation of the duration that the simulation lasts
     */
    public String getDuration() {
        return DurationFormatUtils.formatDurationHMS(this.getEndTime() - this.getBeginTime());
    }

    /**
     * Get the processor object.
     *
     * @return the processor object
     */
    public Processor getProcessor() {
        return this.processor;
    }

    /**
     * Get the experiment object.
     *
     * @return the experiment object
     */
    public Experiment getExperiment() {
        return experiment;
    }

    @Override
    public Simulation getSimulation() {
        return this;
    }

    /**
     * Get the cycle accurate event queue.
     *
     * @return the cycle accurate event queue
     */
    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    /**
     * Get the blocking event dispatcher.
     *
     * @return the blocking event dispatcher
     */
    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    /**
     * Get the latency tracking helper.
     *
     * @return the latency tracking helper
     */
    public LatencyTrackingHelper getLatencyTrackingHelper() {
        return latencyTrackingHelper;
    }

    /**
     * Get the stack distance profiling helper.
     *
     * @return the stack distance profiling helper
     */
    public StackDistanceProfilingHelper getStackDistanceProfilingHelper() {
        return stackDistanceProfilingHelper;
    }

    /**
     * Get the reuse distance prediction helper.
     *
     * @return the reuse distance prediction helper
     */
    public ReuseDistancePredictionHelper getReuseDistancePredictionHelper() {
        return reuseDistancePredictionHelper;
    }

    /**
     * Get the hotspot profiling helper.
     *
     * @return the hotspot profiling helper
     */
    public HotspotProfilingHelper getHotspotProfilingHelper() {
        return hotspotProfilingHelper;
    }

    /**
     * Get the helper thread L2 cache request profiling helper.
     *
     * @return the helper thread L2 cache request profiling helper
     */
    public HelperThreadL2RequestProfilingHelper getHelperThreadL2RequestProfilingHelper() {
        return helperThreadL2RequestProfilingHelper;
    }

    /**
     * Get the cache interaction helper.
     *
     * @return the cache interaction helper
     */
    public CacheInteractionHelper getCacheInteractionHelper() {
        return cacheInteractionHelper;
    }

    /**
     * Get the feedback directed helper threading helper.
     *
     * @return the feedback directed helper threading helper
     */
    public FeedbackDirectedHelperThreadingHelper getFeedbackDirectedHelperThreadingHelper() {
        return feedbackDirectedHelperThreadingHelper;
    }

    /**
     * Get the delinquent load identification helper.
     *
     * @return the delinquent load identification helper
     */
    public DelinquentLoadIdentificationHelper getDelinquentLoadIdentificationHelper() {
        return delinquentLoadIdentificationHelper;
    }

    /**
     * Get the dynamic speculative precomputation helper.
     *
     * @return the dynamic speculative precomputation helper
     */
    public DynamicSpeculativePrecomputationHelper getDynamicSpeculativePrecomputationHelper() {
        return dynamicSpeculativePrecomputationHelper;
    }

    /**
     * Get the MLP profiling helper.
     *
     * @return the MLP profiling helper
     */
    public MLPProfilingHelper getMlpProfilingHelper() {
        return mlpProfilingHelper;
    }

    /**
     * Get the BLP profiling helper.
     *
     * @return the BLP profiling helper
     */
    public BLPProfilingHelper getBlpProfilingHelper() {
        return blpProfilingHelper;
    }

    /**
     * Get the interval helper.
     *
     * @return the interval helper
     */
    public IntervalHelper getIntervalHelper() {
        return intervalHelper;
    }

    /**
     * Get the runtime helper.
     *
     * @return the runtime helper
     */
    public RuntimeHelper getRuntimeHelper() {
        if (runtimeHelper == null) {
            runtimeHelper = new RuntimeHelper();
        }

        return runtimeHelper;
    }

    @Override
    public String getName() {
        return "simulation";
    }

    /**
     * Get the simulation title prefix.
     *
     * @return the simulation title prefix
     */
    public abstract String getPrefix();
}
