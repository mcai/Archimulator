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

import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.isa.memory.Memory;
import archimulator.util.Pair;
import archimulator.util.action.Action1;
import archimulator.util.simpleCache.DefaultSimpleCacheAccessType;
import archimulator.util.simpleCache.GetValueEvent;
import archimulator.util.simpleCache.SetValueEvent;
import archimulator.util.simpleCache.SimpleCache;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleCacheBasedNewBigMemoryDataStore extends BasicSimulationObject implements MemoryDataStore {
    private Memory memory;

    private long accesses;
    private long hits;
    private long evictions;

    private SimpleCache<Integer, ByteBuffer, DefaultSimpleCacheAccessType> cache;

    private Set<Integer> bufferIdsExistsOnDisk = new HashSet<Integer>();

    private Map<String, ByteBuffer> diskBbs = new HashMap<String, ByteBuffer>();

    public SimpleCacheBasedNewBigMemoryDataStore(final Memory memory) {
        super(memory);
        this.memory = memory;

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                event.getStats().put("mem-" + memory.getProcessId() + ".bigMemory", String.format("accesses: %d, hits: %d, misses: %d, evictions: %d, hitRatio: %.4f\n", getAccesses(), getHits(), getMisses(), getEvictions(), getHitRatio()));
            }
        });

        this.cache = new SimpleCache<Integer, ByteBuffer, DefaultSimpleCacheAccessType>(1, NUM_BUFFERS) {
            @Override
            protected void doWriteToNextLevel(Integer key, ByteBuffer value, boolean writeback) {
                if (writeback) {
                    writeToDisk(key, value);
                    bufferIdsExistsOnDisk.add(key);
                }
            }

            @Override
            protected Pair<ByteBuffer, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key, ByteBuffer oldValue) {
                if (oldValue == null) {
//                    oldValue = ByteBuffer.allocateDirect(BUFFER_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                    oldValue = ByteBuffer.allocate(BUFFER_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                }

                if (bufferIdsExistsOnDisk.contains(key)) {
                    readFromDisk(key, oldValue);
                }

                return new Pair<ByteBuffer, DefaultSimpleCacheAccessType>(oldValue, DefaultSimpleCacheAccessType.READ);
            }
        };

        this.cache.getCacheEventDispatcher().addListener(SetValueEvent.class, new Action1<SetValueEvent>() {
            public void apply(SetValueEvent event) {
                accesses++;
                if (event.isHitInCache()) {
                    hits++;
                }
                if (event.isEviction()) {
                    evictions++;
                }
            }
        });

        this.cache.getCacheEventDispatcher().addListener(GetValueEvent.class, new Action1<GetValueEvent>() {
            public void apply(GetValueEvent event) {
                accesses++;
                if (event.isHitInCache()) {
                    hits++;
                }
                if (event.isEviction()) {
                    evictions++;
                }
            }
        });
    }

    public void create(int pageId) {
    }

    public void writeToDisk(int byteBufferIndex, ByteBuffer bb) {
        ByteBuffer diskBb = this.getDiskBb(byteBufferIndex);

        diskBb.position(getDiskCacheFileDisplacement(byteBufferIndex));

        bb.position(0);

        byte[] data = new byte[BUFFER_LENGTH];
        bb.get(data);

        diskBb.put(data);
    }

    public void readFromDisk(int byteBufferIndex, ByteBuffer bb) {
        ByteBuffer diskBb = this.getDiskBb(byteBufferIndex);

        diskBb.position(getDiskCacheFileDisplacement(byteBufferIndex));

        byte[] data = new byte[BUFFER_LENGTH];
        diskBb.get(data);

        bb.clear();
        bb.put(data);
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        int byteBufferIndex = getByteBufferIndex(pageId);

        ByteBuffer bb = this.cache.get(0, byteBufferIndex, write ? DefaultSimpleCacheAccessType.WRITE : DefaultSimpleCacheAccessType.READ);

        int directByteBufferDisplacement = getDirectByteBufferDisplacement(pageId);

        bb.position(directByteBufferDisplacement + displacement);

        if (write) {
            bb.put(buf, offset, size);
            this.cache.put(0, byteBufferIndex, bb, DefaultSimpleCacheAccessType.WRITE);
        } else {
            bb.get(buf, offset, size);
        }
    }

    private ByteBuffer getDiskBb(int byteBufferIndex) {
        try {
            String diskCacheFileName = memory.getSimulationDirectory() + "/mem.process" + memory.getProcessId() + "." + byteBufferIndex / NUM_BUFFERS_PER_DISK_CACHE_FILE + ".diskCache";

            if (!diskBbs.containsKey(diskCacheFileName)) {
                RandomAccessFile raf = new RandomAccessFile(diskCacheFileName, "rws");
                diskBbs.put(diskCacheFileName, raf.getChannel().map(FileChannel.MapMode.PRIVATE, 0, DISK_CACHE_FILE_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
            }

            return diskBbs.get(diskCacheFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getByteBufferIndex(int pageId) {
        return pageId / NUM_PAGES_PER_BUFFER;
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

    private static final int NUM_PAGES_PER_BUFFER = 8;
    private static final int NUM_BUFFERS = 128;
    private static final int NUM_BUFFERS_PER_DISK_CACHE_FILE = 4096;

    private static final int DISK_CACHE_FILE_LENGTH = NUM_BUFFERS_PER_DISK_CACHE_FILE * NUM_PAGES_PER_BUFFER * Memory.getPageSize(); // 128 Mb
    private static final int BUFFER_LENGTH = NUM_PAGES_PER_BUFFER * Memory.getPageSize();
}
