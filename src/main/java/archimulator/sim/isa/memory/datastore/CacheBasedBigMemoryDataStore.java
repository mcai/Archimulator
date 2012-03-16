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
package archimulator.sim.isa.memory.datastore;

import archimulator.model.simulation.BasicSimulationObject;
import archimulator.model.simulation.SimulationObject;
import archimulator.model.event.PollStatsEvent;
import archimulator.sim.isa.memory.Memory;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CacheBasedBigMemoryDataStore extends BasicSimulationObject implements MemoryDataStore {
    private Memory memory;
    private MemoryPageCache cache;

    private long accesses;
    private long hits;
    private long evictions;

    private Set<Integer> bufferIdsExistsOnDisk = new HashSet<Integer>();

    private Map<String, ByteBuffer> diskBbs = new HashMap<String, ByteBuffer>();

    public CacheBasedBigMemoryDataStore(final Memory memory) {
        super(memory);
        this.memory = memory;

        this.cache = new MemoryPageCache(this, "bigMemory.MemoryPageCache", new CacheGeometry(
                NUM_BUFFERS,
                NUM_BUFFERS,
                1),
                LeastRecentlyUsedEvictionPolicy.class,
                new Function3<Cache<?, ?>, Integer, Integer, MemoryPageCacheLine>() {
                    public MemoryPageCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                        return new MemoryPageCacheLine(cache, set, way);
                    }
                });

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                event.getStats().put("mem-" + memory.getProcessId() + ".bigMemory", String.format("accesses: %d, hits: %d, misses: %d, evictions: %d, hitRatio: %.4f\n", getAccesses(), getHits(), getMisses(), getEvictions(), getHitRatio()));
            }
        });
    }

    public void create(int pageId) {
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        int byteBufferIndex = getByteBufferIndex(pageId);

        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.prepare(byteBufferIndex);

        ByteBuffer bb = cacheAccess.getLine().bb;

        bb.position(getDirectByteBufferDisplacement(pageId) + displacement);

        if (write) {
            bb.put(buf, offset, size);
            cacheAccess.getLine().dirty = true;

        } else {
            bb.get(buf, offset, size);
        }
    }

    private CacheAccess<Boolean, MemoryPageCacheLine> prepare(int byteBufferIndex) {
        CacheAccess<Boolean, MemoryPageCacheLine> cacheAccess = this.cache.newAccess(byteBufferIndex, CacheAccessType.UNKNOWN);

        this.accesses++;

        if (cacheAccess.isHitInCache()) {
            cacheAccess.commit();
            this.hits++;
        } else {
            if (cacheAccess.isEviction()) {
                this.evictions++;

                cacheAccess.getLine().writeback();
            }

            cacheAccess.getLine().initOrLoadFromDisk(this.cache.getTag(byteBufferIndex));
            cacheAccess.getLine().setNonInitialState(true);
            cacheAccess.commit();
        }
        return cacheAccess;
    }

    private static int getByteBufferIndex(int pageId) {
        return pageId / NUM_PAGES_PER_BUFFER;
    }

    private static int getDiskCacheFileIndex(int byteBufferIndex) {
        return byteBufferIndex / NUM_BUFFERS_PER_DISK_CACHE_FILE;
    }

    private static int getDiskCacheFileDisplacement(int byteBufferIndex) {
        return (byteBufferIndex % NUM_BUFFERS_PER_DISK_CACHE_FILE) * BUFFER_LENGTH;
    }

    private static int getDirectByteBufferDisplacement(int pageId) {
        return (pageId % NUM_PAGES_PER_BUFFER) * Memory.getPageSize();
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

    private class MemoryPageCacheLine extends CacheLine<Boolean> {
        private transient ByteBuffer bb;
        private boolean dirty;

        private MemoryPageCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, false);
        }

        private ByteBuffer getDiskBb() {
            try {
                String diskCacheFileName = this.getDiskCacheFileName(this.getTag());

                if (!diskBbs.containsKey(diskCacheFileName)) {
                    RandomAccessFile raf = new RandomAccessFile(diskCacheFileName, "rws");
                    diskBbs.put(diskCacheFileName, raf.getChannel().map(FileChannel.MapMode.PRIVATE, 0, DISK_CACHE_FILE_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
                }

                return diskBbs.get(diskCacheFileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private MemoryPageCacheLine writeback() {
            if (dirty) {
                ByteBuffer diskBb = this.getDiskBb();

                diskBb.position(CacheBasedBigMemoryDataStore.getDiskCacheFileDisplacement(this.getTag()));

                this.bb.position(0);

                byte[] data = new byte[BUFFER_LENGTH];
                this.bb.get(data);

                diskBb.put(data);

                this.dirty = false;
            }

            this.bb.clear();

            bufferIdsExistsOnDisk.add(this.getTag());

            return this;
        }

        private MemoryPageCacheLine initOrLoadFromDisk(int newTag) {
            if (this.bb == null) {
                this.bb = ByteBuffer.allocateDirect(BUFFER_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            }

            if (bufferIdsExistsOnDisk.contains(newTag)) {
                if (!this.isDiskCacheFileExist(newTag)) {
                    throw new RuntimeException();
                }

                ByteBuffer diskBb = this.getDiskBb();

                diskBb.position(CacheBasedBigMemoryDataStore.getDiskCacheFileDisplacement(newTag));

                byte[] data = new byte[BUFFER_LENGTH];
                diskBb.get(data);

                this.bb.clear();
                this.bb.put(data);

                bufferIdsExistsOnDisk.remove(newTag);
            }

            return this;
        }

        private String getDiskCacheFileName(int tag) {
            return memory.getSimulationDirectory() + "/mem.process" + memory.getProcessId() + "." + getDiskCacheFileIndex(tag) + ".diskCache";
        }

        private boolean isDiskCacheFileExist(int tag) {
            return diskBbs.containsKey(this.getDiskCacheFileName(tag));
        }
    }

    private class MemoryPageCache extends EvictableCache<Boolean, MemoryPageCacheLine> {
        private MemoryPageCache(SimulationObject parent, String name, CacheGeometry geometry, Class<? extends EvictionPolicy> evictionPolicyClz, Function3<Cache<?, ?>, Integer, Integer, MemoryPageCacheLine> createLine) {
            super(parent, name, geometry, evictionPolicyClz, createLine);
        }
    }

    private static final int NUM_PAGES_PER_BUFFER = 8;
    private static final int NUM_BUFFERS = 128;
    private static final int NUM_BUFFERS_PER_DISK_CACHE_FILE = 4096;

    private static final int DISK_CACHE_FILE_LENGTH = NUM_BUFFERS_PER_DISK_CACHE_FILE * NUM_PAGES_PER_BUFFER * Memory.getPageSize(); // 128 Mb
    private static final int BUFFER_LENGTH = NUM_PAGES_PER_BUFFER * Memory.getPageSize();
}
