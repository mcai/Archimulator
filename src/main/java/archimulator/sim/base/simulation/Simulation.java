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
package archimulator.sim.base.simulation;

import archimulator.util.JaxenHelper;
import archimulator.sim.base.event.*;
import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.capability.ExperimentCapabilityFactory;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.strategy.SimulationStrategy;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.sim.os.Process;
import net.pickapack.StorageUnit;
import net.pickapack.action.Action1;
import net.pickapack.action.Predicate;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import net.pickapack.io.file.FileHelper;
import net.pickapack.io.serialization.MapHelper;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

public class Simulation implements SimulationObject {
    private Experiment experiment;

    private String title;

    private SimulationStrategy strategy;

    private Map<Class<? extends SimulationCapability>, SimulationCapability> capabilities;

    private Processor processor;

    private StopWatch stopWatch;

    private Map<String, Object> statsInFastForward;

    private Map<String, Object> statsInWarmup;

    private Map<String, Object> statsInMeasurement;

    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    public Simulation(final Experiment experiment, String title, SimulationStrategy strategy, List<Class<? extends SimulationCapability>> capabilityClasses, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.experiment = experiment;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.title = title;
        this.strategy = strategy;

        File cwdFile = new File(this.getCwd());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        this.getStrategy().setSimulation(this);

        this.capabilities = new HashMap<Class<? extends SimulationCapability>, SimulationCapability>();

        Kernel kernel = this.getStrategy().prepareKernel();

        if (!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.blockingEventDispatcher, this.cycleAccurateEventQueue, this.experiment.getProcessorConfig(), kernel, this.getStrategy().prepareCacheHierarchy());

        this.stopWatch = new StopWatch();

        this.statsInFastForward = new LinkedHashMap<String, Object>();
        this.statsInWarmup = new LinkedHashMap<String, Object>();
        this.statsInMeasurement = new LinkedHashMap<String, Object>();

        for (Class<? extends SimulationCapability> capabilityClz : capabilityClasses) {
            this.capabilities.put(capabilityClz, ExperimentCapabilityFactory.createSimulationCapability(capabilityClz, this));
        }

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                pollStats(event.getStats());
            }
        });
    }

    public Kernel createKernel() {
        Kernel kernel = new Kernel(this, this.experiment.getProcessorConfig().getNumCores(), this.experiment.getProcessorConfig().getNumThreadsPerCore());

        for (final ContextConfig contextConfig : this.experiment.getContextConfigs()) {
            final Context context = Context.load(kernel, this.getCwd(), contextConfig);

            if (!kernel.map(context, new Predicate<Integer>() {
                public boolean apply(Integer candidateThreadId) {
                    return candidateThreadId == contextConfig.getThreadId();
                }
            })) {
                throw new RuntimeException();
            }

            kernel.getContexts().add(context);
        }
        return kernel;
    }

    public void simulate(Experiment experiment) {
        Logger.infof(Logger.SIMULATOR, "run simulation: %s", this.cycleAccurateEventQueue.getCurrentCycle(), this.getTitle());

        Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

        boolean noErrors = this.getStrategy().execute(experiment);

        if (!this.getStatsInFastForward().isEmpty()) {
            MapHelper.save(this.getStatsInFastForward(), this.getCwd() + "/stat_fastForward.txt");

            this.statsInFastForward = getStatsWithSimulationPrefix(this.getStatsInFastForward());

            this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInFastForward()));
        }

        if (!this.getStatsInWarmup().isEmpty()) {
            MapHelper.save(this.getStatsInWarmup(), this.getCwd() + "/stat_cacheWarmup.txt");

            this.statsInWarmup = getStatsWithSimulationPrefix(this.getStatsInWarmup());

            this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInWarmup()));
        }

        if (!this.getStatsInMeasurement().isEmpty()) {
            MapHelper.save(this.getStatsInMeasurement(), this.getCwd() + "/stat_measurement.txt");

            this.statsInMeasurement = getStatsWithSimulationPrefix(this.getStatsInMeasurement());

            this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInMeasurement()));
        }

        resetIdCounters();

        System.out.println();

        if (!noErrors) {
            System.err.println("Simulation completed with errors");
            System.exit(-1);
        }
    }

    public Map<String, Object> getStatsWithSimulationPrefix(Map<String, Object> stats) {
        String title = this.getTitle();
        String simulationPrefix = title.substring(title.indexOf("/") + 1);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (String key : stats.keySet()) {
            result.put(simulationPrefix + "." + key, stats.get(key));
        }

        return result;
    }

    private void pollStats(Map<String, Object> stats) { //TODO: to be removed!!!
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "duration");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "cycleAccurateEventQueue/currentCycle");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "totalInsts");

        for (Thread thread : JaxenHelper.<Thread>selectNodes(this, "processor/cores/threads[totalInsts > 0]")) {
            JaxenHelper.dumpSingleStatFromXPath(stats, this, "processor/cores/threads[name ='" + thread.getName() + "']/totalInsts");
        }

        JaxenHelper.dumpSingleStatFromXPath(stats, this, "instsPerCycle");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "cyclesPerSecond");
        JaxenHelper.dumpSingleStatFromXPath(stats, this, "instsPerSecond");

        for (Process process : JaxenHelper.<Process>selectNodes(this, "processor/kernel/processes")) {
            process.getMemory().dumpStats(stats);
        }

        stats.put("max memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
        stats.put("total memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
        stats.put("used memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
    }

    public long getDurationInSeconds() {
        return this.stopWatch.getTime() / 1000;
    }

    public String getDuration() {
        return DurationFormatUtils.formatDurationHMS(this.stopWatch.getTime());
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

    public Experiment getExperiment() {
        return experiment;
    }

    public String getTitle() {
        return title;
    }

    public String getCwd() {
        return "experiments" + File.separator + this.title;
    }

    public SimulationStrategy getStrategy() {
        return this.strategy;
    }

    public Processor getProcessor() {
        return this.processor;
    }

    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    public Map<String, Object> getStatsInFastForward() {
        return this.statsInFastForward;
    }

    public Map<String, Object> getStatsInWarmup() {
        return this.statsInWarmup;
    }

    public Map<String, Object> getStatsInMeasurement() {
        return this.statsInMeasurement;
    }

    public Collection<SimulationCapability> getCapabilities() {
        return this.capabilities.values();
    }

    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return this.cycleAccurateEventQueue;
    }

    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
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
