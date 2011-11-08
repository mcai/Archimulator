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
package archimulator.isa.memory.bigMemory;

import archimulator.isa.memory.Memory;
import archimulator.mem.CacheAccessType;
import archimulator.mem.cache.CacheAccess;
import archimulator.mem.cache.CacheGeometry;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.cache.EvictableCache;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.SimulationObject;
import archimulator.sim.event.PollStatsEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class BigMemoryDataStore extends BasicSimulationObject implements MemoryDataStore { //TODO: errors remain
    private Memory memory;
    private MemoryPageCache cache;

    private long accesses;
    private long hits;
    private long evictions;

    public BigMemoryDataStore(final Memory memory) {
        super(memory);
        this.memory = memory;

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

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                event.getStats().put("mem-" + memory.getProcessId() + ".bigMemory", String.format("accesses: %d, hits: %d, misses: %d, evictions: %d, hitRatio: %.4f\n", getAccesses(), getHits(), getMisses(), getEvictions(), getHitRatio()));
            }
        });
    }

    public void create(int pageId) {
        this.prepare(pageId);
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.prepare(pageId);

        ByteBuffer bb = cacheAccess.getLine().bb;

        int directByteBufferDisplacement = getDirectByteBufferDisplacement(pageId);
        bb.position(directByteBufferDisplacement + displacement);

        if (write) {
            bb.put(buf, offset, size);
            cacheAccess.getLine().dirty = true;

            byte[] buf2 = new byte[buf.length];
            this.access(pageId, displacement, buf2, offset, size, false);

            for (int i = offset; i < offset + size; i++) {
                if (buf[i] != buf2[i]) {
                    throw new IllegalArgumentException();
                }
            }

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

    private static int getDiskCacheFileDisplacement(int pageId) {
        return (pageId % NUM_PAGES_PER_DISK_CACHE_FILE) * Memory.getPageSize();
    }

    private static int getDirectByteBufferDisplacement(int pageId) {
        return (pageId % MEMORY_PAGE_CACHE_LINE_SIZE) * Memory.getPageSize();
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

    private Map<String, ByteBuffer> diskBbs = new HashMap<String, ByteBuffer>();

    private Map<Integer, byte[]> perLineDiskBbs = new HashMap<Integer, byte[]>();

    private class MemoryPageCacheLine extends CacheLine<Boolean> {
        private transient ByteBuffer bb;
        private boolean dirty;
        private boolean loadedFromDisk;

        private MemoryPageCacheLine(int set, int way) {
            super(set, way, false);
        }

        private ByteBuffer getDiskBb() {
//            try {
            String diskCacheFileName = this.getDiskCacheFileName();

            if (!diskBbs.containsKey(diskCacheFileName)) {
//                    RandomAccessFile raf = new RandomAccessFile(diskCacheFileName, "rws");
//                    diskBbs.put(diskCacheFileName, raf.getChannel().map(FileChannel.MapMode.PRIVATE, 0, DISK_CACHE_FILE_LENGTH).order(littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));

                diskBbs.put(diskCacheFileName, ByteBuffer.allocateDirect(DISK_CACHE_FILE_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
            }

            return diskBbs.get(diskCacheFileName);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }

        private MemoryPageCacheLine saveToDisk() {
            ByteBuffer diskBb = this.getDiskBb();

            int displacement = getDiskCacheFileDisplacement(this.tag);

            diskBb.position(displacement);

            this.bb.position(0);

            byte[] data = new byte[getByteBufferSize()];
            this.bb.get(data);
            this.bb.clear();

            diskBb.put(data);

            perLineDiskBbs.put(this.tag, data);

//                raf.close();

            this.dirty = false;

            evictedPageIds.add(this.tag);

            loadedFromDisk = false;

            return this;
        }

        private MemoryPageCacheLine initOrLoadFromDisk() {
            if (this.bb == null) {
                this.bb = ByteBuffer.allocateDirect(getByteBufferSize()).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            }

            if (evictedPageIds.contains(this.tag)) {
                if (!this.isDiskCacheFileExist()) {
                    throw new RuntimeException();
                }

                ByteBuffer diskBb = this.getDiskBb();

                int displacement = getDiskCacheFileDisplacement(this.tag);

                diskBb.position(displacement);

                byte[] data = new byte[getByteBufferSize()];
                diskBb.get(data);

                this.bb.clear();
                this.bb.put(data);

                if (!Arrays.equals(data, perLineDiskBbs.get(this.tag))) {
                    throw new IllegalArgumentException();
                }

                evictedPageIds.remove(this.tag);

                loadedFromDisk = true;

//                    raf.close();
            }

            return this;
        }

        private String getDiskCacheFileName() {
            int pageIndex = this.tag;
            int fileIndex = getDiskCacheFileIndex(pageIndex);

            return memory.getSimulationDirectory() + "/mem.process" + memory.getProcessId() + "." + fileIndex + ".diskCache";
        }

        private boolean isDiskCacheFileExist() {
            return diskBbs.containsKey(this.getDiskCacheFileName());
//            return new File(this.getDiskCacheFileName()).exists();
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
