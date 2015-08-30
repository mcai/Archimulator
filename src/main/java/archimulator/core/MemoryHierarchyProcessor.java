/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.core;

import archimulator.common.SimulationObject;
import archimulator.common.report.Reportable;
import archimulator.os.Kernel;
import archimulator.uncore.MemoryHierarchy;

import java.util.List;

/**
 * Processor.
 *
 * @author Min Cai
 */
public interface MemoryHierarchyProcessor extends SimulationObject, Reportable {
    /**
     * Get the list of cores.
     *
     * @return the list of cores
     */
    List<? extends MemoryHierarchyCore> getCores();

    /**
     * Get the kernel.
     *
     * @return the kernel
     */
    Kernel getKernel();

    /**
     * Get the memory hierarchy.
     *
     * @return the memory hierarchy
     */
    MemoryHierarchy getMemoryHierarchy();
}
