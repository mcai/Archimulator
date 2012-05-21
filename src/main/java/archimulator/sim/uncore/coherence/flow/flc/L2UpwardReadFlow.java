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
import archimulator.sim.uncore.cache.FindCacheLineResult;
import archimulator.sim.uncore.cache.FindCacheLineResultType;
import archimulator.sim.uncore.coherence.common.*;
import archimulator.sim.uncore.coherence.flow.Flow;
import net.pickapack.action.Action;

public class L2UpwardReadFlow extends Flow {
    private FirstLevelCache cache;
    private LastLevelCache source;
    private MemoryHierarchyAccess access;
    private int tag;
    private boolean copyBack;

    public L2UpwardReadFlow(Flow producerFlow, FirstLevelCache cache, final LastLevelCache source, MemoryHierarchyAccess access, int tag) {
        super(producerFlow);
        this.cache = cache;
        this.source = source;
        this.access = access;
        this.tag = tag;
    }

    public void start(final Action onSuccessCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        FindCacheLineResult<FirstLevelCacheLine> findCacheLineResult = this.getCache().getCache().findLine(this.tag);
        if(findCacheLineResult.getType() == FindCacheLineResultType.CACHE_HIT) {
            final FirstLevelCacheFindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.UPWARD_READ);

            findAndLockFlow.start(
                    new Action() {
                        @Override
                        public void apply() {
                            copyBack = findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.MODIFIED;

                            findAndLockFlow.getCacheAccess().getLine().getMesiFsm().fireTransition(MESICondition.EXTERNAL_READ);

                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                            getCache().sendReply(source, source.getCache().getLineSize() + 8, onSuccessCallback);
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
        else {
            getCache().sendReply(source, 8, onSuccessCallback);
            onDestroy();
        }
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    public boolean isCopyBack() {
        return copyBack;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: L2UpwardReadFlow#%d", getBeginCycle(), getCache().getName(), getId());
    }
}
