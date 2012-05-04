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
package archimulator.sim.uncore.coherence.llc.process;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.CoherentCache;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.process.CoherentCacheProcess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.MemWriteMessage;
import archimulator.util.action.Action1;

public class MemWriteProcess extends CoherentCacheProcess {
    private boolean completed;

    public MemWriteProcess(CoherentCache cache, final MemoryHierarchyAccess access, final CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        super(cache);
        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                getCache().getBlockingEventDispatcher().dispatch(new LastLevelCacheLineEvictedByMemWriteProcessEvent(getCache(), cacheAccess.getLine()));
//                    cacheAccess.getLine().invalidate();

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
                            getCache().sendRequest(getCache().getNext(), new MemWriteMessage(access, cacheAccess.getLine().getTag(), new Action1<MemWriteMessage>() {
                                public void apply(MemWriteMessage memWriteMessage) {
                                    completed = true;
                                }
                            }), getCache().getCache().getLineSize() + 8);

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

    public LastLevelCache getCache() {
        return (LastLevelCache) super.getCache();
    }
}
