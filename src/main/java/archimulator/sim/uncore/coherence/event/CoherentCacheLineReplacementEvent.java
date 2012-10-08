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
package archimulator.sim.uncore.coherence.event;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;

/**
 *
 * @author Min Cai
 */
public class CoherentCacheLineReplacementEvent extends CoherentCacheEvent {
    private MemoryHierarchyAccess access;
    private int tag;
    private int set;
    private int way;

    /**
     *
     * @param cacheController
     * @param access
     * @param tag
     * @param set
     * @param way
     */
    public CoherentCacheLineReplacementEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, int tag, int set, int way) {
        super(cacheController);

        this.access = access;
        this.tag = tag;
        this.set = set;
        this.way = way;
    }

    /**
     *
     * @return
     */
    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    /**
     *
     * @return
     */
    public int getTag() {
        return tag;
    }

    /**
     *
     * @return
     */
    public int getSet() {
        return set;
    }

    /**
     *
     * @return
     */
    public int getWay() {
        return way;
    }

    @Override
    public String toString() {
        return String.format("CoherentCacheLineReplacementEvent{access=%s, tag=0x%08x, set=%d, way=%d, accessType=%s}", access, tag, set, way, access.getType());
    }
}
