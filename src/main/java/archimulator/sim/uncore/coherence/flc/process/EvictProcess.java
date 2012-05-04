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
package archimulator.sim.uncore.coherence.flc.process;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.LockableCacheLine;
import archimulator.sim.uncore.coherence.common.process.CoherentCacheProcess;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.exception.CoherentCacheMessageProcessException;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.message.EvictMessage;
import archimulator.util.action.Action1;

public class EvictProcess extends CoherentCacheProcess {
    private boolean completed;
    private boolean error;

    public EvictProcess(FirstLevelCache cache, final MemoryHierarchyAccess access, final CacheAccess<MESIState, LockableCacheLine> cacheAccess) {
        super(cache);
        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
//                    cacheAccess.getLine().invalidate();

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
                    final int size = hasData ? getCache().getCache().getLineSize() + 8 : 8;
                    getCache().sendRequest(getCache().getNext(), new EvictMessage(access, cacheAccess.getLine().getTag(), hasData, new Action1<EvictMessage>() {
                        public void apply(EvictMessage evictMessage) {
                            if (evictMessage.isError()) {
                                error = true;
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
        if (this.error) {
            throw new CoherentCacheMessageProcessException();
        }

        return super.processPendingActions() && completed;
    }

    @Override
    public FirstLevelCache getCache() {
        return (FirstLevelCache) super.getCache();
    }
}
