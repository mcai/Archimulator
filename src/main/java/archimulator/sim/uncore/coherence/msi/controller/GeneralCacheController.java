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
package archimulator.sim.uncore.coherence.msi.controller;

import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.cache.CacheGeometry;
import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

public abstract class GeneralCacheController<StateT extends Serializable> extends Controller {
    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;

    private long numEvictions;

    public GeneralCacheController(CacheHierarchy cacheHierarchy, String name) {
        super(cacheHierarchy, name);
    }

    public abstract EvictableCache<StateT> getCache();

    public void updateStats(EvictableCache<?> cache, boolean read, boolean hitInCache) {
        if (read) {
            if (hitInCache) {
                numDownwardReadHits++;
            } else {
                numDownwardReadMisses++;
            }
        } else {
            if (hitInCache) {
                numDownwardWriteHits++;
            } else {
                numDownwardWriteMisses++;
            }
        }
    }

    public void incrementNumEvictions() {
        this.numEvictions++;
    }

    public double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    public long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    public long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    public long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses();
    }

    public long getNumDownwardReadHits() {
        return numDownwardReadHits;
    }

    public long getNumDownwardReadMisses() {
        return numDownwardReadMisses;
    }

    public long getNumDownwardWriteHits() {
        return numDownwardWriteHits;
    }

    public long getNumDownwardWriteMisses() {
        return numDownwardWriteMisses;
    }

    public long getNumEvictions() {
        return numEvictions;
    }

    public double getOccupancyRatio() {
        return getCache().getOccupancyRatio();
    }

    public abstract CacheGeometry getGeometry();
}
