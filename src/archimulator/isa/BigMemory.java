/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.isa;

import archimulator.mem.CacheAccessType;
import archimulator.mem.cache.CacheAccess;
import archimulator.mem.cache.CacheGeometry;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.cache.EvictableCache;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.SimulationObject;
import archimulator.util.action.Function2;

import java.nio.ByteBuffer;

public class BigMemory extends BasicSimulationObject {
    private boolean littleEndian;
    private MemoryPageCache cache;

    private long accesses;
    private long hits;
    private long evictions;

    public BigMemory(SimulationObject parent, boolean littleEndian) {
        super(parent);

        this.littleEndian = littleEndian;

        this.cache = new MemoryPageCache(this, "bigMemory.MemoryPageCache", new CacheGeometry(
                MEMORY_PAGE_CACHE_CAPACITY,
                MEMORY_PAGE_CACHE_CAPACITY / MEMORY_PAGE_CACHE_LINE_SIZE,
                MEMORY_PAGE_CACHE_LINE_SIZE),
                LeastRecentlyUsedEvictionPolicy.FACTORY,
                new Function2<Integer, Integer, MemoryPageCacheLine>() {
                    public MemoryPageCacheLine apply(Integer set, Integer way) {
                        return new MemoryPageCacheLine(set, way);
                    }
                });
    }

    public boolean access(int address) {
        address++;

        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.cache.newAccess(address, CacheAccessType.UNKNOWN);

        this.accesses++;

        if (cacheAccess.isHitInCache()) {
            cacheAccess.commit();
            this.hits++;
        } else {
            if (cacheAccess.isEviction()) {
                this.evictions++;

                cacheAccess.getLine().saveToDisk();
            }
            cacheAccess.commit().getLine().loadFromDisk().setNonInitialState(true);
        }

        return cacheAccess.isHitInCache();
    }

    public long getAccesses() {
        return accesses;
    }

    public long getHits() {
        return hits;
    }

    public long getEvictions() {
        return evictions;
    }

    public long getMisses() {
        return this.accesses - this.hits;
    }

    public double getHitRatio() {
        return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
    }

    private class MemoryPageCacheLine extends CacheLine<Boolean> {
        private transient ByteBuffer bb;

        private MemoryPageCacheLine(int set, int way) {
            super(set, way, false);
        }

        public MemoryPageCacheLine allocate() {
//            this.bb = ByteBuffer.allocateDirect(Memory.getPageSize()).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            return this;
        }

        public MemoryPageCacheLine saveToDisk() {
            //TODO
            return this;
        }

        public MemoryPageCacheLine loadFromDisk() {
            //TODO
            return this;
        }
    }

    private class MemoryPageCache extends EvictableCache<Boolean, MemoryPageCacheLine> {
        private MemoryPageCache(SimulationObject parent, String name, CacheGeometry geometry, EvictionPolicyFactory evictionPolicyFactory, Function2<Integer, Integer, MemoryPageCacheLine> createLine) {
            super(parent, name, geometry, evictionPolicyFactory, createLine);
        }
    }

    private static final int MEMORY_PAGE_CACHE_CAPACITY = 2048;
    private static final int MEMORY_PAGE_CACHE_LINE_SIZE = 4;
}
