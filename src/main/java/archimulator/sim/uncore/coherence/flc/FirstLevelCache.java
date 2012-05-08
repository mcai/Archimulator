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
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.config.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardReadFlow;
import archimulator.sim.uncore.coherence.flow.flc.L2UpwardWriteFlow;
import archimulator.sim.uncore.coherence.flow.flc.LoadFlow;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemoryDeviceMessage;
import archimulator.sim.uncore.coherence.message.UpwardReadMessage;
import archimulator.sim.uncore.coherence.message.UpwardWriteMessage;
import archimulator.sim.uncore.net.Net;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class FirstLevelCache extends CoherentCache {
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
                L2UpwardReadFlow l2UpwardReadFlow = new L2UpwardReadFlow(this, (LastLevelCache) source, (UpwardReadMessage) message);
                l2UpwardReadFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                            }
                        }
                );
                break;
            case UPWARD_WRITE:
                L2UpwardWriteFlow l2UpwardWriteFlow = new L2UpwardWriteFlow(this, (LastLevelCache) source, (UpwardWriteMessage) message);
                l2UpwardWriteFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                            }
                        }
                );
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        LoadFlow loadFlow = new LoadFlow(this, access, access.getPhysicalTag());
        loadFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        onCompletedCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCycleAccurateEventQueue().schedule(new Action() {
                            public void apply() {
                                receiveIfetch(access, onCompletedCallback);
                            }
                        }, getRetryLatency());
                    }
                }
        );
    }

    public void receiveLoad(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        LoadFlow loadFlow = new LoadFlow(this, access, access.getPhysicalTag());
        loadFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        onCompletedCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCycleAccurateEventQueue().schedule(new Action() {
                            public void apply() {
                                receiveLoad(access, onCompletedCallback);
                            }
                        }, getRetryLatency());
                    }
                }
        );
    }

    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        LoadFlow loadFlow = new LoadFlow(this, access, access.getPhysicalTag());
        loadFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        onCompletedCallback.apply();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCycleAccurateEventQueue().schedule(new Action() {
                            public void apply() {
                                receiveStore(access, onCompletedCallback);
                            }
                        }, getRetryLatency());
                    }
                }
        );
    }

    public void setNext(LastLevelCache next) {
        super.setNext(next);
    }

    public LastLevelCache getNext() {
        return (LastLevelCache) super.getNext();
    }

    private int getReadPorts() {
        return ((FirstLevelCacheConfig) getConfig()).getReadPorts();
    }

    private int getWritePorts() {
        return ((FirstLevelCacheConfig) getConfig()).getWritePorts();
    }
}
