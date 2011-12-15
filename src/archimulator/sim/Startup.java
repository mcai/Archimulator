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
package archimulator.sim;

import archimulator.ext.uncore.newHt2.LastLevelCacheHtRequestCachePollutionProfilingCapability;
import archimulator.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.experiment.CheckpointedExperiment;
import archimulator.sim.experiment.DetailedExperiment;
import archimulator.sim.experiment.Experiment;
import archimulator.sim.experiment.FunctionalExperiment;

import java.util.ArrayList;
import java.util.List;

public class Startup {
    public static void main(String[] args) {
//        simulate("mst_baseline_LRU", SIMULATED_PROGRAM_MST_BASELINE, LeastRecentlyUsedEvictionPolicy.FACTORY);
//        simulate("mst_base-ht_LRU", LeastRecentlyUsedEvictionPolicy.FACTORY, SIMULATED_PROGRAM_MST_BASELINE, SIMULATED_PROGRAM_EM3D_BASELINE, SIMULATED_PROGRAM_MST_BASELINE, SIMULATED_PROGRAM_EM3D_BASELINE);
//        simulate("mst_base-ht_LRU", LeastRecentlyUsedEvictionPolicy.FACTORY, SIMULATED_PROGRAM_MST_BASELINE, SIMULATED_PROGRAM_EM3D_BASELINE, SIMULATED_PROGRAM_MST_BASELINE);
//        simulate("mst_base-ht_LRU", LeastRecentlyUsedEvictionPolicy.FACTORY, SIMULATED_PROGRAM_MST_BASELINE, SIMULATED_PROGRAM_MST_BASELINE);

//        List<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 1));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 2));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 3));

        List<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_HT, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 0));
        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_HT, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_429_MCF_BASELINE, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_429_MCF_HT, 0));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 1));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 2));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 3));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 4));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 5));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 6));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 7));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 8));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 9));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 10));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 11));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 12));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 13));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_EM3D_BASELINE, 14));
//        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_BASELINE, 15));

//        simulate("test2", LeastRecentlyUsedEvictionPolicy.FACTORY, 2, 2, contextConfigs);
//        simulate("mst_1000_detailed-HTRequest_Profiling", LeastRecentlyUsedEvictionPolicy.FACTORY, 2, 2, contextConfigs);
        simulate("em3d_4000_ht_detailed", LeastRecentlyUsedEvictionPolicy.FACTORY, 2, 2, contextConfigs);
//        simulate("mst_ht_LRU", LeastRecentlyUsedEvictionPolicy.FACTORY, SIMULATED_PROGRAM_MST_HT);

//        simulate("em3d_baseline_LRU", SIMULATED_PROGRAM_EM3D_BASELINE, LeastRecentlyUsedEvictionPolicy.FACTORY);
//        simulate("libquantum_baseline_LRU", SIMULATED_PROGRAM_462_LIBQUANTUM_BASELINE, LeastRecentlyUsedEvictionPolicy.FACTORY);
//        simulate("em3d_ht_LRU", SIMULATED_PROGRAM_EM3D_HT, LeastRecentlyUsedEvictionPolicy.FACTORY);

//        simulate("mcf_baseline_LRU", SIMULATED_PROGRAM_429_MCF_BASELINE, LeastRecentlyUsedEvictionPolicy.FACTORY);
//        simulate("mcf_ht_LRU", SIMULATED_PROGRAM_429_MCF_HT, LeastRecentlyUsedEvictionPolicy.FACTORY);

//        simulate("mst_baseline_ENHANCED_LRU", SIMULATED_PROGRAM_MST_BASELINE, ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy.FACTORY);
//        simulate("mst_ht_ENHANCED_LRU", SIMULATED_PROGRAM_MST_HT, ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy.FACTORY);
    }

    public static void simulate(String title, EvictionPolicyFactory l2EvictionPolicyFactory, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
//        Experiment experiment = createFunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs);
        Experiment experiment = createDetailedExperiment(title, l2EvictionPolicyFactory, numCores, numThreadsPerCore, contextConfigs);
//        Experiment experiment = createCheckpointedExperiment(title, l2EvictionPolicyFactory, numCores, numThreadsPerCore, contextConfigs);

        experiment.start();
        experiment.join();
    }

    public static Experiment createFunctionalExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new FunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs);
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
//                .addKernelCapabilityFactory(FunctionalExecutionProfilingCapability.class, FunctionalExecutionProfilingCapability.FACTORY);
    }

    public static Experiment createDetailedExperiment(String title, EvictionPolicyFactory l2EvictionPolicyFactory, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new DetailedExperiment(title, numCores, numThreadsPerCore, contextConfigs)
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                .addSimulationCapabilityFactory(LastLevelCacheHtRequestCachePollutionProfilingCapability.class, LastLevelCacheHtRequestCachePollutionProfilingCapability.FACTORY)
                        //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
//                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability2.class, HtRequestL2VictimTrackingCapability2.FACTORY)
//                .addProcessorCapabilityFactory(FsmBasedHtRequestLlcVictimTrackingCapability.class, FsmBasedHtRequestLlcVictimTrackingCapability.FACTORY)
                .setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    public static Experiment createCheckpointedExperiment(String title, EvictionPolicyFactory l2EvictionPolicyFactory, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new CheckpointedExperiment(title, numCores, numThreadsPerCore, contextConfigs, 2000000000)
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                .addSimulationCapabilityFactory(LastLevelCacheHtRequestCachePollutionProfilingCapability.class, LastLevelCacheHtRequestCachePollutionProfilingCapability.FACTORY)
                        //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
//                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability2.class, HtRequestL2VictimTrackingCapability2.FACTORY)
//                .addProcessorCapabilityFactory(FsmBasedHtRequestLlcVictimTrackingCapability.class, FsmBasedHtRequestLlcVictimTrackingCapability.FACTORY)
                .setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_BASELINE = new SimulatedProgram(
            "Olden_Custom1",
            "mst_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
            "mst.mips",
//            "10000");
//            "2000");
//            "1000");
            "1000");
//    "400");

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_HT = new SimulatedProgram(
            "Olden_Custom1",
            "mst_ht_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/ht",
            "mst.mips",
            "10000");
//            "2000");
//            "1000");
//            "400");
//            "200");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_BASELINE = new SimulatedProgram(
            "Olden_Custom1",
            "em3d_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
            "em3d.mips",
//            "400000 128 75 1");
//            "400 128 75 1");
            "1000 128 75 1");
//            "10000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_HT = new SimulatedProgram(
            "Olden_Custom1",
            "em3d_ht_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
            "em3d.mips",
//            "400000 128 75 1");
//            "400 128 75 1");
//            "1000 128 75 1");
            "4000 128 75 1");
//            "10000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_BASELINE = new SimulatedProgram(
            "CPU2006_Custom1",
            "429_mcf_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
            "429.mcf.mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_HT = new SimulatedProgram(
            "CPU2006_Custom1",
            "429_mcf_ht_mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
            "429.mcf.mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in");

    public static final SimulatedProgram SIMULATED_PROGRAM_462_LIBQUANTUM_BASELINE = new SimulatedProgram(
            "CPU2006_Custom1",
            "462_libquantum_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/baseline",
            "462.libquantum.mips",
            "33 5");

    public static final SimulatedProgram SIMULATED_PROGRAM_462_LIBQUANTUM_HT = new SimulatedProgram(
            "CPU2006_Custom1",
            "462_libquantum_ht_mips",
            "/home/itecgo/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/ht",
            "462.libquantum.mips",
            "33 5");
}
