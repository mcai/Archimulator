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
package archimulator.sim.ext.uncore;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.CacheSimulator;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.sim.uncore.coherence.event.LastLevelCacheLineFillEvent;
import archimulator.sim.uncore.coherence.msi.state.DirectoryControllerState;
import archimulator.util.ValueProvider;
import archimulator.util.ValueProviderFactory;
import net.pickapack.action.Action1;
import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;
import org.apache.commons.io.FileUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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

    private Map<Integer, Map<Integer, Integer>> llcLineBroughterThreadIds;
    private EvictableCache<HTLLCRequestVictimCacheLineState> htLLCRequestVictimCache;

    private long numMTLLCMisses;

    private long numTotalHTLLCRequests;

    private long numUsefulHTLLCRequests;

    private long numGoodHTLLCRequests;
    private long numBadHTLLCRequests;

    private long numLateHTLLCRequests;

    private Map<Integer, CacheSetStat> cacheSetStats;

    private BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent> eventDispatcher;

//    private boolean printTrace = false;
    private boolean printTrace = true;

    private class CacheSetStat {
        private long numMTLLCMisses;

        private long numTotalHTLLCRequests;

        private long numUsefulHTLLCRequests;

        private long numGoodHTLLCRequests;
        private long numBadHTLLCRequests;

        private long numLateHTLLCRequests;

        private void reset() {
            numMTLLCMisses = 0;

            numTotalHTLLCRequests = 0;

            numUsefulHTLLCRequests = 0;

            numGoodHTLLCRequests = 0;
            numBadHTLLCRequests = 0;

            numLateHTLLCRequests = 0;
        }

        private long getNumUglyHTLLCRequests() {
            return numTotalHTLLCRequests - numGoodHTLLCRequests - numBadHTLLCRequests;
        }
    }

    public HTLLCRequestProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache());
    }

    public HTLLCRequestProfilingCapability(final DirectoryController llc) {
        this.llc = llc;

        this.llcLineBroughterThreadIds = new HashMap<Integer, Map<Integer, Integer>>();
        for (int set = 0; set < this.llc.getCache().getNumSets(); set++) {
            HashMap<Integer, Integer> llcLineBroughterThreadIdsPerSet = new HashMap<Integer, Integer>();
            this.llcLineBroughterThreadIds.put(set, llcLineBroughterThreadIdsPerSet);

            for (int way = 0; way < this.llc.getCache().getAssociativity(); way++) {
                llcLineBroughterThreadIdsPerSet.put(way, -1);
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

                return new HTLLCRequestVictimCacheLineStateValueProvider(set, way);
            }
        };

        this.htLLCRequestVictimCache = new EvictableCache<HTLLCRequestVictimCacheLineState>(llc, llc.getName() + ".htLLCRequestVictimCache", llc.getCache().getGeometry(), LRUPolicy.class, cacheLineStateProviderFactory);

        this.eventDispatcher = new BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if(printTrace) {
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

                    boolean lineFoundIsHT = HTLLCRequestProfilingCapability.this.getLLCLineBroughterThreadId(set, event.getWay()) == BasicThread.getHelperThreadId();

                    handleRequest(event, requesterIsHT, set, event.getWay(), lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(LastLevelCacheLineFillEvent.class, new Action1<LastLevelCacheLineFillEvent>() {
            @Override
            public void apply(LastLevelCacheLineFillEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if(printTrace) {
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
                    boolean lineFoundIsHT = HTLLCRequestProfilingCapability.this.getLLCLineBroughterThreadId(set, event.getWay()) == BasicThread.getHelperThreadId();

                    handleLineFill(event, requesterIsHT, set, event.getWay(), lineFoundIsHT);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent.class, new Action1<CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent>() {
            @Override
            public void apply(CoherentCacheLastPutSOrPutMAndDataFromOwnerEvent event) {
                if (event.getCacheController().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    if(printTrace) {
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

                    boolean lineFoundIsHT = HTLLCRequestProfilingCapability.this.getLLCLineBroughterThreadId(set, event.getWay()) == BasicThread.getHelperThreadId();

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
                numMTLLCMisses = 0;

                numTotalHTLLCRequests = 0;

                numUsefulHTLLCRequests = 0;

                numGoodHTLLCRequests = 0;
                numBadHTLLCRequests = 0;

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
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        if(this.numMTLLCMisses > 0) {
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numMTLLCMisses", String.valueOf(this.numMTLLCMisses));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numTotalHTLLCRequests", String.valueOf(this.numTotalHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUsefulHTLLCRequests", String.valueOf(this.numUsefulHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestAccuracy", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / this.numTotalHTLLCRequests) + "%");
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestCoverage", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / (this.numMTLLCMisses + this.numUsefulHTLLCRequests)) + "%");

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numGoodHTLLCRequests", String.valueOf(this.numGoodHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numBadHTLLCRequests", String.valueOf(this.numBadHTLLCRequests));
            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUglyHTLLCRequests", String.valueOf(this.numTotalHTLLCRequests - this.numGoodHTLLCRequests - this.numBadHTLLCRequests));

            stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numLateHTLLCRequests", String.valueOf(this.numLateHTLLCRequests));
        }
    }

    public void dumpStats() {
        if(this.numMTLLCMisses > 0) {
            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numMTLLCMisses: %s\n", String.valueOf(this.numMTLLCMisses));

            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numTotalHTLLCRequests: %s\n", String.valueOf(this.numTotalHTLLCRequests));

            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUsefulHTLLCRequests: %s\n", String.valueOf(this.numUsefulHTLLCRequests));

            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestAccuracy: %s\n", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / this.numTotalHTLLCRequests) + "%");
            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".htLLCRequestCoverage: %s\n", String.valueOf(100.0 * (double) this.numUsefulHTLLCRequests / (this.numMTLLCMisses + this.numUsefulHTLLCRequests)) + "%");

            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numGoodHTLLCRequests: %s\n", String.valueOf(this.numGoodHTLLCRequests));
            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numBadHTLLCRequests: %s\n", String.valueOf(this.numBadHTLLCRequests));
            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUglyHTLLCRequests: %s\n", String.valueOf(this.numTotalHTLLCRequests - this.numGoodHTLLCRequests - this.numBadHTLLCRequests));

            System.out.printf("llcHTRequestProfilingCapability." + this.llc.getName() + ".numLateHTLLCRequests: %s\n", String.valueOf(this.numLateHTLLCRequests));
        }
    }

    private void markLateHTRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        int set = event.getSet();
        boolean requesterIsHT = BasicThread.isHelperThread(event.getAccess().getThread());
        boolean lineFoundIsHT = this.getLLCLineBroughterThreadId(set, event.getWay()) == BasicThread.getHelperThreadId();

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

    private void handleRequest(CoherentCacheServiceNonblockingRequestEvent event, boolean requesterIsHT, int set, int llcWay, boolean lineFoundIsHT) {
        checkInvariants(set);

        boolean mtHit = event.isHitInCache() && !requesterIsHT && !lineFoundIsHT;
        boolean htHit = event.isHitInCache() && !requesterIsHT && lineFoundIsHT;

        CacheLine<HTLLCRequestVictimCacheLineState> vtLine = this.findHTLLCRequestVictimLine(event.getTag());

        boolean vtHit = !requesterIsHT && vtLine != null;

        if (!event.isHitInCache()) {
            if (requesterIsHT) {
                this.numTotalHTLLCRequests++;
                this.cacheSetStats.get(set).numTotalHTLLCRequests++;

                if(printTrace) {
                    pw.printf(
                            "[%d] llc.[%d,%d] {%s} %s numTotalHTLLCRequests++ = %d, numUglyHTLLCRequests = %d\n",
                            HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                            event.getSet(),
                            event.getWay(),
                            llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                            (llc.getCache().getLine(event.getSet(), event.getWay()).isValid() ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                            this.cacheSetStats.get(set).numTotalHTLLCRequests,
                            this.cacheSetStats.get(set).getNumUglyHTLLCRequests()
                    );
                    pw.flush();
                }

                this.eventDispatcher.dispatch(new HTLLCRequestEvent());
            }
        }

        if (!requesterIsHT) {
            if (!event.isHitInCache()) {
                this.numMTLLCMisses++;
                this.cacheSetStats.get(set).numMTLLCMisses++;
            } else if (event.isHitInCache() && lineFoundIsHT) {
                this.numUsefulHTLLCRequests++;
                this.cacheSetStats.get(set).numUsefulHTLLCRequests++;
            }
        }

        if (!mtHit && !htHit && vtHit) {
            this.numBadHTLLCRequests++;
            this.cacheSetStats.get(set).numBadHTLLCRequests++;

            if(printTrace) {
                pw.printf(
                        "[%d] llc.[%d,%d] {%s} %s numBadHTLLCRequests++ = %d, numUglyHTLLCRequests = %d (mtHit: %s, htHit: %s, vtHit: %s)\n",
                        HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                        event.getSet(),
                        event.getWay(),
                        llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                        (llc.getCache().getLine(event.getSet(), event.getWay()).isValid() ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                        this.cacheSetStats.get(set).numBadHTLLCRequests,
                        this.cacheSetStats.get(set).getNumUglyHTLLCRequests(),
                        mtHit, htHit, vtHit
                );
                pw.flush();
            }

            this.eventDispatcher.dispatch(new BadHTLLCRequestEvent());
            this.setLRU(set, vtLine.getWay());
        } else if (!mtHit && htHit && !vtHit) {
            this.markMT(set, llcWay);

            this.numGoodHTLLCRequests++;
            this.cacheSetStats.get(set).numGoodHTLLCRequests++;

            if(printTrace) {
                pw.printf(
                        "[%d] llc.[%d,%d] {%s} %s numGoodHTLLCRequests++ = %d, numUglyHTLLCRequests = %d (mtHit: %s, htHit: %s, vtHit: %s)\n",
                        HTLLCRequestProfilingCapability.this.llc.getCycleAccurateEventQueue().getCurrentCycle(),
                        event.getSet(),
                        event.getWay(),
                        llc.getCache().getLine(event.getSet(), event.getWay()).getState(),
                        (llc.getCache().getLine(event.getSet(), event.getWay()).isValid() ? String.format("0x%08x", llc.getCache().getLine(event.getSet(), event.getWay()).getTag()) : "N/A"),
                        this.cacheSetStats.get(set).numGoodHTLLCRequests,
                        this.cacheSetStats.get(set).getNumUglyHTLLCRequests(),
                        mtHit, htHit, vtHit
                );
                pw.flush();
            }

            this.removeLRU(set);
        } else if (!mtHit && htHit && vtHit) {
            this.markMT(set, llcWay);
            this.setLRU(set, vtLine.getWay());
            this.removeLRU(set);
        } else if (mtHit && vtHit) {
            this.setLRU(set, vtLine.getWay());
        }

        checkInvariants(set);
    }

    private void handleLineFill(LastLevelCacheLineFillEvent event, boolean requesterIsHT, int set, int llcWay, boolean lineFoundIsHT) {
        checkInvariants(set);

        int victimTag = event.getVictimTag();

        if (requesterIsHT && !event.isEviction()) {
            // case 1
            this.markHT(set, llcWay);
            this.insertNullEntry(set);
        } else {
            if (requesterIsHT && event.isEviction() && !lineFoundIsHT) {
                // case 2
                this.markHT(set, llcWay);
                this.insertDataEntry(set, victimTag);
            } else if (requesterIsHT && event.isEviction() && lineFoundIsHT) {
                // case 3
            } else if (!requesterIsHT && event.isEviction() && lineFoundIsHT) {
                // case 4
                this.markMT(set, llcWay);
                this.removeLRU(set);
            } else if (!requesterIsHT && event.isEviction() && !lineFoundIsHT) {
                //case 5
                boolean htLLCRequestFound = false;

                for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
                    if (this.htLLCRequestVictimCache.getLine(set, way).getState() != HTLLCRequestVictimCacheLineState.INVALID) {
                        htLLCRequestFound = true;
                        break;
                    }
                }

                if (htLLCRequestFound) {
                    this.removeLRU(set);
                    this.insertDataEntry(set, victimTag);
                }
            }
        }

        checkInvariants(set);
    }

    private void checkInvariants(int set) {
        int numHTLinesInLLC = 0;
        int numNonHTLinesInLLC = 0;
        int numVictimEntriesInVictimCache = 0;

        for(CacheLine<DirectoryControllerState> llcLine : this.llc.getCache().getLines(set)) {
            int llcLineBroughterThreadId = getLLCLineBroughterThreadId(set, llcLine.getWay());
            if(llcLineBroughterThreadId == BasicThread.getHelperThreadId()) {
                numHTLinesInLLC++;
            }
            else {
                numNonHTLinesInLLC++;
            }
        }

        for(CacheLine<HTLLCRequestVictimCacheLineState> victimEntry : this.htLLCRequestVictimCache.getLines(set)) {
            if(victimEntry.getState() != HTLLCRequestVictimCacheLineState.INVALID) {
                numVictimEntriesInVictimCache++;
            }
        }

        if(numHTLinesInLLC != numVictimEntriesInVictimCache) {
            throw new IllegalArgumentException();
        }

        if(numVictimEntriesInVictimCache + numNonHTLinesInLLC > this.llc.getCache().getAssociativity()) {
            throw new IllegalArgumentException();
        }
    }

    private void markInvalid(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, -1);
    }

    private void markHT(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getHelperThreadId());
    }

    private void markMT(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getMainThreadId());
    }

    public int getLLCLineBroughterThreadId(int set, int way) {
        return this.llcLineBroughterThreadIds.get(set).get(way);
    }

    private void setLLCLineBroughterThreadId(int set, int way, int llcLineBroughterThreadId) {
        if(printTrace) {
            pw.printf("[%d] llcLineBroughterThreadIds[%d,%d] = %d\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way, llcLineBroughterThreadId);
            pw.flush();
        }
        this.llcLineBroughterThreadIds.get(set).put(way, llcLineBroughterThreadId);
    }

    private void insertDataEntry(int set, int tag) {
        if(tag == CacheLine.INVALID_TAG) {
            throw new IllegalArgumentException();
        }

        CacheAccess<HTLLCRequestVictimCacheLineState> newMiss = this.findInvalidLineAndNewMiss(tag, set);
        CacheLine<HTLLCRequestVictimCacheLineState> line = newMiss.getLine();
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.setState(HTLLCRequestVictimCacheLineState.DATA);
        line.setTag(tag);
        if(printTrace) {
            pw.printf("[%d] htLLCRequestVictimCache[%d,%d]: insertDataEntry(0x%08x)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(), tag);
            pw.flush();
        }
        htLLCRequestVictimCache.getEvictionPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

    private void insertNullEntry(int set) {
        CacheAccess<HTLLCRequestVictimCacheLineState> newMiss = this.findInvalidLineAndNewMiss(0, set);
        CacheLine<HTLLCRequestVictimCacheLineState> line = newMiss.getLine();
        HTLLCRequestVictimCacheLineStateValueProvider stateProvider = (HTLLCRequestVictimCacheLineStateValueProvider) line.getStateProvider();
        stateProvider.setState(HTLLCRequestVictimCacheLineState.NULL);
        line.setTag(CacheLine.INVALID_TAG);
        if(printTrace) {
            pw.printf("[%d] htLLCRequestVictimCache[%d,%d]: insertNullEntry(%s)\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, line.getWay(), "N/A");
            pw.flush();
        }
        htLLCRequestVictimCache.getEvictionPolicy().handleInsertionOnMiss(set, newMiss.getWay());
    }

    private void setLRU(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
        if(printTrace) {
            pw.printf("[%d] htLLCRequestVictimCache[%d,%d]: setLRU\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way);
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
                stateProvider.setState(HTLLCRequestVictimCacheLineState.INVALID);
                line.setTag(CacheLine.INVALID_TAG);
                if(printTrace) {
                    pw.printf("[%d] htLLCRequestVictimCache[%d,%d]: removeLRU\n", llc.getCycleAccurateEventQueue().getCurrentCycle(), set, way);
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

    private CacheAccess<HTLLCRequestVictimCacheLineState> findInvalidLineAndNewMiss(int address, int set) {
        int tag = this.htLLCRequestVictimCache.getTag(address);

        for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheAccess<HTLLCRequestVictimCacheLineState>(this.htLLCRequestVictimCache, null, set, way, tag);
            }
        }

        throw new IllegalArgumentException();
    }

    private CacheLine<HTLLCRequestVictimCacheLineState> findHTLLCRequestVictimLine(int tag) {
        return this.htLLCRequestVictimCache.findLine(tag);
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
        private final int set;
        private final int way;
        private HTLLCRequestVictimCacheLineState state;

        public HTLLCRequestVictimCacheLineStateValueProvider(int set, int way) {
            this.set = set;
            this.way = way;
            this.state = HTLLCRequestVictimCacheLineState.INVALID;
        }

        @Override
        public HTLLCRequestVictimCacheLineState get() {
            return state;
        }

        public HTLLCRequestVictimCacheLineState getState() {
            return state;
        }

        public void setState(HTLLCRequestVictimCacheLineState state) {
            this.state = state;
        }

        @Override
        public HTLLCRequestVictimCacheLineState getInitialValue() {
            return HTLLCRequestVictimCacheLineState.INVALID;
        }

        public int getSet() {
            return set;
        }

        public int getWay() {
            return way;
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
