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
package archimulator.ext.mem.newHt;

import archimulator.core.BasicThread;
import archimulator.core.Processor;
import archimulator.mem.cache.Cache;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.coherence.CoherentCache;
import archimulator.mem.coherence.MESIState;
import archimulator.mem.coherence.event.CoherentCacheEndCacheAccessEvent;
import archimulator.mem.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.mem.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.sim.capability.ProcessorCapability;
import archimulator.sim.capability.ProcessorCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.action.Function2;
import archimulator.util.fsm.FiniteStateMachine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FsmBasedHtRequestLlcVictimTrackingCapability implements ProcessorCapability {
    private CoherentCache<MESIState>.LockableCache ownerCache;
    private MirrorCache mirrorCache;

    private long totalHtRequests;

    private long goodHtRequests;
    private long badHtRequests;
    private long uglyHtRequests;
    private long unusedHtRequests;

    public FsmBasedHtRequestLlcVictimTrackingCapability(Processor processor) {
        CoherentCache<MESIState>.LockableCache ownerCache = processor.getCacheHierarchy().getL2Cache().getCache();

        this.ownerCache = ownerCache;

        this.mirrorCache = new MirrorCache(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability.mirrorCache");

        processor.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.ownerCache) {
                    serviceRequest(event);
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(CoherentCacheEndCacheAccessEvent.class, new Action1<CoherentCacheEndCacheAccessEvent>() {
            public void apply(CoherentCacheEndCacheAccessEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.ownerCache) {
                    if (!event.isAborted()) {
//                        CacheLine<?> line = (CacheLine<?>) event.getCacheAccess().getLine();
//                        if(line.getTag() != mirrorCache.getLine(line).getTag()) {
//                            throw new IllegalArgumentException();
//                        }
                    }
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(LastLevelCacheLineEvictedByMemWriteProcessEvent.class, new Action1<LastLevelCacheLineEvictedByMemWriteProcessEvent>() {
            public void apply(LastLevelCacheLineEvictedByMemWriteProcessEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.ownerCache) {
                    CacheLine<?> line = (CacheLine<?>) event.getLineToInvalidate();
                    if (mirrorCache.getLine(line).fsm.getState() != CacheLineHtRequestState.INVALID) {
                        throw new IllegalArgumentException();
                    }

                    invalidate(event);
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                totalHtRequests = 0;

                goodHtRequests = 0;
                badHtRequests = 0;
                uglyHtRequests = 0;
                unusedHtRequests = 0;
            }
        });

        processor.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        processor.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    mirrorCache.flush();
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.ownerCache.getName() + ".totalHtRequests", String.valueOf(this.totalHtRequests));

        stats.put(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.ownerCache.getName() + ".goodHtRequests.confirmed", String.valueOf(this.goodHtRequests));
        stats.put(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.ownerCache.getName() + ".badHtRequests.confirmed", String.valueOf(this.badHtRequests));
        stats.put(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.ownerCache.getName() + ".uglyHtRequests.confirmed", String.valueOf(this.uglyHtRequests));
        stats.put(ownerCache.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.ownerCache.getName() + ".unusedHtRequests.confirmed", String.valueOf(this.unusedHtRequests));
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        MirrorCacheLine mirrorLineFound = this.mirrorCache.getLine(event.getLineFound());
        FiniteStateMachine<CacheLineHtRequestState> fsm = mirrorLineFound.fsm;

        if (event.isHitInCache()) {
            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_HIT : CacheLineHtRequestCondition.MT_HIT);
        } else {
            if (event.isEviction()) {
                fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.EVICTED_BY_HT : CacheLineHtRequestCondition.EVICTED_BY_MT, event.getLineFound().getTag());
            }

            if (fsm.getState() != CacheLineHtRequestState.INVALID) {
                throw new IllegalArgumentException();
            }

            MirrorCacheLine lineForVictim = this.mirrorCache.findVictimLineForTag(this.ownerCache.getTag(event.getAddress()));
            if (lineForVictim != null) {
                this.mirrorCache.getLine(lineForVictim).fsm.fireTransition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT);
            }

            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_MISS : CacheLineHtRequestCondition.MT_MISS);
        }
    }

    private void invalidate(LastLevelCacheLineEvictedByMemWriteProcessEvent event) {
        MirrorCacheLine mirrorLineFound = this.mirrorCache.getLine(event.getLineToInvalidate());
        mirrorLineFound.invalidate();
        FiniteStateMachine<CacheLineHtRequestState> fsm = mirrorLineFound.fsm;

        if (fsm.getState() != CacheLineHtRequestState.INVALID) {
            fsm.fireTransition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE);
        }
    }

    private enum CacheLineHtRequestState {
        INVALID,
        MT,
        UNUSED_HT,
        GOOD_HT,
        BAD_HT,
        UGLY_HT;

        public boolean isHt() {
            return this == UNUSED_HT;
        }
    }

    private enum CacheLineHtRequestCondition {
        HT_MISS,
        MT_MISS,
        HT_HIT,
        MT_HIT,
        EVICTED_BY_HT,
        EVICTED_BY_MT,
        INVALIDATED_BY_MEM_WRITE,
        FLUSH,
        VICTIM_HIT_BY_MT;

        public static final List<CacheLineHtRequestCondition> EVICTED = Arrays.asList(EVICTED_BY_HT, EVICTED_BY_MT, FLUSH);
    }

    private class MirrorCacheLine extends CacheLine<Boolean> {
        private FiniteStateMachine<CacheLineHtRequestState> fsm;

        private MirrorCacheLine(int set, int way) {
            super(set, way, true);

            this.fsm = new FiniteStateMachine<CacheLineHtRequestState>(ownerCache.getName() + ".FsmBasedHtRequestLlcVictimTrackingCapability.mirrorCacheLine" + " [" + set + ", " + way + "].fsm", CacheLineHtRequestState.INVALID);

            this.fsm.inState(CacheLineHtRequestState.INVALID)
                    .onCondition(CacheLineHtRequestCondition.HT_MISS, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            totalHtRequests++;
                            return CacheLineHtRequestState.UNUSED_HT;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.MT_MISS, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.MT;
                        }
                    })
                    .ignoreCondition(CacheLineHtRequestCondition.FLUSH);

            this.fsm.inState(CacheLineHtRequestState.MT)
                    .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                    .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                    .onCondition(CacheLineHtRequestCondition.EVICTED_BY_MT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.EVICTED_BY_HT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            int victimTag = (Integer) params[0];
                            setVictimTag(victimTag);

                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .ignoreCondition(CacheLineHtRequestCondition.FLUSH)
                    .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.INVALID;
                        }
                    });

            this.fsm.inState(CacheLineHtRequestState.UNUSED_HT)
                    .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                    .onCondition(CacheLineHtRequestCondition.MT_HIT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.GOOD_HT;
                        }
                    })
                    .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            unusedHtRequests++;
                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState param1, Object... params) {
                            setVictimTag(-1);
                            return CacheLineHtRequestState.BAD_HT;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            unusedHtRequests++;
                            return CacheLineHtRequestState.INVALID;
                        }
                    });

            this.fsm.inState(CacheLineHtRequestState.GOOD_HT)
                    .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                    .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                    .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            goodHtRequests++;
                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState param1, Object... params) {
                            setVictimTag(-1);
                            return CacheLineHtRequestState.UGLY_HT;
                        }
                    })
                    .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.INVALID;
                        }
                    });

            this.fsm.inState(CacheLineHtRequestState.BAD_HT)
                    .onCondition(CacheLineHtRequestCondition.HT_HIT, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState param1, Object... otherParams) {
                            return CacheLineHtRequestState.BAD_HT;
                        }
                    })
                    .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                    .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            badHtRequests++;
                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .ignoreCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT)
                    .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.INVALID;
                        }
                    });

            this.fsm.inState(CacheLineHtRequestState.UGLY_HT)
                    .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                    .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                    .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            uglyHtRequests++;
                            return CacheLineHtRequestState.INVALID;
                        }
                    })
                    .ignoreCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT)
                    .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<CacheLineHtRequestState, CacheLineHtRequestState>() {
                        public CacheLineHtRequestState apply(CacheLineHtRequestState state, Object... params) {
                            return CacheLineHtRequestState.INVALID;
                        }
                    });
        }

        public void setVictimTag(int victimTag) {
            this.fsm.put("victimTag", victimTag);
        }

        public Integer getVictimTag() {
            Integer result = this.fsm.get(VICTIM_TAG_KEY);
            return result != null ? result : -1;
        }

        @Override
        public int getTag() {
            throw new UnsupportedOperationException();
        }

        private static final String VICTIM_TAG_KEY = "victimTag";
    }

    private class MirrorCache extends Cache<Boolean, MirrorCacheLine> {
        private MirrorCache(String name) {
            super(ownerCache, name, ownerCache.getGeometry(), new Function2<Integer, Integer, MirrorCacheLine>() {
                public MirrorCacheLine apply(Integer set, Integer way) {
                    return new MirrorCacheLine(set, way);
                }
            });
        }

        private MirrorCacheLine getLine(CacheLine<?> ownerCacheLine) {
            return mirrorCache.getLine(ownerCacheLine.getSet(), ownerCacheLine.getWay());
        }

        public MirrorCacheLine findVictimLineForTag(int tag) {
            int set = this.getSet(tag);

            for (MirrorCacheLine line : this.sets.get(set).getLines()) {
                if (line.fsm.getState().isHt() && line.getVictimTag() == tag) {
                    return line;
                }
            }

            return null;
        }

        public void flush() {
            for (int set = 0; set < this.getNumSets(); set++) {
                for (MirrorCacheLine line : this.sets.get(set).getLines()) {
                    line.fsm.fireTransition(CacheLineHtRequestCondition.FLUSH);
                }
            }
        }

        @Override
        public MirrorCacheLine findLine(int address) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTag(int addr) {
            throw new UnsupportedOperationException();
        }
    }

    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
        public ProcessorCapability createCapability(Processor processor) {
            return new FsmBasedHtRequestLlcVictimTrackingCapability(processor);
        }
    };
}