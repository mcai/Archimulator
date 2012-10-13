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
 *
 * @author Min Cai
 */
public interface CacheHierarchy extends SimulationObject {
    /**
     *
     * @param from
     * @param to
     * @param size
     * @param message
     */
    void transfer(Controller from, Controller to, int size, CoherenceMessage message);

    /**
     *
     * @return
     */
    MemoryController getMemoryController();

    /**
     *
     * @return
     */
    DirectoryController getL2CacheController();

    /**
     *
     * @return
     */
    List<CacheController> getL1ICacheControllers();

    /**
     *
     * @return
     */
    List<CacheController> getL1DCacheControllers();

    /**
     *
     * @return
     */
    List<TranslationLookasideBuffer> getItlbs();

    /**
     *
     * @return
     */
    List<TranslationLookasideBuffer> getDtlbs();

    /**
     *
     * @return
     */
    Net getL1sToL2Network();

    /**
     *
     * @return
     */
    L2ToMemNet getL2ToMemNetwork();

    /**
     *
     * @param stats
     */
    void dumpCacheControllerFsmStats(List<ExperimentStat> stats);
}
