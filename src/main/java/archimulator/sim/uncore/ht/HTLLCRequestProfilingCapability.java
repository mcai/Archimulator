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
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineFillEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action1;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;

import java.io.PrintWriter;
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

    private Map<Integer, Map<Integer, CacheLineHTRequestState>> llcLineBroughterThreadIds;
    private EvictableCache<HTLLCRequestVictimCacheLineState> htLLCRequestVictimCache;

    private long numMTLLCHits;
    private long numMTLLCMisses;

    private long numTotalHTLLCRequests;

    private long numUsefulHTLLCRequests;

    private long numGoodHTLLCRequests;
    private long numBadHTLLCRequests;
    private long numUglyHTLLCRequests;

    private long numLateHTLLCRequests;

    private Map<Integer, CacheSetStat> cacheSetStats;

    private BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent> eventDispatcher;

        private boolean printTrace = false;
//    private boolean printTrace = true;

    public static enum HTRequestQuality {
        GOOD,
        BAD,
        UGLY,
        INVALID
    }

    private class CacheLineHTRequestState {
        private int inFlightThreadId;
        private int threadId;
        public HTRequestQuality quality;

        private CacheLineHTRequestState() {
            this.inFlightThreadId = -1;
            this.threadId = -1;
        }

        public int getInFlightThreadId() {
            return inFlightThreadId;
        }

        public int getThreadId() {
            return threadId;
        }
    }

    private class CacheSetStat {
        private long numMTLLCHits;
        private long numMTLLCMisses;

        private long numTotalHTLLCRequests;

        private long numUsefulHTLLCRequests;

        private long numGoodHTLLCRequests;
        private long numBadHTLLCRequests;
        private long numUglyHTLLCRequests;

        private long numLateHTLLCRequests;

        private void reset() {
            numMTLLCHits = 0;
            numMTLLCMisses = 0;

            numTotalHTLLCRequests = 0;

            numUsefulHTLLCRequests = 0;

            numGoodHTLLCRequests = 0;
            numBadHTLLCRequests = 0;
            numUglyHTLLCRequests = 0;

            numLateHTLLCRequests = 0;
        }
    }

    public HTLLCRequestProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache());
    }

    public HTLLCRequestProfilingCapability(final DirectoryController llc) {
        this.llc = llc;

        this.llcLineBroughterThreadIds = new HashMap<Integer, Map<Integer, CacheLineHTRequestState>>();
        for (int set = 0; set < this.llc.getCache().getNumSets(); set++) {
            HashMap<Integer, CacheLineHTRequestState> llcLineBroughterThreadIdsPerSet = new HashMap<Integer, CacheLineHTRequestState>();
            this.llcLineBroughterThreadIds.put(set, llcLineBroughterThreadIdsPerSet);

            for (int way = 0; way < this.llc.getCache().getAssociativity(); way++) {
                llcLineBroughterThreadIdsPerSet.put(way, new CacheLineHTRequestState());
            }
        }

        this.cacheSetStats = new HashMap<Integer, CacheSetStat>();
        for (int set = 0; set < this.llc.getCache().getNumSets(); set++) {
            this.cacheSetStats.put(set, new CacheSetStat());
        }

        ValueProviderFactory<HTLLCRequestVictimCacheLineState, ValueProvider<HTLLCRequestVictimCacheLineState>> cacheLineStateProviderFactory = new ValueProviderFactory<HTLLCRequestVictimCacheLineState, ValueProvider<HTLLCRequestVictimCacheLineState>>() {
            @Override
            public ValueProvider<HTLLCRequestVictimCacheLineState> createValueProvider(Object... args) {
                int set = (Integer) args[0];
                int way = (Integer) args[1];

                return new HTLLCRequestVictimCacheLineStateValueProvider();
            }
        };

        this.htLLCRequestVictimCache = new EvictableCache<HTLLCRequestVictimCacheLineState>(llc, llc.getName() + ".htLLCRequestVictimCache", llc.getCache().getGeometry(), LRUPolicy.class, cacheLineStateProviderFactory);

        this.eventDispatcher = new BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (printTrace) {
                        pw.printf(
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
                        pw.flush();
                    }

                    int set = event.getSet();
                    boolean requesterIsHT = BasicThread.isHelperThread(event.getAccess().getThread());

                    boolean lineFoundIsHT = getLLCLineHTRequestState(set, event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleRequest(event, requesterIsHT, lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(LastLevelCacheLineFillEvent.class, new Action1<LastLevelCacheLineFillEvent>() {
            @Override
            public void apply(LastLevelCacheLineFillEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (printTrace) {
                        pw.printf(
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
                        pw.flush();
                    }

                    int set = event.getSet();
                    boolean requesterIsHT = BasicThread.isHelperThread(event.getAccess().getThread());
                    boolean lineFoundIsHT = getLLCLineHTRequestState(set, event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    handleLineFill(event, requesterIsHT, lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if (printTrace) {
                        pw.printf(
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
                        pw.flush();
                    }

                    int set = event.getSet();

                    checkInvariants(set);

                    boolean lineFoundIsHT = getLLCLineHTRequestState(set, event.getWay()).getThreadId() == BasicThread.getHelperThreadId();

                    markInvalid(set, event.getWay());

                    if (lineFoundIsHT) {
                        removeLRU(set);
                    }

                    checkInvariants(set);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    markLateHTRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numMTLLCHits = 0;
                numMTLLCMisses = 0;

                numTotalHTLLCRequests = 0;

                numUsefulHTLLCRequests = 0;

                numGoodHTLLCRequests = 0;
                numBadHTLLCRequests = 0;
                numUglyHTLLCRequests = 0;

                numLateHTLLCRequests = 0;

                for (int set = 0; set < llc.getCache().getNumSets(); set++) {
                    cacheSetStats.get(set).reset();
                }
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
        for(int set = 0; set < llc.getCache().getNumSets(); set++) {
            for(int way = 0; way < llc.getCache().getAssociativity(); way++) {
                CacheLineHTRequestState cacheLineHTRequestState = llcLineBroughterThreadIds.get(set).get(way);
                if(cacheLineHTRequestState.quality == HTRequestQuality.BAD) {
                    incBadHTLLCRequests(set);
                }
                else if(cacheLineHTRequestState.quality == HTRequestQuality.UGLY) {
                    incUglyHTLLCRequests(set);
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

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestAccuracy", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / this.numTotalHTLLCRequests) + "%");
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestCoverage", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / (this.numMTLLCMisses + this.numUsefulHTLLCRequests)) + "%");

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numGoodHTLLCRequests", String.valueOf(this.numGoodHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numBadHTLLCRequests", String.valueOf(this.numBadHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUglyHTLLCRequests", String.valueOf(this.numUglyHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numLateHTLLCRequests", String.valueOf(this.numLateHTLLCRequests));
        }
    }

    public void dumpStats() {
        this.sumUpUnstableHTLLCRequests();
        Map<String, Object> stats = new LinkedHashMap<String, Object>();
        this.dumpStats(stats);
        for(String key : stats.keySet()) {
            System.out.println(key + ": " + stats.get(key));
        }
    }

    private void markLateHTRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        int set = event.getSet();

        int requesterThreadId = event.getAccess().getThread().getId();
        int lineFoundThreadId = getLLCLineHTRequestState(set, event.getWay()).inFlightThreadId;

        if (lineFoundThreadId == -1) {
            throw new IllegalArgumentException();
        }

        boolean requesterIsHT = BasicThread.isHelperThread(requesterThreadId);
        boolean lineFoundIsHT = BasicThread.isHelperThread(lineFoundThreadId);

        if (!requesterIsHT && lineFoundIsHT) {
            this.numLateHTLLCRequests++;
            this.cacheSetStats.get(set).numLateHTLLCRequests++;
            this.eventDispatcher.dispatch(new LateHTLLCRequestEvent());
        }
    }

    private static PrintWriter pw;

    static {
        pw = CacheSimulator.pw;

//        try {
//            pw = new PrintWriter(new FileWriter(FileUtils.getUserDirectoryPath() + "/event_trace.txt"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    private void handleRequest(CoherentCacheServiceNonblockingRequestEvent event, boolean requesterIsHT, boolean lineFoundIsHT) {
        checkInvariants(event.getSet());

        boolean mtHit = event.isHitInCache() && !requesterIsHT && !lineFoundIsHT;
        boolean htHit = event.isHitInCache() && !requesterIsHT && lineFoundIsHT;

        CacheLine<HTLLCRequestVictimCacheLineState> vtLine = this.htLLCRequestVictimCache.findLine(event.getTag());

        boolean vtHit = !requesterIsHT && vtLine != null;

        if (!event.isHitInCache()) {
            if (requesterIsHT) {
                this.numTotalHTLLCRequests++;
                this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests++;
                this.eventDispatcher.dispatch(new HTLLCRequestEvent());
            }

            this.markTransientThreadId(event.getSet(), event.getWay(), event.getAccess().getThread().getId());
        }

        if (!requesterIsHT) {
            if (!event.isHitInCache()) {
                this.numMTLLCMisses++;
                this.cacheSetStats.get(event.getSet()).numMTLLCMisses++;
            } else {
                this.numMTLLCHits++;
                this.cacheSetStats.get(event.getSet()).numMTLLCHits++;

                if (lineFoundIsHT) {
                    this.numUsefulHTLLCRequests++;
                    this.cacheSetStats.get(event.getSet()).numUsefulHTLLCRequests++;
                }
            }
        }

        if (!mtHit && !htHit && vtHit) {
            handleRequestCase1(event, vtLine);
        } else if (!mtHit && htHit && !vtHit) {
            handleRequestCase2(event);
        } else if (!mtHit && htHit && vtHit) {
            handleRequestCase3(event, vtLine);
        } else if (mtHit && vtHit) {
            handleRequestCase4(event, vtLine);
        }
    }

    private void handleRequestCase1(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HTLLCRequestVictimCacheLineState> vtLine) {
        this.llcLineBroughterThreadIds.get(event.getSet()).get(event.getWay()).quality = HTRequestQuality.BAD;

        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase1 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.eventDispatcher.dispatch(new BadHTLLCRequestEvent());
        this.setLRU(event.getSet(), vtLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase2(CoherentCacheServiceNonblockingRequestEvent event) {
        this.llcLineBroughterThreadIds.get(event.getSet()).get(event.getWay()).quality = HTRequestQuality.GOOD;
        this.numGoodHTLLCRequests++;
        this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests++;

        this.markMT(event.getSet(), event.getWay());

        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase2 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase3(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HTLLCRequestVictimCacheLineState> vtLine) {
        this.markMT(event.getSet(), event.getWay());

        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase2 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.setLRU(event.getSet(), vtLine.getWay());
        this.removeLRU(event.getSet());

        checkInvariants(event.getSet());
    }

    private void handleRequestCase4(CoherentCacheServiceNonblockingRequestEvent event, CacheLine<HTLLCRequestVictimCacheLineState> vtLine) {
        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleRequestCase2 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.setLRU(event.getSet(), vtLine.getWay());

        checkInvariants(event.getSet());
    }

    private void handleLineFill(LastLevelCacheLineFillEvent event, boolean requesterIsHT, boolean lineFoundIsHT) {
        checkInvariants(event.getSet());

        int victimTag = event.getVictimTag();

        if(lineFoundIsHT) {
            HTRequestQuality quality = llcLineBroughterThreadIds.get(event.getSet()).get(event.getWay()).quality;

            if(quality == HTRequestQuality.BAD) {
                this.incBadHTLLCRequests(event.getSet());
            }
            else if(quality == HTRequestQuality.UGLY) {
                this.incUglyHTLLCRequests(event.getSet());
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        if (requesterIsHT) {
            markHT(event.getSet(), event.getWay());
            this.llcLineBroughterThreadIds.get(event.getSet()).get(event.getWay()).quality = HTRequestQuality.UGLY;
        } else {
            markMT(event.getSet(), event.getWay());
            this.llcLineBroughterThreadIds.get(event.getSet()).get(event.getWay()).quality = HTRequestQuality.INVALID;
        }

        if (requesterIsHT && !event.isEviction()) {
            handleLineFillCase1(event);

        } else {
            if (requesterIsHT && event.isEviction() && !lineFoundIsHT) {
                handleLineFillCase2(event, victimTag);
            } else if (requesterIsHT && event.isEviction() && lineFoundIsHT) {
                handleLineFillCase3(event);
            } else if (!requesterIsHT && event.isEviction() && lineFoundIsHT) {
                handleLineFillCase4(event);
            } else if (!requesterIsHT && event.isEviction() && !lineFoundIsHT) {
                handleLineFillCase5(event, victimTag);
            }
        }
    }

    private void handleLineFillCase1(LastLevelCacheLineFillEvent event) {
        // case 1
        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineFillCase1 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.insertNullEntry(event.getSet(), event.getTag());
        checkInvariants(event.getSet());
    }

    private void handleLineFillCase2(LastLevelCacheLineFillEvent event, int victimTag) {
        // case 2
        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineFillCase2 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.insertDataEntry(event.getSet(), victimTag, event.getTag());
        checkInvariants(event.getSet());
    }

    private void handleLineFillCase3(LastLevelCacheLineFillEvent event) {
        // case 3
        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineFillCase3 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        checkInvariants(event.getSet());
    }

    private void handleLineFillCase4(LastLevelCacheLineFillEvent event) {
        // case 4
        if (printTrace) {
            pw.printf(
                    "[%d] llc.[%d,%d] {%s} %s: handleLineFillCase4 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                    HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                    event.getSet(),
                    event.getWay(),
                    (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                    llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                    this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                    this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
            );
            pw.flush();
        }

        this.removeLRU(event.getSet());
        checkInvariants(event.getSet());
    }

    private void handleLineFillCase5(LastLevelCacheLineFillEvent event, int victimTag) {
        boolean htLLCRequestFound = false;

        for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
            if (this.htLLCRequestVictimCache.getLine(event.getSet(), way).getState() != HTLLCRequestVictimCacheLineState.INVALID) {
                htLLCRequestFound = true;
                break;
            }
        }

        if (htLLCRequestFound) {
            //case 5
            if (printTrace) {
                pw.printf(
                        "[%d] llc.[%d,%d] {%s} %s: handleLineFillCase5 (totalHT = %d, goodHT = %d, badHT = %d, uglyHT = %d)\n",
                        HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                        event.getSet(),
                        event.getWay(),
                        (llc.getCache().getLine(event.getSet(), event.getWay()).getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                        llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                        this.cacheSetStats.get(event.getSet()).numTotalHTLLCRequests,
                        this.cacheSetStats.get(event.getSet()).numGoodHTLLCRequests,
                        this.cacheSetStats.get(event.getSet()).numBadHTLLCRequests,
                        this.cacheSetStats.get(event.getSet()).numUglyHTLLCRequests
                );
                pw.flush();
            }

            this.removeLRU(event.getSet());
            this.insertDataEntry(event.getSet(), victimTag, event.getTag());
        }
        checkInvariants(event.getSet());
    }

    private void checkInvariants(int set) {
        int numHTLinesInLLC = select(this.llcLineBroughterThreadIds.get(set).values(), having(on(CacheLineHTRequestState.class).getThreadId(), equalTo(BasicThread.getHelperThreadId()))).size();
        int numNonHTLinesInLLC = select(this.llcLineBroughterThreadIds.get(set).values(), having(on(CacheLineHTRequestState.class).getThreadId(), not(BasicThread.getHelperThreadId()))).size();
        int numVictimEntriesInVictimCache = select(this.htLLCRequestVictimCache.getLines(set), having(on(CacheLine.class).getState(), not(HTLLCRequestVictimCacheLineState.INVALID))).size();

        if (numHTLinesInLLC != numVictimEntriesInVictimCache || numVictimEntriesInVictimCache + numNonHTLinesInLLC > this.llc.getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }

        CacheSetStat cacheSetStat = this.cacheSetStats.get(set);
        if (cacheSetStat.numUglyHTLLCRequests < 0) {
            throw new IllegalArgumentException();
        }
    }

    private void incUglyHTLLCRequests(int set) {
        this.numUglyHTLLCRequests++;
        this.cacheSetStats.get(set).numUglyHTLLCRequests++;
    }

    private void incBadHTLLCRequests(int set) {
        this.numBadHTLLCRequests++;
        this.cacheSetStats.get(set).numBadHTLLCRequests++;
    }

    private void markInvalid(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, -1, false);
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

    private CacheLineHTRequestState getLLCLineHTRequestState(int set, int way) {
        return this.llcLineBroughterThreadIds.get(set).get(way);
    }

    private void setLLCLineBroughterThreadId(int set, int way, int llcLineBroughterThreadId, boolean inFlight) {
        if (printTrace) {
            pw.printf("[%d] llc.[%d,%d].broughterThreadId = %d, inflight: %s\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way, llcLineBroughterThreadId, inFlight);
            pw.flush();
        }

        CacheLineHTRequestState htRequestState = getLLCLineHTRequestState(set, way);

        if (inFlight) {
            htRequestState.inFlightThreadId = llcLineBroughterThreadId;
        } else {
            htRequestState.inFlightThreadId = -1;
            htRequestState.threadId = llcLineBroughterThreadId;
        }
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
        if (printTrace) {
            pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: insertDataEntry(0x%08x)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(),
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState(),
                    tag);
            pw.flush();
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
        if (printTrace) {
            pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: insertNullEntry(%s)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(),
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState(),
                    "N/A");
            pw.flush();
        }
        htLLCRequestVictimCache.getEvictionPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

    private void setLRU(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
        CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        if (printTrace) {
            pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: setLRU\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way,
                    line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                    stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                    line.getState()
            );
            pw.flush();
        }
    }

    private void removeLRU(int set) {
        LRUPolicy<HTLLCRequestVictimCacheLineState> lru = this.getLruPolicyForHtRequestVictimCache();

        for (int i = this.llc.getCache().getAssociativity() - 1; i >= 0; i--) {
            int way = lru.getWayInStackPosition(set, i);
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            if (!line.getState().equals(HTLLCRequestVictimCacheLineState.INVALID)) {
                HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
                stateProvider.state = HTLLCRequestVictimCacheLineState.INVALID;
                stateProvider.htRequestTag = CacheLine.INVALID_TAG;
                line.setTag(CacheLine.INVALID_TAG);
                if (printTrace) {
                    pw.printf("[%d] hvc.[%d,%d] {%s (htRequestTag: %s)} %s: removeLRU\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way,
                            line.getTag() != CacheLine.INVALID_TAG ? String.format("0x%08x", line.getTag()) : "N/A",
                            stateProvider.htRequestTag != CacheLine.INVALID_TAG ? String.format("0x%08x", stateProvider.htRequestTag) : "N/A",
                            line.getState()
                    );
                    pw.flush();
                }
                return;
            }
        }

        throw new IllegalArgumentException();
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

    public BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent> getEventDispatcher() {
        return eventDispatcher;
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

    public abstract class HTLLCRequestProfilingCapabilityEvent implements BlockingEvent {
    }

    public class HTLLCRequestEvent extends HTLLCRequestProfilingCapabilityEvent {
    }

    public class BadHTLLCRequestEvent extends HTLLCRequestProfilingCapabilityEvent {
    }

    public class LateHTLLCRequestEvent extends HTLLCRequestProfilingCapabilityEvent {
    }
}
