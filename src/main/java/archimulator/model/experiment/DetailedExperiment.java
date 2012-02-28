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
import archimulator.model.simulation.ContextConfig;
import archimulator.model.strategy.RunToEndDetailedSimulationStrategy;
import archimulator.sim.os.KernelCapability;
import archimulator.sim.os.KernelCapabilityFactory;
import archimulator.sim.uncore.cache.eviction.EvictionPolicyFactory;

import java.util.List;
import java.util.Map;

public class DetailedExperiment extends Experiment {
    public DetailedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, EvictionPolicyFactory l2EvictionPolicyFactory, int l2Size, int l2Associativity, Map<Class<? extends ProcessorCapability>, ProcessorCapabilityFactory> processorCapabilityFactories, Map<Class<? extends KernelCapability>, KernelCapabilityFactory> kernelCapabilityFactories) {
        super(title, numCores, numThreadsPerCore, contextConfigs, l2Size, l2Associativity, l2EvictionPolicyFactory, processorCapabilityFactories, kernelCapabilityFactories);
    }

    @Override
    protected void doStart() {
        doSimulation(getTitle() + "/detailedSimulation", new RunToEndDetailedSimulationStrategy(this.getPhaser()), getBlockingEventDispatcher(), getCycleAccurateEventQueue());
    }
}
