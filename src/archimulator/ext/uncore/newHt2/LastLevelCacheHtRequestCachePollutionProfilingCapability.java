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
package archimulator.ext.uncore.newHt2;

import archimulator.core.BasicThread;
import archimulator.uncore.cache.Cache;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.eviction.LeastRecentlyUsedEvictionPolicy;
import archimulator.uncore.coherence.CoherentCache;
import archimulator.uncore.coherence.MESIState;
import archimulator.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.Simulation;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

import java.util.HashMap;
import java.util.Map;

public class LastLevelCacheHtRequestCachePollutionProfilingCapability implements SimulationCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private EvictableCache<HtRequestVictimCacheLineState, HtRequestVictimCacheLine> htRequestVictimCache;
    private Map<Integer, Map<Integer, LastLevelCacheLineHtRequestState>> htRequestStates;

    private long totalHtRequests;
    private long usedHtRequests;
    private long pollutingHtRequests;
    private long unusedHtRequests;

    public LastLevelCacheHtRequestCachePollutionProfilingCapability(final Simulation simulation) {
        this.llc = simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache();

        this.htRequestVictimCache = new EvictableCache<HtRequestVictimCacheLineState, HtRequestVictimCacheLine>(simulation.getProcessor(), this.llc.getName() + ".htRequestVictimCache", this.llc.getGeometry(), LeastRecentlyUsedEvictionPolicy.FACTORY, new Function3<Cache<?, ?>, Integer, Integer, HtRequestVictimCacheLine>() {
            public HtRequestVictimCacheLine apply(Cache<?, ?> cache, Integer set, Integer way) {
                return new HtRequestVictimCacheLine(cache, set, way);
            }
        });

        this.htRequestStates = new HashMap<Integer, Map<Integer, LastLevelCacheLineHtRequestState>>();
        for (int set = 0; set < this.llc.getNumSets(); set++) {
            HashMap<Integer, LastLevelCacheLineHtRequestState> htRequestStatesPerSet = new HashMap<Integer, LastLevelCacheLineHtRequestState>();
            this.htRequestStates.put(set, htRequestStatesPerSet);

            for (int way = 0; way < this.llc.getAssociativity(); way++) {
                htRequestStatesPerSet.put(way, LastLevelCacheLineHtRequestState.INVALID);
            }
        }

        simulation.getProcessor().getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
                    serviceRequest(event);
                }
            }
        });

        simulation.getProcessor().getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                totalHtRequests = 0;
                usedHtRequests = 0;
                pollutingHtRequests = 0;
                unusedHtRequests = 0;
            }
        });

        simulation.getProcessor().getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });

        simulation.getProcessor().getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".totalHtRequests", String.valueOf(this.totalHtRequests));
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".usedHtRequests.confirmed", String.valueOf(this.usedHtRequests));
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".pollutingHtRequests.confirmed", String.valueOf(this.pollutingHtRequests));
        stats.put("lastLevelCacheHtRequestCachePollutionProfilingCapability." + this.llc.getName() + ".unusedHtRequests.confirmed", String.valueOf(this.unusedHtRequests));
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> ownerCacheLine = event.getLineFound();
        
        LastLevelCacheLineHtRequestState htRequestState = this.htRequestStates.get(ownerCacheLine.getSet()).get(ownerCacheLine.getWay());
        boolean victimIsHt = htRequestState == LastLevelCacheLineHtRequestState.HT;

        HtRequestVictimCacheLine htRequestVictimCacheLine = this.findHtRequestVictimCacheLine(this.llc.getTag(event.getAddress()));

        if(requesterIsHt && !event.isHitInCache() && !event.isEviction()) {
            // case 1

        }
        else if(requesterIsHt && !event.isHitInCache() && event.isEviction() && !victimIsHt) {
            // case 2
        }
        else if(requesterIsHt && !event.isHitInCache() && event.isEviction() && victimIsHt) {
            //  case 3
        }
        else if(!requesterIsHt && !event.isHitInCache() && event.isEviction() && victimIsHt) {
            // case 4
        }
        else if(!requesterIsHt && !victimIsHt) { //TODO: case 5
            //case 5
        }
    }

    public HtRequestVictimCacheLine findHtRequestVictimCacheLine(int tag) {
        int set = this.llc.getSet(tag);

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            HtRequestVictimCacheLine line = this.htRequestVictimCache.getLine(set, way);
            if (line.getState() == HtRequestVictimCacheLineState.DATA && line.getState().getVictimTag() == tag) {
                return line;
            }
        }

        return null;
    }

    private class HtRequestVictimCacheLine extends CacheLine<HtRequestVictimCacheLineState> {
        private HtRequestVictimCacheLine(Cache<?, ?> cache, int set, int way) {
            super(cache, set, way, HtRequestVictimCacheLineState.INVALID);
        }
    }

    public static final SimulationCapabilityFactory FACTORY = new SimulationCapabilityFactory() {
        public SimulationCapability createCapability(Simulation simulation) {
            return new LastLevelCacheHtRequestCachePollutionProfilingCapability(simulation);
        }
    };
}