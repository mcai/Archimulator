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
package archimulator.sim.uncore.ht;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.CacheSimulator;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineInsertEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action1;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.CoreMatchers.*;

//on last puts or putm from owner:
//  RemoveLRU
//
//on replacement:
//	1. HT replaces INVALID => insert NULL
//	2. HT replaces MT => insert DATA
//	3. HT replaces HT => No action
//	4. MT replaces HT => Remove LRU
//	5. MT replaces MT, Exists HT => Remove LRU, Insert DATA
//
//on reference:
//	1. MT miss + HT miss + VT Hit => ; Bad HT, VT.setLRU
//	2. MT miss + HT hit + VT miss => MT to HT; Good HT, removeLRU
//	3. MT miss + HT hit + VT hit => MT to HT; VT.setLRU, removeLRU
//	4. MT hit + HT miss + VT hit => ; VT.setLRU
public class HTLLCRequestProfilingCapability implements SimulationCapability {
    private DirectoryController llc;

    private Map<Integer, Map<Integer, CacheLineHTRequestState>> llcLineHTRequestStates;
    private EvictableCache<HTLLCRequestVictimCacheLineState> htLLCRequestVictimCache;

    private long numMTLLCHits;
    private long numMTLLCMisses;

    private long numTotalHTLLCRequests;

    private long numRedundantHitToTransientTagHTLLCRequests;
    private long numRedundantHitToCacheHTLLCRequests;

    private long numUsefulHTLLCRequests;

    private long numTimelyHTLLCRequests;
    private long numLateHTLLCRequests;

    private long numBadHTLLCRequests;

    private long numUglyHTLLCRequests;

    private boolean isPrintTrace(int set) {
//        return set == 57;
        return false;
//        return true;
    }

    public static enum HTRequestQuality {
        TIMELY,
        LATE,
        BAD,
        UGLY,
        INVALID;

        public boolean isModifiable() {
            return this == UGLY;
        }
    }

    private class CacheLineHTRequestState {
        private int inFlightThreadId;
        private int threadId;
        private HTRequestQuality quality;
        public boolean hitToTransientTag;

        private CacheLineHTRequestState() {
            this.inFlightThreadId = -1;
            this.threadId = -1;
            this.quality = HTRequestQuality.INVALID;
        }

        public int getInFlightThreadId() {
            return inFlightThreadId;
        }

        public int getThreadId() {
            return threadId;
        }

        private void setQuality(HTRequestQuality quality) {
            if(this.quality != HTRequestQuality.INVALID && quality != HTRequestQuality.INVALID && !this.quality.isModifiable()) {
                throw new IllegalArgumentException();
            }

            this.quality = quality;
        }
    }

