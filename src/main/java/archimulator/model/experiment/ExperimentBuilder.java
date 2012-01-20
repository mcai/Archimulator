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
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.*;

public class ExperimentBuilder {
    public static ProcessorProfile on() {
        return new ProcessorProfile();
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    public static class ProcessorProfile implements Serializable {
        private int numCores = 2;
        private int numThreadsPerCore = 2;
        private int l2Size = 1024 * 1024 * 4;
        private int l2Associativity = 8;

        private Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories = new HashMap<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>();
        private Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories = new HashMap<Class<? extends KernelCapability>, KernelCapabilityFactory>();

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

        public ProcessorProfile addProcessorCapabilityFactory(Class<? extends ProcessorCapability> clz, ProcessorCapabilityFactory factory) {
            this.processorCapabilityFactories.put(clz, factory);
            return this;
        }

        public ProcessorProfile addKernelCapabilityFactory(Class<? extends KernelCapability> clz, KernelCapabilityFactory factory) {
            this.kernelCapabilityFactories.put(clz, factory);
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

        public Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> getProcessorCapabilityFactories() {
            return processorCapabilityFactories;
        }

        public Map<Class<? extends KernelCapability>, KernelCapabilityFactory> getKernelCapabilityFactories() {
            return kernelCapabilityFactories;
        }
    }

    public static class WorkloadProfile implements Serializable {
        private ProcessorProfile processorProfile;
        private List<ContextConfig> contextConfigs;

        public WorkloadProfile(ProcessorProfile processorProfile) {
            this.processorProfile = processorProfile;
            this.contextConfigs = new ArrayList<ContextConfig>();
        }

        public WorkloadProfile workload(SimulatedProgram simulatedProgram) {
            return this.workload(simulatedProgram, 0);
        }
        
        public WorkloadProfile workload(SimulatedProgram simulatedProgram, int threadId) {
            this.contextConfigs.add(new ContextConfig(simulatedProgram, threadId));
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

    public static enum ExperimentProfileType {
        FUNCTIONAL_EXPERIMENT,
        DETAILED_EXPERIMENT,
        CHECKPOINTED_EXPERIMENT
    }
    
    public static enum ExperimentProfileState {
        SUBMITTED,
        RUNNING,
        STOPPED
    }

    @DatabaseTable
    public static class ExperimentProfile implements Serializable {
        @DatabaseField(generatedId = true)
        private long id;

        @DatabaseField(dataType = DataType.SERIALIZABLE)
        private WorkloadProfile workloadProfile;

        @DatabaseField(dataType = DataType.SERIALIZABLE)
        private HashMap<Class<? extends SimulationCapability>, SimulationCapabilityFactory> simulationCapabilityFactories = new HashMap<Class<? extends SimulationCapability>, SimulationCapabilityFactory>();

        @DatabaseField
        private ExperimentProfileType type;

        @DatabaseField
        private int pthreadSpawnedIndex;

        @DatabaseField
        private int maxInsts;

        @DatabaseField
        private ExperimentProfileState state;
        
        public ExperimentProfile() {
        }

        public ExperimentProfile(WorkloadProfile workloadProfile) {
            this.workloadProfile = workloadProfile;
            this.type = ExperimentProfileType.FUNCTIONAL_EXPERIMENT;
            this.state = ExperimentProfileState.SUBMITTED;
        }

        public ExperimentProfile functionallyToEnd() {
            this.type = ExperimentProfileType.FUNCTIONAL_EXPERIMENT;
            return this;
        }

        public ExperimentProfile inDetailToEnd() {
            this.type = ExperimentProfileType.DETAILED_EXPERIMENT;
            return this;
        }

        public ExperimentProfile functionallyToPseudoCallAndInDetailForMaxInsts(int pthreadSpawnedIndex, int maxInsts) {
            this.type = ExperimentProfileType.CHECKPOINTED_EXPERIMENT;
            this.pthreadSpawnedIndex = pthreadSpawnedIndex;
            this.maxInsts = maxInsts;
            return this;
        }

        public ExperimentProfile addSimulationCapabilityFactory(Class<? extends SimulationCapability> clz, SimulationCapabilityFactory factory) {
            this.simulationCapabilityFactories.put(clz, factory);
            return this;
        }
        
        public void runToEnd() {
            switch (this.type) {
                case FUNCTIONAL_EXPERIMENT:
                    createFunctionExperiment(UUID.randomUUID().toString(),
                            this.getProcessorProfile().getNumCores(),
                            this.getProcessorProfile().getNumThreadsPerCore(),
                            this.workloadProfile.getContextConfigs(),
                            this.simulationCapabilityFactories, this.getProcessorProfile().getProcessorCapabilityFactories(),
                            this.getProcessorProfile().getKernelCapabilityFactories()
                    ).runToEnd();
                    break;
                case DETAILED_EXPERIMENT:
                    createDetailedExperiment(UUID.randomUUID().toString(),
                            this.getProcessorProfile().getNumCores(),
                            this.getProcessorProfile().getNumThreadsPerCore(),
                            this.workloadProfile.getContextConfigs(),
                            this.getProcessorProfile().getL2Size(), this.getProcessorProfile().getL2Associativity(), LeastRecentlyUsedEvictionPolicy.FACTORY,
                            this.simulationCapabilityFactories, this.getProcessorProfile().getProcessorCapabilityFactories(),
                            this.getProcessorProfile().getKernelCapabilityFactories()
                    ).runToEnd();
                    break;
                case CHECKPOINTED_EXPERIMENT:
                    createCheckpointedExperiment(UUID.randomUUID().toString(),
                            this.getProcessorProfile().getNumCores(),
                            this.getProcessorProfile().getNumThreadsPerCore(),
                            this.workloadProfile.getContextConfigs(),
                            this.maxInsts, this.getProcessorProfile().getL2Size(), this.getProcessorProfile().getL2Associativity(), LeastRecentlyUsedEvictionPolicy.FACTORY,
                            this.pthreadSpawnedIndex,
                            this.simulationCapabilityFactories, this.getProcessorProfile().getProcessorCapabilityFactories(),
                            this.getProcessorProfile().getKernelCapabilityFactories()
                    ).runToEnd();
                    break;
            }
        }

        public long getId() {
            return id;
        }

        public ProcessorProfile getProcessorProfile() {
            return this.workloadProfile.getProcessorProfile();
        }

        public WorkloadProfile getWorkloadProfile() {
            return workloadProfile;
        }

        public Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> getSimulationCapabilityFactories() {
            return simulationCapabilityFactories;
        }

        public ExperimentProfileType getType() {
            return type;
        }

        public int getPthreadSpawnedIndex() {
            return pthreadSpawnedIndex;
        }

        public int getMaxInsts() {
            return maxInsts;
        }

        public ExperimentProfileState getState() {
            return state;
        }

        public void setState(ExperimentProfileState state) {
            this.state = state;
        }
    }

    private static FunctionalExperiment createFunctionExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                                 Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> simulationCapabilityFactories, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories,
                                                                 Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        return new FunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs,
                simulationCapabilityFactories, processorCapabilityFactories, kernelCapabilityFactories);
    }

    private static DetailedExperiment createDetailedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                               int l2Size, int l2Associativity, EvictionPolicyFactory l2EvictionPolicyFactory,
                                                               Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> simulationCapabilityFactories, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories,
                                                               Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        return new DetailedExperiment(title, numCores, numThreadsPerCore, contextConfigs, l2EvictionPolicyFactory, l2Size, l2Associativity,
                simulationCapabilityFactories, processorCapabilityFactories, kernelCapabilityFactories);
    }

    private static CheckpointedExperiment createCheckpointedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                                       int maxInsts, int l2Size, int l2Associativity, EvictionPolicyFactory l2EvictionPolicyFactory,
                                                                       int pthreadSpawnedIndex,
                                                                       Map<Class<? extends SimulationCapability>, SimulationCapabilityFactory> simulationCapabilityFactories, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories,
                                                                       Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        return new CheckpointedExperiment(title, numCores, numThreadsPerCore, contextConfigs, maxInsts, l2Size, l2Associativity, l2EvictionPolicyFactory, pthreadSpawnedIndex,
                simulationCapabilityFactories, processorCapabilityFactories, kernelCapabilityFactories);
    }
}
