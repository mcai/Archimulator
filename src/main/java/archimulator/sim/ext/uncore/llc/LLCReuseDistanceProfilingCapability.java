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
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import archimulator.util.action.Action1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//TODO
public class LLCReuseDistanceProfilingCapability implements SimulationCapability {
    private EvictableCache<?, ?> llc;

    private int maxStackDistance = 50;

    private List<List<StackEntry>> stackEntries;

    private Map<Integer, Long> stackDistances;

    public LLCReuseDistanceProfilingCapability(Simulation simulation) {
        this(simulation.getProcessor().getCacheHierarchy().getL2Cache().getCache());
    }

    public LLCReuseDistanceProfilingCapability(EvictableCache<?, ?> llc) {
        this.llc = llc;

        this.stackEntries = new ArrayList<List<StackEntry>>();

        for (int set = 0; set < this.llc.getNumSets(); set++) {
            this.stackEntries.add(new ArrayList<StackEntry>());

            for (int way = 0; way < this.maxStackDistance; way++) {
                this.stackEntries.get(set).add(new StackEntry());
            }
        }

        this.stackDistances = new TreeMap<Integer, Long>();

        llc.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(LLCReuseDistanceProfilingCapability.this.llc)) {
                    handleServicingRequest(event);
                }
            }
        });

        llc.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                //TODO: clear stackEntries
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
        for (int stackDistance : this.stackDistances.keySet()) {
            stats.put("llcReuseDistanceProfilingCapability." + this.llc.getName() + ".stackDistances[" + stackDistance + "]", String.valueOf(this.stackDistances.get(stackDistance)));
        }
    }

    private void handleServicingRequest(CoherentCacheServiceNonblockingRequestEvent event) {
        this.doAccess(event.getLineFound().getSet(), this.llc.getTag(event.getAddress()), event.getRequesterAccess().getThread().getId());
    }

    private void doAccess(int set, int tag, int broughterThreadId) {
        this.setLRU(set, tag, broughterThreadId);
    }

    private StackEntry getLRUStackEntry(int set) {
        return this.stackEntries.get(set).get(this.maxStackDistance - 1);
    }

    private void setLRU(int set, int tag, int broughterThreadId) {
        StackEntry stackEntryFound = this.getStackEntry(set, tag);

        int stackDistance = -1;

        if (stackEntryFound != null) {
            int oldStackPosition = this.stackEntries.get(set).indexOf(stackEntryFound);

            stackEntryFound.broughterThreadId = broughterThreadId;

            this.stackEntries.get(set).remove(stackEntryFound);
            this.stackEntries.get(set).add(0, stackEntryFound);

            stackDistance = this.maxStackDistance - oldStackPosition;
        } else {
            for (int way = 0; way < this.maxStackDistance; way++) {
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

        this.incStackDistanceStat(stackDistance);
    }

    private void incStackDistanceStat(int stackDistance) {
        if (!this.stackDistances.containsKey(stackDistance)) {
            this.stackDistances.put(stackDistance, 0L);
        }

        this.stackDistances.put(stackDistance, this.stackDistances.get(stackDistance));
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

        private StackEntry() {
            this.broughterThreadId = -1;
            this.tag = -1;
        }
    }
}
