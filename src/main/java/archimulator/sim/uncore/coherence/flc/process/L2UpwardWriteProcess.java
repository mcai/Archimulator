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

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.event.FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardWriteMessage;

public class L2UpwardWriteProcess extends FirstLevelCacheLockingProcess {
    public L2UpwardWriteProcess(FirstLevelCache cache, final LastLevelCache source, final UpwardWriteMessage message) {
        super(cache, message.getAccess(), message.getTag(), CacheAccessType.UPWARD_WRITE);

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                findAndLockProcess.getCacheAccess().getLine().invalidate();

                findAndLockProcess.getCacheAccess().commit().getLine().unlock();

                getCache().getBlockingEventDispatcher().dispatch(new FirstLevelCacheLineEvictedByL2UpwardWriteProcessEvent(getCache(), findAndLockProcess.getCacheAccess().getLine()));

                int size = findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.MODIFIED ? getCache().getCache().getLineSize() + 8 : 8;
                getCache().sendReply(source, message, size);

                return true;
            }
        });
    }
}
