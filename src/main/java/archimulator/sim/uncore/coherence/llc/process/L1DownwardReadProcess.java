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

import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.coherence.action.ActionBasedPendingActionOwner;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.exception.CoherentCacheException;
import archimulator.sim.uncore.coherence.flc.FirstLevelCache;
import archimulator.sim.uncore.coherence.llc.LastLevelCache;
import archimulator.sim.uncore.coherence.message.DownwardReadMessage;
import archimulator.util.action.Action1;

public class L1DownwardReadProcess extends LastLevelCacheLockingProcess {
    private FirstLevelCache source;
    private DownwardReadMessage message;
    private boolean copyBack;

    public L1DownwardReadProcess(final LastLevelCache cache, final FirstLevelCache source, final DownwardReadMessage message) {
        super(cache, message.getAccess(), message.getTag(), CacheAccessType.DOWNWARD_READ);

        this.source = source;
        this.message = message;

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                getCache().getShadowTagDirectories().get(source).addTag(message.getTag());
                message.setShared(getCache().isShared(message.getTag()));

                getCache().sendReply(source, message, source.getCache().getLineSize() + 8);

                if (!findAndLockProcess.getCacheAccess().isHitInCache() && !findAndLockProcess.getCacheAccess().isBypass()) {
                    if (copyBack) {
                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                    } else {
                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
                    }
                }

                findAndLockProcess.getCacheAccess().commit().getLine().unlock();

                return true;
            }
        });

        this.getPendingActions().push(new ActionBasedPendingActionOwner() {
            @Override
            public boolean apply() {
                if (!findAndLockProcess.getCacheAccess().isHitInCache()) {
                    if (getCache().isOwnedOrShared(message.getTag())) {
                        getPendingActions().push(new UpwardReadProcess(getCache(), message.getAccess(), message.getTag(), getCache().getOwnerOrFirstSharer(message.getTag())).addOnCompletedCallback(new Action1<UpwardReadProcess>() {
                            public void apply(UpwardReadProcess upwardReadProcess) {
                                if (upwardReadProcess.isCopyBack()) {
                                    copyBack = true;
//                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.MODIFIED);
                                }
                            }
                        }));
                    } else {
                        getPendingActions().push(new MemReadProcess(getCache(), message.getAccess(), message.getTag()).addOnCompletedCallback(new Action1<MemReadProcess>() {
                            public void apply(MemReadProcess memReadProcess) {
//                                    if (!findAndLockProcess.getCacheAccess().isHitInCache() && !findAndLockProcess.getCacheAccess().isBypass()) {
//                                        findAndLockProcess.getCacheAccess().getLine().setNonInitialState(MESIState.EXCLUSIVE);
//                                    }
                            }
                        }));
                    }
                }

                return true;
            }
        });
    }

    @Override
    public boolean processPendingActions() throws CoherentCacheException {
        try {
            return super.processPendingActions();
        } catch (CoherentCacheException e) {
            this.complete();

            this.message.setError(true);
            getCache().sendReply(this.source, this.message, 8);

            throw e;
        }
    }
}