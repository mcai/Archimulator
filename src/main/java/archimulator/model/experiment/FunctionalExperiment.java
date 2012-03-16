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

import archimulator.model.capability.*;
import archimulator.model.simulation.ContextConfig;
import archimulator.model.strategy.RunToEndFunctionalSimulationStrategy;
import archimulator.model.capability.KernelCapability;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;

import java.util.List;

public class FunctionalExperiment extends Experiment {
    public FunctionalExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, List<Class<? extends SimulationCapability>> simulationCapabilityClasses, List<Class<? extends ProcessorCapability>> processorCapabilityClasses, List<Class<? extends KernelCapability>> kernelCapabilityClasses) {
        super(title, numCores, numThreadsPerCore, contextConfigs, 524288 * 8, 8, LeastRecentlyUsedEvictionPolicy.class, simulationCapabilityClasses, processorCapabilityClasses, kernelCapabilityClasses);
    }

    @Override
    protected void doStart() {
        this.doSimulation(this.getTitle() + "/functionalSimulation", new RunToEndFunctionalSimulationStrategy(this.getPhaser()), this.getBlockingEventDispatcher(), this.getCycleAccurateEventQueue());
    }
}
