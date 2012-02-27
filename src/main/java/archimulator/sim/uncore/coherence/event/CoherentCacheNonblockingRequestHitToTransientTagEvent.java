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
import archimulator.sim.uncore.coherence.CoherentCache;

public class CoherentCacheNonblockingRequestHitToTransientTagEvent extends CoherentCacheEvent {
    private int address;
    private MemoryHierarchyAccess requesterAccess;
    private CacheLine<?> lineFound;

    public CoherentCacheNonblockingRequestHitToTransientTagEvent(CoherentCache<?> cache, int address, MemoryHierarchyAccess requesterAccess, CacheLine<?> lineFound) {
        super(cache);

        this.address = address;
        this.requesterAccess = requesterAccess;
        this.lineFound = lineFound;
    }

    public int getAddress() {
        return address;
    }

    public MemoryHierarchyAccess getRequesterAccess() {
        return requesterAccess;
    }

    public CacheLine<?> getLineFound() {
        return lineFound;
    }
}