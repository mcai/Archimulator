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

import archimulator.sim.base.event.*;
import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.capability.ExperimentCapabilityFactory;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.strategy.SimulationStrategy;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.os.*;
import archimulator.sim.os.Process;
import net.pickapack.StorageUnit;
import net.pickapack.StringHelper;
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
    private SimulationConfig config;

    private SimulationStrategy strategy;

    private Map<Class<? extends SimulationCapability>, SimulationCapability> capabilities;

    private Processor processor;

    private StopWatch stopWatch;

    private Map<String, Object> statsInFastForward;

    private Map<String, Object> statsInWarmup;

    private Map<String, Object> statsInMeasurement;

    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    public Simulation(SimulationConfig config, SimulationStrategy strategy, List<Class<? extends SimulationCapability>> capabilityClasses, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.config = config;
        this.strategy = strategy;

        File cwdFile = new File(this.config.getCwd());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        this.getStrategy().setSimulation(this);

        this.capabilities = new HashMap<Class<? extends SimulationCapability>, SimulationCapability>();

        Kernel kernel = this.getStrategy().prepareKernel();

        if(!this.blockingEventDispatcher.isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.processor = new BasicProcessor(this.blockingEventDispatcher, this.cycleAccurateEventQueue, this.config.getProcessorConfig(), kernel, this.getStrategy().prepareCacheHierarchy(), this.config.getProcessorConfig().getProcessorCapabilityClasses());

        this.getBlockingEventDispatcher().dispatch(new ProcessorInitializedEvent(this.processor));

        this.stopWatch = new StopWatch();

        this.statsInFastForward = new LinkedHashMap<String, Object>();
        this.statsInWarmup = new LinkedHashMap<String, Object>();
        this.statsInMeasurement = new LinkedHashMap<String, Object>();

        for (Class<? extends SimulationCapability> capabilityClz : capabilityClasses) {
            this.capabilities.put(capabilityClz, ExperimentCapabilityFactory.createSimulationCapability(capabilityClz, this));
        }

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                Map<String, Object> stats = event.getStats();

                stats.put("duration", getFormattedDuration());

                stats.put("totalCycles", MessageFormat.format("{0}", getCycleAccurateEventQueue().getCurrentCycle()));

                List<String> totalInstsPerThread = new ArrayList<String>();

                for (int i = 0; i < getConfig().getProcessorConfig().getNumCores(); i++) {
                    for (int j = 0; j < getConfig().getProcessorConfig().getNumThreadsPerCore(); j++) {
                        totalInstsPerThread.add("c" + i + "t" + j + ": " + getProcessor().getCores().get(i).getThreads().get(j).getTotalInsts());
                    }
                }

                stats.put("totalInsts", MessageFormat.format("{0}", getTotalInsts()) + " (" + StringHelper.join(totalInstsPerThread, ", ") + ")");

                stats.put("instsPerCycle", MessageFormat.format("{0}", getInstsPerCycle()));
                stats.put("cyclesPerSecond", MessageFormat.format("{0}", getCyclesPerSecond()));
                stats.put("instsPerSecond", MessageFormat.format("{0}", getInstsPerSecond()));

                for(Process process : getProcessor().getKernel().getProcesses()) {
                    process.getMemory().dumpStats(stats);
                }

                stats.put("max memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
                stats.put("total memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
                stats.put("used memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                dumpStat(event.getStats());
            }
        });
    }

    public Kernel createKernel() {
        Kernel kernel = new Kernel(this, this.config.getProcessorConfig().getNumCores(), this.config.getProcessorConfig().getNumThreadsPerCore(), this.config.getProcessorConfig().getKernelCapabilityClasses());

        for (final ContextConfig contextConfig : this.config.getContextConfigs()) {
            final Context context = Context.load(kernel, this.config.getCwd(), contextConfig);

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
        try {
            Logger.infof(Logger.SIMULATOR, "run simulation: %s", this.cycleAccurateEventQueue.getCurrentCycle(), this.getConfig().getTitle());

            Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

            this.getStrategy().execute(experiment);

            if (!this.getStatsInFastForward().isEmpty()) {
                MapHelper.save(this.getStatsInFastForward(), this.getConfig().getCwd() + "/stat_fastForward.txt");

                this.statsInFastForward = getStatsWithSimulationPrefix(this.getStatsInFastForward());

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInFastForward()));
            }

            if (!this.getStatsInWarmup().isEmpty()) {
                MapHelper.save(this.getStatsInWarmup(), this.getConfig().getCwd() + "/stat_cacheWarmup.txt");

                this.statsInWarmup = getStatsWithSimulationPrefix(this.getStatsInWarmup());

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInWarmup()));
            }

            if (!this.getStatsInMeasurement().isEmpty()) {
                MapHelper.save(this.getStatsInMeasurement(), this.getConfig().getCwd() + "/stat_measurement.txt");

                this.statsInMeasurement = getStatsWithSimulationPrefix(this.getStatsInMeasurement());

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this, this.getStatsInMeasurement()));
            }

            resetIdCounters();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public Map<String, Object> getStatsWithSimulationPrefix(Map<String, Object> stats) {
        String title = this.getConfig().getTitle();
        String simulationPrefix = title.substring(title.indexOf("/") + 1);

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        for (String key : stats.keySet()) {
            result.put(simulationPrefix + "." + key, stats.get(key));
        }

        return result;
    }

    private void dumpStat(Map<String, Object> stats) {
        stats.put("duration", this.getFormattedDuration());

        stats.put("totalCycles", String.valueOf(this.getCycleAccurateEventQueue().getCurrentCycle()));
        stats.put("totalInsts", String.valueOf(this.getTotalInsts()));

        stats.put("instsPerCycle", String.valueOf(this.getInstsPerCycle()));
        stats.put("cyclesPerSecond", String.valueOf(this.getCyclesPerSecond()));
        stats.put("instsPerSecond", String.valueOf(this.getInstsPerSecond()));
    }

    public long getDurationInSeconds() {
        return this.stopWatch.getTime() / 1000;
    }

    public String getFormattedDuration() {
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

    public SimulationConfig getConfig() {
        return this.config;
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

    public Map<Class<? extends SimulationCapability>, SimulationCapability> getCapabilities() {
        return this.capabilities;
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
