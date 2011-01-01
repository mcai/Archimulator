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
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LLCReuseDistanceProfilingCapability implements SimulationCapability {
    private EvictableCache<?, ?> llc;

    private int maxReuseDistance = 64; //TODO: should not be hardcoded!!!

    private List<List<StackEntry>> stackEntries;

    private Map<CacheAccessType, Map<Integer, Long>> reuseDistances;

    private long numDownwardReads = 0;
    private long numDownwardWrites = 0;
    private long numEvicts = 0;

    public LLCReuseDistanceProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache());
    }

    public LLCReuseDistanceProfilingCapability(EvictableCache<?, ?> llc) {
        this.llc = llc;

        this.stackEntries = new ArrayList<List<StackEntry>>();

        for (int set = 0; set < this.llc.getNumSets(); set++) {
            this.stackEntries.add(new ArrayList<StackEntry>());

            for (int way = 0; way < this.maxReuseDistance; way++) {
                this.stackEntries.get(set).add(new StackEntry());
            }
        }

        this.reuseDistances = new TreeMap<CacheAccessType, Map<Integer, Long>>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(LLCReuseDistanceProfilingCapability.this.llc)) {
                    switch (event.getAccessType()) {
                        case DOWNWARD_READ:
                            numDownwardReads++;
                            break;
                        case DOWNWARD_WRITE:
                            numDownwardWrites++;
                            break;
                        case EVICT:
                            numEvicts++;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    handleServicingRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                reuseDistances.clear();

                numDownwardReads = 0;
                numDownwardWrites = 0;
                numEvicts = 0;
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
        stats.put("llcReuseDistanceProfilingCapability." + this.llc.getName() + ".numDownwardReads", this.numDownwardReads);
        stats.put("llcReuseDistanceProfilingCapability." + this.llc.getName() + ".numDownwardWrites", this.numDownwardWrites);
        stats.put("llcReuseDistanceProfilingCapability." + this.llc.getName() + ".numEvicts", this.numEvicts);

        for (CacheAccessType accessType : this.reuseDistances.keySet()) {
            for (int reuseDistance : this.reuseDistances.get(accessType).keySet()) {
                stats.put("llcReuseDistanceProfilingCapability." + this.llc.getName() + ".ht_mt_inter-thread_reuseDistances[" + accessType + "][" + reuseDistance + "]", String.valueOf(this.reuseDistances.get(accessType).get(reuseDistance)));
            }
        }
    }

    private void handleServicingRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        this.setLRU(event.getLineFound().getSet(), this.llc.getTag(event.getAddress()), event.getRequesterAccess().getThread().getId(), event.getAccessType());
    }

    private void setLRU(int set, int tag, int broughterThreadId, CacheAccessType accessType) {
        StackEntry stackEntryFound = this.getStackEntry(set, tag);

        int reuseDistance = -1;
        int oldBroughterThreadId = -1;

        if (stackEntryFound != null) {
            int oldStackPosition = this.stackEntries.get(set).indexOf(stackEntryFound);
            
            oldBroughterThreadId = stackEntryFound.broughterThreadId;
            stackEntryFound.broughterThreadId = broughterThreadId;
            stackEntryFound.accessType = accessType;

            this.stackEntries.get(set).remove(stackEntryFound);
            this.stackEntries.get(set).add(0, stackEntryFound);

            reuseDistance = oldStackPosition + 1;
        } else {
            for (int way = 0; way < this.maxReuseDistance; way++) {
                StackEntry stackEntry = this.stackEntries.get(set).get(way);
                if (stackEntry.tag == -1) {
                    stackEntryFound = stackEntry;
                    break;
                }
            }

            if (stackEntryFound == null) {
                stackEntryFound = this.getLRUStackEntry(set);
            }

            stackEntryFound.tag = tag;
            stackEntryFound.broughterThreadId = broughterThreadId;

            this.stackEntries.get(set).remove(stackEntryFound);
            this.stackEntries.get(set).add(0, stackEntryFound);
        }

        if(BasicThread.isHelperThread(oldBroughterThreadId) && BasicThread.isMainThread(broughterThreadId)) {
            this.incReuseDistanceStat(reuseDistance, accessType);
        }
    }

    private StackEntry getLRUStackEntry(int set) {
        return this.stackEntries.get(set).get(this.maxReuseDistance - 1);
    }

    private void incReuseDistanceStat(int reuseDistance, CacheAccessType accessType) {
        if (!this.reuseDistances.containsKey(accessType)) {
            this.reuseDistances.put(accessType, new TreeMap<Integer, Long>());
        }

        if (!this.reuseDistances.get(accessType).containsKey(reuseDistance)) {
            this.reuseDistances.get(accessType).put(reuseDistance, 0L);
        }

        this.reuseDistances.get(accessType).put(reuseDistance, this.reuseDistances.get(accessType).get(reuseDistance) + 1);
    }

    private StackEntry getStackEntry(int set, int tag) {
        List<StackEntry> lineReplacementStatesPerSet = this.stackEntries.get(set);

        for (StackEntry stackEntry : lineReplacementStatesPerSet) {
            if (stackEntry.tag == tag) {
                return stackEntry;
            }
        }

        return null;
    }

    private static class StackEntry implements Serializable {
        private int broughterThreadId;
        private int tag;
        private CacheAccessType accessType;

        private StackEntry() {
            this.broughterThreadId = -1;
            this.tag = -1;
            this.accessType = CacheAccessType.UNKNOWN;
        }
    }
}
