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
package archimulator.isa.bigMemory;

import archimulator.isa.Memory;
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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

public class BigMemory extends BasicSimulationObject {
    private String simulationDirectory;
    private boolean littleEndian;
    private int processId;
    private MemoryPageCache cache;

    private long accesses;
    private long hits;
    private long evictions;

    public BigMemory(Memory memory, String simulationDirectory, boolean littleEndian, int processId) {
        super(memory);

        this.simulationDirectory = simulationDirectory;
        this.littleEndian = littleEndian;
        this.processId = processId;

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

    public void create(int pageId) {
        this.prepare(pageId);
    }

    public void readWrite(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.prepare(pageId);

        ByteBuffer bb = cacheAccess.getLine().bb;

        int directByteBufferDisplacement = getDirectByteBufferDisplacement(pageId);
        bb.position(directByteBufferDisplacement + displacement);

        if (write) {
            bb.put(buf, offset, size);
            cacheAccess.getLine().dirty = true;
        } else {
            bb.get(buf, offset, size);
        }
    }

    private CacheAccess<Boolean, MemoryPageCacheLine> prepare(int pageId) {
        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.cache.newAccess(pageId, CacheAccessType.UNKNOWN);

        this.accesses++;

        if (cacheAccess.isHitInCache()) {
            cacheAccess.commit();
            this.hits++;
        } else {
            if (cacheAccess.isEviction()) {
                this.evictions++;

                if (cacheAccess.getLine().dirty) {
                    cacheAccess.getLine().saveToDisk();
                }
            }
            cacheAccess.commit().getLine().initOrLoadFromDisk().setNonInitialState(true);
        }
        return cacheAccess;
    }

    private static int getByteBufferSize() {
        return Memory.getPageSize() * MEMORY_PAGE_CACHE_LINE_SIZE;
    }

    private static int getDiskCacheFileIndex(int pageIndex) {
        return pageIndex / NUM_PAGES_PER_DISK_CACHE_FILE;
    }

    private static int getDiskCacheFileDisplacement(int pageIndex) {
        return (pageIndex % NUM_PAGES_PER_DISK_CACHE_FILE) * Memory.getPageSize();
    }

    private static int getDirectByteBufferDisplacement(int pageIndex) {
        return (pageIndex % MEMORY_PAGE_CACHE_LINE_SIZE) * Memory.getPageSize();
    }

    public long getAccesses() {
        return this.accesses;
    }

    public long getHits() {
        return this.hits;
    }

    public long getEvictions() {
        return this.evictions;
    }

    public long getMisses() {
        return this.accesses - this.hits;
    }

    public double getHitRatio() {
        return this.accesses > 0 ? (double) this.hits / this.accesses : 0.0;
    }

    private Set<Integer> evictedPageIds = new HashSet<Integer>();

    private class MemoryPageCacheLine extends CacheLine<Boolean> {
        private transient ByteBuffer bb;
        private boolean dirty;

        private MemoryPageCacheLine(int set, int way) {
            super(set, way, false);
        }

        public MemoryPageCacheLine saveToDisk() {
            try {
                RandomAccessFile raf = new RandomAccessFile(getDiskCacheFileName(), "rws");
                ByteBuffer diskBb = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, DISK_CACHE_FILE_LENGTH).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

                int displacement = getDiskCacheFileDisplacement(this.tag);

                diskBb.position(displacement);
                this.bb.position(0);

                diskBb.put(this.bb);

                raf.close();

                this.dirty = false;

                evictedPageIds.add(this.tag);

                return this;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public MemoryPageCacheLine initOrLoadFromDisk() {
            if(this.bb == null)
            {
                this.bb = ByteBuffer.allocateDirect(getByteBufferSize()).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            }

            if (evictedPageIds.contains(this.tag)) {
                if(!isDiskCacheFileExist()) {
                    throw new RuntimeException();
                }

                try {
                    RandomAccessFile raf = new RandomAccessFile(getDiskCacheFileName(), "r");
                    ByteBuffer diskBb = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, DISK_CACHE_FILE_LENGTH).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

                    int displacement = getDiskCacheFileDisplacement(this.tag);

                    diskBb.position(displacement);

                    this.bb.clear();

                    byte[] data = new byte[getByteBufferSize()];
                    diskBb.get(data);

                    this.bb.position(0);
                    this.bb.put(data);

                    evictedPageIds.remove(this.tag);

                    raf.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return this;
        }

        private String getDiskCacheFileName() {
            int pageIndex = this.tag;
            int fileIndex = getDiskCacheFileIndex(pageIndex);

            return simulationDirectory + "/mem.process" + processId + "." + fileIndex + ".diskCache";
        }

        private boolean isDiskCacheFileExist() {
            return new File(getDiskCacheFileName()).exists();
        }
    }

    private class MemoryPageCache extends EvictableCache<Boolean, MemoryPageCacheLine> {
        private MemoryPageCache(SimulationObject parent, String name, CacheGeometry geometry, EvictionPolicyFactory evictionPolicyFactory, Function2<Integer, Integer, MemoryPageCacheLine> createLine) {
            super(parent, name, geometry, evictionPolicyFactory, createLine);
        }
    }

    private static final int MEMORY_PAGE_CACHE_CAPACITY = 512; // 8Mb
    //    private static final int MEMORY_PAGE_CACHE_CAPACITY = 512;
    private static final int MEMORY_PAGE_CACHE_LINE_SIZE = 8;

    private static final int NUM_PAGES_PER_DISK_CACHE_FILE = 32 * 1024;
    private static final int DISK_CACHE_FILE_LENGTH = NUM_PAGES_PER_DISK_CACHE_FILE * Memory.getPageSize(); // 128 Mb
}
