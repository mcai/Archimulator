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
package archimulator.sim.uncore.coherence.flow.llc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.FirstLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCacheLineState;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.util.action.Action;

public class L1EvictFlow extends LockingFlow {
    private LastLevelCache cache;
    private FirstLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean hasData;

    public L1EvictFlow(Flow producerFlow, LastLevelCache cache, final FirstLevelCache source, MemoryHierarchyAccess access, int tag, boolean hasData) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
        this.hasData = hasData;
    }

    public void start(final Action onSuccessCallback, final Action onFailureCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        final LastLevelCacheFindAndLockFlow findAndLockFlow = new LastLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.EVICT);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (findAndLockFlow.getCacheAccess().isHitInCache() || !findAndLockFlow.getCacheAccess().isBypass()) {
                            findAndLockFlow.getCacheAccess().getLine().setNonInitialState(hasData ? LastLevelCacheLineState.DIRTY : LastLevelCacheLineState.CLEAN);
                        }

                        getCache().getShadowTagDirectories().get(source).removeTag(tag);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        getCache().sendReply(source, 8, onSuccessCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().sendReply(source, 8, onFailureCallback);
                        onDestroy();
                    }
                }
        );
    }

    public LastLevelCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L1EvictFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
