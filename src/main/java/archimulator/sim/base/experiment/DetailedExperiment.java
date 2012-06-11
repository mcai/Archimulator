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
package archimulator.sim.base.experiment;

import archimulator.sim.base.event.SimulationEvent;
import archimulator.sim.base.experiment.capability.KernelCapability;
import archimulator.sim.base.experiment.capability.ProcessorCapability;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.ContextConfig;
import archimulator.sim.base.simulation.strategy.RunToEndDetailedSimulationStrategy;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import net.pickapack.event.BlockingEventDispatcher;

import java.util.List;

public class DetailedExperiment extends Experiment {
    public DetailedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, int l1ISize, int l1IAssociativity, int l1DSize, int l1DAssociativity, int l2Size, int l2Associativity, Class<? extends EvictionPolicy> l2EvictionPolicyClz, List<Class<? extends SimulationCapability>> simulationCapabilityClasses, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
        super(title, numCores, numThreadsPerCore, contextConfigs, l1ISize, l1IAssociativity, l1DSize, l1DAssociativity, l2Size, l2Associativity, l2EvictionPolicyClz, simulationCapabilityClasses, processorCapabilityClasses, kernelCapabilityClasses);
    }

    @Override
    protected void doStart() {
        BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
        doSimulation(getTitle() + "/detailedSimulation", new RunToEndDetailedSimulationStrategy(this.getPhaser()), blockingEventDispatcher, getCycleAccurateEventQueue());
    }
}
