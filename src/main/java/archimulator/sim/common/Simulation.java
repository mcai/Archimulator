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
import archimulator.model.metric.ExperimentGauge;
import archimulator.model.metric.ExperimentStat;
import archimulator.service.ServiceManager;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.BasicCacheHierarchy;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.coherence.msi.flow.CacheCoherenceFlow;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
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

    private HelperThreadL2CacheRequestProfilingHelper helperThreadL2CacheRequestProfilingHelper;

    private RuntimeHelper runtimeHelper;

    /**
     *
     */
    public long currentDynamicInstructionId;
    /**
     *
     */
    public long currentReorderBufferEntryId;
    /**
     *
     */
    public long currentDecodeBufferEntryId;
    /**
     *
     */
    public long currentMemoryHierarchyAccessId;
    /**
     *
     */
    public long currentNetMessageId;
    /**
     *
     */
    public long currentCacheCoherenceFlowId;

    /**
     *
     */
    public List<CacheCoherenceFlow> pendingFlows = new ArrayList<CacheCoherenceFlow>();

    /**
     * @param type
     * @param experiment
     * @param blockingEventDispatcher
     * @param cycleAccurateEventQueue
     */
    public Simulation(SimulationType type, Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Reference<Kernel> kernelRef) {
        this.experiment = experiment;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.title = experiment.getTitle() + "/" + getPrefix();
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

        this.processor = new BasicProcessor(this.experiment, this, this.blockingEventDispatcher, this.cycleAccurateEventQueue, kernel, this.prepareCacheHierarchy());

        if (getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled()) {
            this.helperThreadL2CacheRequestProfilingHelper = new HelperThreadL2CacheRequestProfilingHelper(this);
        }
    }

    /**
     * @return
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
     *
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

    private void collectStats(boolean endOfSimulation) {
        List<ExperimentStat> stats = new ArrayList<ExperimentStat>();

        if (this.getExperiment().getArchitecture().getHelperThreadL2CacheRequestProfilingEnabled() && (this.getType() == SimulationType.MEASUREMENT || this.getType() == SimulationType.CACHE_WARMUP)) {
            if (endOfSimulation) {
                this.getHelperThreadL2CacheRequestProfilingHelper().sumUpUnstableHelperThreadL2CacheRequests();
            }
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
            getProcessor().getCacheHierarchy().dumpCacheControllerFsmStats(stats);
        }

        ServiceManager.getExperimentStatService().addStats(stats);
    }

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
            } else if (valueObj instanceof List) {
                List resultList = (List) valueObj;

                for (int i = 0; i < resultList.size(); i++) {
                    String nodeKey = JaxenHelper.escape(initialNodeKey) + "/" + i;
                    String value = JaxenHelper.toString(resultList.get(i));

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
     * @return
     */
    protected abstract boolean canDoFastForwardOneCycle();

    /**
     * @return
     */
    protected abstract boolean canDoCacheWarmupOneCycle();

    /**
     * @return
     */
    protected abstract boolean canDoMeasurementOneCycle();

    /**
     *
     */
    protected abstract void beginSimulation();

    /**
     *
     */
    protected abstract void endSimulation();

    /**
     *
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
     *
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
     *
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
     *
     */
    public void doHouseKeeping() {
        this.getProcessor().getKernel().advanceOneCycle();
        this.getProcessor().updateContextToThreadAssignments();
    }

    /**
     * @return
     */
    public Kernel prepareKernel() {
        return this.createKernel();
    }

    /**
     * @return
     */
    public CacheHierarchy prepareCacheHierarchy() {
        return new BasicCacheHierarchy(this.getExperiment(), this, this.getBlockingEventDispatcher(), this.getCycleAccurateEventQueue());
    }

    /**
     *
     */
    public void dumpPendingFlowTree() {
        for (CacheCoherenceFlow pendingFlow : pendingFlows) {
            NodeHelper.print(pendingFlow);
            System.out.println();
        }

        System.out.println();
    }

    /**
     * @return
     */
    public SimulationType getType() {
        return type;
    }

    /**
     * @return
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * @return
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * @return
     */
    public String getBeginTimeAsString() {
        return DateHelper.toString(beginTime);
    }

    /**
     * @return
     */
    public String getEndTimeAsString() {
        return DateHelper.toString(endTime);
    }

    /**
     * @return
     */
    public long getDurationInSeconds() {
        return (this.getEndTime() - this.getBeginTime()) / 1000;
    }

    /**
     * @return
     */
    public String getDuration() {
        return DurationFormatUtils.formatDurationHMS(this.getEndTime() - this.getBeginTime());
    }

    /**
     * @return
     */
    public long getTotalInstructions() {
        long totalInstructions = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                totalInstructions += thread.getTotalInstructions();
            }
        }

        return totalInstructions;
    }

    /**
     * @return
     */
    public double getInstructionsPerCycle() {
        return (double) this.getTotalInstructions() / this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * @return
     */
    public double getCyclesPerInstruction() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getTotalInstructions();
    }

    /**
     * @return
     */
    public double getCyclesPerSecond() {
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getDurationInSeconds();
    }

    /**
     * @return
     */
    public double getInstructionsPerSecond() {
        return (double) this.getTotalInstructions() / this.getDurationInSeconds();
    }

    /**
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return
     */
    public String getWorkingDirectory() {
        return "experiments" + File.separator + this.title;
    }

    /**
     * @return
     */
    public Processor getProcessor() {
        return this.processor;
    }

    /**
     * @return
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * @return
     */
    @Override
    public Simulation getSimulation() {
        return this;
    }

    /**
     * @return
     */
    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    /**
     * @return
     */
    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    /**
     * @return
     */
    public HelperThreadL2CacheRequestProfilingHelper getHelperThreadL2CacheRequestProfilingHelper() {
        return helperThreadL2CacheRequestProfilingHelper;
    }

    /**
     * @return
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
     * @return
     */
    public abstract String getPrefix();
}
