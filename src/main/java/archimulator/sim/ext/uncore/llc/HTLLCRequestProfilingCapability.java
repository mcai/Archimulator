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

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.experiment.capability.SimulationCapability;
import archimulator.sim.base.simulation.Simulation;
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

import java.util.HashMap;
import java.util.Map;

public class HTLLCRequestProfilingCapability implements SimulationCapability {
    private EvictableCache<?, ?> llc;

    private Map<Integer, Map<Integer, Integer>> llcLineBroughterThreadIds;
    private EvictableCache<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> htLLCRequestVictimCache;

    private long numMTLLCMisses;

    private long numTotalHTLLCRequests;

    private long numUsefulHTLLCRequests;

    private long numGoodHTLLCRequests;
    private long numBadHTLLCRequests;

    private long numLateHTLLCRequests;

    private BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent> eventDispatcher;

    public HTLLCRequestProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache());
    }

    public HTLLCRequestProfilingCapability(EvictableCache<?, ?> llc) {
        this.llc = llc;

        this.llcLineBroughterThreadIds = new HashMap<Integer, Map<Integer, Integer>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, Integer> llcLineBroughterThreadIdsPerSet = new HashMap<Integer, Integer>();
            this.llcLineBroughterThreadIds.put(set, llcLineBroughterThreadIdsPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                llcLineBroughterThreadIdsPerSet.put(way, -1);
            }
        }

        this.htLLCRequestVictimCache = new EvictableCache<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>>(this.llc, "", this.llc.getGeometry(), LRUPolicy.class, new Function3<Cache<?, ?>, Integer, Integer, CacheLine<HTLLCRequestVictimCacheLineState>>() {
            public CacheLine<HTLLCRequestVictimCacheLineState> apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new CacheLine<HTLLCRequestVictimCacheLineState>(cache, set, way, HTLLCRequestVictimCacheLineState.INVALID);
            }
        });

        this.eventDispatcher = new BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(HTLLCRequestProfilingCapability.this.llc)) {
                    handleServicingRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(CoherentCacheNonblockingRequestHitToTransientTagEvent.class, new Action1<CoherentCacheNonblockingRequestHitToTransientTagEvent>() {
            @SuppressWarnings("Unchecked")
            public void apply(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
                if (event.getCache().getCache().equals(HTLLCRequestProfilingCapability.this.llc)) {
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

    private void markLateHTRequest(CoherentCacheNonblockingRequestHitToTransientTagEvent event) {
        boolean requesterIsHT = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        boolean lineFoundIsHT = this.getLLCLineBroughterThreadId(set, llcLine.getWay()) == BasicThread.getHelperThreadId();

        if (!requesterIsHT && lineFoundIsHT) {
            this.numLateHTLLCRequests++;
            this.eventDispatcher.dispatch(new LateHTLLCRequestEvent());
        }
    }

    private void handleServicingRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHT = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> llcLine = event.getLineFound();

        int set = llcLine.getSet();

        boolean lineFoundIsHT = this.getLLCLineBroughterThreadId(set, llcLine.getWay()) == BasicThread.getHelperThreadId();

        if (!event.isHitInCache()) {
            if (requesterIsHT) {
                this.numTotalHTLLCRequests++;
                this.eventDispatcher.dispatch(new HTLLCRequestEvent());
            }
        }

        if (!requesterIsHT) {
            if (!event.isHitInCache()) {
                this.numMTLLCMisses++;
            } else if (event.isHitInCache() && lineFoundIsHT) {
                this.numUsefulHTLLCRequests++;
            }
        }

        if (requesterIsHT && !event.isHitInCache() && !event.isEviction()) {
            this.markHT(set, llcLine.getWay());
            this.insertNullEntry(set);
        } else if (requesterIsHT && !event.isHitInCache() && event.isEviction() && !lineFoundIsHT) {
            this.markHT(set, llcLine.getWay());
            this.insertDataEntry(set, llcLine.getTag());
        } else if (requesterIsHT && !event.isHitInCache() && event.isEviction() && lineFoundIsHT) {
        } else if (!requesterIsHT && !event.isHitInCache() && event.isEviction() && lineFoundIsHT) {
            this.markMT(set, llcLine.getWay());
            this.removeLRU(set);
        } else if (!requesterIsHT && !lineFoundIsHT) {
            boolean htLLCRequestFound = false;

            for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
                if (this.htLLCRequestVictimCache.getLine(set, way).getState() != HTLLCRequestVictimCacheLineState.INVALID) {
                    htLLCRequestFound = true;
                    break;
                }
            }

            if (htLLCRequestFound) {
                this.removeLRU(set);
                this.insertDataEntry(set, llcLine.getTag());
            }
        }

        boolean mtHit = event.isHitInCache() && !requesterIsHT && !lineFoundIsHT;
        boolean htHit = event.isHitInCache() && !requesterIsHT && lineFoundIsHT;

        CacheLine<HTLLCRequestVictimCacheLineState> vtLine = this.findHTLLCRequestVictimLine(this.llc.getTag(event.getAddress()));

        boolean vtHit = !requesterIsHT && vtLine != null;

        if (!mtHit && !htHit && vtHit) {
            this.numBadHTLLCRequests++;
            this.eventDispatcher.dispatch(new BadHTLLCRequestEvent());
            this.setLRU(set, vtLine.getWay());
        } else if (!mtHit && htHit && !vtHit) {
            this.markMT(set, llcLine.getWay());
            this.numGoodHTLLCRequests++;
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
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getHelperThreadId());
    }

    private void markMT(int set, int way) {
        this.setLLCLineBroughterThreadId(set, way, BasicThread.getMainThreadId());
    }

    public int getLLCLineBroughterThreadId(int set, int way) {
        return this.llcLineBroughterThreadIds.get(set).get(way);
    }

    private void setLLCLineBroughterThreadId(int set, int way, int llcLineBroughterThreadId) {
        this.llcLineBroughterThreadIds.get(set).put(way, llcLineBroughterThreadId);
    }

    private void insertDataEntry(int set, int tag) {
        CacheMiss<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(tag, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(HTLLCRequestVictimCacheLineState.DATA);
        newMiss.commit();
    }

    private void insertNullEntry(int set) {
        CacheMiss<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> newMiss = this.findInvalidLineAndNewMiss(0, CacheAccessType.UNKNOWN, set);
        newMiss.getLine().setNonInitialState(HTLLCRequestVictimCacheLineState.NULL);
        newMiss.commit();
    }

    private void setLRU(int set, int way) {
        this.getLruPolicyForHtRequestVictimCache().setLRU(set, way);
    }

    private void removeLRU(int set) {
        LRUPolicy<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> lru = this.getLruPolicyForHtRequestVictimCache();

        for (int i = this.llc.getAssociativity() - 1; i >= 0; i--) {
            int way = lru.getWayInStackPosition(set, i);
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            if (!line.getState().equals(HTLLCRequestVictimCacheLineState.INVALID)) {
                line.invalidate();
                return;
            }
        }

        throw new IllegalArgumentException();
    }

    private LRUPolicy<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> getLruPolicyForHtRequestVictimCache() {
        return (LRUPolicy<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>>) this.htLLCRequestVictimCache.getEvictionPolicy();
    }

    private CacheMiss<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>> findInvalidLineAndNewMiss(int address, CacheAccessType accessType, int set) {
        CacheReference reference = new CacheReference(null, null, address, this.htLLCRequestVictimCache.getTag(address), accessType, set);

        for (int way = 0; way < this.htLLCRequestVictimCache.getAssociativity(); way++) {
            CacheLine<HTLLCRequestVictimCacheLineState> line = this.htLLCRequestVictimCache.getLine(set, way);
            if (line.getState() == line.getInitialState()) {
                return new CacheMiss<HTLLCRequestVictimCacheLineState, CacheLine<HTLLCRequestVictimCacheLineState>>(this.htLLCRequestVictimCache, reference, way);
            }
        }

        throw new IllegalArgumentException();
    }

    private CacheLine<HTLLCRequestVictimCacheLineState> findHTLLCRequestVictimLine(int tag) {
        return this.htLLCRequestVictimCache.findLine(tag).getLine();
    }

    public BlockingEventDispatcher<HTLLCRequestProfilingCapabilityEvent> getEventDispatcher() {
        return eventDispatcher;
    }

    public static enum HTLLCRequestVictimCacheLineState {
        INVALID,
        NULL,
        DATA
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