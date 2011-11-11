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
package archimulator.ext.mem.newHt2;

import archimulator.core.BasicThread;
import archimulator.mem.cache.Cache;
import archimulator.mem.cache.CacheLine;
import archimulator.mem.cache.event.CacheLineInvalidatedEvent;
import archimulator.mem.cache.event.CacheLineStateChangedToNonInitialStateEvent;
import archimulator.mem.cache.event.CacheLineTagChangedEvent;
import archimulator.mem.cache.event.CacheLineValidatedEvent;
import archimulator.mem.coherence.CoherentCache;
import archimulator.mem.coherence.MESIState;
import archimulator.mem.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.sim.Simulation;
import archimulator.sim.capability.SimulationCapability;
import archimulator.sim.capability.SimulationCapabilityFactory;
import archimulator.sim.event.DumpStatEvent;
import archimulator.sim.event.PollStatsEvent;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.action.Action1;
import archimulator.util.action.Function3;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class LastLevelCacheHtRequestCachePollutionProfilingCapability implements SimulationCapability {
    private CoherentCache<MESIState>.LockableCache llc;

    private Cache<HtRequestVictimCacheLineState, HtRequestVictimCacheLine> htRequestVictimCache;
    private Map<Integer, Map<Integer, LastLevelCacheLineHtRequestState>> htRequestStates;

    private long totalHtRequests;
    private long usedHtRequests;
    private long pollutingHtRequests;
    private long unusedHtRequests;

    private PrintWriter fileWriter;
    private Simulation simulation;

    public LastLevelCacheHtRequestCachePollutionProfilingCapability(final Simulation simulation) {
        this.simulation = simulation;
        this.llc = simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache();

        this.htRequestVictimCache = new Cache<HtRequestVictimCacheLineState, HtRequestVictimCacheLine>(simulation.getProcessor(), this.llc.getName() + ".htRequestVictimCache", this.llc.getGeometry(), new Function3<Cache<?, ?>, Integer, Integer, HtRequestVictimCacheLine>() {
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

        simulation.getBlockingEventDispatcher().addListener(CacheLineInvalidatedEvent.class, new Action1<CacheLineInvalidatedEvent>() {
            public void apply(CacheLineInvalidatedEvent event) {
                if (event.getLine().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
                    fileWriter.printf("[%d] %s\n", simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CacheLineValidatedEvent.class, new Action1<CacheLineValidatedEvent>() {
            public void apply(CacheLineValidatedEvent event) {
                if (event.getLine().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
                    fileWriter.printf("[%d] %s\n", simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CacheLineStateChangedToNonInitialStateEvent.class, new Action1<CacheLineStateChangedToNonInitialStateEvent>() {
            public void apply(CacheLineStateChangedToNonInitialStateEvent event) {
                if (event.getLine().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
//                    fileWriter.printf("[%d] %s\n", simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(CacheLineTagChangedEvent.class, new Action1<CacheLineTagChangedEvent>() {
            public void apply(CacheLineTagChangedEvent event) {
                if (event.getLine().getCache() == LastLevelCacheHtRequestCachePollutionProfilingCapability.this.llc) {
//                    fileWriter.printf("[%d] %s\n", simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);
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

        String logFileName = simulation.getConfig().getCwd() + "/LastLevelCacheHtRequestCachePollutionProfilingCapability.out";
        try {
            this.fileWriter = new PrintWriter(logFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".totalHtRequests", String.valueOf(this.totalHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".usedHtRequests.confirmed", String.valueOf(this.usedHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".pollutingHtRequests.confirmed", String.valueOf(this.pollutingHtRequests));
        stats.put(this.llc.getName() + ".fsmBasedHtRequestLlcVictimTrackingCapability." + this.llc.getName() + ".unusedHtRequests.confirmed", String.valueOf(this.unusedHtRequests));
    }

    private void serviceRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        if(!event.isHitInCache() && event.isEviction()) {
//        if (event.isHitInCache()) {
            this.fileWriter.printf("[%d] %s\n", this.simulation.getCycleAccurateEventQueue().getCurrentCycle(), event);
        }

        boolean requesterIsHt = BasicThread.isHelperThread(event.getRequesterAccess().getThread());
        CacheLine<?> ownerCacheLine = event.getLineFound();
        LastLevelCacheLineHtRequestState htRequestState = this.htRequestStates.get(ownerCacheLine.getSet()).get(ownerCacheLine.getWay());

        if (event.isHitInCache()) {
//            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_HIT : CacheLineHtRequestCondition.MT_HIT);
        } else {
            if (event.isEviction()) {
//                fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.EVICTED_BY_HT : CacheLineHtRequestCondition.EVICTED_BY_MT, event.getLineFound().getTag());
            }

            HtRequestVictimCacheLine lineForVictim = this.findVictimLineForTag(this.llc.getTag(event.getAddress()));
            if (lineForVictim != null) {
//                lineForVictim.fireTransition(CacheLineHtRequestCondition.VICTIM_HIT_BY_MT);
            }
//
//            fsm.fireTransition(requesterIsHt ? CacheLineHtRequestCondition.HT_MISS : CacheLineHtRequestCondition.MT_MISS);
        }
    }

    public HtRequestVictimCacheLine findVictimLineForTag(int tag) {
        int set = this.llc.getSet(tag);

        for (int way = 0; way < this.llc.getAssociativity(); way++) {
            HtRequestVictimCacheLine line = this.htRequestVictimCache.getLine(set, way);
            if (line.getState() == HtRequestVictimCacheLineState.DATA && line.getState().getVictimTag() == tag) {
                return line;
            }
        }

        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        this.fileWriter.flush();
        this.fileWriter.close();

        super.finalize();
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