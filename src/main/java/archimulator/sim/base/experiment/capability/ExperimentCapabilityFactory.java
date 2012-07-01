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
package archimulator.sim.base.experiment.capability;

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.isa.FunctionalExecutionProfilingCapability;
import archimulator.sim.uncore.MemoryAccessTraceGenerationCapability;
import archimulator.sim.uncore.ht.HTLLCRequestProfilingCapability;
import archimulator.sim.uncore.ht.HelperThreadParamsDynamicTuningCapability;
import archimulator.sim.uncore.ht.LLCReuseDistanceProfilingCapability;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentCapabilityFactory {
    private static List<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();

    static {
        simulationCapabilityClasses.add(MemoryAccessTraceGenerationCapability.class);
        simulationCapabilityClasses.add(HelperThreadParamsDynamicTuningCapability.class);
        simulationCapabilityClasses.add(LLCReuseDistanceProfilingCapability.class);
        simulationCapabilityClasses.add(HTLLCRequestProfilingCapability.class);
        simulationCapabilityClasses.add(FunctionalExecutionProfilingCapability.class);
    }

    public static <SimulationCapabilityT extends SimulationCapability> SimulationCapabilityT createSimulationCapability(Class<SimulationCapabilityT> simulationCapabilityClz, Simulation simulation) {
        try {
            return simulationCapabilityClz.getConstructor(new Class[]{Simulation.class}).newInstance(simulation);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Class<? extends SimulationCapability>> getSimulationCapabilityClasses() {
        return simulationCapabilityClasses;
    }
}
