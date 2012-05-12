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
import archimulator.sim.uncore.coherence.common.MESICondition;
import archimulator.sim.uncore.coherence.common.MESIState;
import archimulator.sim.uncore.coherence.flow.LockingFlow;
import archimulator.sim.uncore.coherence.flow.llc.L1DownwardWriteFlow;
import archimulator.util.action.Action;

public class StoreFlow extends LockingFlow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public StoreFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
        super(null);
        this.cache = cache;
        this.access = access;
        this.tag = tag;
    }

    public void run(final Action onSuccessCallback) {
        this.onCreate(this.cache.getCycleAccurateEventQueue().getCurrentCycle());

        findAndLock(onSuccessCallback);
    }

    private void findAndLock(final Action onSuccessCallback) {
        final FirstLevelCacheFindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.STORE);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.SHARED || findAndLockFlow.getCacheAccess().getLine().getState() == MESIState.INVALID) {
                            downwardWrite(findAndLockFlow, onSuccessCallback);
                        } else {
                            findAndLockFlow.getCacheAccess().getLine().getMesiFsm().fireTransition(MESICondition.WRITE);
                            findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                            onSuccessCallback.apply();
                            onDestroy();
                        }
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                            public void apply() {
                                findAndLock(onSuccessCallback);
                            }
                        }, getCache().getRetryLatency());
                    }
                }, new Action() {
                    @Override
                    public void apply() {
                        findAndLockFlow.getCacheAccess().abort();
                        findAndLockFlow.getCacheAccess().getLine().unlock();

                        getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                            public void apply() {
                                findAndLock(onSuccessCallback);
                            }
                        }, getCache().getRetryLatency());
                    }
                }
        );
    }

    private void downwardWrite(final FirstLevelCacheFindAndLockFlow findAndLockFlow, final Action onSuccessCallback) {
        getCache().sendRequest(getCache().getNext(), 8, new Action() {
            @Override
            public void apply() {
                L1DownwardWriteFlow l1DownwardWriteFlow = new L1DownwardWriteFlow(StoreFlow.this, getCache().getNext(), getCache(), access, tag);
                l1DownwardWriteFlow.run(
                        new Action() {
                            @Override
                            public void apply() {
                                findAndLockFlow.getCacheAccess().getLine().getMesiFsm().fireTransition(MESICondition.WRITE);
                                findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                onSuccessCallback.apply();
                                onDestroy();
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                numDownwardWriteRetries++;

                                findAndLockFlow.getCacheAccess().abort();
                                findAndLockFlow.getCacheAccess().getLine().unlock();

                                getCache().getCycleAccurateEventQueue().schedule(this, new Action() {
                                    public void apply() {
                                        findAndLock(onSuccessCallback);
                                    }
                                }, getCache().getRetryLatency());
                            }
                        }
                );
            }
        });
    }

    public FirstLevelCache getCache() {
        return cache;
    }

    private int numDownwardWriteRetries = 0;

    public int getNumDownwardWriteRetries() {
        return numDownwardWriteRetries;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s {tag=0x%08x}: StoreFlow#%d", getBeginCycle(), getCache().getName(), access.getPhysicalTag(), getId());
    }
}
