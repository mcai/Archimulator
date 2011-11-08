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

import archimulator.core.DynamicInstruction;
import archimulator.mem.*;
import archimulator.mem.cache.CacheAccess;
import archimulator.mem.coherence.exception.CoherentCacheException;
import archimulator.mem.coherence.exception.CoherentCacheMessageProcessException;
import archimulator.mem.coherence.message.*;
import archimulator.mem.net.Net;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.NamedAction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class FirstLevelCache extends CoherentCache<MESIState> {
    private LastLevelCache next;

    private transient List<MemoryHierarchyAccess> pendingAccesses;
    private transient EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;

    private long upwardReads;
    private long upwardWrites;

    public FirstLevelCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config, MESIState.INVALID);

        this.init();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        this.init();
    }

    private void init() {
        this.pendingAccesses = new ArrayList<MemoryHierarchyAccess>();

        this.pendingAccessesPerType = new EnumMap<MemoryHierarchyAccessType, Integer>(MemoryHierarchyAccessType.class);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.IFETCH, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.LOAD, 0);
        this.pendingAccessesPerType.put(MemoryHierarchyAccessType.STORE, 0);

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                upwardReads = 0;
                upwardWrites = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(FirstLevelCache.this.getName() + ".upwardReads", String.valueOf(upwardReads));
                    event.getStats().put(FirstLevelCache.this.getName() + ".upwardWrites", String.valueOf(upwardWrites));
                }
            }
        });
    }

    @Override
    protected void updateStats(CacheAccessType cacheAccessType, CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        super.updateStats(cacheAccessType, cacheAccess);

        if (cacheAccessType.isRead()) {
            if (cacheAccessType.isUpward()) {
                upwardReads++;
            }
        } else {
            if (cacheAccessType.isUpward()) {
                upwardWrites++;
            }
        }
    }

    public boolean canAccess(MemoryHierarchyAccessType type, int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        return access == null ?
                this.pendingAccessesPerType.get(type) < (type == MemoryHierarchyAccessType.STORE ? this.getWritePorts() : this.getReadPorts()) :
                type != MemoryHierarchyAccessType.STORE && access.getType() != MemoryHierarchyAccessType.STORE;
    }

    public MemoryHierarchyAccess findAccess(int physicalTag) {
        for (MemoryHierarchyAccess access : this.pendingAccesses) {
            if (access.getPhysicalTag() == physicalTag) {
                return access;
            }
        }

        return null;
    }

    public MemoryHierarchyAccess beginAccess(DynamicInstruction dynamicInst, MemoryHierarchyThread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback) {
        MemoryHierarchyAccess newAccess = new MemoryHierarchyAccess(dynamicInst, thread, type, virtualPc, physicalAddress, physicalTag, onCompletedCallback, this.getCycleAccurateEventQueue().getCurrentCycle());

        MemoryHierarchyAccess access = this.findAccess(physicalTag);

        if (access != null) {
            access.getAliases().add(0, newAccess);
        } else {
            this.pendingAccesses.add(newAccess);
            this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) + 1);
        }

        return newAccess;
    }

    public void endAccess(int physicalTag) {
        MemoryHierarchyAccess access = this.findAccess(physicalTag);
        assert (access != null);

        access.complete(this.getCycleAccurateEventQueue().getCurrentCycle());

        for (MemoryHierarchyAccess alias : access.getAliases()) {
            alias.complete(this.getCycleAccurateEventQueue().getCurrentCycle());
        }

        MemoryHierarchyAccessType type = access.getType();
        this.pendingAccessesPerType.put(type, this.pendingAccessesPerType.get(type) - 1);

        this.pendingAccesses.remove(access);
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return this.getCacheHierarchy().getL1sToL2Network();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receiveRequest(MemoryDevice source, MemoryDeviceMessage message) {
        switch (message.getType()) {
            case UPWARD_READ:
                this.scheduleProcess(new L2UpwardReadProcess((LastLevelCache) source, (UpwardReadMessage) message));
                break;
            case UPWARD_WRITE:
                this.scheduleProcess(new L2UpwardWriteProcess((LastLevelCache) source, (UpwardWriteMessage) message));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.scheduleProcess(new LoadProcess(access).addOnCompletedCallback(new Action1<LoadProcess>() {
            public void apply(LoadProcess loadProcess) {
                if (!loadProcess.isHasError()) {
                    onCompletedCallback.apply();
                } else {
                    getCycleAccurateEventQueue().schedule(new NamedAction("FirstLevelCache.retry(receiveIfetch)") {
                        public void apply() {
                            receiveIfetch(access, onCompletedCallback);
                        }
                    }, getRetryLatency());
                }
            }
        }));
    }

    public void receiveLoad(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.scheduleProcess(new LoadProcess(access).addOnCompletedCallback(new Action1<LoadProcess>() {
            public void apply(LoadProcess loadProcess) {
                if (!loadProcess.isHasError()) {
                    onCompletedCallback.apply();
                } else {
                    getCycleAccurateEventQueue().schedule(new NamedAction("FirstLevelCache.retry(receiveLoad)") {
                        public void apply() {
                            receiveLoad(access, onCompletedCallback);
                        }
                    }, getRetryLatency());
                }
            }
        }));
    }

    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.scheduleProcess(new StoreProcess(access).addOnCompletedCallback(new Action1<StoreProcess>() {
            public void apply(StoreProcess storeProcess) {
                if (!storeProcess.isHasError()) {
                    onCompletedCallback.apply();
                } else {
                    getCycleAccurateEventQueue().schedule(new NamedAction("FirstLevelCache.retry(receiveStore)") {
                        public void apply() {
                            receiveStore(access, onCompletedCallback);
                        }
                    }, getRetryLatency());
                }
            }
        }));
    }

    public void setNext(LastLevelCache next) {
        this.next = next;
    }

    private int getReadPorts() {
        return ((FirstLevelCacheConfig) config).getReadPorts();
    }

    private int getWritePorts() {
        return ((FirstLevelCacheConfig) config).getWritePorts();
    }

    private class FindAndLockProcess extends AbstractFindAndLockProcess {
        private FindAndLockProcess(MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
            super(access, tag, cacheAccessType);
        }

        @Override
        protected void evict(MemoryHierarchyAccess access, final Action onCompletedCallback) {
            getPendingActions().push(new EvictProcess(access, cacheAccess).addOnCompletedCallback(new Action1<EvictProcess>() {
                public void apply(EvictProcess evictProcess) {
                    onCompletedCallback.apply();
                }
            }));
        }
    }

    private abstract class LockingProcess extends AbstractLockingProcess {
        public LockingProcess(final MemoryHierarchyAccess access, final int tag, final CacheAccessType cacheAccessType) {
            super(access, tag, cacheAccessType);
        }

        @Override
        public AbstractFindAndLockProcess newFindAndLockProcess(CacheAccessType cacheAccessType) {
            return new FindAndLockProcess(this.access, this.tag, cacheAccessType);
        }
    }

    private abstract class CpuSideProcess extends LockingProcess {
        protected boolean hasError;

        protected CpuSideProcess(MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
            super(access, tag, cacheAccessType);
        }

        public boolean isHasError() {
            return hasError;
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            try {
                return hasError || super.processPendingActions();
            } catch (CoherentCacheException e) {
                this.hasError = true;
                this.complete();
                return true;
            }
        }
    }

    private class LoadProcess extends CpuSideProcess {
        private LoadProcess(final MemoryHierarchyAccess access) {
            super(access, access.getPhysicalTag(), CacheAccessType.LOAD);

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (!findAndLockProcess.getCacheAccess().isHitInCache()) {
                        getPendingActions().push(new DownwardReadProcess(access, access.getPhysicalTag()).addOnCompletedCallback(new Action1<DownwardReadProcess>() {
                            public void apply(DownwardReadProcess downwardReadProcess) {
                                findAndLockProcess.getCacheAccess().getLine().setNonInitialState(downwardReadProcess.isShared() ? MESIState.SHARED : MESIState.EXCLUSIVE);
                            }
                        }));
                    }

                    return true;
                }
            });
        }
    }

    private class StoreProcess extends CpuSideProcess {
        private StoreProcess(final MemoryHierarchyAccess access) {
            super(access, access.getPhysicalTag(), CacheAccessType.STORE);

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.SHARED || findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.INVALID) {
                        getPendingActions().push(new DownwardWriteProcess(access, access.getPhysicalTag()));
                    }

                    return true;
                }
            });
        }
    }

    private class DownwardReadProcess extends CoherentCacheProcess {
        private boolean shared;
        private boolean completed;
        private boolean hasError;

        private DownwardReadProcess(final MemoryHierarchyAccess access, final int tag) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    sendRequest(next, new DownwardReadMessage(access, tag, new Action1<DownwardReadMessage>() {
                        public void apply(DownwardReadMessage downwardReadMessage) {
                            if (downwardReadMessage.isHasError()) {
                                hasError = true;
                            } else {
                                completed = true;
                                shared = downwardReadMessage.isShared();
                            }
                        }
                    }), 8);

                    return true;
                }
            });
        }

        private boolean isShared() {
            return shared;
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            if (this.hasError) {
                throw new CoherentCacheMessageProcessException();
            }

            return super.processPendingActions() && completed;
        }
    }

    private class DownwardWriteProcess extends CoherentCacheProcess {
        private boolean completed;
        private boolean hasError;

        private DownwardWriteProcess(final MemoryHierarchyAccess access, final int tag) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    sendRequest(next, new DownwardWriteMessage(access, tag, new Action1<DownwardWriteMessage>() {
                        public void apply(DownwardWriteMessage writeMessage) {
                            if (writeMessage.isHasError()) {
                                hasError = true;
                            } else {
                                completed = true;
                            }
                        }
                    }), 8);

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            if (this.hasError) {
                throw new CoherentCacheMessageProcessException();
            }

            return super.processPendingActions() && completed;
        }
    }

    private class EvictProcess extends CoherentCacheProcess {
        private boolean completed;
        private boolean hasError;

        private EvictProcess(final MemoryHierarchyAccess access, final CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    cacheAccess.getLine().invalidate();

                    return true;
                }
            });

            if (cacheAccess.getLine().getState() == MESIState.INVALID) {
                throw new IllegalArgumentException();
            } else {
                final boolean hasData = cacheAccess.getLine().getState() == MESIState.MODIFIED;

                this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                    @Override
                    public boolean apply() {
                        final int size = hasData ? getCache().getLineSize() + 8 : 8;
                        sendRequest(next, new EvictMessage(access, cacheAccess.getLine().getTag(), hasData, new Action1<EvictMessage>() {
                            public void apply(EvictMessage evictMessage) {
                                if (evictMessage.isHasError()) {
                                    hasError = true;
                                } else {
                                    completed = true;
                                }
                            }
                        }), size);

                        return true;
                    }
                });
            }
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            if (this.hasError) {
                throw new CoherentCacheMessageProcessException();
            }

            return super.processPendingActions() && completed;
        }
    }

    public class L2UpwardReadProcess extends LockingProcess {
        public L2UpwardReadProcess(final LastLevelCache source, final UpwardReadMessage message) {
            super(message.getAccess(), message.getTag(), CacheAccessType.UPWARD_READ);

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    message.setHasCopyback(findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.MODIFIED);
                    sendReply(source, message, source.getCache().getLineSize() + 8);

                    findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.SHARED);

                    return true;
                }
            });
        }
    }

    public class L2UpwardWriteProcess extends LockingProcess {
        public L2UpwardWriteProcess(final LastLevelCache source, final UpwardWriteMessage message) {
            super(message.getAccess(), message.getTag(), CacheAccessType.UPWARD_WRITE);

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    findAndLockProcess.getCacheAccess().getLine().invalidate();

                    final int size = findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? getCache().getLineSize() + 8 : 8;
                    sendReply(source, message, size);

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            try {
                return super.processPendingActions();
            } catch (CoherentCacheException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
