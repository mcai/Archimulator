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
package archimulator.sim.ext.uncore.llc;

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.event.*;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.sim.uncore.coherence.CoherentCache;
import archimulator.sim.uncore.coherence.MESIState;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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
public class LLCHTRequestProfilingCapability implements SimulationCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private Map<Integer, Map<Integer, LLCLineHTRequestState>> htRequestStates;
    private EvictableCache<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> htRequestVictimCache;

    private long numMtMisses;

    private long numTotalHtRequests;

    private long numUsefulHtRequests;

    private long numGoodHtRequests;
    private long numBadHtRequests;

    private long numLateHtRequests;

    private PrintWriter fileWriter;

    public LLCHTRequestProfilingCapability(final Simulation simulation) {
        this.llc = simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache();

        this.htRequestStates = new HashMap<Integer, Map<Integer, LLCLineHTRequestState>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, LLCLineHTRequestState> htRequestStatesPerSet = new HashMap<Integer, LLCLineHTRequestState>();
            this.htRequestStates.put(set, htRequestStatesPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                htRequestStatesPerSet.put(way, LLCLineHTRequestState.INVALID);
            }
        }

        this.htRequestVictimCache = new EvictableCache<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>>(this.llc, "", this.llc.getGeometry(), LeastRecentlyUsedEvictionPolicy.class, new Function3<Cache<?, ?>, Integer, Integer, CacheLine<HTRequestVictimCacheLineState>>() {
            public CacheLine<HTRequestVictimCacheLineState> apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new CacheLine<HTRequestVictimCacheLineState>(cache, set, way, HTRequestVictimCacheLineState.INVALID);
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == LLCHTRequestProfilingCapability.this.llc) {
                    serviceRequest(event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCache().getCache() == LLCHTRequestProfilingCapability.this.llc) {
                    markLateHtRequest(event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numMtMisses = 0;

                numTotalHtRequests = 0;

                numUsefulHtRequests = 0;

                numGoodHtRequests = 0;
                numBadHtRequests = 0;

                numLateHtRequests = 0;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        simulation.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(SimulationStartedEvent.class, new Action1<SimulationStartedEvent>() {
            public void apply(SimulationStartedEvent event) {
                try {
                    fileWriter = new PrintWriter(simulation.getConfig().getCwd() + "/llcHTRequestProfilingCapability_out.txt");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(SimulationStoppedEvent.class, new Action1<SimulationStoppedEvent>() {
            public void apply(SimulationStoppedEvent event) {
                fileWriter.flush();
                fileWriter.close();
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numMtMisses", String.valueOf(this.numMtMisses));

        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numTotalHtRequests", String.valueOf(this.numTotalHtRequests));

        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUsefulHtRequests", String.valueOf(this.numUsefulHtRequests));

        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".ht_accuracy", String.valueOf(100.0 * (double) this.numUsefulHtRequests / this.numTotalHtRequests) + "%");
        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".ht_coverage", String.valueOf(100.0 * (double) this.numUsefulHtRequests / (this.numMtMisses + this.numUsefulHtRequests)) + "%");

        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numGoodHtRequests", String.valueOf(this.numGoodHtRequests));
        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numBadHtRequests", String.valueOf(this.numBadHtRequests));
        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numUglyHtRequests", String.valueOf(this.numTotalHtRequests - this.numGoodHtRequests - this.numBadHtRequests));

        stats.put("llcHTRequestProfilingCapability." + this.llc.getName() + ".numLateHtRequests", String.valueOf(this.numLateHtRequests));
    }

    private void markLateHtRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LLCLineHTRequestState htRequestState = this.htRequestStates.get(set).get(llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LLCLineHTRequestState.HT;

        if (!requesterIsHt && lineFoundIsHt) {
            this.numLateHtRequests++;
        }
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LLCLineHTRequestState htRequestState = this.htRequestStates.get(set).get(llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LLCLineHTRequestState.HT;

        if (!event.isHitInCache()) {
            if (requesterIsHt) {
//                this.fileWriter.printf("[%d] htRequest: %s\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);

//                this.setHt(set, llcLine.getWay());
                this.numTotalHtRequests++;
            } else {
//                this.setMt(set, llcLine.getWay());
            }
        }

        if (!requesterIsHt) {
            if (!event.isHitInCache()) {
                this.numMtMisses++;
            } else if (event.isHitInCache() && lineFoundIsHt) {
                this.numUsefulHtRequests++;
            }
        }

        if (requesterIsHt && !event.isHitInCache() && !event.isEviction()) {
            // case 1
//            this.checkInvariants(set);

            this.setHt(set, llcLine.getWay());
            this.insertNullEntry(set);

//            this.checkInvariants(set);
        } else if (requesterIsHt && !event.isHitInCache() && event.isEviction() && !lineFoundIsHt) {
            // case 2
//            this.checkInvariants(set);

            this.setHt(set, llcLine.getWay());
            this.insertDataEntry(set, llcLine.getTag());

//            this.checkInvariants(set);
        } else if (requesterIsHt && !event.isHitInCache() && event.isEviction() && lineFoundIsHt) {
            // case 3

//            this.checkInvariants(set);
        } else if (!requesterIsHt && !event.isHitInCache() && event.isEviction() && lineFoundIsHt) {
            // case 4
//            this.checkInvariants(set);

            this.setMt(set, llcLine.getWay());
            this.removeLru(set);

//            this.checkInvariants(set);
        } else if (!requesterIsHt && !lineFoundIsHt) {
            //case 5
//            this.checkInvariants(set);
            boolean htRequestFound = false;

            for (int way = 0; way < this.htRequestVictimCache.getAssociativity(); way++) {
                if (this.htRequestVictimCache.getLine(set, way).getState() != HTRequestVictimCacheLineState.INVALID) {
                    htRequestFound = true;
                    break;
                }
            }

            if (htRequestFound) {
                this.removeLru(set);
                this.insertDataEntry(set, llcLine.getTag());
            }

//            this.checkInvariants(set);
        }

        boolean mtHit = event.isHitInCache() && !requesterIsHt && !lineFoundIsHt;
        boolean htHit = event.isHitInCache() && !requesterIsHt && lineFoundIsHt;

        CacheLine<HTRequestVictimCacheLineState> vtLine = this.findHtRequestVictimLine(this.llc.getTag(event.getAddress()));

        boolean vtHit = !requesterIsHt && vtLine != null;

        if (!mtHit && !htHit && vtHit) {
//            this.checkInvariants(set);

//            this.fileWriter.printf("[%d] \tbadHtRequest, tag: 0x%08x\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), vtLine.getTag());

            this.numBadHtRequests++;
            this.setLru(set, vtLine.getWay());

//            this.checkInvariants(set);
        } else if (!mtHit && htHit && !vtHit) {
//            this.checkInvariants(set);

//            this.fileWriter.printf("[%d] \tgoodHtRequest, tag: 0x%08x\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), event.getLineFound().getTag());

            this.setMt(set, llcLine.getWay());
            this.numGoodHtRequests++;
            this.removeLru(set);

//            this.checkInvariants(set);
        } else if (!mtHit && htHit && vtHit) {
//            this.checkInvariants(set);

            this.setMt(set, llcLine.getWay());
            this.setLru(set, vtLine.getWay());
            this.removeLru(set);

//            this.checkInvariants(set);
        } else if (mtHit && vtHit) {
//            this.checkInvariants(set);

            this.setLru(set, vtLine.getWay());

//            this.checkInvariants(set);
        }
    }

    private void checkInvariants(int set) {
        int numhtRequestsInLlc = 0;
        int numVictimEntries = 0;

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            if (this.htRequestStates.get(set).get(way) == LLCLineHTRequestState.HT) {
                numhtRequestsInLlc++;
            }
        }

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            if (this.htRequestVictimCache.getLine(set, way).getState() != HTRequestVictimCacheLineState.INVALID) {
                numVictimEntries++;
            }
        }

        if (numhtRequestsInLlc != numVictimEntries) {
            throw new IllegalArgumentException();
        }
    }

    private void setHt(int set, int way) {
        if (this.htRequestStates.get(set).get(way).equals(LLCLineHTRequestState.HT)) {
            throw new IllegalArgumentException();
        }

        this.htRequestStates.get(set).put(way, LLCLineHTRequestState.HT);
    }

    private void setMt(int set, int way) {
        if (this.htRequestStates.get(set).get(way).equals(LLCLineHTRequestState.MT)) {
            throw new IllegalArgumentException();
        }

        this.htRequestStates.get(set).put(way, LLCLineHTRequestState.MT);
    }

    private void insertDataEntry(int set, int tag) {
        CacheMiss<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(tag, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(HTRequestVictimCacheLineState.DATA);
        newMiss.commit();
    }

    private void insertNullEntry(int set) {
        CacheMiss<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(0, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(HTRequestVictimCacheLineState.NULL);
        newMiss.commit();
    }

    private void setLru(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
    }

    private void removeLru(int set) {
        LeastRecentlyUsedEvictionPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> lru = this.getLruPolicyForHtRequestVictimCache();

        for (int i = this.llc.getAssociativity() - 1; i >= 0; i--) {
            int way = lru.getWayInStackPosition(set, i);
            CacheLine<HTRequestVictimCacheLineState> line = this.htRequestVictimCache.getLine(set, way);
            if (!line.getState().equals(HTRequestVictimCacheLineState.INVALID)) {
                line.invalidate();
                return;
            }
        }

        throw new IllegalArgumentException();
    }

    private LeastRecentlyUsedEvictionPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> getLruPolicyForHtRequestVictimCache() {
        return (LeastRecentlyUsedEvictionPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>>) this.htRequestVictimCache.getEvictionPolicy();
    }

    private CacheMiss<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> findInvalidLineAndNewMiss(int address, CacheAccessType accessType, int set) {
        CacheReference reference = new CacheReference(null, null, address, this.htRequestVictimCache.getTag(address), accessType, set);

        for (int way = 0; way < this.htRequestVictimCache.getAssociativity(); way++) {
            CacheLine<HTRequestVictimCacheLineState> line = this.htRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheMiss<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>>(this.htRequestVictimCache, reference, way);
            }
        }

        throw new IllegalArgumentException();
    }

    public CacheLine<HTRequestVictimCacheLineState> findHtRequestVictimLine(int tag) {
        return this.htRequestVictimCache.findLine(tag);
    }

    public static enum LLCLineHTRequestState {
        INVALID,
        HT,
        MT
    }

    public static enum HTRequestVictimCacheLineState {
        INVALID,
        NULL,
        DATA
    }
}