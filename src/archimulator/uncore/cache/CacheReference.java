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
package archimulator.uncore.cache;

import archimulator.uncore.CacheAccessType;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.coherence.CoherentCache;

public class CacheReference {
    private CoherentCache<?> coherentCache;
    private MemoryHierarchyAccess access;
    private int set;
    private int address;
    private int tag;
    private CacheAccessType accessType;

    public CacheReference(CoherentCache<?> coherentCache, MemoryHierarchyAccess access, int address, int tag, CacheAccessType accessType, int set) {
        this.coherentCache = coherentCache;
        this.access = access;
        this.set = set;
        this.address = address;
        this.tag = tag;
        this.accessType = accessType;
    }

    public CoherentCache<?> getCoherentCache() {
        return coherentCache;
    }

    public MemoryHierarchyAccess getAccess() {
        return access;
    }

    public int getSet() {
        return set;
    }

    public int getAddress() {
        return address;
    }

    public int getTag() {
        return tag;
    }

    public CacheAccessType getAccessType() {
        return accessType;
    }

    @Override
    public String toString() {
        return String.format("CacheReference{threadId=%d, tag=%s, accessType=%s}", (getAccess() != null ? getAccess().getThread().getId() : -1), tag == -1 ? "<INVALID>" : String.format("0x%08x", tag), accessType);
    }
}
