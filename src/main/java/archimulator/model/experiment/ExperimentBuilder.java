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
package archimulator.model.experiment;

import archimulator.model.capability.ProcessorCapability;
import archimulator.model.capability.ProcessorCapabilityFactory;
import archimulator.model.capability.SimulationCapability;
import archimulator.model.capability.SimulationCapabilityFactory;
import archimulator.model.simulation.ContextConfig;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.sim.os.KernelCapability;
import archimulator.sim.os.KernelCapabilityFactory;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExperimentBuilder {
    public static FunctionalExperiment createFunctionExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                                List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                                                List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                                                List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(new FunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs), simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs);
    }

    public static DetailedExperiment createDetailedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                              int l2Size, int l2Associativity, EvictionPolicyFactory l2EvictionPolicyFactory,
                                                              List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                                              List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                                              List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(
                new DetailedExperiment(title, numCores, numThreadsPerCore, contextConfigs, l2EvictionPolicyFactory, l2Size, l2Associativity),
                simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs
        );
    }

    public static CheckpointedExperiment createCheckpointedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                                      int maxInsts, int l2Size, int l2Associativity, EvictionPolicyFactory l2EvictionPolicyFactory,
                                                                      int pthreadSpawnedIndex, List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                                                      List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                                                      List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(
                new CheckpointedExperiment(title, numCores, numThreadsPerCore, contextConfigs, maxInsts, l2Size, l2Associativity, l2EvictionPolicyFactory, pthreadSpawnedIndex),
                simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs
        );
    }

    private static <ExperimentT extends Experiment> ExperimentT addCapabilityFactoriesToExperiment(ExperimentT experiment, List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs, List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs, List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        if (simulationCapabilityFactoryPairs != null) {
            for (Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory> pair : simulationCapabilityFactoryPairs) {
                experiment.addSimulationCapabilityFactory(pair.getFirst(), pair.getSecond());
            }
        }

        if (processorCapabilityFactoryPairs != null) {
            for (Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> pair : processorCapabilityFactoryPairs) {
                experiment.addProcessorCapabilityFactory(pair.getFirst(), pair.getSecond());
            }
        }

        if (kernelCapabilityFactoryPairs != null) {
            for (Pair<Class<? extends KernelCapability>, KernelCapabilityFactory> pair : kernelCapabilityFactoryPairs) {
                experiment.addKernelCapabilityFactory(pair.getFirst(), pair.getSecond());
            }
        }

        return experiment;
    }

    public static List<ContextConfig> createContextConfigs(String cwd, String exe, String args) {
        List<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();
        contextConfigs.add(new ContextConfig(new SimulatedProgram(cwd, exe, args), 0));
        return contextConfigs;
    }

    public static void main(String[] args) {
        on().cores(2).threadsPerCore(2)
                .with().workload(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht", "mst.mips", "10000")
                .simulate().functionallyToEnd()
                .runToEnd();

        on().cores(2).threadsPerCore(2).l2Size(1024 * 1024 * 4).l2Associativity(8)
                .with().workload(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht", "mst.mips", "10000")
                .simulate().inDetailToEnd()
                .runToEnd();

        on().cores(2).threadsPerCore(2).l2Size(1024 * 1024 * 4).l2Associativity(8)
                .with().workload(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht", "mst.mips", "10000")
                .simulate().functionallyToPseudoCallAndInDetailForMaxInsts(3720, 2000000000)
                .runToEnd();
    }

    public static ProcessorProfile on() {
        return new ProcessorProfile();
    }

    public static class ProcessorProfile {
        private int numCores = 2;
        private int numThreadsPerCore = 2;
        private int l2Size = 1024 * 1024 * 4;
        private int l2Associativity = 8;

        public ProcessorProfile cores(int numCores) {
            this.numCores = numCores;
            return this;
        }

        public ProcessorProfile threadsPerCore(int numThreadsPerCore) {
            this.numThreadsPerCore = numThreadsPerCore;
            return this;
        }

        public ProcessorProfile l2Size(int l2Size) {
            this.l2Size = l2Size;
            return this;
        }
        
        public ProcessorProfile l2Associativity(int l2Associativity) {
            this.l2Associativity = l2Associativity;
            return this;
        }

        public WorkloadProfile with() {
            return new WorkloadProfile(this);
        }

        public int getNumCores() {
            return numCores;
        }

        public int getNumThreadsPerCore() {
            return numThreadsPerCore;
        }

        public int getL2Size() {
            return l2Size;
        }

        public int getL2Associativity() {
            return l2Associativity;
        }
    }

    public static class WorkloadProfile {
        private ProcessorProfile processorProfile;
        private List<ContextConfig> contextConfigs;

        public WorkloadProfile(ProcessorProfile processorProfile) {
            this.processorProfile = processorProfile;
            this.contextConfigs = new ArrayList<ContextConfig>();
        }

        public WorkloadProfile workload(String cwd, String exe, String args) {
            return this.workload(cwd, exe, args, 0);
        }
        
        public WorkloadProfile workload(String cwd, String exe, String args, int threadId) {
            this.contextConfigs.add(new ContextConfig(new SimulatedProgram(cwd,  exe, args), threadId));
            return this;
        }

        public ExperimentProfile simulate() {
            return new ExperimentProfile(this);
        }

        public ProcessorProfile getProcessorProfile() {
            return processorProfile;
        }

        public List<ContextConfig> getContextConfigs() {
            return contextConfigs;
        }
    }

    public static class ExperimentProfile {//TODO: add experiment event listener support to experiment
        private WorkloadProfile workloadProfile;

        public ExperimentProfile(WorkloadProfile workloadProfile) {
            this.workloadProfile = workloadProfile;
        }

        public FunctionalExperiment functionallyToEnd() {
            return createFunctionExperiment(UUID.randomUUID().toString(),
                    this.workloadProfile.getProcessorProfile().getNumCores(),
                    this.workloadProfile.getProcessorProfile().getNumThreadsPerCore(),
                    this.workloadProfile.getContextConfigs(),
                    null, null, null); //TODO: capabilities support
        }

        public DetailedExperiment inDetailToEnd() {
            return createDetailedExperiment(UUID.randomUUID().toString(),
                    this.workloadProfile.getProcessorProfile().getNumCores(),
                    this.workloadProfile.getProcessorProfile().getNumThreadsPerCore(),
                    this.workloadProfile.getContextConfigs(),
                    this.workloadProfile.getProcessorProfile().getL2Size(), this.workloadProfile.getProcessorProfile().getL2Associativity(), LeastRecentlyUsedEvictionPolicy.FACTORY,
                    null, null, null); //TODO: capabilities support
        }

        public CheckpointedExperiment functionallyToPseudoCallAndInDetailForMaxInsts(int pthreadSpawnedIndex, int maxInsts) {
            return createCheckpointedExperiment(UUID.randomUUID().toString(),
                    this.workloadProfile.getProcessorProfile().getNumCores(),
                    this.workloadProfile.getProcessorProfile().getNumThreadsPerCore(),
                    this.workloadProfile.getContextConfigs(),
                    maxInsts, this.workloadProfile.getProcessorProfile().getL2Size(), this.workloadProfile.getProcessorProfile().getL2Associativity(), LeastRecentlyUsedEvictionPolicy.FACTORY,
                    pthreadSpawnedIndex,
                    null, null, null); //TODO: capabilities support
        }

        public ProcessorProfile getProcessorProfile() {
            return this.workloadProfile.getProcessorProfile();
        }

        public WorkloadProfile getWorkloadProfile() {
            return workloadProfile;
        }
    }

    // DSL example:
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").runFunctionallyToEnd().addExperimentListener(..)
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").inDetailToEnd().addExperimentListener(..)
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").functionallyTillPseudoCall(3721).inDetailForCycles(2000000000).addExperimentListener(..)
}
