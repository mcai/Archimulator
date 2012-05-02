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
package archimulator.sim.uncore.coherence.flc;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.uncore.*;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.flc.process.L2UpwardReadProcess;
import archimulator.sim.uncore.coherence.flc.process.L2UpwardWriteProcess;
import archimulator.sim.uncore.coherence.flc.process.LoadProcess;
import archimulator.sim.uncore.coherence.flc.process.StoreProcess;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.common.*;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.config.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.message.*;
import archimulator.sim.uncore.net.Net;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.NamedAction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class FirstLevelCache extends CoherentCache {
    private LastLevelCache next;

    private List<MemoryHierarchyAccess> pendingAccesses;
    private EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;

    private long upwardReads;
    private long upwardWrites;

    public FirstLevelCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config, MESIState.INVALID);

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
    public void updateStats(CacheAccessType cacheAccessType, CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
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
                this.scheduleProcess(new L2UpwardReadProcess(this, (LastLevelCache) source, (UpwardReadMessage) message));
                break;
            case UPWARD_WRITE:
                this.scheduleProcess(new L2UpwardWriteProcess(this, (LastLevelCache) source, (UpwardWriteMessage) message));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        this.scheduleProcess(new LoadProcess(this, access).addOnCompletedCallback(new Action1<LoadProcess>() {
            public void apply(LoadProcess loadProcess) {
                if (!loadProcess.isError()) {
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
        this.scheduleProcess(new LoadProcess(this, access).addOnCompletedCallback(new Action1<LoadProcess>() {
            public void apply(LoadProcess loadProcess) {
                if (!loadProcess.isError()) {
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
        this.scheduleProcess(new StoreProcess(this, access).addOnCompletedCallback(new Action1<StoreProcess>() {
            public void apply(StoreProcess storeProcess) {
                if (!storeProcess.isError()) {
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

    public LastLevelCache getNext() {
        return next;
    }

    private int getReadPorts() {
        return ((FirstLevelCacheConfig) config).getReadPorts();
    }

    private int getWritePorts() {
        return ((FirstLevelCacheConfig) config).getWritePorts();
    }
}
