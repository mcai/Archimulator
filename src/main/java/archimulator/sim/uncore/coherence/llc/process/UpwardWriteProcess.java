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
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.process.CoherentCacheProcess;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.exception.CoherentCacheMessageProcessException;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardWriteMessage;
import archimulator.util.action.Action1;

public class UpwardWriteProcess extends CoherentCacheProcess {
    private int pending;
    private boolean error;

    public UpwardWriteProcess(LastLevelCache cache, final MemoryHierarchyAccess access, final int tag, final FirstLevelCache except) {
        super(cache);
        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                for (final FirstLevelCache sharer : getCache().getSharers(tag)) {
                    if (sharer != except) {
                        getPendingActions().push(new ActionBasedPendingActionOwner() {
                            @Override
                            public boolean apply() {
                                getCache().sendRequest(sharer, new UpwardWriteMessage(access, tag, new Action1<UpwardWriteMessage>() {
                                    public void apply(UpwardWriteMessage upwardWriteMessage) {
                                        if (upwardWriteMessage.isError()) {
                                            error = true;
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
        if (this.error) {
            throw new CoherentCacheMessageProcessException();
        }

        return super.processPendingActions() && pending == 0;
    }

    public LastLevelCache getCache() {
        return (LastLevelCache) super.getCache();
    }
}
