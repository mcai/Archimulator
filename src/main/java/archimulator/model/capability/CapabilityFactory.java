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
import archimulator.sim.os.Kernel;

import java.lang.reflect.InvocationTargetException;

public class CapabilityFactory {
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
}
