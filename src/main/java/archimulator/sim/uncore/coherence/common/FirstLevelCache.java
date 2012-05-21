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
import archimulator.sim.core.DynamicInstruction;
import archimulator.sim.uncore.*;
import archimulator.sim.uncore.cache.Cache;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.config.CoherentCacheConfig;
import archimulator.sim.uncore.coherence.config.FirstLevelCacheConfig;
import archimulator.sim.uncore.coherence.flow.flc.LoadFlow;
import archimulator.sim.uncore.coherence.flow.flc.StoreFlow;
import archimulator.sim.uncore.net.Net;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Function3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class FirstLevelCache extends CoherentCache<MESIState, FirstLevelCacheLine> {
    private List<MemoryHierarchyAccess> pendingAccesses;
    private EnumMap<MemoryHierarchyAccessType, Integer> pendingAccessesPerType;

    private long numUpwardReads;
    private long numUpwardWrites;

    public FirstLevelCache(CacheHierarchy cacheHierarchy, String name, CoherentCacheConfig config) {
        super(cacheHierarchy, name, config, new LockableCache<MESIState, FirstLevelCacheLine>(cacheHierarchy, name, config.getGeometry(), config.getEvictionPolicyClz(), new Function3<Cache<?, ?>, Integer, Integer, FirstLevelCacheLine>() {
            public FirstLevelCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new FirstLevelCacheLine(cache, set, way, MESIState.INVALID);
            }
        }));

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
                numUpwardReads = 0;
                numUpwardWrites = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(FirstLevelCache.this.getName() + ".numUpwardReads", String.valueOf(numUpwardReads));
                    event.getStats().put(FirstLevelCache.this.getName() + ".numUpwardWrites", String.valueOf(numUpwardWrites));
                }
            }
        });
    }

    @Override
    public void updateStats(CacheAccessType cacheAccessType, CacheAccess<?, ?> cacheAccess) {
        super.updateStats(cacheAccessType, cacheAccess);

        if (cacheAccessType.isRead()) {
            if (cacheAccessType.isUpward()) {
                numUpwardReads++;
            }
        } else {
            if (cacheAccessType.isUpward()) {
                numUpwardWrites++;
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

    public void receiveIfetch(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        LoadFlow loadFlow = new LoadFlow(this, access, access.getPhysicalTag());
        loadFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        onCompletedCallback.apply();
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
                }
        );
    }

    public void receiveStore(final MemoryHierarchyAccess access, final Action onCompletedCallback) {
        StoreFlow storeFlow = new StoreFlow(this, access, access.getPhysicalTag());
        storeFlow.run(
                new Action() {
                    @Override
                    public void apply() {
                        onCompletedCallback.apply();
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
        return ((FirstLevelCacheConfig) getConfig()).getNumReadPorts();
    }

    private int getWritePorts() {
        return ((FirstLevelCacheConfig) getConfig()).getNumWritePorts();
    }
}
