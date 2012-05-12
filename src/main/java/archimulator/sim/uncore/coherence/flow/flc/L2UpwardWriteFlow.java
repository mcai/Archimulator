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
import archimulator.sim.uncore.coherence.common.FirstLevelCache;
import archimulator.sim.uncore.coherence.common.LastLevelCache;
import archimulator.sim.uncore.coherence.common.MESICondition;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent;
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.util.action.Action;

public class L2UpwardWriteFlow extends LockingFlow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;

    public L2UpwardWriteFlow(Flow producerFlow, FirstLevelCache cache, final LastLevelCache source, MemoryHierarchyAccess access, int tag) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback) {
        onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        final FirstLevelCacheFindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this, this.cache, access, tag, CacheAccessType.UPWARD_READ);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().getLine().getMesiFsm().fireTransition(MESICondition.EXTERNAL_WRITE);

                        findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                        getCache().getBlockingEventDispatcher().dispatch(new FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent(getCache(), findAndLockFlow.getCacheAccess().getLine()));

                        int size = findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? getCache().getCache().getLineSize() + 8 : 8;

                        getCache().sendReply(source, size, onSuccessCallback);
                        onDestroy();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        Flow.dumpTree();
                        throw new IllegalArgumentException();
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        Flow.dumpTree();
                        throw new IllegalArgumentException();
                    }
                }
        );

    }

    public FirstLevelCache getCache() {
        return cache;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L2UpwardWriteFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
