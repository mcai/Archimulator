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
package archimulator.model.capability;

import archimulator.model.simulation.Simulation;
import archimulator.sim.core.Processor;
import archimulator.sim.ext.core.DynamicSpeculativePrecomputationCapability;
import archimulator.sim.ext.uncore.delinquentLoad.DelinquentLoadIdentificationCapability;
import archimulator.sim.ext.uncore.ht.HtRequestL2VictimTrackingCapability;
import archimulator.sim.ext.uncore.ht.HtRequestL2VictimTrackingCapability2;
import archimulator.sim.ext.uncore.newHt.FsmBasedHtRequestLlcVictimTrackingCapability;
import archimulator.sim.ext.uncore.newHt2.LastLevelCacheHtRequestCachePollutionProfilingCapability;
import archimulator.sim.isa.FunctionalExecutionProfilingCapability;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.MemoryAccessTraceGenerationCapability;
import archimulator.sim.uncore.coherence.ext.LastLevelCacheMissProfilingCapability;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CapabilityFactory {
    private static List<Class<? extends SimulationCapability>> simulationCapabilityClasses = new ArrayList<Class<? extends SimulationCapability>>();
    private static List<Class<? extends ProcessorCapability>> processorCapabilityClasses = new ArrayList<Class<? extends ProcessorCapability>>();
    private static List<Class<? extends KernelCapability>> kernelCapabilityClasses = new ArrayList<Class<? extends KernelCapability>>();

    static {
        simulationCapabilityClasses.add(LastLevelCacheHtRequestCachePollutionProfilingCapability.class);
        simulationCapabilityClasses.add(LastLevelCacheMissProfilingCapability.class);
        simulationCapabilityClasses.add(MemoryAccessTraceGenerationCapability.class);

        processorCapabilityClasses.add(DynamicSpeculativePrecomputationCapability.class);
        processorCapabilityClasses.add(DelinquentLoadIdentificationCapability.class);
        processorCapabilityClasses.add(HtRequestL2VictimTrackingCapability.class);
        processorCapabilityClasses.add(FsmBasedHtRequestLlcVictimTrackingCapability.class);
        processorCapabilityClasses.add(HtRequestL2VictimTrackingCapability2.class);

        kernelCapabilityClasses.add(FunctionalExecutionProfilingCapability.class);
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

    public static <ProcessorCapabilityT extends ProcessorCapability> ProcessorCapabilityT createProcessorCapability(Class<ProcessorCapabilityT> processorCapabilityClz, Processor processor){
        try {
            return processorCapabilityClz.getConstructor(new Class[]{Processor.class}).newInstance(processor);
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

    public static <KernelCapabilityT extends KernelCapability> KernelCapabilityT createKernelCapability(Class<KernelCapabilityT> kernelCapabilityClz, Kernel kernel) {
        try {
            return kernelCapabilityClz.getConstructor(new Class[]{Kernel.class}).newInstance(kernel);
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

    public static List<Class<? extends ProcessorCapability>> getProcessorCapabilityClasses() {
        return processorCapabilityClasses;
    }

    public static List<Class<? extends KernelCapability>> getKernelCapabilityClasses() {
        return kernelCapabilityClasses;
    }
}