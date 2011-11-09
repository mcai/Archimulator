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
package archimulator.mem.coherence;

import archimulator.mem.CacheAccessType;
import archimulator.mem.CacheHierarchy;
import archimulator.mem.MemoryDevice;
import archimulator.mem.MemoryHierarchyAccess;
import archimulator.mem.cache.CacheAccess;
import archimulator.mem.cache.CacheGeometry;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.cache.EvictableCache;
import archimulator.mem.cache.eviction.EvictionPolicyFactory;
import archimulator.mem.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.mem.coherence.event.CoherentCacheFillLineEvent;
import archimulator.mem.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.mem.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.mem.coherence.exception.CacheLineLockFailedException;
import archimulator.mem.coherence.exception.CoherentCacheException;
import archimulator.sim.Logger;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function2;
import archimulator.util.action.NamedAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

public abstract class CoherentCache<StateT extends Serializable> extends MemoryDevice {
    private transient List<PendingActionOwner> pendingProcesses;

    protected CoherentCacheConfig config;
    protected LockableCache cache;

    private Random random;

    private long downwardReadHits;
    private long downwardReadMisses;
    private long downwardReadBypasses;
    private long downwardWriteHits;
    private long downwardWriteMisses;
    private long downwardWriteBypasses;

    private long evictions;

