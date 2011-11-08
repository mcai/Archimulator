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
import archimulator.mem.coherence.exception.CoherentCacheException;
import archimulator.mem.coherence.exception.CoherentCacheMessageProcessException;
import archimulator.mem.coherence.message.*;
import archimulator.mem.dram.MainMemory;
import archimulator.mem.net.Net;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

import java.io.Serializable;
import java.util.*;

public class LastLevelCache extends CoherentCache<MESIState> {
    private Map<FirstLevelCache, ShadowTagDirectory> shadowTagDirectories;
    private transient MainMemory next;

    public LastLevelCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config, MESIState.INVALID);

        this.shadowTagDirectories = new LinkedHashMap<FirstLevelCache, ShadowTagDirectory>();
    }

    @Override
    protected Net getNet(MemoryDevice to) {
        return to instanceof MainMemory ? this.getCacheHierarchy().getL2ToMemNetwork() : this.getCacheHierarchy().getL1sToL2Network();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void receiveRequest(MemoryDevice source, MemoryDeviceMessage message) {
        switch (message.getType()) {
            case EVICT:
                this.scheduleProcess(new L1EvictProcess((FirstLevelCache) source, (EvictMessage) message));
                break;
            case DOWNWARD_READ:
                this.scheduleProcess(new L1DownwardReadProcess((FirstLevelCache) source, (DownwardReadMessage) message));
                break;
            case DOWNWARD_WRITE:
                this.scheduleProcess(new L1DownwardWriteProcess((FirstLevelCache) source, (DownwardWriteMessage) message));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void addShadowTagDirectoryForL1(FirstLevelCache l1Cache) {
        this.shadowTagDirectories.put(l1Cache, new ShadowTagDirectory(l1Cache));
    }

    private boolean isShared(int addr) {
        return this.getSharers(addr).size() > 1;
    }

    private boolean isOwned(int addr) {
        return this.getSharers(addr).size() == 1;
    }

    private boolean isOwnedOrShared(int addr) {
        return this.getSharers(addr).size() > 0;
    }

    private FirstLevelCache getOwnerOrFirstSharer(int addr) {
        return this.getSharers(addr).get(0);
    }

    private List<FirstLevelCache> getSharers(int addr) {
        List<FirstLevelCache> sharers = new ArrayList<FirstLevelCache>();
        for (Map.Entry<FirstLevelCache, ShadowTagDirectory> entry : this.shadowTagDirectories.entrySet()) {
            if (entry.getValue().containsTag(addr)) {
                sharers.add(entry.getKey());
            }
        }
        return sharers;
    }

    public void setNext(MainMemory next) {
        this.next = next;
    }

    private class ShadowTagDirectory implements Serializable {
        private CacheGeometry geometry;
        private List<Set<Integer>> sets;

        private ShadowTagDirectory(FirstLevelCache l1Cache) {
            this.geometry = l1Cache.getCache().getGeometry();

            this.sets = new ArrayList<Set<Integer>>();
            for (int i = 0; i < this.geometry.getNumSets(); i++) {
                this.sets.add(new TreeSet<Integer>());
            }
        }

        private boolean containsTag(int addr) {
            return this.sets.get(this.getSet(addr)).contains(this.getTag(addr));
        }

        private void addTag(int addr) {
            this.sets.get(this.getSet(addr)).add(this.getTag(addr));
        }

        private void removeTag(int addr) {
            this.sets.get(this.getSet(addr)).remove(this.getTag(addr));
        }

        private int getTag(int addr) {
            return CacheGeometry.getTag(addr, this.geometry);
        }

        private int getSet(int addr) {
            return CacheGeometry.getSet(addr, this.geometry);
        }
    }

    private class FindAndLockProcess extends AbstractFindAndLockProcess {
        private FindAndLockProcess(MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
            super(access, tag, cacheAccessType);
        }

        @Override
        protected void evict(MemoryHierarchyAccess access, final Action onCompletedCallback) {
            getPendingActions().push(new MemWriteProcess(access, cacheAccess).addOnCompletedCallback(new Action1<MemWriteProcess>() {
                public void apply(MemWriteProcess memWriteProcess) {
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

    private class MemWriteProcess extends CoherentCacheProcess {
        private boolean completed;

        private MemWriteProcess(final MemoryHierarchyAccess access, final CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    cacheAccess.getLine().invalidate();

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (cacheAccess.getLine().getState() == MESIState.MODIFIED) {
                        getPendingActions().push(new ActionBasedPendingActionOwner() {
                            @Override
                            public boolean apply() {
                                sendRequest(next, new MemWriteMessage(access, cacheAccess.getLine().getTag(), new Action1<MemWriteMessage>() {
                                    public void apply(MemWriteMessage memWriteMessage) {
                                        completed = true;
                                    }
                                }), getCache().getLineSize() + 8);

                                return true;
                            }
                        });
                    } else {
                        completed = true;
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            return super.processPendingActions() && completed;
        }
    }

    private class L1EvictProcess extends LockingProcess {
        private FirstLevelCache source;
        private EvictMessage message;

        private L1EvictProcess(final FirstLevelCache source, final EvictMessage message) {
            super(message.getAccess(), message.getTag(), CacheAccessType.EVICT);

            this.source = source;
            this.message = message;

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    shadowTagDirectories.get(source).removeTag(message.getTag());

                    sendReply(source, message, 8);

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (message.isDirty()) {
                        if (findAndLockProcess.getCacheAccess().isHitInCache() || !findAndLockProcess.getCacheAccess().isBypass()) {
                            findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                        }
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            try {
                return super.processPendingActions();
            } catch (CoherentCacheException e) {
                this.complete();

                this.message.setHasError(true);
                sendReply(this.source, this.message, 8);

                throw e;
            }
        }
    }

    private class L1DownwardReadProcess extends LockingProcess {
        private FirstLevelCache source;
        private DownwardReadMessage message;

        private L1DownwardReadProcess(final FirstLevelCache source, final DownwardReadMessage message) {
            super(message.getAccess(), message.getTag(), CacheAccessType.DOWNWARD_READ);

            this.source = source;
            this.message = message;

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    shadowTagDirectories.get(source).addTag(message.getTag());
                    message.setShared(isShared(message.getTag()));

                    sendReply(source, message, source.getCache().getLineSize() + 8);

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (!findAndLockProcess.getCacheAccess().isHitInCache()) {
                        if (isOwnedOrShared(message.getTag())) {
                            getPendingActions().push(new UpwardReadProcess(message.getAccess(), message.getTag(), getOwnerOrFirstSharer(message.getTag())).addOnCompletedCallback(new Action1<UpwardReadProcess>() {
                                public void apply(UpwardReadProcess upwardReadProcess) {
                                    if (upwardReadProcess.hasCopyback) {
                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                                    }
                                }
                            }));
                        } else {
                            getPendingActions().push(new MemReadProcess(message.getAccess(), message.getTag()).addOnCompletedCallback(new Action1<MemReadProcess>() {
                                public void apply(MemReadProcess memReadProcess) {
                                    if (!findAndLockProcess.getCacheAccess().isHitInCache() && !findAndLockProcess.getCacheAccess().isBypass()) {
                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
                                    }
                                }
                            }));
                        }
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            try {
                return super.processPendingActions();
            } catch (CoherentCacheException e) {
                this.complete();

                this.message.setHasError(true);
                sendReply(this.source, this.message, 8);

                throw e;
            }
        }
    }

    private class L1DownwardWriteProcess extends LockingProcess {
        private FirstLevelCache source;
        private DownwardWriteMessage message;

        private L1DownwardWriteProcess(final FirstLevelCache source, final DownwardWriteMessage message) {
            super(message.getAccess(), message.getTag(), CacheAccessType.DOWNWARD_WRITE);

            this.source = source;
            this.message = message;

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    for (final FirstLevelCache sharer : getSharers(tag)) {
                        shadowTagDirectories.get(sharer).removeTag(tag);
                    }

                    shadowTagDirectories.get(source).addTag(message.getTag());

                    if (findAndLockProcess.getCacheAccess().isHitInCache() || !findAndLockProcess.getCacheAccess().isBypass()) {
                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? MESIState.MODIFIED : MESIState.EXCLUSIVE);
                    }

                    sendReply(source, message, source.getCache().getLineSize() + 8);

                    return true;
                }
            });

            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    if (!findAndLockProcess.getCacheAccess().isHitInCache() && !isOwnedOrShared(tag)) {
                        getPendingActions().push(new MemReadProcess(message.getAccess(), message.getTag()));
                    }

                    return true;
                }
            });

            this.getPendingActions().push(new UpwardWriteProcess(message.getAccess(), message.getTag(), source));
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            try {
                return super.processPendingActions();
            } catch (CoherentCacheException e) {
                this.complete();

                this.message.setHasError(true);
                sendReply(this.source, this.message, 8);

                throw e;
            }
        }
    }

    private class UpwardReadProcess extends CoherentCacheProcess {
        private boolean completed;
        private boolean hasError;
        private boolean hasCopyback;

        private UpwardReadProcess(final MemoryHierarchyAccess access, final int tag, final MemoryDevice target) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    sendRequest(target, new UpwardReadMessage(access, tag, new Action1<UpwardReadMessage>() {
                        public void apply(UpwardReadMessage upwardReadMessage) {
                            if (upwardReadMessage.isHasError()) {
                                hasError = true;
                            } else {
                                hasCopyback = upwardReadMessage.isHasCopyback();
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

    private class UpwardWriteProcess extends CoherentCacheProcess {
        private int pending;
        private boolean hasError;

        private UpwardWriteProcess(final MemoryHierarchyAccess access, final int tag, final FirstLevelCache except) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    for (final FirstLevelCache sharer : getSharers(tag)) {
                        if (sharer != except) {
                            getPendingActions().push(new ActionBasedPendingActionOwner() {
                                @Override
                                public boolean apply() {
                                    sendRequest(sharer, new UpwardWriteMessage(access, tag, new Action1<UpwardWriteMessage>() {
                                        public void apply(UpwardWriteMessage upwardWriteMessage) {
                                            if (upwardWriteMessage.isHasError()) {
                                                hasError = true;
                                            } else {
                                                pending--;
                                            }
                                        }
                                    }), 8);
                                    pending++;

                                    return true;
                                }
                            });
                        }
                    }

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            if (this.hasError) {
                throw new CoherentCacheMessageProcessException();
            }

            return super.processPendingActions() && pending == 0;
        }
    }

    private class MemReadProcess extends CoherentCacheProcess {
        private boolean completed;

        private MemReadProcess(final MemoryHierarchyAccess access, final int tag) {
            this.getPendingActions().push(new ActionBasedPendingActionOwner() {
                @Override
                public boolean apply() {
                    sendRequest(next, new MemReadMessage(access, tag, new Action1<MemReadMessage>() {
                        public void apply(MemReadMessage memReadMessage) {
                            completed = true;
                        }
                    }), 8);

                    return true;
                }
            });
        }

        @Override
        public boolean processPendingActions() throws CoherentCacheException {
            return super.processPendingActions() && completed;
        }
    }
}
