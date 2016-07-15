/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore;

import archimulator.common.CPUExperiment;
import archimulator.common.ExperimentStat;
import archimulator.common.Simulation;
import archimulator.common.SimulationObject;
import archimulator.core.Core;
import archimulator.uncore.coherence.msi.controller.*;
import archimulator.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.uncore.dram.MemoryController;
import archimulator.uncore.tlb.TranslationLookasideBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory hierarchy.
 *
 * @author Min Cai
 */
public interface MemoryHierarchy extends SimulationObject<CPUExperiment, Simulation> {
    /**
     * Transfer a message of the specified size from the source controller to the destination controller.
     *
     * @param from    the source controller
     * @param to      the destination controller
     * @param size    the size of the message to be transferred
     * @param message the message to be transferred
     */
    void transfer(Controller from, Controller to, int size, CoherenceMessage message);

    /**
     * Get the net for the specified source and destination devices.
     *
     * @param from the source device
     * @param to   the destination device
     * @return the net for the specified source and destination devices
     */
    Net getNet(MemoryDevice from, MemoryDevice to);

    /**
     * Get the memory controller.
     *
     * @return the memory controller
     */
    MemoryController getMemoryController();

    /**
     * Get the L2 cache controller.
     *
     * @return the L2 cache controller
     */
    DirectoryController getL2Controller();

    /**
     * Get the L1I cache controller.
     *
     * @return the L1I cache controller
     */
    List<L1IController> getL1IControllers();

    /**
     * Get the L1D cache controller.
     *
     * @return the L1D cache controller
     */
    List<L1DController> getL1DControllers();

    /**
     * Get the the list of L1 cache controllers.
     *
     * @return the list of L1 cache controllers
     */
    @SuppressWarnings("unchecked")
    default List<GeneralCacheController> getCacheControllers() {
        List<GeneralCacheController> cacheControllers = new ArrayList<>();
        cacheControllers.add(getL2Controller());
        cacheControllers.addAll(getL1IControllers());
        cacheControllers.addAll(getL1DControllers());
        return cacheControllers;
    }

    /**
     * Get the list of instruction translation lookaside buffers (iTLBs).
     *
     * @return the list of instruction translation lookaside buffers (iTLBs)
     */
    List<TranslationLookasideBuffer> getItlbs();

    /**
     * Get the list of data translation lookaside buffers (dTLBs).
     *
     * @return the list of data translation lookaside buffers (dTLBs)
     */
    List<TranslationLookasideBuffer> getDtlbs();

    /**
     * Get the list of translation lookaside buffers (TLBs).
     *
     * @return the list of translation lookaside buffers (TLBs)
     */
    default List<TranslationLookasideBuffer> getTlbs() {
        List<TranslationLookasideBuffer> tlbs = new ArrayList<>();
        tlbs.addAll(getItlbs());
        tlbs.addAll(getDtlbs());
        return tlbs;
    }

    /**
     * Get the list of cores.
     *
     * @return the list of cores
     */
    default List<Core> getCores() {
        return this.getSimulation().getProcessor().getCores();
    }

    /**
     * Dump the cache controller finite state machine statistics.
     *
     * @param stats the list of statistics to be manipulated
     */
    void dumpCacheControllerFsmStats(List<ExperimentStat> stats);
}