    public HTLLCRequestProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache());
    }

    public HTLLCRequestProfilingCapability(final DirectoryController llc) {
        this.llc = llc;

        this.llcLineHTRequestStates = new HashMap<Integer, Map<Integer, CacheLineHTRequestState>>();
        for (int set = 0; set < this.llc.getCache().getNumSets(); set++) {
            HashMap<Integer, CacheLineHTRequestState> llcLineHTRequestStatesPerSet = new HashMap<Integer, CacheLineHTRequestState>();
            this.llcLineHTRequestStates.put(set, llcLineHTRequestStatesPerSet);

            for (int way = 0; way < this.llc.getCache().getAssociativity(); way++) {
                llcLineHTRequestStatesPerSet.put(way, new CacheLineHTRequestState());
            }
        }

        ValueProviderFactory<HTLLCRequestVictimCacheLineState, ValueProvider<HTLLCRequestVictimCacheLineState>> cacheLineStateProviderFactory = new ValueProviderFactory<HTLLCRequestVictimCacheLineState, ValueProvider<HTLLCRequestVictimCacheLineState>>() {
            @Override
            public ValueProvider<HTLLCRequestVictimCacheLineState> createValueProvider(Object... args) {
                return new HTLLCRequestVictimCacheLineStateValueProvider();
            }
        };

        this.htLLCRequestVictimCache = new EvictableCache<HTLLCRequestVictimCacheLineState>(llc, llc.getName() + ".htLLCRequestVictimCache", llc.getCache().getGeometry(), LRUPolicy.class, cacheLineStateProviderFactory);

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (isPrintTrace(event.getSet())) {
                        CacheSimulator.pw.printf(
                                "[%d] llc.[%d,%d] {%s} %s: %s.0x%08x %s (%s)\n",
                                HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                                event.getSet(),
                                event.getWay(),
                                (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                                llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                                event.getAccess().getThread().getName(),
                                event.getAccess().getPhysicalTag(),
                                event.getAccess().getType(),
                                event.isHitInCache() ? "hit" : "miss"
                        );
                        CacheSimulator.pw.flush();
                    }

                    int set = event.getSet();
                    boolean requesterIsHT = BasicThread.isHelperThread(event.getAccess().getThread());

                    boolean lineFoundIsHT = llcLineHTRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleRequest(event, requesterIsHT, lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(LastLevelCacheLineInsertEvent.class, new Action1<LastLevelCacheLineInsertEvent>() {
            @Override
            public void apply(LastLevelCacheLineInsertEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (isPrintTrace(event.getSet())) {
                        CacheSimulator.pw.printf(
                                "[%d] llc.[%d,%d] {%s} %s: %s.0x%08x %s (%s)\n",
                                HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                                event.getSet(),
                                event.getWay(),
                                (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                                llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                                event.getAccess().getThread().getName(),
                                event.getAccess().getPhysicalTag(),
                                event.getAccess().getType(),
                                "fill"
                        );
                        CacheSimulator.pw.flush();
                    }

                    int set = event.getSet();
                    boolean requesterIsHT = BasicThread.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHT = HTLLCRequestProfilingCapability.this.llcLineHTRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleLineInsert(event, requesterIsHT, lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (isPrintTrace(event.getSet())) {
                        CacheSimulator.pw.printf(
                                "[%d] llc.[%d,%d] {%s} %s: %s.0x%08x %s LAST_PUTS or PUTM_AND_DATA_FROM_OWNER\n",
                                HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                                event.getSet(),
                                event.getWay(),
                                (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                                llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                                event.getAccess().getThread().getName(),
                                event.getAccess().getPhysicalTag(),
                                event.getAccess().getType()
                        );
                        CacheSimulator.pw.flush();
                    }

                    int set = event.getSet();

                    checkInvariants(set);

                    boolean lineFoundIsHT = HTLLCRequestProfilingCapability.this.llcLineHTRequestStates.get(set).get(event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    markInvalid(set, event.getWay());

                    if (lineFoundIsHT) {
                        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHtRequestTag(event.getSet(), event.getTag(), false);

                        if (wayOfVictimCacheLine == -1) {
                            throw new IllegalArgumentException();
                        }

                        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);
                    }

                    checkInvariants(set);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    int set = event.getSet();

                    int requesterThreadId = event.getAccess().getThread().getId();
                    int lineFoundThreadId = HTLLCRequestProfilingCapability.this.llcLineHTRequestStates.get(set).get(event.getWay()).inFlightThreadId;

                    if (lineFoundThreadId == -1) {
                        throw new IllegalArgumentException();
                    }

                    boolean requesterIsHT = BasicThread.isHelperThread(requesterThreadId);
                    boolean lineFoundIsHT = BasicThread.isHelperThread(lineFoundThreadId);

                    if (!requesterIsHT && lineFoundIsHT) {
                        markLate(set,  event.getWay(), true);
                    } else if (requesterIsHT && !lineFoundIsHT) {
                        markLate(set,  event.getWay(), true);
                    }
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numMTLLCHits = 0;
                numMTLLCMisses = 0;

                numTotalHTLLCRequests = 0;

                numRedundantHitToTransientTagHTLLCRequests = 0;
                numRedundantHitToCacheHTLLCRequests = 0;

                numUsefulHTLLCRequests = 0;

                numTimelyHTLLCRequests = 0;
                numLateHTLLCRequests = 0;

                numBadHTLLCRequests = 0;

                numUglyHTLLCRequests = 0;
            }
        });

        llc.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        llc.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    sumUpUnstableHTLLCRequests();
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void sumUpUnstableHTLLCRequests() {
        for (int set = 0; set < llc.getCache().getNumSets(); set++) {
            for (int way = 0; way < llc.getCache().getAssociativity(); way++) {
                CacheLineHTRequestState cacheLineHTRequestState = llcLineHTRequestStates.get(set).get(way);
                if (cacheLineHTRequestState.quality == HTRequestQuality.BAD) {
                    incBadHTLLCRequests();
                } else if (cacheLineHTRequestState.quality == HTRequestQuality.UGLY) {
                    incUglyHTLLCRequests();
                }
            }
        }
    }

    private void dumpStats(Map<String, Object> stats) {
        if (this.numTotalHTLLCRequests > 0) {
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numMTLLCHits", String.valueOf(this.numMTLLCHits));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numMTLLCMisses", String.valueOf(this.numMTLLCMisses));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numTotalHTLLCRequests", String.valueOf(this.numTotalHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUsefulHTLLCRequests", String.valueOf(this.numUsefulHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numRedundantHitToTransientTagHTLLCRequests", String.valueOf(this.numRedundantHitToTransientTagHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numRedundantHitToCacheHTLLCRequests", String.valueOf(this.numRedundantHitToCacheHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numTimelyHTLLCRequests", String.valueOf(this.numTimelyHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numLateHTLLCRequests", String.valueOf(this.numLateHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numBadHTLLCRequests", String.valueOf(this.numBadHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUglyHTLLCRequests", String.valueOf(this.numUglyHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestAccuracy", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / this.numTotalHTLLCRequests) + "%");
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestCoverage", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / (this.numMTLLCMisses + this.numUsefulHTLLCRequests)) + "%");
        }
    }

    public void dumpStats() {
        this.sumUpUnstableHTLLCRequests();
        Map<String, Object> stats = new LinkedHashMap<String, Object>();
        this.dumpStats(stats);
        for (String key : stats.keySet()) {
            System.out.println(key + ": " + stats.get(key));
        }
    }

    private void handleRequest(CoherentCacheServiceNonblockingRequestEvent event, boolean requesterIsHT, boolean lineFoundIsHT) {
        checkInvariants(event.getSet());

        boolean mtHit = event.isHitInCache() && !requesterIsHT && !lineFoundIsHT;
        boolean htHit = event.isHitInCache() && !requesterIsHT && lineFoundIsHT;

        CacheLine<HTLLCRequestVictimCacheLineState> vtLine = this.htLLCRequestVictimCache.findLine(event.getTag());

        boolean vtHit = vtLine != null;

        if (!event.isHitInCache()) {
            this.markTransientThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId());
        }

        if (!requesterIsHT) {
            if (!event.isHitInCache()) {
                this.numMTLLCMisses++;
            } else {
                this.numMTLLCHits++;

                if (lineFoundIsHT) {
                    this.numUsefulHTLLCRequests++;
                }
            }
        } else {
            this.numTotalHTLLCRequests++;

            if (event.isHitInCache() && !lineFoundIsHT) {
                if(this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).hitToTransientTag) {
                    this.numRedundantHitToTransientTagHTLLCRequests++;
                }
                else {
                    this.numRedundantHitToCacheHTLLCRequests++;
                }
            }
        }

        if (!requesterIsHT && !mtHit && !htHit && vtHit) {
            handleRequestCase1(event, vtLine);
        } else if (!requesterIsHT && !mtHit && htHit && !vtHit) {
            handleRequestCase2(event);
        } else if (!requesterIsHT && !mtHit && htHit && vtHit) {
            handleRequestCase3(event);
        } else if (!requesterIsHT && mtHit && vtHit) {
            handleRequestCase4(event, vtLine);
        } else if (requesterIsHT && vtHit) {
            clearVictimInVictimCacheLine(event.getSet(), vtLine.getWay());
            checkInvariants(event.getSet());
        }

        if (this.htLLCRequestVictimCache.findWay(event.getTag()) != -1) {
            throw new IllegalArgumentException();
        }
    }

    private void handleRequestCase1(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HTLLCRequestVictimCacheLineState> vtLine) {
        this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.BAD);

        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase1\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        clearVictimInVictimCacheLine(event.getSet(), vtLine.getWay());

//        this.setLRU(event.getSet(), vtLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase2(CoherentCacheServiceNonblockingRequestEvent event) {
        if(this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).hitToTransientTag) {
            this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.LATE);
            this.numLateHTLLCRequests++;
        }
        else {
            this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.TIMELY);
            this.numTimelyHTLLCRequests++;
        }

        this.markMT(event.getSet(), event.getWay());
        this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.INVALID);

        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase2\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHtRequestTag(event.getSet(), event.getTag(), false);

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

//        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase3(CoherentCacheServiceNonblockingRequestEvent event) {
        this.markMT(event.getSet(), event.getWay());
        this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.INVALID);

        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase3\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        int wayOfVictimCacheLine = findWayOfVictimCacheLineByHtRequestTag(event.getSet(), event.getTag(), false);

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

//        this.setLRU(event.getSet(), vtLine.getWay());
//        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase4(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HTLLCRequestVictimCacheLineState> vtLine) {
        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase4\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        clearVictimInVictimCacheLine(event.getSet(), vtLine.getWay());

//        this.setLRU(event.getSet(), vtLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert(LastLevelCacheLineInsertEvent event, boolean requesterIsHT, boolean lineFoundIsHT) {
        checkInvariants(event.getSet());

        CacheLine<HTLLCRequestVictimCacheLineState> vtLine = this.htLLCRequestVictimCache.findLine(event.getTag());

        if(vtLine != null) {
            clearVictimInVictimCacheLine(event.getSet(), vtLine.getWay());
        }

        if (lineFoundIsHT) {
            HTRequestQuality quality = llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).quality;

            if (quality == HTRequestQuality.BAD) {
                this.incBadHTLLCRequests();
            } else if (quality == HTRequestQuality.UGLY) {
                this.incUglyHTLLCRequests();
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (requesterIsHT) {
            markHT(event.getSet(), event.getWay());
            this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.UGLY);
        } else {
            markMT(event.getSet(), event.getWay());
            this.llcLineHTRequestStates.get(event.getSet()).get(event.getWay()).setQuality(HTRequestQuality.INVALID);
        }

        if (requesterIsHT && !event.isEviction()) {
            handleLineInsert1(event);
        } else if (requesterIsHT && event.isEviction() && !lineFoundIsHT) {
            handleLineInsert2(event);
        } else if (requesterIsHT && event.isEviction() && lineFoundIsHT) {
            handleLineInsert3(event);
        } else if (!requesterIsHT && event.isEviction() && lineFoundIsHT) {
            handleLineInsert4(event);
        } else if (!requesterIsHT && event.isEviction() && !lineFoundIsHT) {
            handleLineInsert5(event);
        }
        else {
            checkInvariants(event.getSet());
        }

        if (this.htLLCRequestVictimCache.findWay(event.getTag()) != -1) {
            throw new IllegalArgumentException();
        }
    }

    private void handleLineInsert1(LastLevelCacheLineInsertEvent event) {
        // case 1
        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineInsert1\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        this.insertNullEntry(event.getSet(), event.getTag());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert2(LastLevelCacheLineInsertEvent event) {
        // case 2
        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineInsert2\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        this.insertDataEntry(event.getSet(), event.getVictimTag(), event.getTag());

        checkInvariants(event.getSet());
    }

    private void handleLineInsert3(LastLevelCacheLineInsertEvent event) {
        // case 3
        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineInsert3\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHtRequestTag(event.getSet(), event.getVictimTag(), false);

        CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(event.getSet(), wayOfVictimCacheLine);
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.htRequestTag = event.getTag();

        checkInvariants(event.getSet());
    }

    private void handleLineInsert4(LastLevelCacheLineInsertEvent event) {
        // case 4
        if (isPrintTrace(event.getSet())) {
            CacheSimulator.pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineInsert4\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState()
            );
            CacheSimulator.pw.flush();
        }

        int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHtRequestTag(event.getSet(), event.getVictimTag(), false);

        if (wayOfVictimCacheLine == -1) {
            throw new IllegalArgumentException();
        }

        invalidateVictimCacheLine(event.getSet(), wayOfVictimCacheLine);

        checkInvariants(event.getSet());
    }

    private void handleLineInsert5(LastLevelCacheLineInsertEvent event) {
//        boolean htLLCRequestFound = false;
//
//        for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
//            if (this.htLLCRequestVictimCache.getLine(event.getSet(), way).getState() != HTLLCRequestVictimCacheLineState.INVALID) {
//                htLLCRequestFound = true;
//                break;
//            }
//        }
//
//        if (htLLCRequestFound) {
//            //case 5
//            if (isPrintTrace(event.getSet())) {
//                CacheSimulator.pw.printf(
//                        "[%d] llc.[%d,%d] {%s} %s: handleLineInsert5 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
//                        HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
//                        event.getSet(),
//                        event.getWay(),
//                        (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
//                        llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
//                        this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
//                        this.cacheSetStats.get(event.getSet()).numTimelyHTLLCRequests,
//                        this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
//                        this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
//                );
//                CacheSimulator.pw.flush();
//            }
//
//            this.removeLRU(event.getSet());
//            this.insertDataEntry(event.getSet(), victimTag, event.getTag());
//        }

        checkInvariants(event.getSet());
    }

    private void checkInvariants(int set) {
        int numHTLinesInLLC = select(this.llcLineHTRequestStates.get(set).values(), having(on(CacheLineHTRequestState.class).getThreadId(), equalTo(BasicThread.getHelperThreadId()))).size();
        int numNonHTLinesInLLC = select(this.llcLineHTRequestStates.get(set).values(), having(on(CacheLineHTRequestState.class).getThreadId(), not(BasicThread.getHelperThreadId()))).size();
        int numVictimEntriesInVictimCache = select(this.htLLCRequestVictimCache.getLines(set), having(on(CacheLine.class).getState(), not(HTLLCRequestVictimCacheLineState.INVALID))).size();

        if (numHTLinesInLLC != numVictimEntriesInVictimCache || numVictimEntriesInVictimCache + numNonHTLinesInLLC > this.llc.getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < this.llc.getCache().getAssociativity(); i++) {
            CacheLine<DirectoryControllerState> line = this.llc.getCache().getLine(set, i);
            if (line.getState().isStable() && line.isValid() && this.llcLineHTRequestStates.get(set).get(i).getThreadId() == BasicThread.getHelperThreadId()) {
                int wayOfVictimCacheLine = this.findWayOfVictimCacheLineByHtRequestTag(set, line.getTag(), true);

                if (wayOfVictimCacheLine == -1) {
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private void incUglyHTLLCRequests() {
        this.numUglyHTLLCRequests++;
    }

    private void incBadHTLLCRequests() {
        this.numBadHTLLCRequests++;
    }

    private void markInvalid(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, -1, false);
        this.markLate(set, way, false);
    }

    private void markHT(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getHelperThreadId(), false);
    }

    private void markMT(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getMainThreadId(), false);
    }

    private void markTransientThreadId(int set, int way, int threadId) {
        this.setLLCLineBroughterThreadId(set, way, threadId, true);
    }

    private void setLLCLineBroughterThreadId(int set, int way, int llcLineBroughterThreadId, boolean inFlight) {
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] llc.[%d,%d].broughterThreadId = %d, inflight: %s\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way, llcLineBroughterThreadId, inFlight);
            CacheSimulator.pw.flush();
        }

        CacheLineHTRequestState htRequestState = this.llcLineHTRequestStates.get(set).get(way);

        if (inFlight) {
            htRequestState.inFlightThreadId = llcLineBroughterThreadId;
        } else {
            htRequestState.inFlightThreadId = -1;
            htRequestState.threadId = llcLineBroughterThreadId;
        }
    }

    private void markLate(int set, int way, boolean late) {
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] llc.[%d,%d].markLate()\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way);
            CacheSimulator.pw.flush();
        }

        CacheLineHTRequestState htRequestState = this.llcLineHTRequestStates.get(set).get(way);
        htRequestState.hitToTransientTag = late;
    }

    private void insertDataEntry(int set, int tag, int htRequestTag) {
        if (tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        CacheAccess<HTLLCRequestVictimCacheLineState> newMiss = this.newMiss(tag, set);
        CacheLine<HTLLCRequestVictimCacheLineState> line = newMiss.getLine();
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HTLLCRequestVictimCacheLineState.DATA;
        stateProvider.htRequestTag = htRequestTag;
        line.setTag(tag);
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: insertDataEntry(0x%08x)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(),
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState(),
                    tag);
            CacheSimulator.pw.flush();
        }
        htLLCRequestVictimCache.getEvictionPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

    private void insertNullEntry(int set, int htRequestTag) {
        CacheAccess<HTLLCRequestVictimCacheLineState> newMiss = this.newMiss(0, set);
        CacheLine<HTLLCRequestVictimCacheLineState> line = newMiss.getLine();
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HTLLCRequestVictimCacheLineState.NULL;
        stateProvider.htRequestTag = htRequestTag;
        line.setTag(CacheLine.INVALID_TAG);
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: insertNullEntry(%s)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(),
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState(),
                    "N/A");
            CacheSimulator.pw.flush();
        }
        htLLCRequestVictimCache.getEvictionPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

