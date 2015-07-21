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
import archimulator.uncore.coherence.msi.controller.CacheController;
import archimulator.util.action.Action;

/**
 * Memory hierarchy core.
 *
 * @author Min Cai
 */
public interface MemoryHierarchyCore extends SimulationObject, Reportable {
    /**
     * Get a value indicating whether the specified thread can fetch the instruction at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can fetch the instruction at the specified address
     */
    boolean canIfetch(Thread thread, int virtualAddress);

    /**
     * Get a value indicating whether the specified thread can perform a load at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can perform a load at the specified address
     */
    boolean canLoad(Thread thread, int virtualAddress);

    /**
     * Get a value indicating whether the specified thread can perform a store at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can perform a store at the specified address
     */
    boolean canStore(Thread thread, int virtualAddress);

    /**
     * Act on when the specified thread fetch the instruction at the specified address.
     *
     * @param thread              the thread
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the instruction fetch is completed
     */
    void ifetch(Thread thread, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Act on when the specified thread perform a load at the specified address.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the load is completed
     */
    void load(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Act on when the specified thread perform a store at the specified address.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the store is completed
     */
    void store(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Do measurement for one cycle.
     */
    void doMeasurementOneCycle();

    /**
     * Get the number of the core.
     *
     * @return the number of the core
     */
    int getNum();

    /**
     * Get the name of the core.
     *
     * @return the name of the core
     */
    String getName();

    /**
     * Get the parent processor.
     *
     * @return the parent processor
     */
    Processor getProcessor();

    /**
     * Get the L1I cache controller.
     *
     * @return the L1I cache controller
     */
    CacheController getL1IController();

    /**
     * Set the L1I cache controller.
     *
     * @param l1IController the L1I cache controller
     */
    void setL1IController(CacheController l1IController);

    /**
     * Get the L1D cache controller.
     *
     * @return the L1D cache controller
     */
    CacheController getL1DController();

    /**
     * Set the L1D cache controller.
     *
     * @param l1DController the L1D cache controller
     */
    void setL1DController(CacheController l1DController);
}
