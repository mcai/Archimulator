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
package archimulator.sim.uncore.coherence.flow.flc;

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.flow.FindAndLockFlow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.llc.L1EvictFlow;
import archimulator.util.action.Action;

public class FirstLevelCacheFindAndLockFlow extends FindAndLockFlow {
    public FirstLevelCacheFindAndLockFlow(LockingFlow producerFlow, FirstLevelCache cache, MemoryHierarchyAccess access, int tag, CacheAccessType cacheAccessType) {
        super(producerFlow, cache, access, tag, cacheAccessType);
    }

    @Override
    protected void evict(final MemoryHierarchyAccess access, final Action onSuccessCallback, final Action onFailureCallback) {
        final boolean hasData = getCacheAccess().getLine().getState() == MESIState.MODIFIED;

        final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;

        getCache().sendRequest(getCache().getNext(), size, new Action() {
            @Override
            public void apply() {
                L1EvictFlow l1EvictFlow = new L1EvictFlow(FirstLevelCacheFindAndLockFlow.this, getCache().getNext(), getCache(), access, getCacheAccess().getLine().getTag(), hasData);
                l1EvictFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                                onSuccessCallback.apply();
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                onFailureCallback.apply();
                            }
                        }
                );
            }
        });
    }

    @Override
    public FirstLevelCache getCache() {
        return (FirstLevelCache) super.getCache();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s%s: FirstLevelCacheFindAndLockFlow#%d {state=%s} ", getBeginCycle(), getCache().getName(), getCacheAccess().getLine(), getId(), getState());
    }
}
