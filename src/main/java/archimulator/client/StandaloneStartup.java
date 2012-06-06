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
package archimulator.client;

import archimulator.sim.base.event.DumpStatsCompletedEvent;
import archimulator.sim.base.event.PollStatsCompletedEvent;
import archimulator.sim.base.event.SimulationCreatedEvent;
import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import net.pickapack.DateHelper;
import net.pickapack.action.Action1;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StandaloneStartup {
    @Option(name = "-s", usage = "L2 cache size in KBytes", metaVar = "<l2SizeInKByte>", required = true)
    private int l2SizeInKByte;

    @Option(name = "-a", usage = "L2 cache associativity", metaVar = "<l2Associativity>", required = true)
    private int l2Associativity;

    @Option(name = "-c", usage = "number of processor cores", metaVar = "<numCores>", required = true)
    private int numCores;

    @Option(name = "-t", usage = "number of threads per core", metaVar = "<numThreadsPerCore>", required = true)
    private int numThreadsPerCore;

    @Option(name = "-e", usage = "class name of L2 eviction policy", metaVar = "<l2EvictionPolicyClassName>", required = true)
    private String l2EvictionPolicyClassName;

    public void parseArgs(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("java -cp <path/to/achimulator.jar> archimulator.client.StandaloneStartup [options]");
            parser.printUsage(System.err);
            System.err.println();
            throw new IOException();
        }
    }

    public static void main(String[] args) {
        StandaloneStartup standaloneStartup = new StandaloneStartup();
//        try {
//            standaloneStartup.parseArgs(args);
            standaloneStartup.run();
//        } catch (IOException e) {
//            System.out.println(e);
//        }
    }

    private void run() {
        int pthreadSpawnedIndex = 3720;
        int maxInsts = 200000000;

        //        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_BASELINE;
//        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_HT(20, 10);
        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_HT(640, 320);

//        ProcessorProfile processorProfile = Presets.processor(1024 * 4, 8, 2, 2, "LRU", LRUPolicy.class);
        ProcessorProfile processorProfile = Presets.processor(1024 / 4, 8, 2, 2, "LRU", LRUPolicy.class); //256K L2
//        ProcessorProfile processorProfile = Presets.processor(1024 / 4, 8, 2, 2, "LLCHTAwareLRU", LLCHTAwareLRUPolicy.class); //256K L2
//        ProcessorProfile processorProfile = Presets.processor(1024 / 2, 8, 2, 2, "LLCHTAwareLRU", LLCHTAwareLRUPolicy.class); //256K L2

//        final ExperimentProfile experimentProfile = Presets.baseline_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram);
        final ExperimentProfile experimentProfile = Presets.ht_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram);

        final Experiment experiment = experimentProfile.createExperiment();

        final Map<String, String> stats1 = new LinkedHashMap<String, String>();

        experiment.start();

        experiment.getBlockingEventDispatcher().addListener(SimulationCreatedEvent.class, new Action1<SimulationCreatedEvent>() {
            @Override
            public void apply(SimulationCreatedEvent event) {
                experiment.getSimulation().getBlockingEventDispatcher().addListener(PollStatsCompletedEvent.class, new Action1<PollStatsCompletedEvent>() {
                    @Override
                    public void apply(final PollStatsCompletedEvent event) {
                        try {
                            Map<String, Object> stats = event.getStats();

                            for (String key : stats.keySet()) {
                                stats1.put(key, stats.get(key) + "");
                            }

//                    Context context = experiment.getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getContext();
//                    if(context != null) {
//                        FileWriter out = new FileWriter("/home/itecgo/Desktop/dis.txt");          e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                        ((BasicProcess) context.getProcess()).getElfAnalyzer().dumpAnalysisResult(out);
//                        out.close();
//                    }
                        } catch (Exception e) {
                            recordException(e);
//                        throw new RuntimeException(e);
                        }
                    }
                });

                experiment.getSimulation().getBlockingEventDispatcher().addListener(DumpStatsCompletedEvent.class, new Action1<DumpStatsCompletedEvent>() {
                    @Override
                    public void apply(final DumpStatsCompletedEvent event) {
                        try {
                            Map<String, Object> stats = event.getStats();

                            for (String key : stats.keySet()) {
                                stats1.put(key, stats.get(key) + "");
                            }
                        } catch (Exception e) {
                            recordException(e);
//                        throw new RuntimeException(e);
                        }
                    }
                });
            }
        });

        experiment.join();

        try {
            PrintWriter pw = new PrintWriter(new FileWriter("/home/itecgo/Desktop/stats.txt"));

            for (String key : stats1.keySet()) {
                pw.println(key + ": " + stats1.get(key) + "");
            }

            pw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }
}
