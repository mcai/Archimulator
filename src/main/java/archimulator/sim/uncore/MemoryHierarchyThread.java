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

import archimulator.sim.common.SimulationObject;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;

/**
 * Memory hierarchy thread.
 *
 * @author Min Cai
 */
public interface MemoryHierarchyThread extends SimulationObject {
    /**
     * Get the number of the memory hierarchy thread.
     *
     * @return the number of the memory hierarchy thread
     */
    int getNum();

    /**
     * Get the ID of the memory hierarchy thread.
     *
     * @return the ID of the memory hierarchy thread
     */
    int getId();

    /**
     * Get the name of the memory hierarchy thread.
     *
     * @return the name of the memory hierarchy thread
     */
    String getName();

    /**
     * Get the instruction translation lookaside buffer (iTLB).
     *
     * @return the instruction translation lookaside buffer (iTLB)
     */
    TranslationLookasideBuffer getItlb();

    /**
     * Set the instruction translation lookaside buffer (iTLB).
     *
     * @param itlb the instruction translation lookaside buffer (iTLB)
     */
    void setItlb(TranslationLookasideBuffer itlb);

    /**
     * Get the data translation lookaside buffer (dTLB).
     *
     * @return the data translation lookaside buffer (dTLB)
     */
    TranslationLookasideBuffer getDtlb();

    /**
     * Set the data translation lookaside buffer (dTLB).
     *
     * @param dtlb the data translation lookaside buffer (dTLB)
     */
    void setDtlb(TranslationLookasideBuffer dtlb);

    /**
     * Get the parent core.
     *
     * @return the parent core.
     */
    MemoryHierarchyCore getCore();
}
