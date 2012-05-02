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
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;
import archimulator.sim.uncore.coherence.action.PendingActionOwner;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.util.action.*;

import java.util.*;

public abstract class CoherentCache extends MemoryDevice {
    private List<PendingActionOwner> pendingProcesses;

    protected CoherentCacheConfig config;
    protected LockableCache cache;

    private Random random;

    private long downwardReadHits;
    private long downwardReadMisses;
    private long downwardReadBypasses;
    private long downwardWriteHits;
    private long downwardWriteMisses;
    private long downwardWriteBypasses;

    protected long evictions;

    public CoherentCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config, MESIState initialState) {
        super(cacheHierarchy, name);

        this.cache = new LockableCache(name, config.getGeometry(), initialState, config.getEvictionPolicyClz());

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
            this.getCycleAccurateEventQueue().schedule(new NamedAction("CoherentCache.processPendingActions") {
                //            this.getCycleAccurateEventQueue().schedule(new Action() {
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

    public class LockableCacheLine extends CacheLine<MESIState> {
        private int transientTag = -1;
        private List<Action> suspendedActions;

        public LockableCacheLine(Cache<?, ?> cache, int set, int way, MESIState initialState) {
            super(cache, set, way, initialState);

            this.suspendedActions = new ArrayList<Action>();
        }

        public boolean lock(Action action, int transientTag) {
            if (this.isLocked()) {
                this.suspendedActions.add(action);
                return false;
            } else {
                this.transientTag = transientTag;
                return true;
            }
        }

        public LockableCacheLine unlock() {
            assert (this.isLocked());

            this.transientTag = -1;

            for (Action action : this.suspendedActions) {
                getCycleAccurateEventQueue().schedule(action, 0);
            }

            this.suspendedActions.clear();

            return this;
        }

        public int getTransientTag() {
            return transientTag;
        }

        public boolean isLocked() {
            return transientTag != -1;
        }

        @Override
        public String toString() {
            return String.format("LockableCacheLine{set=%d, way=%d, tag=%s, transientTag=%s, state=%s}", getSet(), getWay(), getTag() == -1 ? "<INVALID>" : String.format("0x%08x", getTag()), transientTag == -1 ? "<INVALID>" : String.format("0x%08x", transientTag), getState());
        }
    }

    public class LockableCache extends EvictableCache<MESIState, LockableCacheLine> {
        public LockableCache(String name, CacheGeometry geometry, final MESIState initialState, Class<? extends EvictionPolicy> evictionPolicyClz) {
            super(CoherentCache.this, name, geometry, evictionPolicyClz, new Function3<Cache<?, ?>, Integer, Integer, LockableCacheLine>() {
                public LockableCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                    return new LockableCacheLine(cache, set, way, initialState);
                }
            });
        }

        @Override
        public LockableCacheLine findLine(int address) {
            int tag = this.getTag(address);
            int set = this.getSet(address);

            for (int way = 0; way < this.getAssociativity(); way++) {
                LockableCacheLine line = this.getLine(set, way);
                if (!line.isLocked() && line.getTag() == tag && line.getState() != line.getInitialState()) {
                    return line;
                } else if (line.isLocked() && line.getTransientTag() == tag) {
                    return line;
                }
            }

            return null;
        }
    }
}
