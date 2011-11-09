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
package archimulator.isa.memory.datastore;

import archimulator.isa.memory.Memory;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.event.PollStatsEvent;
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

public class NewBigMemoryDataStore extends BasicSimulationObject implements MemoryDataStore {
    private Memory memory;

    private long accesses;
    private long hits;
    private long evictions;

    private SimpleCache<Integer, ByteBuffer, DefaultSimpleCacheAccessType> cache;

    private Set<Integer> nextLevel = new HashSet<Integer>();

    private Map<String, ByteBuffer> diskBbs = new HashMap<String, ByteBuffer>();

    public NewBigMemoryDataStore(final Memory memory) {
        super(memory);
        this.memory = memory;

        this.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                event.getStats().put("mem-" + memory.getProcessId() + ".bigMemory", String.format("accesses: %d, hits: %d, misses: %d, evictions: %d, hitRatio: %.4f\n", getAccesses(), getHits(), getMisses(), getEvictions(), getHitRatio()));
            }
        });

        this.cache = new SimpleCache<Integer, ByteBuffer, DefaultSimpleCacheAccessType>(1024) {
            @Override
            protected void doWriteToNextLevel(Integer key, ByteBuffer value, DefaultSimpleCacheAccessType accessType) {
                nextLevel.add(key);

                if (accessType == DefaultSimpleCacheAccessType.WRITE) {
                    writeToDisk(key, value);
                }
            }

            @Override
            protected Pair<ByteBuffer, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key) {
                ByteBuffer bb = ByteBuffer.allocateDirect(Memory.getPageSize()).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);

                if (nextLevel.contains(key)) {
                    readFromDisk(key, bb);
                }

                return new Pair<ByteBuffer, DefaultSimpleCacheAccessType>(bb, DefaultSimpleCacheAccessType.READ);
            }

            @Override
            protected boolean existsOnNextLevel(Integer key) {
                return nextLevel.contains(key);
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
        ByteBuffer bb = ByteBuffer.allocateDirect(Memory.getPageSize()).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        writeToDisk(pageId, bb);

        this.nextLevel.add(pageId);
    }

    public void writeToDisk(int pageId, ByteBuffer bb) {
        ByteBuffer diskBb = this.getDiskBb(pageId);

        diskBb.position(getDiskCacheFileDisplacement(pageId));

        bb.position(0);

        byte[] data = new byte[Memory.getPageSize()];
        bb.get(data);

        diskBb.put(data);
    }

    public void readFromDisk(int pageId, ByteBuffer bb) {
        ByteBuffer diskBb = this.getDiskBb(pageId);

        diskBb.position(getDiskCacheFileDisplacement(pageId));

        byte[] data = new byte[Memory.getPageSize()];
        diskBb.get(data);

        bb.clear();
        bb.put(data);
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        ByteBuffer bb = this.cache.get(pageId, write ? DefaultSimpleCacheAccessType.WRITE : DefaultSimpleCacheAccessType.READ);

        bb.position(displacement);

        if (write) {
            bb.put(buf, offset, size);

            this.cache.put(pageId, bb, DefaultSimpleCacheAccessType.WRITE);
        } else {
            bb.get(buf, offset, size);
        }
    }

    private ByteBuffer getDiskBb(int pageIndex) {
        try {
            String diskCacheFileName = memory.getSimulationDirectory() + "/mem.process" + memory.getProcessId() + "." + pageIndex / NUM_PAGES_PER_DISK_CACHE_FILE + ".diskCache";

            if (!diskBbs.containsKey(diskCacheFileName)) {
                RandomAccessFile raf = new RandomAccessFile(diskCacheFileName, "rws");
                diskBbs.put(diskCacheFileName, raf.getChannel().map(FileChannel.MapMode.PRIVATE, 0, DISK_CACHE_FILE_LENGTH).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
            }

            return diskBbs.get(diskCacheFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int getDiskCacheFileDisplacement(int pageId) {
        return (pageId % NUM_PAGES_PER_DISK_CACHE_FILE) * Memory.getPageSize();
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

    private static final int NUM_PAGES_PER_DISK_CACHE_FILE = 32 * 1024;
    private static final int DISK_CACHE_FILE_LENGTH = NUM_PAGES_PER_DISK_CACHE_FILE * Memory.getPageSize(); // 128 Mb
}
