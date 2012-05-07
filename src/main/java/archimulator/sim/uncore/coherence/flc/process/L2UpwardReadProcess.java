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
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.UpwardReadMessage;

public class L2UpwardReadProcess extends FirstLevelCacheLockingProcess {
    public L2UpwardReadProcess(FirstLevelCache cache, final LastLevelCache source, final UpwardReadMessage message) {
        super(cache, message.getAccess(), message.getTag(), CacheAccessType.UPWARD_READ);

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                message.setHasCopyback(findAndLockProcess.getCacheAccess().getLine().getState() == MESIState.MODIFIED);
                getCache().sendReply(source, message, source.getCache().getLineSize() + 8);

                findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.SHARED);

                findAndLockProcess.getCacheAccess().commit().getLine().unlock();

                return true;
            }
        });
    }
}