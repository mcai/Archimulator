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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class NewBigMemoryDataStore extends BasicSimulationObject implements MemoryDataStore {
    private Memory memory;

    private long accesses;
    private long hits;
    private long evictions;

    private SimpleCache<Integer, ByteBuffer, DefaultSimpleCacheAccessType> cache;

    private Map<Integer, ByteBuffer> nextLevel = new HashMap<Integer, ByteBuffer>();

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
                if (accessType == DefaultSimpleCacheAccessType.WRITE) {
                    nextLevel.put(key, value);
                }
            }

            @Override
            protected Pair<ByteBuffer, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key) {
                ByteBuffer value = nextLevel.get(key);
                return new Pair<ByteBuffer, DefaultSimpleCacheAccessType>(value, DefaultSimpleCacheAccessType.READ);
            }

            @Override
            protected boolean existsOnNextLevel(Integer key) {
                return nextLevel.containsKey(key);
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
        this.nextLevel.put(pageId, bb);
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        ByteBuffer bb = this.cache.get(pageId, write ? DefaultSimpleCacheAccessType.WRITE : DefaultSimpleCacheAccessType.READ);

        bb.position(displacement);

        if (write) {
            bb.put(buf, offset, size);
        } else {
            bb.get(buf, offset, size);
        }
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
}
