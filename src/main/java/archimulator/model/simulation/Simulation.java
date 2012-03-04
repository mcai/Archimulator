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
package archimulator.model.simulation;

import archimulator.model.capability.CapabilityFactory;
import archimulator.model.capability.SimulationCapability;
import archimulator.model.event.*;
import archimulator.model.strategy.SimulationStrategy;
import archimulator.sim.core.BasicProcessor;
import archimulator.sim.core.Core;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.os.Context;
import archimulator.sim.os.Kernel;
import archimulator.util.StorageUnit;
import archimulator.util.StringHelper;
import archimulator.util.action.Action1;
import archimulator.util.action.NamedAction;
import archimulator.util.action.Predicate;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.io.file.FileHelper;
import archimulator.util.io.serialization.MapHelper;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
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

    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private Logger logger;

    public Simulation(SimulationConfig config, SimulationStrategy strategy, List<Class<? extends SimulationCapability>> capabilityClasses, BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.logger = new SimulationObjectLogger(this);

        this.config = config;
        this.strategy = strategy;

        File cwdFile = new File(this.config.getCwd());
        if (cwdFile.exists() && !FileHelper.deleteDir(cwdFile) || !cwdFile.mkdirs()) {
            throw new RuntimeException();
        }

        this.getStrategy().setSimulation(this);

        this.capabilities = new HashMap<Class<? extends SimulationCapability>, SimulationCapability>();

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                Map<String, Object> stats = event.getStats();

                stats.put("simulation.duration", getFormattedDuration());

                stats.put("simulation.totalCycles", MessageFormat.format("{0}", getCycleAccurateEventQueue().getCurrentCycle()));

                List<String> totalInstsPerThread = new ArrayList<String>();
                List<String> llcReadMissesPerThread = new ArrayList<String>();
                List<String> llcWriteMissesPerThread = new ArrayList<String>();

                for (int i = 0; i < getConfig().getProcessorConfig().getNumCores(); i++) {
                    for (int j = 0; j < getConfig().getProcessorConfig().getNumThreadsPerCore(); j++) {
                        totalInstsPerThread.add("c" + i + "t" + j + ": " + getProcessor().getCores().get(i).getThreads().get(j).getTotalInsts());
                        llcReadMissesPerThread.add("c" + i + "t" + j + ": " + getProcessor().getCores().get(i).getThreads().get(j).getLlcReadMisses());
                        llcWriteMissesPerThread.add("c" + i + "t" + j + ": " + getProcessor().getCores().get(i).getThreads().get(j).getLlcWriteMisses());
                    }
                }

                stats.put("simulation.totalInsts", MessageFormat.format("{0}", getTotalInsts()) + " (" + StringHelper.join(totalInstsPerThread, ", ") + ")");
                stats.put("simulation.llcReadMisses", MessageFormat.format("{0}", getLlcReadMisses()) + " (" + StringHelper.join(llcReadMissesPerThread, ", ") + ")");
                stats.put("simulation.llcWriteMisses", MessageFormat.format("{0}", getLlcWriteMisses()) + " (" + StringHelper.join(llcWriteMissesPerThread, ", ") + ")");

                stats.put("simulation.instsPerCycle", MessageFormat.format("{0}", getInstsPerCycle()));
                stats.put("simulation.cyclesPerSecond", MessageFormat.format("{0}", getCyclesPerSecond()));
                stats.put("simulation.instsPerSecond", MessageFormat.format("{0}", getInstsPerSecond()));

                stats.put("simulation.max memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().maxMemory())));
                stats.put("simulation.total memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory())));
                stats.put("simulation.used memory", MessageFormat.format("{0}", StorageUnit.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                pollSimulationState();
                dumpStat(event.getStats());
            }
        });

        this.processor = new BasicProcessor(this.blockingEventDispatcher, this.cycleAccurateEventQueue, this.logger, this.config.getProcessorConfig(), this.getStrategy().prepareKernel(), this.getStrategy().prepareCacheHierarchy(), this.config.getProcessorConfig().getProcessorCapabilityClasses());

        this.getBlockingEventDispatcher().dispatch(new ProcessorInitializedEvent(this.processor));

        this.stopWatch = new StopWatch();

        this.statsInFastForward = new LinkedHashMap<String, Object>();
        this.statsInWarmup = new LinkedHashMap<String, Object>();
        this.statsInMeasurement = new LinkedHashMap<String, Object>();

        for(Class<? extends SimulationCapability> capabilityClz : capabilityClasses) {
            this.capabilities.put(capabilityClz, CapabilityFactory.createSimulationCapability(capabilityClz, this));
        }
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

    public void simulate() {
        try {
            this.logger.infof(Logger.SIMULATOR, "run simulation: %s", this.getConfig().getTitle());

            this.logger.info(Logger.SIMULATOR, "");

            Timer timerDumpState = new Timer(true);
            timerDumpState.schedule(new TimerTask() {
                @Override
                public void run() {
                    Simulation.this.getCycleAccurateEventQueue().schedule(new NamedAction("Simulation.dumpState") {
                        public void apply() {
                            pollSimulationState();
                        }
                    }, 0);
                }
            }, 2000, 10000);

            this.getBlockingEventDispatcher().dispatch(new SimulationStartedEvent());

            this.getStrategy().execute();

            this.getBlockingEventDispatcher().dispatch(new SimulationStoppedEvent());

            timerDumpState.cancel();

            if(!this.getStatsInFastForward().isEmpty()) {
                MapHelper.save(this.getStatsInFastForward(), this.getConfig().getCwd() + "/stat_fastForward.txt"); //TODO: report stats to archimulator service

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this.getStatsInFastForward()));
            }

            if(!this.getStatsInWarmup().isEmpty()) {
                MapHelper.save(this.getStatsInWarmup(), this.getConfig().getCwd() + "/stat_cacheWarmup.txt"); //TODO: report stats to archimulator service

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this.getStatsInWarmup()));
            }

            if(!this.getStatsInMeasurement().isEmpty()) {
                MapHelper.save(this.getStatsInMeasurement(), this.getConfig().getCwd() + "/stat_measurement.txt"); //TODO: report stats to archimulator service

                this.blockingEventDispatcher.dispatch(new DumpStatsCompletedEvent(this.getStatsInMeasurement()));
            }

            resetIdCounters();
            this.getBlockingEventDispatcher().clearListeners();
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
            System.exit(-1);
        }
    }

    private void pollSimulationState() {
        this.logger.infof(Logger.SIMULATION, "------ Simulation %s: BEGIN DUMP STATE at %s ------", this.getConfig().getTitle(), new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));

        Map<String, Object> polledStats = new LinkedHashMap<String, Object>();

        this.getBlockingEventDispatcher().dispatch(new PollStatsEvent(polledStats));

        for (Map.Entry<String, Object> entry : polledStats.entrySet()) {
            this.logger.infof(Logger.SIMULATION, "\t%s: %s", entry.getKey(), entry.getValue());
        }

        //TODO: report polledStats to archimulator service

        this.blockingEventDispatcher.dispatch(new PollStatsCompletedEvent(polledStats));

        this.getProcessor().getCacheHierarchy().dumpState();

        this.logger.info(Logger.SIMULATION, "------ END DUMP STATE ------\n");
    }

    public static class PollStatsCompletedEvent implements BlockingEvent {
        private Map<String, Object> stats;

        public PollStatsCompletedEvent(Map<String, Object> stats) {
            this.stats = stats;
        }

        public Map<String, Object> getStats() {
            return stats;
        }
    }
    
    public static class DumpStatsCompletedEvent implements BlockingEvent {
        private Map<String, Object> stats;

        public DumpStatsCompletedEvent(Map<String, Object> stats) {
            this.stats = stats;
        }

        public Map<String, Object> getStats() {
            return stats;
        }
    }

    private void dumpStat(Map<String, Object> stats) {
        stats.put("simulation.duration", this.getFormattedDuration());

        stats.put("simulation.totalCycles", String.valueOf(this.getCycleAccurateEventQueue().getCurrentCycle()));
        stats.put("simulation.totalInsts", String.valueOf(this.getTotalInsts()));

        stats.put("simulation.instsPerCycle", String.valueOf(this.getInstsPerCycle()));
        stats.put("simulation.cyclesPerSecond", String.valueOf(this.getCyclesPerSecond()));
        stats.put("simulation.instsPerSecond", String.valueOf(this.getInstsPerSecond()));
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

    public long getLlcReadMisses() {
        long llcReadMisses = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                llcReadMisses += thread.getLlcReadMisses();
            }
        }

        return llcReadMisses;
    }

    public long getLlcWriteMisses() {
        long llcWriteMisses = 0;

        for (Core core : this.processor.getCores()) {
            for (Thread thread : core.getThreads()) {
                llcWriteMisses += thread.getLlcWriteMisses();
            }
        }

        return llcWriteMisses;
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

    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
        return this.blockingEventDispatcher;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public static boolean hasError = false;

    public static long currentMessageId = 0;

    public static long currentDynamicInstructionId = 0;
    public static long currentReorderBufferEntryId = 0;
    public static long currentDecodeBufferEntryId = 0;
    public static long currentMemoryHierarchyAccessId = 0;

    private static void resetIdCounters() {
        currentMessageId = 0;

        currentDynamicInstructionId = 0;
        currentReorderBufferEntryId = 0;
        currentDecodeBufferEntryId = 0;
        currentMemoryHierarchyAccessId = 0;
    }
}
