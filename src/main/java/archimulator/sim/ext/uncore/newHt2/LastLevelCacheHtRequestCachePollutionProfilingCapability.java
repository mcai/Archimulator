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
package archimulator.sim.ext.uncore.newHt2;

import archimulator.model.simulation.Simulation;
import archimulator.model.capability.SimulationCapability;
import archimulator.model.event.*;
import archimulator.sim.core.BasicThread;
import archimulator.sim.ext.uncore.newHt2.state.*;
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
public class LastLevelCacheHtRequestCachePollutionProfilingCapability implements SimulationCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private Map<Integer, Map<Integer, LastLevelCacheLineHtRequestState>> htRequestStates;
    private EvictableCache<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> htRequestVictimCache;

    private long mtMisses;

    private long totalHtRequests;

    private long usefulHtRequests;

    private long goodHtRequests;
    private long badHtRequests;

    private long lateHtRequests;

    private PrintWriter fileWriter;
    private Simulation simulation;

    public LastLevelCacheHtRequestCachePollutionProfilingCapability(final Simulation simulation) {
        this.simulation = simulation;
        this.llc = simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache();

        this.htRequestStates = new HashMap<Integer, Map<Integer, LastLevelCacheLineHtRequestState>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, LastLevelCacheLineHtRequestState> htRequestStatesPerSet = new HashMap<Integer, LastLevelCacheLineHtRequestState>();
            this.htRequestStates.put(set, htRequestStatesPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                htRequestStatesPerSet.put(way, LastLevelCacheLineHtRequestState.INVALID);
            }
        }

        this.htRequestVictimCache = new EvictableCache<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>>(this.llc, "", this.llc.getGeometry(), LeastRecentlyUsedEvictionPolicy.FACTORY, new Function3<Cache<?, ?>, Integer, Integer, CacheLine<HtRequestVictimCacheLineState>>() {
            public CacheLine<HtRequestVictimCacheLineState> apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new CacheLine<HtRequestVictimCacheLineState>(cache, set, way, new InvalidHtRequestVictimCacheLineState());
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
                    serviceRequest(event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCache().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
                    markLateHtRequest(event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                mtMisses = 0;

                totalHtRequests = 0;

                usefulHtRequests = 0;

                goodHtRequests = 0;
                badHtRequests = 0;

                lateHtRequests = 0;
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
                    fileWriter = new PrintWriter(simulation.getConfig().getCwd() + "/lastLevelCacheHtRequestCachePollutionProfilingCapability_out.txt");
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
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".mtMisses", String.valueOf(this.mtMisses));

        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".totalHtRequests", String.valueOf(this.totalHtRequests));

        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".usefulHtRequests", String.valueOf(this.usefulHtRequests));

        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".ht_accuracy", String.valueOf(100.0 * (double) this.usefulHtRequests / this.totalHtRequests) + "%");
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".ht_coverage", String.valueOf(100.0 * (double) this.usefulHtRequests / this.mtMisses) + "%");

        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".goodHtRequests", String.valueOf(this.goodHtRequests));
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".badHtRequests", String.valueOf(this.badHtRequests));
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".uglyHtRequests", String.valueOf(this.totalHtRequests - this.goodHtRequests - this.badHtRequests));

        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".lateHtRequests", String.valueOf(this.lateHtRequests));
    }

    private void markLateHtRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LastLevelCacheLineHtRequestState htRequestState = this.htRequestStates.get(set).get(llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LastLevelCacheLineHtRequestState.HT;

        if (!requesterIsHt && lineFoundIsHt) {
            this.lateHtRequests++;
        }
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LastLevelCacheLineHtRequestState htRequestState = this.htRequestStates.get(set).get(llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LastLevelCacheLineHtRequestState.HT;

        if (!event.isHitInCache()) {
            if (requesterIsHt) {
//                this.fileWriter.printf("[%d] htRequest: %s\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);

//                this.setHt(set, llcLine.getWay());
                this.totalHtRequests++;
            } else {
//                this.setMt(set, llcLine.getWay());
            }
        }

        if (!requesterIsHt) {
            if (!event.isHitInCache()) {
                this.mtMisses++;
            } else if (event.isHitInCache() && lineFoundIsHt) {
                this.mtMisses++;
            }
        }

        if (event.isHitInCache()) {
            if (!requesterIsHt && lineFoundIsHt) {
                this.usefulHtRequests++;
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
                if (!(this.htRequestVictimCache.getLine(set, way).getState() instanceof InvalidHtRequestVictimCacheLineState)) {
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

        CacheLine<HtRequestVictimCacheLineState> vtLine = this.findHtRequestVictimLine(this.llc.getTag(event.getAddress()));

        boolean vtHit = !requesterIsHt && vtLine != null;

        if (!mtHit && !htHit && vtHit) {
//            this.checkInvariants(set);

//            this.fileWriter.printf("[%d] \tbadHtRequest, tag: 0x%08x\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), vtLine.getTag());

            this.badHtRequests++;
            this.setLru(set, vtLine.getWay());

//            this.checkInvariants(set);
        } else if (!mtHit && htHit && !vtHit) {
//            this.checkInvariants(set);

//            this.fileWriter.printf("[%d] \tgoodHtRequest, tag: 0x%08x\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), event.getLineFound().getTag());

            this.setMt(set, llcLine.getWay());
            this.goodHtRequests++;
            this.removeLru(set);

//            this.checkInvariants(set);
        } else if (!mtHit && htHit && vtHit) {
//            this.checkInvariants(set);

            this.setMt(set, llcLine.getWay());
            this.setLru(set, vtLine.getWay());
            this.removeLru(set);

//            this.checkInvariants(set);
        } else if (mtHit && !htHit && vtHit) {
//            this.checkInvariants(set);

            this.setLru(set, vtLine.getWay());

//            this.checkInvariants(set);
        }
    }

    private void checkInvariants(int set) {
        int numhtRequestsInLlc = 0;
        int numVictimEntries = 0;

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            if (this.htRequestStates.get(set).get(way).equals(LastLevelCacheLineHtRequestState.HT)) {
                numhtRequestsInLlc++;
            }
        }

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            if (!this.htRequestVictimCache.getLine(set, way).getState().getType().equals(HtRequestVictimCacheLineStateType.INVALID)) {
                numVictimEntries++;
            }
        }

        if (numhtRequestsInLlc != numVictimEntries) {
            throw new IllegalArgumentException();
        }
    }

    private void setHt(int set, int way) {
        if (this.htRequestStates.get(set).get(way).equals(LastLevelCacheLineHtRequestState.HT)) {
            throw new IllegalArgumentException();
        }

        this.htRequestStates.get(set).put(way, LastLevelCacheLineHtRequestState.HT);
    }

    private void setMt(int set, int way) {
        if (this.htRequestStates.get(set).get(way).equals(LastLevelCacheLineHtRequestState.MT)) {
            throw new IllegalArgumentException();
        }

        this.htRequestStates.get(set).put(way, LastLevelCacheLineHtRequestState.MT);
    }

    private void insertDataEntry(int set, int tag) {
        CacheMiss<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(tag, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(new DataHtRequestVictimCacheLineState());
        newMiss.commit();
    }

    private void insertNullEntry(int set) {
        CacheMiss<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(0, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(new NullHtRequestVictimCacheLineState());
        newMiss.commit();
    }

    private void setLru(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
    }

    private void removeLru(int set) {
        LeastRecentlyUsedEvictionPolicy<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> lru = this.getLruPolicyForHtRequestVictimCache();

        for (int i = this.llc.getAssociativity() - 1; i >= 0; i--) {
            int way = lru.getWayInStackPosition(set, i);
            CacheLine<HtRequestVictimCacheLineState> line = this.htRequestVictimCache.getLine(set, way);
            HtRequestVictimCacheLineState state = line.getState();
            if (!state.getType().equals(HtRequestVictimCacheLineStateType.INVALID)) {
                line.invalidate();
                return;
            }
        }

        throw new IllegalArgumentException();
    }

    private LeastRecentlyUsedEvictionPolicy<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> getLruPolicyForHtRequestVictimCache() {
        return (LeastRecentlyUsedEvictionPolicy<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>>) this.htRequestVictimCache.getEvictionPolicy();
    }

    private CacheMiss<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>> findInvalidLineAndNewMiss(int address, CacheAccessType accessType, int set) {
        CacheReference reference = new CacheReference(null, null, address, this.htRequestVictimCache.getTag(address), accessType, set);

        for (int way = 0; way < this.htRequestVictimCache.getAssociativity(); way++) {
            CacheLine<HtRequestVictimCacheLineState> line = this.htRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheMiss<HtRequestVictimCacheLineState, CacheLine<HtRequestVictimCacheLineState>>(this.htRequestVictimCache, reference, way);
            }
        }

        throw new IllegalArgumentException();
    }

    public CacheLine<HtRequestVictimCacheLineState> findHtRequestVictimLine(int tag) {
        return this.htRequestVictimCache.findLine(tag);
    }
}