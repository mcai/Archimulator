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
import archimulator.ext.uncore.newHt2.state.*;
import archimulator.uncore.cache.CacheLine;
import archimulator.uncore.coherence.CoherentCache;
import archimulator.uncore.coherence.MESIState;
import archimulator.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.Simulation;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.Pair;
import archimulator.util.action.Action1;
import archimulator.util.simpleCache.DefaultSimpleCacheAccessType;
import archimulator.util.simpleCache.SimpleCache;

import java.util.HashMap;
import java.util.Map;

public class LastLevelCacheHtRequestCachePollutionProfilingCapability implements SimulationCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private SimpleCache<Integer, HtRequestVictimCacheLineState, DefaultSimpleCacheAccessType> htRequestVictimCache;
    private Map<Integer, Map<Integer, LastLevelCacheLineHtRequestState>> htRequestStates;

    private long totalHtRequests;
    private long usedHtRequests;
    private long pollutingHtRequests;
    private long unusedHtRequests;

    public LastLevelCacheHtRequestCachePollutionProfilingCapability(final Simulation simulation) {
        this.llc = simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache();

        this.htRequestVictimCache = new SimpleCache<Integer, HtRequestVictimCacheLineState, DefaultSimpleCacheAccessType>(this.llc.getNumSets(), this.llc.getAssociativity()) {
            @Override
            protected Pair<HtRequestVictimCacheLineState, DefaultSimpleCacheAccessType> doReadFromNextLevel(Integer key, HtRequestVictimCacheLineState oldValue) {
                return new Pair<HtRequestVictimCacheLineState, DefaultSimpleCacheAccessType>(new InvalidHtRequestVictimCacheLineState(), DefaultSimpleCacheAccessType.READ);
            }

            @Override
            protected void doWriteToNextLevel(Integer key, HtRequestVictimCacheLineState value, boolean writeback) {
            }
        };

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
        
        int set = ownerCacheLine.getSet();
        
        LastLevelCacheLineHtRequestState htRequestState = this.htRequestStates.get(ownerCacheLine.getSet()).get(ownerCacheLine.getWay());
        boolean victimIsHt = htRequestState == LastLevelCacheLineHtRequestState.HT;

        int htRequestVictimWay = this.findHtRequestVictimWayFromTag(this.llc.getTag(event.getAddress()));

        if(htRequestVictimWay == -1) {
            //TODO: not found
        }

        if(requesterIsHt && !event.isHitInCache() && !event.isEviction()) {
            // case 1
            this.htRequestVictimCache.put(set, -1, new NullHtRequestVictimCacheLineState(), DefaultSimpleCacheAccessType.READ);

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

    public int findHtRequestVictimWayFromTag(int tag) {
        int set = this.llc.getSet(tag);

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            HtRequestVictimCacheLineState line = this.htRequestVictimCache.get(set, way, DefaultSimpleCacheAccessType.READ); //TODO:...
            if (line instanceof DataHtRequestVictimCacheLineState && ((DataHtRequestVictimCacheLineState) line).getVictimTag() == tag) {
                return way;
            }
        }

        return -1;
    }

    public static final SimulationCapabilityFactory FACTORY = new SimulationCapabilityFactory() {
        public SimulationCapability createCapability(Simulation simulation) {
            return new LastLevelCacheHtRequestCachePollutionProfilingCapability(simulation);
        }
    };
}