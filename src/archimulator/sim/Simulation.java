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
package archimulator.sim;

import archimulator.core.*;
import archimulator.core.Thread;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.os.Context;
import archimulator.os.Kernel;
import archimulator.util.action.Action1;
import archimulator.util.action.NamedAction;
import archimulator.util.action.Predicate;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import archimulator.util.io.file.FileHelper;
import archimulator.util.io.serialization.MapHelper;
import archimulator.util.StopWatch;
import archimulator.util.StringHelper;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ProcessorInitializedEvent;
import archimulator.sim.strategy.SimulationStrategy;

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

    private Map<String, Object> statInFastForward;

    private Map<String, Object> statInWarmup;

    private Map<String, Object> statInMeasurement;

    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private Logger logger;

    public Simulation(SimulationConfig config, SimulationStrategy strategy, Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> capabilityFactories, BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
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

        for (Map.Entry<Class<? extends SimulationCapability>, SimulationCapabilityFactory> entry : capabilityFactories.entrySet()) {
            this.capabilities.put(entry.getKey(), entry.getValue().createCapability(this));
        }

        this.processor = new BasicProcessor(this.blockingEventDispatcher, this.cycleAccurateEventQueue, this.logger, this.config.getProcessorConfig(), this.getStrategy().prepareKernel(), this.getStrategy().prepareCacheHierarchy(), this.config.getProcessorConfig().getProcessorCapabilityFactories());

        this.getBlockingEventDispatcher().dispatch(new ProcessorInitializedEvent(this.processor));

        this.stopWatch = new StopWatch();

        this.statInFastForward = new LinkedHashMap<String, Object>();
        this.statInWarmup = new LinkedHashMap<String, Object>();
        this.statInMeasurement = new LinkedHashMap<String, Object>();

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                dumpStat(event.getStats());
            }
        });
    }

    public Kernel createKernel() {
        Kernel kernel = new Kernel(this, this.config.getProcessorConfig().getNumCores(), this.config.getProcessorConfig().getNumThreadsPerCore(), this.config.getProcessorConfig().getKernelCapabilityFactories());

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
                            Simulation.this.logger.infof(Logger.SIMULATION, "%s", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tduration: %s", getFormattedDuration());

                            Simulation.this.logger.infof(Logger.SIMULATION, "\ttotalCycles: %s", MessageFormat.format("{0}", Simulation.this.getCycleAccurateEventQueue().getCurrentCycle()));

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

                            Simulation.this.logger.infof(Logger.SIMULATION, "\ttotalInsts: %s", MessageFormat.format("{0}", getTotalInsts()) + " (" + StringHelper.join(totalInstsPerThread, ", ") + ")");
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tllcReadMisses: %s", MessageFormat.format("{0}", getLlcReadMisses()) + " (" + StringHelper.join(llcReadMissesPerThread, ", ") + ")");
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tllcWriteMisses: %s", MessageFormat.format("{0}", getLlcWriteMisses()) + " (" + StringHelper.join(llcWriteMissesPerThread, ", ") + ")");

                            Simulation.this.logger.infof(Logger.SIMULATION, "\tinstsPerCycle: %s", MessageFormat.format("{0}", getInstsPerCycle()));
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tcyclesPerSecond: %s", MessageFormat.format("{0}", getCyclesPerSecond()));
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tinstsPerSecond: %s\n", MessageFormat.format("{0}", getInstsPerSecond()));

                            Map<String, Object> polledStats = new LinkedHashMap<String, Object>();

                            Simulation.this.getBlockingEventDispatcher().dispatch(new PollStatsEvent(polledStats));

                            for (Map.Entry<String, Object> entry : polledStats.entrySet()) {
                                Simulation.this.logger.infof(Logger.SIMULATION, "\t%s: %s", entry.getKey(), entry.getValue());
                            }

                            Simulation.this.logger.infof(Logger.SIMULATION, "\tmax memory: %s", MessageFormat.format("{0}", Runtime.getRuntime().maxMemory()));
                            Simulation.this.logger.infof(Logger.SIMULATION, "\ttotal memory: %s", MessageFormat.format("{0}", Runtime.getRuntime().totalMemory()));
                            Simulation.this.logger.infof(Logger.SIMULATION, "\tused memory: %s\n", MessageFormat.format("{0}", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

                            getProcessor().getCacheHierarchy().dumpState();
//                            Simulation.this.logger.info(Logger.SIMULATION, "");
                        }
                    }, 0);
                }
            }, 10000, 10000);

            this.getStrategy().execute();

            timerDumpState.cancel();

            MapHelper.save(this.getStatInFastForward(), this.getConfig().getCwd() + "/stat.fastForward");
            MapHelper.save(this.getStatInWarmup(), this.getConfig().getCwd() + "/stat.cacheWarmup");
            MapHelper.save(this.getStatInMeasurement(), this.getConfig().getCwd() + "/stat.measurement");

            resetIdCounters();
            this.getBlockingEventDispatcher().clearListeners();
        } catch (Exception e) {
            e.printStackTrace();
            hasError = true;
            System.exit(-1);
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

    public long getDuration() {
        return this.stopWatch.getElapsedSeconds();
    }

    public String getFormattedDuration() {
        return this.stopWatch.getElapsed();
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
        return (double) this.getCycleAccurateEventQueue().getCurrentCycle() / this.getDuration();
    }

    public double getInstsPerSecond() {
        return (double) this.getTotalInsts() / this.getDuration();
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

    public Map<String, Object> getStatInFastForward() {
        return this.statInFastForward;
    }

    public Map<String, Object> getStatInWarmup() {
        return this.statInWarmup;
    }

    public Map<String, Object> getStatInMeasurement() {
        return this.statInMeasurement;
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
