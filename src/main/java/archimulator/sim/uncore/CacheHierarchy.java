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

import archimulator.model.simulation.SimulationObject;
import archimulator.sim.uncore.coherence.FirstLevelCache;
import archimulator.sim.uncore.coherence.LastLevelCache;
import archimulator.sim.uncore.dram.MainMemory;
import archimulator.sim.uncore.net.L2ToMemNet;
import archimulator.sim.uncore.net.Net;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;

import java.util.List;

public interface CacheHierarchy extends SimulationObject {
    void dumpState();

    MainMemory getMainMemory();

    LastLevelCache getL2Cache();

    List<FirstLevelCache> getInstructionCaches();

    List<FirstLevelCache> getDataCaches();

    List<TranslationLookasideBuffer> getItlbs();

    List<TranslationLookasideBuffer> getDtlbs();

    Net getL1sToL2Network();

    L2ToMemNet getL2ToMemNetwork();
}