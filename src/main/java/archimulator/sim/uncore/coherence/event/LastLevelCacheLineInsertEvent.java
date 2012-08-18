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
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.msi.controller.GeneralCacheController;

public class LastLevelCacheLineInsertEvent extends CoherentCacheEvent {
    private MemoryHierarchyAccess access;
    private int tag;
    private int set;
    private int way;
    private int victimTag;

    public LastLevelCacheLineInsertEvent(GeneralCacheController cacheController, MemoryHierarchyAccess access, int tag, int set, int way, int victimTag) {
        super(cacheController);
        this.tag = tag;
        this.set = set;
        this.way = way;
        this.access = access;
        this.victimTag = victimTag;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public int getTag() {
        return tag;
    }

    public int getSet() {
        return set;
    }

    public int getWay() {
        return way;
    }

    public int getVictimTag() {
        return victimTag;
    }

    public boolean isEviction() {
        return victimTag != CacheLine.INVALID_TAG;
    }

    @Override
    public String toString() {
        return String.format("LastLevelCacheLineInsertEvent{access=%s, tag=0x%08x, set=%d, way=%d, cache.name=%s}", access, tag, set, way, getCacheController().getCache().getName());
    }
}
