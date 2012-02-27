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

public class ExperimentBuilder {
    public Experiment createFunctionExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                               List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                               List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                               List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(new FunctionalExperiment(title, numCores, numThreadsPerCore, contextConfigs), simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs);
    }

    public Experiment createDetailedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                               EvictionPolicyFactory l2EvictionPolicyFactory,
                                               List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                               List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                               List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(
                new DetailedExperiment(title, numCores, numThreadsPerCore, contextConfigs),
                simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs
        ).setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    public Experiment createCheckpointedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs,
                                                   EvictionPolicyFactory l2EvictionPolicyFactory,
                                                   List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs,
                                                   List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs,
                                                   List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
        return addCapabilityFactoriesToExperiment(
                new CheckpointedExperiment(title, numCores, numThreadsPerCore, contextConfigs, 2000000000),
                simulationCapabilityFactoryPairs, processorCapabilityFactoryPairs, kernelCapabilityFactoryPairs
        ).setL2EvictionPolicyFactory(l2EvictionPolicyFactory);
    }

    private Experiment addCapabilityFactoriesToExperiment(Experiment experiment, List<Pair<Class<? extends SimulationCapability>, SimulationCapabilityFactory>> simulationCapabilityFactoryPairs, List<Pair<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory>> processorCapabilityFactoryPairs, List<Pair<Class<? extends KernelCapability>, KernelCapabilityFactory>> kernelCapabilityFactoryPairs) {
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

    public void runExperimentTillEnd(Experiment experiment) {
        experiment.start();
        experiment.join();
    }

    public List<ContextConfig> createContextConfigs(String cwd, String exe, String args) {
        List<ContextConfig> contextConfigs = new ArrayList<ContextConfig>();
        contextConfigs.add(new ContextConfig(new SimulatedProgram(cwd, exe, args), 0));
        return contextConfigs;
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.home"));
        
        ExperimentBuilder experimentBuilder = new ExperimentBuilder();
        Experiment experiment = experimentBuilder.createCheckpointedExperiment("mst_10000_detailed-HTRequest_Profiling_l2_4M", 2, 2, experimentBuilder.createContextConfigs(
                "/home/itecgo/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                "mst.mips",
                "10000"
        ), LeastRecentlyUsedEvictionPolicy.FACTORY, null, null, null);
        experimentBuilder.runExperimentTillEnd(experiment);
    }

    // DSL example:
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").runFunctionallyToEnd().addExperimentListener(..)
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").runInDetailToEnd().addExperimentListener(..)
//    on(cores(2).threadsPerCore(2).l2Size("8M")).with(System.getProperty("user.home") + "/Archimulator/benchmarks/Olden_Custom1/mst/ht/mst.mips 10000").runFunctionallyTillPseudoCall(3721).runInDetailForCycles(2000000000).addExperimentListener(..)
}
