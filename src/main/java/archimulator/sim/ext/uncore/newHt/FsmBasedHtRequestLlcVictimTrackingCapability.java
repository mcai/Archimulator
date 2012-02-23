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
package archimulator.sim.ext.uncore.newHt;

import archimulator.model.capability.ProcessorCapability;
import archimulator.model.capability.ProcessorCapabilityFactory;
import archimulator.model.event.DumpStatEvent;
import archimulator.model.event.PollStatsEvent;
import archimulator.model.event.ResetStatEvent;
import archimulator.sim.core.BasicThread;
import archimulator.sim.core.Processor;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.coherence.CoherentCache;
import archimulator.sim.uncore.coherence.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheEndCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineEvictedByMemWriteProcessEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1X;
import archimulator.util.fsm.FiniteStateMachine;
import archimulator.util.fsm.FiniteStateMachineFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FsmBasedHtRequestLlcVictimTrackingCapability implements ProcessorCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private long totalHtRequests;
    private long usedHtRequests;
    private long pollutingHtRequests;
    private long usedPollutingHtRequests;
    private long unusedHtRequests;

    private FiniteStateMachineFactory<CacheLineHtRequestState, CacheLineHtRequestCondition> fsmFactory;
    private Map<Integer, Map<Integer, FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>>> fsms;

    public FsmBasedHtRequestLlcVictimTrackingCapability(Processor processor) {
        this.llc = processor.getCacheHierarchy().getL2Cache().getCache();

        this.setupFsmFactory();

        this.fsms = new HashMap<Integer, Map<Integer, FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>> fsmPerSet = new HashMap<Integer, FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>>();
            this.fsms.put(set, fsmPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                fsmPerSet.put(way, new FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>(this.fsmFactory, this.llc.getName() + ".LastLevelCacheHtRequestCachePollutionProfilingCapability.mirrorCacheLine" + " [" + set + ", " + way + "].fsm", CacheLineHtRequestState.INVALID));
            }
        }

        processor.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.llc) {
                    serviceRequest(event);
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(CoherentCacheEndCacheAccessEvent.class, new Action1<CoherentCacheEndCacheAccessEvent>() {
            public void apply(CoherentCacheEndCacheAccessEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.llc) {
                    if (!event.isAborted()) {
//                        CacheLine<?> line = (CacheLine<?>) event.getCacheAccess().getFsm();
//                        if(line.getTag() != mirrorCache.getFsm(line).getTag()) {
//                            throw new IllegalArgumentException();
//                        }
                    }
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(LastLevelCacheLineEvictedByMemWriteProcessEvent.class, new Action1<LastLevelCacheLineEvictedByMemWriteProcessEvent>() {
            public void apply(LastLevelCacheLineEvictedByMemWriteProcessEvent event) {
                if (event.getCache().getCache() == FsmBasedHtRequestLlcVictimTrackingCapability.this.llc) {
                    if (FsmBasedHtRequestLlcVictimTrackingCapability.this.fsms.get(event.getLineToInvalidate().getSet()).get(event.getLineToInvalidate().getWay()).getState() != CacheLineHtRequestState.INVALID) {
                        throw new IllegalArgumentException();
                    }

                    invalidate(event);
                }
            }
        });

        processor.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                totalHtRequests = 0;
                usedHtRequests = 0;
                pollutingHtRequests = 0;
                usedPollutingHtRequests = 0;
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
                    flush();
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".totalHtRequests", String.valueOf(this.totalHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".usedHtRequests.confirmed", String.valueOf(this.usedHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".pollutingHtRequests.confirmed", String.valueOf(this.pollutingHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".usedPollutingHtRequests.confirmed", String.valueOf(this.usedPollutingHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".unusedHtRequests.confirmed", String.valueOf(this.unusedHtRequests));
    }

    private void setupFsmFactory() {
        this.fsmFactory = new FiniteStateMachineFactory<CacheLineHtRequestState, CacheLineHtRequestCondition>();

        this.fsmFactory.inState(CacheLineHtRequestState.INVALID)
                .onCondition(CacheLineHtRequestCondition.HT_MISS, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> state, Object... params) {
                        totalHtRequests++;
                        return CacheLineHtRequestState.UNUSED_HT;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.MT_MISS, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> state, Object... params) {
                        return CacheLineHtRequestState.MT;
                    }
                })
                .ignoreCondition(CacheLineHtRequestCondition.FLUSH);

        this.fsmFactory.inState(CacheLineHtRequestState.MT)
                .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                .onCondition(CacheLineHtRequestCondition.EVICTED_BY_MT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> state, Object... params) {
                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.EVICTED_BY_HT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        from.put("victimTag", params[0]);

                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .ignoreCondition(CacheLineHtRequestCondition.FLUSH)
                .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.INVALID;
                    }
                });

        this.fsmFactory.inState(CacheLineHtRequestState.UNUSED_HT)
                .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                .onCondition(CacheLineHtRequestCondition.MT_HIT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.USED_HT;
                    }
                })
                .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        unusedHtRequests++;
                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        from.put("victimTag", -1);
                        return CacheLineHtRequestState.POLLUTING_HT;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        unusedHtRequests++;
                        return CacheLineHtRequestState.INVALID;
                    }
                });

        this.fsmFactory.inState(CacheLineHtRequestState.USED_HT)
                .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        usedHtRequests++;
                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        from.put("victimTag", -1);
                        return CacheLineHtRequestState.USED_POLLUTING_HT;
                    }
                })
                .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.INVALID;
                    }
                });

        this.fsmFactory.inState(CacheLineHtRequestState.POLLUTING_HT)
                .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                .onCondition(CacheLineHtRequestCondition.MT_HIT, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.USED_POLLUTING_HT;
                    }
                })
                .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        pollutingHtRequests++;
                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .ignoreCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT)
                .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.INVALID;
                    }
                });

        this.fsmFactory.inState(CacheLineHtRequestState.USED_POLLUTING_HT)
                .ignoreCondition(CacheLineHtRequestCondition.HT_HIT)
                .ignoreCondition(CacheLineHtRequestCondition.MT_HIT)
                .onConditions(CacheLineHtRequestCondition.EVICTED, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        usedPollutingHtRequests++;
                        return CacheLineHtRequestState.INVALID;
                    }
                })
                .ignoreCondition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT)
                .onCondition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE, new Function1X<FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition>, CacheLineHtRequestState>() {
                    public CacheLineHtRequestState apply(FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> from, Object... params) {
                        return CacheLineHtRequestState.INVALID;
                    }
                });
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> ownerCacheLine = event.getLineFound();
        FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> fsm = this.fsms.get(ownerCacheLine.getSet()).get(ownerCacheLine.getWay());

        if (event.isHitInCache()) {
            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_HIT : CacheLineHtRequestCondition.MT_HIT);
        } else {
            if (event.isEviction()) {
                fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.EVICTED_BY_HT : CacheLineHtRequestCondition.EVICTED_BY_MT, event.getLineFound().getTag());
            }

            if (fsm.getState() != CacheLineHtRequestState.INVALID) {
                throw new IllegalArgumentException();
            }

            FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> lineForVictim = this.findVictimLineForTag(this.llc.getTag(event.getAddress()));
            if (lineForVictim != null) {
                lineForVictim.fireTransition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT);
            }

            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_MISS : CacheLineHtRequestCondition.MT_MISS);
        }
    }

    private void invalidate(LastLevelCacheLineEvictedByMemWriteProcessEvent event) {
        CacheLine<?> ownerCacheLine = event.getLineToInvalidate();
        FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> fsm = this.fsms.get(ownerCacheLine.getSet()).get(ownerCacheLine.getWay());
        if (fsm.getState() != CacheLineHtRequestState.INVALID) {
            fsm.fireTransition(CacheLineHtRequestCondition.INVALIDATED_BY_MEM_WRITE);
        }
    }

    public FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> findVictimLineForTag(int tag) {
        int set = this.llc.getSet(tag);

        for (FiniteStateMachine<CacheLineHtRequestState, CacheLineHtRequestCondition> fsm : this.fsms.get(set).values()) {
            if (fsm.getState().isHt() && fsm.get(Integer.class, "victimTag") == tag) {
                return fsm;
            }
        }

        return null;
    }

    public void flush() {
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                this.fsms.get(set).get(way).fireTransition(CacheLineHtRequestCondition.FLUSH);
            }
        }
    }

    private enum CacheLineHtRequestState {
        INVALID,
        MT,
        UNUSED_HT,
        USED_HT,
        POLLUTING_HT,
        USED_POLLUTING_HT;

        public boolean isHt() {
            return this == UNUSED_HT || this == POLLUTING_HT;
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

    public static final ProcessorCapabilityFactory FACTORY = new ProcessorCapabilityFactory() {
        public ProcessorCapability createCapability(Processor processor) {
            return new FsmBasedHtRequestLlcVictimTrackingCapability(processor);
        }
    };
}