//    private void setLRU(int set, int way) {
//        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
//        CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
//        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
//        if (isPrintTrace(set)) {
//            CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: setLRU\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way,
//                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
//                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
//                    line.getState()
//            );
//            CacheSimulator.pw.flush();
//        }
//    }
//
//    private void removeLRU(int set) {
//        LRUPolicy<HTLLCRequestVictimCacheLineState> lru = this.getLruPolicyForHtRequestVictimCache();
//
//        for (int i = this.llc.getCache().getAssociativity() - 1; i >= 0; i--) {
//            int way = lru.getWayInStackPosition(set, i);
//            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
//            if (!line.getState().equals(HTLLCRequestVictimCacheLineState.INVALID)) {
//                invalidateVictimCacheLine(set, way);
//                return;
//            }
//        }
//
//        throw new IllegalArgumentException();
//    }

    private int findWayOfVictimCacheLineByHtRequestTag(int set, int htRequestTag, boolean checkOnly) {
        for (int way = 0; way < this.llc.getCache().getAssociativity(); way++) {
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
            if (stateProvider.htRequestTag == htRequestTag) {
                if (isPrintTrace(set) && !checkOnly) {
                    CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: findWayOfVictimCacheLineByHtRequestTag(%s)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(),
                            line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                            stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                            line.getState(),
                            String.format("0x%08x", htRequestTag));
                    CacheSimulator.pw.flush();
                }
                return way;
            }
        }

        return -1;
    }

    private void invalidateVictimCacheLine(int set, int way) {
        CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HTLLCRequestVictimCacheLineState.INVALID;
        stateProvider.htRequestTag = CacheLine.INVALID_TAG;
        line.setTag(CacheLine.INVALID_TAG);
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: invalidate\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way,
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState()
            );
            CacheSimulator.pw.flush();
        }
    }

    private void clearVictimInVictimCacheLine(int set, int way) {
        CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.state = HTLLCRequestVictimCacheLineState.NULL;
        line.setTag(CacheLine.INVALID_TAG);
        if (isPrintTrace(set)) {
            CacheSimulator.pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: clearVictimInVictimCacheLine\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way,
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState()
            );
            CacheSimulator.pw.flush();
        }
    }

    private LRUPolicy<HTLLCRequestVictimCacheLineState> getLruPolicyForHtRequestVictimCache() {
        return (LRUPolicy<HTLLCRequestVictimCacheLineState>) this.htLLCRequestVictimCache.getEvictionPolicy();
    }

    private CacheAccess<HTLLCRequestVictimCacheLineState> newMiss(int address, int set) {
        int tag = this.htLLCRequestVictimCache.getTag(address);

        for (int i = 0; i < this.htLLCRequestVictimCache.getAssociativity(); i++) {
            int way = this.getLruPolicyForHtRequestVictimCache().getWayInStackPosition(set, i);
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<HTLLCRequestVictimCacheLineState>(this.htLLCRequestVictimCache, null, set, way, tag);
            }
        }

        throw new IllegalArgumentException();
    }

    public static enum HTLLCRequestVictimCacheLineState {
        INVALID,
        NULL,
        DATA
    }

    private static class HTLLCRequestVictimCacheLineStateValueProvider implements ValueProvider<HTLLCRequestVictimCacheLineState> {
        private HTLLCRequestVictimCacheLineState state;
        private int htRequestTag;

        public HTLLCRequestVictimCacheLineStateValueProvider() {
            this.state = HTLLCRequestVictimCacheLineState.INVALID;
            this.htRequestTag = CacheLine.INVALID_TAG;
        }

        @Override
        public HTLLCRequestVictimCacheLineState get() {
            return state;
        }

        @Override
        public HTLLCRequestVictimCacheLineState getInitialValue() {
            return HTLLCRequestVictimCacheLineState.INVALID;
        }
    }
}
