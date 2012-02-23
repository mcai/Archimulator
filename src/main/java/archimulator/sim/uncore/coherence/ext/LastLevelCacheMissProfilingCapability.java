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
package archimulator.sim.uncore.coherence.ext;

import archimulator.model.base.Simulation;
import archimulator.model.capability.SimulationCapability;
import archimulator.model.capability.SimulationCapabilityFactory;
import archimulator.model.event.DumpStatEvent;
import archimulator.model.event.PollStatsEvent;
import archimulator.model.event.PseudocallEncounteredEvent;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.coherence.event.CoherentCacheEndCacheAccessEvent;
import archimulator.util.IntegerIntegerPair;
import archimulator.util.Triple;
import archimulator.util.action.Action1;
import archimulator.util.math.FrequencyCalculator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: split this class!!! Miss Latency & MLP; pseudocall guided inter-thread reuse distance profiling;
public class LastLevelCacheMissProfilingCapability implements SimulationCapability {
    private Simulation simulation;
    private int numOutstandingMisses;
    private Map<CacheAccess<?, ?>, OutstandingCacheMissInfo> outstandingMisses;

    private FrequencyCalculator<IntegerIntegerPair> frequencyCalculatorLlcDownwardReadMissesPerPc;
    private FrequencyCalculator<IntegerIntegerPair> frequencyCalculatorLlcDownwardReadsFromDelinquentPcs;

    private List<IntegerIntegerPair> predefinedDelinquentPcs;

    public LastLevelCacheMissProfilingCapability(Simulation simulation) {
        this.simulation = simulation;

        this.outstandingMisses = new HashMap<CacheAccess<?, ?>, OutstandingCacheMissInfo>();

        this.frequencyCalculatorLlcDownwardReadMissesPerPc = new FrequencyCalculator<IntegerIntegerPair>();
        this.frequencyCalculatorLlcDownwardReadsFromDelinquentPcs = new FrequencyCalculator<IntegerIntegerPair>();

        this.predefinedDelinquentPcs = new ArrayList<IntegerIntegerPair>();
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(2, 0x004014d8));
        this.predefinedDelinquentPcs.add(new IntegerIntegerPair(0, 0x00400a34));

