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
package archimulator.uncore.coherence.event;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.coherence.CoherentCache;

public class CoherentCacheServiceNonblockingRequestEvent extends CoherentCacheEvent {
    private int address;
    private MemoryHierarchyAccess requesterAccess;
    private CacheLine<?> lineFound;
    private boolean hitInCache;
    private boolean eviction;

    public CoherentCacheServiceNonblockingRequestEvent(CoherentCache<?> cache, int address, MemoryHierarchyAccess requesterAccess, CacheLine<?> lineFound, boolean hitInCache, boolean eviction) {
        super(cache);

        this.address = address;
        this.requesterAccess = requesterAccess;
        this.lineFound = lineFound;
        this.hitInCache = hitInCache;
        this.eviction = eviction;
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

    public boolean isHitInCache() {
        return hitInCache;
    }

    public boolean isEviction() {
        return eviction;
    }

    @Override
    public String toString() {
        return String.format("CoherentCacheServiceNonblockingRequestEvent{address=0x%08x, requesterAccess=%s, lineFound=%s, hitInCache=%s, eviction=%s}", address, requesterAccess, lineFound, hitInCache, eviction);
    }
}
