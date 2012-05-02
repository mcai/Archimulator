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

import archimulator.sim.uncore.MemoryDevice;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.process.CoherentCacheProcess;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.exception.CoherentCacheMessageProcessException;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardReadMessage;
import archimulator.util.action.Action1;

public class UpwardReadProcess extends CoherentCacheProcess {
    private boolean completed;
    private boolean error;
    private boolean copyBack;

    public UpwardReadProcess(LastLevelCache cache, final MemoryHierarchyAccess access, final int tag, final MemoryDevice target) {
        super(cache);
        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                getCache().sendRequest(target, new UpwardReadMessage(access, tag, new Action1<UpwardReadMessage>() {
                    public void apply(UpwardReadMessage upwardReadMessage) {
                        if (upwardReadMessage.isError()) {
                            error = true;
                        } else {
                            copyBack = upwardReadMessage.isHasCopyback();
                            completed = true;
                        }
                    }
                }), 8);

                return true;
            }
        });
    }

    @Override
    public boolean processPendingActions() throws CoherentCacheException {
        if (this.error) {
            throw new CoherentCacheMessageProcessException();
        }

        return super.processPendingActions() && completed;
    }

    public boolean isCopyBack() {
        return copyBack;
    }

    public LastLevelCache getCache() {
        return (LastLevelCache) super.getCache();
    }
}
