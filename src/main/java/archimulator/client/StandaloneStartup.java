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

import archimulator.sim.base.experiment.Experiment;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.ext.uncore.cache.eviction.LLCHTAwareLRUPolicy;
import archimulator.sim.os.*;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.DateHelper;
import archimulator.util.action.Action1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class StandaloneStartup {
    public static void main(String[] args) {
        int pthreadSpawnedIndex = 3720;
        int maxInsts = 200000000;

//        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_BASELINE;
//        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_HT(20, 10);
        SimulatedProgram simulatedProgram = Presets.SIMULATED_PROGRAM_MST_HT(640, 320);
//        ProcessorProfile processorProfile = Presets.processor(1024 * 4, 8, 2, 2, "LRU", LRUPolicy.class);
//        ProcessorProfile processorProfile = Presets.processor(1024 / 4, 8, 2, 2, "LRU", LRUPolicy.class); //256K L2
        ProcessorProfile processorProfile = Presets.processor(1024 / 4, 8, 2, 2, "LLCHTAwareLRU", LLCHTAwareLRUPolicy.class); //256K L2
//        final ExperimentProfile experimentProfile = Presets.baseline_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram);
        final ExperimentProfile experimentProfile = Presets.ht_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram);

        final Experiment experiment = experimentProfile.createExperiment();

        final Map<String, String> stats1 = new LinkedHashMap<String, String>();

        experiment.getBlockingEventDispatcher().addListener(Simulation.PollStatsCompletedEvent.class, new Action1<Simulation.PollStatsCompletedEvent>() {
            @Override
            public void apply(final Simulation.PollStatsCompletedEvent event) {
                try {
                    Map<String, Object> stats = event.getStats();

                    for (String key : stats.keySet()) {
                        stats1.put(key, stats.get(key) + "");
                    }

//                    Context context = experiment.getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getContext();
//                    if(context != null) {
//                        FileWriter out = new FileWriter("/home/itecgo/Desktop/dis.txt");
//                        ((BasicProcess) context.getProcess()).getElfAnalyzer().dumpAnalysisResult(out);
//                        out.close();
//                    }
                } catch (Exception e) {
                    recordException(e);
//                        throw new RuntimeException(e);
                }
            }
        });

        experiment.getBlockingEventDispatcher().addListener(Simulation.DumpStatsCompletedEvent.class, new Action1<Simulation.DumpStatsCompletedEvent>() {
            @Override
            public void apply(final Simulation.DumpStatsCompletedEvent event) {
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

        experiment.runToEnd();

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
