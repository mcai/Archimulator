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
import archimulator.sim.base.simulation.Logger;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.action.PendingActionOwner;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class CoherentCache extends MemoryDevice {
    private List<PendingActionOwner> pendingProcesses;

    private CoherentCacheConfig config;
    private LockableCache cache;

    private Random random;

    private long downwardReadHits;
    private long downwardReadMisses;
    private long downwardReadBypasses;
    private long downwardWriteHits;
    private long downwardWriteMisses;
    private long downwardWriteBypasses;

    private long evictions;

    public CoherentCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config, MESIState initialState) {
        super(cacheHierarchy, name);

        this.cache = new LockableCache(this, name, config.getGeometry(), initialState, config.getEvictionPolicyClz());

        this.config = config;

        this.random = new Random(13);

        this.init();
    }

    private void init() {
        this.pendingProcesses = new ArrayList<PendingActionOwner>();

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                downwardReadHits = 0;
                downwardReadMisses = 0;
                downwardReadBypasses = 0;
                downwardWriteHits = 0;
                downwardWriteMisses = 0;
                downwardWriteBypasses = 0;

                evictions = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(CoherentCache.this.getName() + ".hitRatio", String.valueOf(getHitRatio()));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardAccesses", String.valueOf(getDownwardAccesses()));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardHits", String.valueOf(getDownwardHits()));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardMisses", String.valueOf(getDownwardMisses()));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardBypasses", String.valueOf(getDownwardBypasses()));

                    event.getStats().put(CoherentCache.this.getName() + ".downwardReadHits", String.valueOf(downwardReadHits));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardReadMisses", String.valueOf(downwardReadMisses));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardReadBypasses", String.valueOf(downwardReadBypasses));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardWriteHits", String.valueOf(downwardWriteHits));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardWriteMisses", String.valueOf(downwardWriteMisses));
                    event.getStats().put(CoherentCache.this.getName() + ".downwardWriteBypasses", String.valueOf(downwardWriteBypasses));

                    event.getStats().put(CoherentCache.this.getName() + ".evictions", String.valueOf(evictions));
                }
            }
        });
    }

    public void incEvictions() {
        this.evictions++;
    }

    public void updateStats(CacheAccessType cacheAccessType, CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        if (cacheAccessType.isRead()) {
            if (!cacheAccessType.isUpward()) {
                if (cacheAccess.isHitInCache()) {
                    downwardReadHits++;
                } else {
                    if (!cacheAccess.isBypass()) {
                        downwardReadMisses++;
                    } else {
                        downwardReadBypasses++;
                    }
                }
            }
        } else {
            if (!cacheAccessType.isUpward()) {
                if (cacheAccess.isHitInCache()) {
                    downwardWriteHits++;
                } else {
                    if (!cacheAccess.isBypass()) {
                        downwardWriteMisses++;
                    } else {
                        downwardWriteBypasses++;
                    }
                }
            }
        }
    }

    public void dumpState() {
        if (!this.pendingProcesses.isEmpty()) {
            Logger.infof(Logger.COHRENCE, this.getName() + ":", this.getCycleAccurateEventQueue().getCurrentCycle());
            for (PendingActionOwner pendingProcess : this.pendingProcesses) {
                Logger.infof(Logger.COHRENCE, "\t%s\n", this.getCycleAccurateEventQueue().getCurrentCycle(), pendingProcess);
            }
        }
    }

    protected int getRetryLatency() {
        int hitLatency = getHitLatency();
        return hitLatency + random.nextInt(hitLatency + 2);
    }

    private int getHitLatency() {
        return config.getHitLatency();
    }

    public LockableCache getCache() {
        return cache;
    }

    public CoherentCacheConfig getConfig() {
        return config;
    }

    private double getHitRatio() {
        return getDownwardAccesses() > 0 ? (double) getDownwardHits() / (getDownwardAccesses()) : 0.0;
    }

    private long getDownwardHits() {
        return downwardReadHits + downwardWriteHits;
    }

    private long getDownwardMisses() {
        return downwardReadMisses + downwardWriteMisses;
    }

    private long getDownwardBypasses() {
        return downwardReadBypasses + downwardWriteBypasses;
    }

    private long getDownwardAccesses() {
        return getDownwardHits() + getDownwardMisses() + getDownwardBypasses();
    }

    private void processPendingActions() {
        for (Iterator<PendingActionOwner> it = this.pendingProcesses.iterator(); it.hasNext(); ) {
            try {
                if (it.next().processPendingActions()) {
                    it.remove();
                }
            } catch (CoherentCacheException e) {
                it.remove();
            }
        }

        if (!this.pendingProcesses.isEmpty()) {
            this.getCycleAccurateEventQueue().schedule(new Action() {
                public void apply() {
                    processPendingActions();
                }
            }, 1);
        }
    }

    protected void scheduleProcess(PendingActionOwner process) {
        this.pendingProcesses.add(process);
        this.processPendingActions();
    }

    public boolean isLastLevelCache() {
        return this.config.getLevelType() == CoherentCacheLevelType.LAST_LEVEL_CACHE;
    }
}
