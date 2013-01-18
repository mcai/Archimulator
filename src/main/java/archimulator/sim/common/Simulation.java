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
package archimulator.sim.common;

import archimulator.model.ContextMapping;
import archimulator.model.Experiment;
import archimulator.model.metric.ExperimentStat;
import archimulator.model.metric.gauge.ExperimentGauge;
import archimulator.service.ServiceManager;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.core.speculativePrecomputation.DynamicSpeculativePrecomputationHelper;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.BasicMemoryHierarchy;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.delinquentLoad.DelinquentLoadIdentificationHelper;
import archimulator.sim.uncore.helperThread.FeedbackDirectedHelperThreadingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestQuality;
import archimulator.sim.uncore.helperThread.hotspot.HotspotProfilingHelper;
import archimulator.util.RuntimeHelper;
import net.pickapack.Reference;
import net.pickapack.action.Predicate;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.io.file.FileHelper;
import net.pickapack.tree.NodeHelper;
import net.pickapack.util.JaxenHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.io.File;
import java.util.*;

/**
 * Simulation.
 *
 * @author Min Cai
 */
public abstract class Simulation implements SimulationObject {
    protected Reference<Kernel> kernelRef;

    private String title;

    private long beginTime;

    private long endTime;

    private SimulationType type;

    private Processor processor;

    private Experiment experiment;

    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private HotspotProfilingHelper hotspotProfilingHelper;

    private HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper;

    private FeedbackDirectedHelperThreadingHelper feedbackDirectedHelperThreadingHelper;

    private DelinquentLoadIdentificationHelper delinquentLoadIdentificationHelper;

    private DynamicSpeculativePrecomputationHelper dynamicSpeculativePrecomputationHelper;

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
    public List<CacheCoherenceFlow> pendingFlows = new ArrayList<CacheCoherenceFlow>();

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

        this.title = experiment.getId() + "/" + getPrefix();
        this.type = type;

        File cwdFile = new File(this.getWorkingDirectory());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        this.kernelRef = kernelRef;
        Kernel kernel = this.prepareKernel();

        if (!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.experiment, this, this.blockingEventDispatcher, this.cycleAccurateEventQueue, kernel, this.prepareMemoryHierarchy());

        if (getExperiment().getArchitecture().getHotspotProfilingEnabled()) {
            this.hotspotProfilingHelper = new HotspotProfilingHelper(this);
        }

        if (getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled()) {
            this.helperThreadL2CacheRequestProfilingHelper = new HelperThreadL2CacheRequestProfilingHelper(this);
        }

        //TODO: handle configuration support of conditional attribute
        this.feedbackDirectedHelperThreadingHelper = new FeedbackDirectedHelperThreadingHelper(this);

        if (getExperiment().getArchitecture().getDelinquentLoadIdentificationEnabled()) {
            this.delinquentLoadIdentificationHelper = new DelinquentLoadIdentificationHelper(this);
        }

