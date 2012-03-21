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

import archimulator.sim.base.event.*;
import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.cache.*;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheNonblockingRequestHitToTransientTagEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LLCHTRequestProfilingHelper<StateT extends Serializable, LineT extends CacheLine<StateT>> {
    private EvictableCache<StateT, LineT> llc;

    private Map<Integer, Map<Integer, LLCLineHTRequestState>> htRequestStates;
    private EvictableCache<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> htRequestVictimCache;

    private long numMtMisses;

    private long numTotalHtRequests;

    private long numUsefulHtRequests;

    private long numGoodHtRequests;
    private long numBadHtRequests;

    private long numLateHtRequests;
    
    private BlockingEventDispatcher<LLCHTRequestProfilingHelperEvent> eventDispatcher;

    public LLCHTRequestProfilingHelper(EvictableCache<StateT, LineT> llc) {
        this.llc = llc;

        this.htRequestStates = new HashMap<Integer, Map<Integer, LLCLineHTRequestState>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, LLCLineHTRequestState> htRequestStatesPerSet = new HashMap<Integer, LLCLineHTRequestState>();
            this.htRequestStates.put(set, htRequestStatesPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                htRequestStatesPerSet.put(way, LLCLineHTRequestState.INVALID);
            }
        }

        this.htRequestVictimCache = new EvictableCache<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>>(this.llc, "", this.llc.getGeometry(), LRUPolicy.class, new Function3<Cache<?, ?>, Integer, Integer, CacheLine<HTRequestVictimCacheLineState>>() {
            public CacheLine<HTRequestVictimCacheLineState> apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new CacheLine<HTRequestVictimCacheLineState>(cache, set, way, HTRequestVictimCacheLineState.INVALID);
            }
        });

        this.eventDispatcher = new BlockingEventDispatcher<LLCHTRequestProfilingHelperEvent>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(LLCHTRequestProfilingHelper.this.llc)) {
                    serviceRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCache().getCache().equals(LLCHTRequestProfilingHelper.this.llc)) {
                    markLateHTRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                numMtMisses = 0;

                numTotalHtRequests = 0;

                numUsefulHtRequests = 0;

                numGoodHtRequests = 0;
                numBadHtRequests = 0;

                numLateHtRequests = 0;
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
        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numMtMisses", String.valueOf(this.numMtMisses));

        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numTotalHtRequests", String.valueOf(this.numTotalHtRequests));

        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numUsefulHtRequests", String.valueOf(this.numUsefulHtRequests));

        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".htRequestAccuracy", String.valueOf(100.0 * (double) this.numUsefulHtRequests / this.numTotalHtRequests) + "%");
        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".htRequestCoverage", String.valueOf(100.0 * (double) this.numUsefulHtRequests / (this.numMtMisses + this.numUsefulHtRequests)) + "%");

        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numGoodHtRequests", String.valueOf(this.numGoodHtRequests));
        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numBadHtRequests", String.valueOf(this.numBadHtRequests));
        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numUglyHtRequests", String.valueOf(this.numTotalHtRequests - this.numGoodHtRequests - this.numBadHtRequests));

        stats.put("llcHTRequestProfilingHelper." + this.llc.getName() + ".numLateHtRequests", String.valueOf(this.numLateHtRequests));
    }

    private void markLateHTRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LLCLineHTRequestState htRequestState = this.getHTRequestState(set, llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LLCLineHTRequestState.HT;

        if (!requesterIsHt && lineFoundIsHt) {
            this.numLateHtRequests++;
        }
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        LLCLineHTRequestState htRequestState = this.getHTRequestState(set, llcLine.getWay());
        boolean lineFoundIsHt = htRequestState == LLCLineHTRequestState.HT;

        if (!event.isHitInCache()) {
            if (requesterIsHt) {
                this.numTotalHtRequests++;
                this.eventDispatcher.dispatch(new HTRequestEvent());
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
            this.markHT(set, llcLine.getWay());
            this.insertNullEntry(set);
        } else if (requesterIsHt && !event.isHitInCache() && event.isEviction() && !lineFoundIsHt) {
            this.markHT(set, llcLine.getWay());
            this.insertDataEntry(set, llcLine.getTag());
        } else if (requesterIsHt && !event.isHitInCache() && event.isEviction() && lineFoundIsHt) {
        } else if (!requesterIsHt && !event.isHitInCache() && event.isEviction() && lineFoundIsHt) {
            this.markMT(set, llcLine.getWay());
            this.removeLRU(set);
        } else if (!requesterIsHt && !lineFoundIsHt) {
            boolean htRequestFound = false;

            for (int way = 0; way < this.htRequestVictimCache.getAssociativity(); way++) {
                if (this.htRequestVictimCache.getLine(set, way).getState() != HTRequestVictimCacheLineState.INVALID) {
                    htRequestFound = true;
                    break;
                }
            }

            if (htRequestFound) {
                this.removeLRU(set);
                this.insertDataEntry(set, llcLine.getTag());
            }
        }

        boolean mtHit = event.isHitInCache() && !requesterIsHt && !lineFoundIsHt;
        boolean htHit = event.isHitInCache() && !requesterIsHt && lineFoundIsHt;

        CacheLine<HTRequestVictimCacheLineState> vtLine = this.findHtRequestVictimLine(this.llc.getTag(event.getAddress()));

        boolean vtHit = !requesterIsHt && vtLine != null;

        if (!mtHit && !htHit && vtHit) {
            this.numBadHtRequests++;
            this.eventDispatcher.dispatch(new BadHTRequestEvent());
            this.setLRU(set, vtLine.getWay());
        } else if (!mtHit && htHit && !vtHit) {
            this.markMT(set, llcLine.getWay());
            this.numGoodHtRequests++;
            this.removeLRU(set);
        } else if (!mtHit && htHit && vtHit) {
            this.markMT(set, llcLine.getWay());
            this.setLRU(set, vtLine.getWay());
            this.removeLRU(set);
        } else if (mtHit && vtHit) {
            this.setLRU(set, vtLine.getWay());
        }
    }

    private void markHT(int set, int way) {
        if (this.getHTRequestState(set, way).equals(LLCLineHTRequestState.HT)) {
            throw new IllegalArgumentException();
        }

        this.setHTRequestState(set, way, LLCLineHTRequestState.HT);
    }

    private void markMT(int set, int way) {
        if (this.getHTRequestState(set, way).equals(LLCLineHTRequestState.MT)) {
            throw new IllegalArgumentException();
        }

        this.setHTRequestState(set, way, LLCLineHTRequestState.MT);
    }
    
    public LLCLineHTRequestState getHTRequestState(int set, int way) {
        return this.htRequestStates.get(set).get(way);
    }
    
    private void setHTRequestState(int set, int way, LLCLineHTRequestState htRequestState) {
        this.htRequestStates.get(set).put(way, htRequestState);
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

    private void setLRU(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
    }

    private void removeLRU(int set) {
        LRUPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> lru = this.getLruPolicyForHtRequestVictimCache();

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

    private LRUPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>> getLruPolicyForHtRequestVictimCache() {
        return (LRUPolicy<HTRequestVictimCacheLineState, CacheLine<HTRequestVictimCacheLineState>>) this.htRequestVictimCache.getEvictionPolicy();
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

    private CacheLine<HTRequestVictimCacheLineState> findHtRequestVictimLine(int tag) {
        return this.htRequestVictimCache.findLine(tag);
    }

    public BlockingEventDispatcher<LLCHTRequestProfilingHelperEvent> getEventDispatcher() {
        return eventDispatcher;
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
    
    public abstract class LLCHTRequestProfilingHelperEvent implements BlockingEvent {
    }
    
    public class HTRequestEvent extends LLCHTRequestProfilingHelperEvent {
    }
    
    public class BadHTRequestEvent extends LLCHTRequestProfilingHelperEvent {
    }
}