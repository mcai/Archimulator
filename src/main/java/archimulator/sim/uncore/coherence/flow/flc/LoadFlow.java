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
import archimulator.sim.uncore.coherence.flow.Flow;
import archimulator.sim.uncore.coherence.flow.llc.L1DownwardReadFlow;
import net.pickapack.action.Action;

public class LoadFlow extends Flow {
    private FirstLevelCache cache;
    private MemoryHierarchyAccess access;
    private int tag;

    public LoadFlow(FirstLevelCache cache, MemoryHierarchyAccess access, int tag) {
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
        final FirstLevelCacheFindAndLockFlow findAndLockFlow = new FirstLevelCacheFindAndLockFlow(this, this.cache, this.access, this.tag, CacheAccessType.LOAD);

        findAndLockFlow.start(
                new Action() {
                    @Override
                    public void apply() {
                        if (!findAndLockFlow.getCacheAccess().isHitInCache()) {
                            downwardRead(findAndLockFlow, onSuccessCallback);
                        } else {
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

    private void downwardRead(final FirstLevelCacheFindAndLockFlow findAndLockFlow, final Action onSuccessCallback) {
        getCache().sendRequest(getCache().getNext(), 8, new Action() {
            @Override
            public void apply() {
                final L1DownwardReadFlow l1DownwardReadFlow = new L1DownwardReadFlow(LoadFlow.this, getCache().getNext(), getCache(), access, tag);
                l1DownwardReadFlow.start(
                        new Action() {
                            @Override
                            public void apply() {
                                findAndLockFlow.getCacheAccess().getLine().getMesiFsm().fireTransition(l1DownwardReadFlow.isShared() ? MESICondition.READ_WITH_SHARERS : MESICondition.READ_NO_SHARERS);

                                findAndLockFlow.getCacheAccess().commit().getLine().unlock();

                                onSuccessCallback.apply();
                                onDestroy();
                            }
                        }, new Action() {
                            @Override
                            public void apply() {
                                numDownwardReadRetries++;

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

    private int numDownwardReadRetries = 0;

    public int getNumDownwardReadRetries() {
        return numDownwardReadRetries;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s {tag=0x%08x}: LoadFlow#%d", getBeginCycle(), getCache().getName(), access.getPhysicalTag(), getId());
    }
}
