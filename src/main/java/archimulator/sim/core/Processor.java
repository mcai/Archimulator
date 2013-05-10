/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.core;

import archimulator.sim.common.SimulationObject;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.MemoryHierarchy;

import java.util.List;

/**
 * Processor.
 *
 * @author Min Cai
 */
public interface Processor extends SimulationObject {
    /**
     * Get the list of cores.
     *
     * @return the list of cores
     */
    List<Core> getCores();

    /**
     * Get the list of threads.
     *
     * @return the list of threads
     */
    List<Thread> getThreads();

    /**
     * Get the kernel.
     *
     * @return the kernel
     */
    Kernel getKernel();

    /**
     * Update the assignments of contexts to threads.
     */
    void updateContextToThreadAssignments();

    /**
     * Get the memory hierarchy.
     *
     * @return the memory hierarchy
     */
    MemoryHierarchy getMemoryHierarchy();
}