        if (getExperiment().getArchitecture().getDynamicSpeculativePrecomputationEnabled()) {
            this.dynamicSpeculativePrecomputationHelper = new DynamicSpeculativePrecomputationHelper(this);
        }
    }

    /**
     * Create the kernel.
     *
     * @return the kernel that is created
     */
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

    /**
     * Perform the simulation.
     */
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

            this.endSimulation();

            throw new RuntimeException(e);
        }

        this.endTime = DateHelper.toTick(new Date());
        this.collectStats(true);

        this.endSimulation();
    }

    /**
     * Collect the statistics.
     *
     * @param endOfSimulation a value indicating whether it is the end of simulation or not
     */
    private void collectStats(boolean endOfSimulation) {
        List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

        if (endOfSimulation && this.getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled() && (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP)) {
            this.getHelperThreadL2CacheRequestProfilingHelper().sumUpUnstableHelperThreadL2CacheRequests();
        }

        for (ExperimentGauge gauge : ServiceManager.getExperimentMetricService().getGaugesByExperiment(experiment)) {
            Object node = StringUtils.isEmpty(gauge.getType().getNodeExpression()) ? this : JaxenHelper.evaluate(this, gauge.getType().getNodeExpression());

            if (node instanceof Collection) {
                for (Object obj : (Collection) node) {
                    collectStats(stats, gauge, obj);
                }
            } else {
                collectStats(stats, gauge, node);
            }
        }

        if (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP) {
            getProcessor().getMemoryHierarchy().dumpCacheControllerFsmStats(stats);
        }

        if (endOfSimulation && this.getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled() && (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP)) {
            CacheBasedPredictor<HelperThreadL2CacheRequestQuality> helperThreadL2CacheRequestQualityPredictor = (CacheBasedPredictor<HelperThreadL2CacheRequestQuality>) this.getHelperThreadL2CacheRequestProfilingHelper().getHelperThreadL2CacheRequestQualityPredictor();
            helperThreadL2CacheRequestQualityPredictor.dumpState();

        }

        ServiceManager.getExperimentStatService().addStatsByParent(this.getExperiment(), stats);
    }

    /**
     * Collect the statistics.
     *
     * @param stats the list of experiment statistics
     * @param gauge the experiment gauge object
     * @param obj   the owning object
     */
    private void collectStats(List<ExperimentStat> stats, ExperimentGauge gauge, Object obj) {
        Object keyObj = !StringUtils.isEmpty(gauge.getType().getKeyExpression()) ? JaxenHelper.evaluate(obj, gauge.getType().getKeyExpression()) : null;
        Object valueObj;
        valueObj = JaxenHelper.evaluate(obj, gauge.getValueExpression());

        String initialNodeKey = getNodeKey(gauge, obj, keyObj);

        if (valueObj != null) {
            if (valueObj instanceof Map) {
                Map resultMap = (Map) valueObj;

                for (Object key : resultMap.keySet()) {
                    String nodeKey = JaxenHelper.escape(initialNodeKey) + "/" + key;
                    String value = JaxenHelper.toString(resultMap.get(key));

                    addStat(stats, gauge, nodeKey, value);
                }
            } else if (valueObj instanceof Collection) {
                Collection resultCollection = (Collection) valueObj;

                int i = 0;
                for (Object obj1 : resultCollection) {
                    String nodeKey = JaxenHelper.escape(initialNodeKey) + "/" + (i++);
                    String value = JaxenHelper.toString(obj1);

                    addStat(stats, gauge, nodeKey, value);
                }
            } else {
                String nodeKey = JaxenHelper.escape(initialNodeKey);
                String value = JaxenHelper.toString(valueObj);

                addStat(stats, gauge, nodeKey, value);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void addStat(List<ExperimentStat> stats, ExperimentGauge gauge, String nodeKey, String value) {
        stats.add(new ExperimentStat(experiment, getPrefix(), gauge, nodeKey, value));
    }

    private String getNodeKey(ExperimentGauge gauge, Object obj, Object keyObj) {
        String key = "";

        if (obj instanceof Named) {
            key = ((Named) obj).getName();
        } else {
            key = String.format("%s", !StringUtils.isEmpty(gauge.getType().getNodeExpression()) ? gauge.getType().getNodeExpression() : "simulation");

            if (!StringUtils.isEmpty(gauge.getType().getKeyExpression())) {
                key += "/" + keyObj;
            }
        }
        return key;
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
            for (Core core : this.getProcessor().getCores()) {
                core.doFastForwardOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    /**
     * Do cache warmup.
     */
    public void doCacheWarmup() {
        Logger.info(Logger.SIMULATION, "Switched to cache warmup mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoCacheWarmupOneCycle()) {
            for (Core core : this.getProcessor().getCores()) {
                core.doCacheWarmupOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    /**
     * Do measurement.
     */
    public void doMeasurement() {
        Logger.info(Logger.SIMULATION, "Switched to measurement mode.", this.getCycleAccurateEventQueue().getCurrentCycle());

        while (!this.getProcessor().getKernel().getContexts().isEmpty() && this.canDoMeasurementOneCycle()) {
            for (Core core : this.getProcessor().getCores()) {
                core.doMeasurementOneCycle();
            }

            this.advanceOneCycle();
        }
    }

    /**
     * Advance one cycle.
     */
    private void advanceOneCycle() {
        this.doHouseKeeping();
        this.getCycleAccurateEventQueue().advanceOneCycle();

        if (this.getCycleAccurateEventQueue().getCurrentCycle() % (this.type == SimulationType.FAST_FORWARD ? 100000000 : 10000000) == 0) {
            this.endTime = DateHelper.toTick(new Date());
            this.collectStats(false);
            ServiceManager.getExperimentService().updateExperiment(this.experiment);
        }
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
        return this.createKernel();
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
        for (CacheCoherenceFlow pendingFlow : pendingFlows) {
            NodeHelper.print(pendingFlow);
            System.out.println();
        }

        System.out.println();
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
     * Get the number of instructions that the simulation has executed till now.
     *
     * @return the number of instructions that the simulation has executed till now.
     */
    public long getNumInstructions() {
        long numInstructions = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                numInstructions += thread.getNumInstructions();
            }
        }

        return numInstructions;
    }

    /**
     * Get the IPC (instructions per cycle) value.
     *
     * @return the IPC (instructions per cycle) value
     */
    public double getInstructionsPerCycle() {
        return (double) this.getNumInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the CPI (cycles per instruction) value.
     *
     * @return the CPI (cycles per instruction) value
     */
    public double getCyclesPerInstruction() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getNumInstructions();
    }

    /**
     * Get the simulation speed expressed as the CPS (cycles per second) value.
     *
     * @return the CPS (cycles per second) value
     */
    public double getCyclesPerSecond() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getDurationInSeconds();
    }

    /**
     * Get the simulation speed expressed as the IPS (instructions per second) value.
     *
     * @return the IPS (instructions per second) value
     */
    public double getInstructionsPerSecond() {
        return (double) this.getNumInstructions() / this.getDurationInSeconds();
    }

    /**
     * Get the simulation title.
     *
     * @return the simulation title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the simulation's working directory.
     *
     * @return the simulation's working directory
     */
    public String getWorkingDirectory() {
        return "experiments" + File.separator + this.title;
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
    public HelperThreadL2CacheRequestProfilingHelper getHelperThreadL2CacheRequestProfilingHelper() {
        return helperThreadL2CacheRequestProfilingHelper;
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
