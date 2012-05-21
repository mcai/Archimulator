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
package archimulator.sim.uncore.coherence.common;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.dram.MainMemory;
import net.pickapack.action.Action1;

import java.io.Serializable;
import java.util.Random;

public abstract class CoherentCache<StateT extends Serializable, LineT extends LockableCacheLine<StateT>> extends MemoryDevice {
    private MemoryDevice next;

    private CoherentCacheConfig config;
    private LockableCache<StateT, LineT> cache;

    private Random random;

    private long numDownwardReadHits;
    private long numDownwardReadMisses;
    private long numDownwardReadBypasses;
    private long numDownwardWriteHits;
    private long numDownwardWriteMisses;
    private long numDownwardWriteBypasses;

    private long numEvictions;

    public CoherentCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config, LockableCache<StateT, LineT> cache) {
        super(cacheHierarchy, name);

        this.cache = cache;

        this.config = config;

        this.random = new Random(13);

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numDownwardReadHits = 0;
                numDownwardReadMisses = 0;
                numDownwardReadBypasses = 0;
                numDownwardWriteHits = 0;
                numDownwardWriteMisses = 0;
                numDownwardWriteBypasses = 0;

                numEvictions = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(CoherentCache.this.getName() + ".hitRatio", String.valueOf(getHitRatio()));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardAccesses", String.valueOf(getNumDownwardAccesses()));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardHits", String.valueOf(getNumDownwardHits()));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardMisses", String.valueOf(getNumDownwardMisses()));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardBypasses", String.valueOf(getNumDownwardBypasses()));

                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardReadHits", String.valueOf(numDownwardReadHits));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardReadMisses", String.valueOf(numDownwardReadMisses));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardReadBypasses", String.valueOf(numDownwardReadBypasses));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardWriteHits", String.valueOf(numDownwardWriteHits));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardWriteMisses", String.valueOf(numDownwardWriteMisses));
                    event.getStats().put(CoherentCache.this.getName() + ".numDownwardWriteBypasses", String.valueOf(numDownwardWriteBypasses));

                    event.getStats().put(CoherentCache.this.getName() + ".numEvictions", String.valueOf(numEvictions));
                }
            }
        });
    }

    public void updateStats(CacheAccessType cacheAccessType, CacheAccess<?, ?> cacheAccess) {
        if (cacheAccessType.isRead()) {
            if (!cacheAccessType.isUpward()) {
                if (cacheAccess.isHitInCache()) {
                    numDownwardReadHits++;
                } else {
                    if (!cacheAccess.isBypass()) {
                        numDownwardReadMisses++;
                    } else {
                        numDownwardReadBypasses++;
                    }
                }
            }
        } else {
            if (!cacheAccessType.isUpward()) {
                if (cacheAccess.isHitInCache()) {
                    numDownwardWriteHits++;
                } else {
                    if (!cacheAccess.isBypass()) {
                        numDownwardWriteMisses++;
                    } else {
                        numDownwardWriteBypasses++;
                    }
                }
            }
        }
    }

    public void incNumEvictions() {
        this.numEvictions++;
    }

    public int getRetryLatency() {
        int hitLatency = getHitLatency();
        return hitLatency + random.nextInt(hitLatency + 2);
    }

    private int getHitLatency() {
        return config.getHitLatency();
    }

    public LockableCache<StateT, LineT> getCache() {
        return cache;
    }

    public CoherentCacheConfig getConfig() {
        return config;
    }

    private double getHitRatio() {
        return getNumDownwardAccesses() > 0 ? (double) getNumDownwardHits() / (getNumDownwardAccesses()) : 0.0;
    }

    private long getNumDownwardHits() {
        return numDownwardReadHits + numDownwardWriteHits;
    }

    private long getNumDownwardMisses() {
        return numDownwardReadMisses + numDownwardWriteMisses;
    }

    private long getNumDownwardBypasses() {
        return numDownwardReadBypasses + numDownwardWriteBypasses;
    }

    private long getNumDownwardAccesses() {
        return getNumDownwardHits() + getNumDownwardMisses() + getNumDownwardBypasses();
    }

    public boolean isLastLevelCache() {
        return this.next instanceof MainMemory;
    }

    public MemoryDevice getNext() {
        return next;
    }

    public void setNext(MemoryDevice next) {
        this.next = next;
    }
}
