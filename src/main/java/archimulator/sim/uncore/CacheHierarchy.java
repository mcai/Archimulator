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
package archimulator.sim.uncore;

import archimulator.model.metric.ExperimentStat;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import archimulator.sim.uncore.coherence.msi.controller.Controller;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.message.CoherenceMessage;
import archimulator.sim.uncore.dram.MemoryController;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;

import java.util.List;

/**
 * Cache hierarchy.
 *
 * @author Min Cai
 */
public interface CacheHierarchy extends SimulationObject {
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
    DirectoryController getL2CacheController();

    /**
     * Get the L1I cache controller.
     *
     * @return the L1I cache controller
     */
    List<CacheController> getL1ICacheControllers();

    /**
     * Get the L1D cache controller.
     *
     * @return the L1D cache controller
     */
    List<CacheController> getL1DCacheControllers();

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
     * Get the net for the L1 cache controllers to the L2 cache controller.
     *
     * @return the net for the L1 cache controllers to the L2 cache controller
     */
    Net getL1sToL2Net();

    /**
     * Get the net for the L2 cache controller to the memory controller.
     *
     * @return the net for the L2 cache controller to the memory controller
     */
    L2ToMemNet getL2ToMemNet();

    /**
     * Dump the cache controller finite state machine statistics.
     *
     * @param stats the list of statistics to be manipulated
     */
    void dumpCacheControllerFsmStats(List<ExperimentStat> stats);
}
