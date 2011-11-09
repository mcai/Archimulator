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

import archimulator.ext.mem.ht.HtRequestL2VictimTrackingCapability2;
import archimulator.isa.NativeMipsIsaEmulatorCapability;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.cache.eviction.LeastRecentlyUsedEvictionPolicy;
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
        contextConfigs.add(new ContextConfig(SIMULATED_PROGRAM_MST_HT, 0));
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
        simulate("test_mst_200_detailed", LeastRecentlyUsedEvictionPolicy.FACTORY, 2, 2, contextConfigs);
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
//        Experiment experiment = createFunctionalExperiment(title, contextConfigs);
        Experiment experiment = createDetailedExperiment(title, l2EvictionPolicyFactory, numCores, numThreadsPerCore, contextConfigs);
//        Experiment experiment = createCheckpointedExperiment(title, l2EvictionPolicyFactory, numCores, numThreadsPerCore, contextConfigs);

        experiment.start();
        experiment.join();
    }

    public static Experiment createFunctionalExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new FunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs)
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
                .addKernelCapabilityFactory(NativeMipsIsaEmulatorCapability.class, NativeMipsIsaEmulatorCapability.FACTORY);
//                .addKernelCapabilityFactory(FunctionalExecutionProfilingCapability.class, FunctionalExecutionProfilingCapability.FACTORY);
    }

    public static Experiment createDetailedExperiment(String title, EvictionPolicyFactory l2EvictionPolicyFactory, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new DetailedExperiment(title, numCores, numThreadsPerCore, contextConfigs)
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability2.class, HtRequestL2VictimTrackingCapability2.FACTORY)
                .addKernelCapabilityFactory(NativeMipsIsaEmulatorCapability.class, NativeMipsIsaEmulatorCapability.FACTORY)
                .setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    public static Experiment createCheckpointedExperiment(String title, EvictionPolicyFactory l2EvictionPolicyFactory, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs) {
        return new CheckpointedExperiment(title, numCores, numThreadsPerCore, contextConfigs, 2000000000)
                //                .addSimulationCapabilityFactory(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY)
                //                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability.class, HtRequestL2VictimTrackingCapability.FACTORY)
                .addProcessorCapabilityFactory(HtRequestL2VictimTrackingCapability2.class, HtRequestL2VictimTrackingCapability2.FACTORY)
                .addKernelCapabilityFactory(NativeMipsIsaEmulatorCapability.class, NativeMipsIsaEmulatorCapability.FACTORY)
                .setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_BASELINE = new SimulatedProgram(
            "Olden_Custom1",
            "mst_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
            "mst.mips",
//            "10000");
//            "1000");
            "1000");
//    "400");

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_HT = new SimulatedProgram(
            "Olden_Custom1",
            "mst_ht_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/ht",
            "mst.mips",
//            "10000");
//            "1000");
//            "100");
            "200");
//            "400");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_BASELINE = new SimulatedProgram(
            "Olden_Custom1",
            "em3d_baseline_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
            "em3d.mips",
            "400000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_HT = new SimulatedProgram(
            "Olden_Custom1",
            "em3d__ht_mips",
            "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/em3d_/ht",
            "em3d_.mips",
            "400000 128 75 1");

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

    //TODO: hints, to be removed
    //    public static void main(String[] args) {
////        simulationCapabilityFactories.put(MemoryAccessTraceGenerationCapability.class, MemoryAccessTraceGenerationCapability.FACTORY);
////        simulationCapabilityFactories.put(CacheHierarchyEventVisualizationCapability.class, CacheHierarchyEventVisualizationCapability.FACTORY);
////        simulationCapabilityFactories.put(LastLevelCacheMissProfilingCapability.class, LastLevelCacheMissProfilingCapability.FACTORY);
////        simulationCapabilityFactories.put(FunctionalExecutionProfilingCapability.class, FunctionalExecutionProfilingCapability.FACTORY);
//
////        processorCapabilityFactories.put(DelinquentLoadIdentificationCapability.class, DelinquentLoadIdentificationCapability.FACTORY);
////        processorCapabilityFactories.put(DynamicSpeculativePrecomputationCapability.class, DynamicSpeculativePrecomputationCapability.FACTORY);
//
//        kernelCapabilityFactories.put(NativeMipsIsaEmulatorCapability.class, NativeMipsIsaEmulatorCapability.FACTORY);
//
////        Simulation.simulate(createDefaultDetailedSimulationConfig(TestEvictionPolicy1.FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(TestEvictionPolicy2.FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(HtRequestAwareLeastRecentlyUsedEvictionPolicy.XOR_BASED_VICTIM_TRACKING_FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(HtRequestAwareLeastRecentlyUsedEvictionPolicy.COUNTING_BLOOM_FILTER_BASED_VICTIM_TRACKING_FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(HtRequestAwareLeastRecentlyUsedEvictionPolicy.TREE_SET_BASED_VICTIM_TRACKING_FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
//        Simulation.simulate(createDefaultDetailedSimulationConfig(ThrashingSensitiveHTEnhancedLeastRecentlyUsedEvictionPolicy.FACTORY, processorCapabilityFactories, kernelCapabilityFactories, 2, 2, new ArrayList<ContextConfig>()), simulationCapabilityFactories);
////        Simulation.simulate(createFunctionalSimulationConfig(LeastRecentlyUsedEvictionPolicy.FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
//        Simulation.simulate(createDefaultDetailedSimulationConfig(LeastRecentlyUsedEvictionPolicy.FACTORY, processorCapabilityFactories, kernelCapabilityFactories, 2, 2, new ArrayList<ContextConfig>()), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(LeastFrequentlyUsedEvictionPolicy.FACTORY, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistancePredictionEvictionPolicy.FACTORY_WITH_SELECTIVE_CACHING, processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
//
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistanceBasedEvaluatorEvictionPolicy.factory(LeastRecentlyUsedEvictionPolicy.FACTORY)), processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistanceBasedEvaluatorEvictionPolicy.factory(RandomEvictionPolicy.FACTORY)), processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistanceBasedEvaluatorEvictionPolicy.factory(ReuseDistancePredictionEvictionPolicy.FACTORY_WITH_SELECTIVE_CACHING), processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistanceBasedEvaluatorEvictionPolicy.factory(RereferenceIntervalPredictionEvictionPolicy.FACTORY)), processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
////        Simulation.simulate(createDefaultDetailedSimulationConfig(ReuseDistanceBasedEvaluatorEvictionPolicy.factory(HtRequestAwareLeastRecentlyUsedEvictionPolicy.XOR_BASED_VICTIM_TRACKING_FACTORY)), processorCapabilityFactories, kernelCapabilityFactories), simulationCapabilityFactories);
//
////        for (SimulationConfig simulationConfig : createDefaultCheckpointedSimulationConfig()) {
////            Simulation.simulate(simulationConfig);
////        }
//    }
}