        final PrintWriter printWriter;
//        final PrintWriter printWriter = new PrintWriter(System.out);
        try {
//            printWriter = new PrintWriter(simulation.getConfig().getCwd() + "/llcDownwardReadMissesFromDelinquentPCs");
            printWriter = new PrintWriter(simulation.getConfig().getCwd() + "/llcDownwardReadMisses"); //TODO: renamed to **Reads
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        this.simulation.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
            public void apply(CoherentCacheBeginCacheAccessEvent event) {
                if (event.getCache().isLastLevelCache() && event.getCacheAccess().getReference().getAccessType().isDownwardRead()) {
                    IntegerIntegerPair sample = new IntegerIntegerPair(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc());
                    if (predefinedDelinquentPcs.contains(sample)) {
                        frequencyCalculatorLlcDownwardReadsFromDelinquentPcs.addSample(new IntegerIntegerPair(event.getAccess().getThread().getId(), event.getAccess().getPhysicalTag()));
                    }
                    printWriter.println(event.getAccess());
                    printWriter.flush();
                }
            }
        });

        this.simulation.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
            public void apply(CoherentCacheBeginCacheAccessEvent event) {
                if (!event.getCacheAccess().isHitInCache() && event.getCache().isLastLevelCache()) {
                    beginMiss(event);
                    //                    System.out.println("begin LLC miss, # outstanding misses: " + numOutstandingMisses);

                    if (event.getCacheAccess().getReference().getAccessType().isDownwardRead()) {
                        frequencyCalculatorLlcDownwardReadMissesPerPc.addSample(new IntegerIntegerPair(event.getAccess().getThread().getId(), event.getAccess().getVirtualPc()));
                    }
                }
            }
        });

        this.simulation.getBlockingEventDispatcher().addListener(CoherentCacheEndCacheAccessEvent.class, new Action1<CoherentCacheEndCacheAccessEvent>() {
            public void apply(CoherentCacheEndCacheAccessEvent event) {
                if (!event.getCacheAccess().isHitInCache() && event.getCache().isLastLevelCache()) {
                    endMiss(event);
//                    System.out.println("end LLC miss, # outstanding misses: " + numOutstandingMisses);
                }
            }
        });

        this.simulation.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats(), false);
            }
        });

        this.simulation.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats(), true);
                }

                printWriter.close();
            }
        });

        this.simulation.getBlockingEventDispatcher().addListener(PseudocallEncounteredEvent.class, new Action1<PseudocallEncounteredEvent>() {
            public void apply(PseudocallEncounteredEvent event) {
//                if (BasicThread.isHelperThread(event.getContext().getThread()))
//                    System.out.println("pseudocall: " + event.getContext().getThread().getName() + " - " + event.getArg());
            }
        });
    }

    private void beginMiss(CoherentCacheBeginCacheAccessEvent event) {
        numOutstandingMisses++;
        this.outstandingMisses.put(event.getCacheAccess(), new OutstandingCacheMissInfo(event.getCacheAccess()));
    }

    private void endMiss(CoherentCacheEndCacheAccessEvent event) {
        numOutstandingMisses--;
        this.outstandingMisses.remove(event.getCacheAccess());
    }

    private void dumpStats(Map<String, Object> stats, boolean pollStats) {
        NumberFormat nf = NumberFormat.getPercentInstance();

        final List<Triple<IntegerIntegerPair, Integer, Float>> frequenciesCalculatorLlcDownwardReadMissesPerPc = this.frequencyCalculatorLlcDownwardReadMissesPerPc.getFrequencies();

        stats.put("LastLevelCacheMissProfilingCapability" + ".llcDownwardReadMissesPerPc.size", frequenciesCalculatorLlcDownwardReadMissesPerPc.size());

        for (Triple<IntegerIntegerPair, Integer, Float> triple : frequenciesCalculatorLlcDownwardReadMissesPerPc.subList(0, Math.min(4, frequenciesCalculatorLlcDownwardReadMissesPerPc.size()))) {
            stats.put("LastLevelCacheMissProfilingCapability" + ".llcDownwardReadMissesPerPc @ " + String.format("ctx%d: PC 0x%08x", triple.getFirst().getFirst(), triple.getFirst().getSecond()),
                    triple.getSecond() + "(" + nf.format(triple.getThird()) + ")");
        }

        ///////////////////////////////////

        //TODO: the following consumes lots of time, maybe never end? bug!
//        if (pollStats) {
//            final List<Triple<IntegerIntegerPair, Long, Double>> frequenciesCalculatorLlcDownwardReadMissesFromDelinquentPcs = this.frequencyCalculatorLlcDownwardReadsFromDelinquentPcs.getFrequencies();
//
//            stats.put("LastLevelCacheMissProfilingCapability" + ".frequencyCalculatorLlcDownwardReadsFromDelinquentPcs.size", frequenciesCalculatorLlcDownwardReadMissesFromDelinquentPcs.size());
//
//            for (Triple<IntegerIntegerPair, Long, Double> triple : frequenciesCalculatorLlcDownwardReadMissesFromDelinquentPcs) {
//                stats.put("LastLevelCacheMissProfilingCapability" + ".frequencyCalculatorLlcDownwardReadsFromDelinquentPcs @ " + String.format("ctx%d: 0x%08x", triple.getFirst().getFirst(), triple.getFirst().getSecond()),
//                        triple.getSecond() + "(" + nf.format(triple.getThird()) + ")");
//            }
//        }
    }

    private class OutstandingCacheMissInfo {
        private CacheAccess<?, ?> cacheAccess;
        private long beginCycle;
        private long endCycle;

        private OutstandingCacheMissInfo(CacheAccess<?, ?> cacheAccess) {
            this.cacheAccess = cacheAccess;
            this.beginCycle = simulation.getCycleAccurateEventQueue().getCurrentCycle();
        }

        public void complete() {
            this.endCycle = simulation.getCycleAccurateEventQueue().getCurrentCycle();
        }

        private int getCycles() {
            return (int) (endCycle - beginCycle);
        }
    }

    public static final SimulationCapabilityFactory FACTORY = new SimulationCapabilityFactory() {
        public SimulationCapability createCapability(Simulation simulation) {
            return new LastLevelCacheMissProfilingCapability(simulation);
        }
    };
}