    public CoherentCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config, StateT initialState) {
        super(cacheHierarchy, name);

        this.cache = new LockableCache(name, config.getGeometry(), initialState, config.getEvictionPolicyFactory());

        this.config = config;

        this.random = new Random(13);

        this.init();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

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

    protected void updateStats(CacheAccessType cacheAccessType, CacheAccess<StateT, LockableCacheLine> cacheAccess) {
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
            this.getLogger().infof(Logger.COHRENCE, this.getName() + ":");
            for (PendingActionOwner pendingProcess : this.pendingProcesses) {
                this.getLogger().infof(Logger.COHRENCE, "\t%s\n", pendingProcess);
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

    protected void scheduleProcess(final PendingActionOwner process) {
        this.pendingProcesses.add(process);
        this.processPendingActions();
    }

    public boolean isLastLevelCache() {
        return this.config.getLevelType() == CoherentCacheLevelType.LAST_LEVEL_CACHE;
    }

    public class LockableCacheLine extends CacheLine<StateT> {
        private transient int transientTag;
        private List<Action> suspendedActions;

        public LockableCacheLine(int set, int way, StateT initialState) {
            super(set, way, initialState);

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

            this.transientTag = 0;

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
            return transientTag != 0;
        }
    }

    public class LockableCache extends EvictableCache<StateT, LockableCacheLine> {
        public LockableCache(String name, CacheGeometry geometry, final StateT initialState, EvictionPolicyFactory evictionPolicyFactory) {
            super(CoherentCache.this, name, geometry, evictionPolicyFactory, new Function2<Integer, Integer, LockableCacheLine>() {
                public LockableCacheLine apply(Integer set, Integer way) {
                    return new LockableCacheLine(set, way, initialState);
                }
            });
        }

        @Override
        public LockableCacheLine findLine(int address) {
            int tag = this.getTag(address);
            int set = this.getSet(address);

            for (LockableCacheLine line : this.sets.get(set).getLines()) {
                if (line.getTag() == tag && line.getState() != line.getInitialState() || line.getTransientTag() == tag && line.isLocked()) {
                    return line;
                }
            }

            return null;
        }
    }

    public abstract class CoherentCacheProcess implements PendingActionOwner {
        protected long id;
        private List<Action1<? extends PendingActionOwner>> onCompletedCallbacks;
        private Stack<PendingActionOwner> pendingActions;

        public CoherentCacheProcess() {
            this.id = currentCoherentCacheProcessId++;
            this.onCompletedCallbacks = new ArrayList<Action1<? extends PendingActionOwner>>();
            this.pendingActions = new Stack<PendingActionOwner>();
        }

        @SuppressWarnings("unchecked")
        protected void complete() {
            for (Action1<? extends PendingActionOwner> onCompletedCallback : this.onCompletedCallbacks) {
                ((Action1<PendingActionOwner>) onCompletedCallback).apply(this);
            }

            this.onCompletedCallbacks.clear();
        }

        public CoherentCacheProcess addOnCompletedCallback(Action1<? extends PendingActionOwner> onCompletedCallback) {
            this.onCompletedCallbacks.add(onCompletedCallback);
            return this;
        }

        public boolean processPendingActions() throws CoherentCacheException {
            if (this.pendingActions.empty()) {
                this.complete();
                return true;
            } else {
                PendingActionOwner peek = this.pendingActions.peek();
                if (peek.processPendingActions()) {
                    this.pendingActions.remove(peek);
                }

                if (this.pendingActions.empty()) {
                    this.complete();
                    return true;
                }

                return false;
            }
        }

        protected Stack<PendingActionOwner> getPendingActions() {
            return pendingActions;
        }
    }

    private static long currentCoherentCacheProcessId = 0;

    public enum FindAndLockStatus {
        IDLE,
        ACQUIRED,
        EVICTING,
        WAITING,
        RELEASED,
        FAILED,
        BYPASSED;

        public boolean isAcquiredOrBypassed() {
            return this == ACQUIRED || this == BYPASSED;
        }
    }

    public abstract class AbstractFindAndLockProcess extends CoherentCacheProcess {
        protected CacheAccess<StateT, LockableCacheLine> cacheAccess;
        private FindAndLockStatus status;

        protected AbstractFindAndLockProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
            this.status = FindAndLockStatus.IDLE;

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (cacheAccess.isEviction()) {
                        evict(access, new Action() {
                            public void apply() {
                                status = FindAndLockStatus.ACQUIRED;
                            }
                        });
                    }

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() throws CoherentCacheException {
                    if (status == FindAndLockStatus.IDLE) {
                        doLockingProcess(access, tag, cacheAccessType);
                    }

                    return status != FindAndLockStatus.WAITING;
                }
            });
        }

        private void doLockingProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
            if (this.status == FindAndLockStatus.IDLE) {
                if (this.cacheAccess == null) {
                    this.cacheAccess = getCache().newAccess(CoherentCache.this, access, tag, cacheAccessType);
                }

                if (this.cacheAccess.isHitInCache() || !this.cacheAccess.isBypass()) {
                    if (this.cacheAccess.getLine().isLocked() && !cacheAccessType.isUpward()) {
                        if (this.cacheAccess.isHitInCache()) {
                            getBlockingEventDispatcher().dispatch(new CoherentCacheNonblockingRequestHitToTransientTagEvent(CoherentCache.this, tag, access, this.cacheAccess.getLine()));
                        }
                    }

                    if (this.cacheAccess.getLine().isLocked() && cacheAccessType.isDownward()) {
                        this.status = FindAndLockStatus.FAILED;
                    }

                    if (this.cacheAccess.getLine().lock(new Action() {
                        public void apply() {
                            status = FindAndLockStatus.IDLE;
                            doLockingProcess(access, tag, cacheAccessType);
                        }
                    }, tag)) {
                        if (!cacheAccessType.isUpward()) {
                            getBlockingEventDispatcher().dispatch(new CoherentCacheServiceNonblockingRequestEvent(CoherentCache.this, tag, access, this.cacheAccess.getLine()));

                            if (!this.cacheAccess.isHitInCache()) {
                                getBlockingEventDispatcher().dispatch(new CoherentCacheFillLineEvent(CoherentCache.this, tag, access, this.cacheAccess.getLine()));
                            }
                        }

                        if (this.cacheAccess.isEviction()) {
                            evictions++;
                            status = FindAndLockStatus.EVICTING;
                        } else {
                            status = FindAndLockStatus.ACQUIRED;
                        }
                    } else {
                        status = FindAndLockStatus.WAITING;
                    }
                } else {
                    status = FindAndLockStatus.BYPASSED;
                }

                if (status == FindAndLockStatus.ACQUIRED || status == FindAndLockStatus.EVICTING || status == FindAndLockStatus.BYPASSED) {
                    updateStats(cacheAccessType, this.cacheAccess);
                    getBlockingEventDispatcher().dispatch(new CoherentCacheBeginCacheAccessEvent(CoherentCache.this, access, this.cacheAccess));
                }
            }
        }

        public CacheAccess<StateT, LockableCacheLine> getCacheAccess() {
            return cacheAccess;
        }

        protected abstract void evict(MemoryHierarchyAccess access, Action onCompletedCallback);

        @Override
        public String toString() {
            return String.format("%s %s", status, status == FindAndLockStatus.IDLE ? "" : cacheAccess);
        }
    }

    protected abstract class AbstractLockingProcess extends CoherentCacheProcess {
        protected AbstractFindAndLockProcess findAndLockProcess;
        protected MemoryHierarchyAccess access;
        protected int tag;

        protected AbstractLockingProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
            this.access = access;
            this.tag = tag;
            this.findAndLockProcess = this.newFindAndLockProcess(cacheAccessType);
        }

        @Override
        protected void complete() {
            if (this.findAndLockProcess.status == FindAndLockStatus.ACQUIRED) {
                this.findAndLockProcess.cacheAccess.commit().getLine().unlock();
                this.findAndLockProcess.status = FindAndLockStatus.RELEASED;
            } else if (this.findAndLockProcess.status == FindAndLockStatus.FAILED) {
                this.findAndLockProcess.cacheAccess.abort();
            }

            if (this.findAndLockProcess.getCacheAccess().getReference().getAccessType().isUpward()) {
                assert this.findAndLockProcess.cacheAccess.isHitInCache();
            }

            if (this.findAndLockProcess.getCacheAccess().getReference().getAccessType().isWriteback()) { //TODO: verifying the case of writeToNextLevel
//                assert !this.findAndLockProcess.cacheAccess.isBypass() || this.findAndLockProcess.cacheAccess.getReference();
            }

            super.complete();
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            if (this.findAndLockProcess.status == FindAndLockStatus.IDLE || this.findAndLockProcess.status == FindAndLockStatus.EVICTING) {
                this.findAndLockProcess.processPendingActions();
            } else if (this.findAndLockProcess.status == FindAndLockStatus.FAILED) {
                throw new CacheLineLockFailedException();
            }

            return this.findAndLockProcess.status.isAcquiredOrBypassed() && super.processPendingActions();
        }

        public abstract AbstractFindAndLockProcess newFindAndLockProcess(CacheAccessType cacheAccessType);

        @Override
        public String toString() {
            return String.format("%s: %s {id=%d} @ 0x%08x %s", access, findAndLockProcess.getCacheAccess() != null ? findAndLockProcess.getCacheAccess().getReference().getAccessType() : "<NOT_INITIALIZED>", id, tag, findAndLockProcess);
        }
    }
}
