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
package archimulator.sim.ext.uncore.cache.eviction;

import archimulator.sim.base.event.DumpStatEvent;
import archimulator.sim.base.event.PollStatsEvent;
import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.core.BasicThread;
import archimulator.sim.ext.uncore.llc.HTLLCRequestProfilingCapability;
import archimulator.sim.uncore.cache.CacheLine;
import archimulator.sim.uncore.cache.CacheMiss;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.sim.uncore.coherence.event.CoherentCacheServiceNonblockingRequestEvent;
import net.pickapack.action.Action1;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.Serializable;
import java.util.Map;

public class LLCHTAwareLRUPolicy<StateT extends Serializable, LineT extends CacheLine<StateT>> extends LRUPolicy<StateT, LineT> {
    private HTLLCRequestProfilingCapability llcHtRequestProfilingCapability;

    private int evictedMtLinesPerInterval;

    private long intervals;
    private int evictedMtLines;

    private HTRequestCachePollution htRequestCachePollution;

    private IntervalStat totalHtRequests;
    private IntervalStat badHtRequests;
    private IntervalStat lateHtRequests;

    @SuppressWarnings("unchecked")
    public LLCHTAwareLRUPolicy(EvictableCache<StateT, LineT> cache) {
        super(cache);

        this.llcHtRequestProfilingCapability = new HTLLCRequestProfilingCapability(cache);

        this.evictedMtLinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        this.htRequestCachePollution = HTRequestCachePollution.MEDIUM;

        this.totalHtRequests = new IntervalStat();
        this.badHtRequests = new IntervalStat();
        this.lateHtRequests = new IntervalStat();

        this.llcHtRequestProfilingCapability.getEventDispatcher().addListener(HTLLCRequestProfilingCapability.HTLLCRequestEvent.class, new Action1<HTLLCRequestProfilingCapability.HTLLCRequestEvent>() {
            @Override
            public void apply(HTLLCRequestProfilingCapability.HTLLCRequestEvent event) {
                totalHtRequests.inc();
            }
        });

        this.llcHtRequestProfilingCapability.getEventDispatcher().addListener(HTLLCRequestProfilingCapability.BadHTLLCRequestEvent.class, new Action1<HTLLCRequestProfilingCapability.BadHTLLCRequestEvent>() {
            @Override
            public void apply(HTLLCRequestProfilingCapability.BadHTLLCRequestEvent event) {
                badHtRequests.inc();
            }
        });

        this.llcHtRequestProfilingCapability.getEventDispatcher().addListener(HTLLCRequestProfilingCapability.LateHTLLCRequestEvent.class, new Action1<HTLLCRequestProfilingCapability.LateHTLLCRequestEvent>() {
            @Override
            public void apply(HTLLCRequestProfilingCapability.LateHTLLCRequestEvent event) {
                lateHtRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(CoherentCacheServiceNonblockingRequestEvent.class, new Action1<CoherentCacheServiceNonblockingRequestEvent>() {
            public void apply(CoherentCacheServiceNonblockingRequestEvent event) {
                if (event.getCache().getCache().equals(getCache()) && !event.isHitInCache() &&
                        event.isEviction() &&
                        llcHtRequestProfilingCapability.getLLCLineBroughterThreadId(event.getLineFound().getSet(), event.getLineFound().getWay()) == BasicThread.getMainThreadId()) {
                    evictedMtLines++;

                    if (evictedMtLines == evictedMtLinesPerInterval) {
                        newInterval();

                        evictedMtLines = 0;
                        intervals++;
                    }
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
            }
        });

        cache.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    dumpStats(event.getStats());
                }
            }
        });

        cache.getBlockingEventDispatcher().addListener(PollStatsEvent.class, new Action1<PollStatsEvent>() {
            public void apply(PollStatsEvent event) {
                dumpStats(event.getStats());
            }
        });
    }

    private void dumpStats(Map<String, Object> stats) {
        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.const.evictedMtLinesPerInterval", String.valueOf(this.evictedMtLinesPerInterval));

        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.intervals", String.valueOf(this.intervals));

        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.currentInterval.totalHtRequests", String.valueOf(this.totalHtRequests));
        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.currentInterval.badHtRequests", String.valueOf(this.badHtRequests));
        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.currentInterval.lateHtRequests", String.valueOf(this.lateHtRequests));

        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.currentInterval.htRequestCachePollution", String.valueOf(this.htRequestCachePollution));

        stats.put(this.getCache().getName() + ".llcHtAwareLRUPolicy.stat", String.format("n: %d, sum: %.4f, std dev: %.4f, mean: %.4f, min: %.4f, max: %.4f", stat.getN(), stat.getSum(), stat.getStandardDeviation(), stat.getMean(), stat.getMin(), stat.getMax()));
    }

    @Override
    public void handleInsertionOnMiss(CacheMiss<StateT, LineT> miss) {
        if (BasicThread.isHelperThread(miss.getReference().getAccess().getThread().getId())) {
            int newStackPosition;

            switch (this.htRequestCachePollution) {
                case HIGH:
                    newStackPosition = this.getCache().getAssociativity() - 1;
                    break;
                case MEDIUM:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) * 3 / 4);
                    break;
                case LOW:
                    newStackPosition = (int) ((double) (this.getCache().getAssociativity() - 1) / 2) - 1;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            this.setStackPosition(miss.getReference().getSet(), miss.getWay(), newStackPosition);
        } else {
            super.handleInsertionOnMiss(miss);
        }
    }

    private DescriptiveStatistics stat = new DescriptiveStatistics();

    private void newInterval() {
        long numTotalHtRequests = this.totalHtRequests.newInterval();
        long numBadHtRequests = this.badHtRequests.newInterval();
        long numLateHtRequests = this.lateHtRequests.newInterval();

        double pollution = this.getHtRequestPollution(numBadHtRequests, numTotalHtRequests);
        double lateness = this.getHtRequestLateness(numLateHtRequests, numTotalHtRequests);

//        stat.addValue(pollution);
//        stat.addValue(numTotalHtRequests);
        stat.addValue(numBadHtRequests);
//        stat.addValue(numLateHtRequests);

        if (pollution > htRequestCachePollutionDegreeHighThreshold) {
            this.htRequestCachePollution = HTRequestCachePollution.HIGH;
        } else if (pollution < htRequestCachePollutionDegreeLowThreshold) {
            this.htRequestCachePollution = HTRequestCachePollution.LOW;
        } else {
            this.htRequestCachePollution = HTRequestCachePollution.MEDIUM;
        }
    }

    private double getHtRequestPollution(long numBadHtRequests, long numTotalHtRequests) {
        return (double) numBadHtRequests / numTotalHtRequests;
    }

    private double getHtRequestLateness(long numLateHtRequests, long numTotalHtRequests) {
        return (double) numLateHtRequests / numTotalHtRequests;
    }

    private enum HTRequestCachePollution {
        HIGH,
        MEDIUM,
        LOW
    }

    private class IntervalStat {
        private long valueInPreviousInterval;
        private long value;

        private void inc() {
            this.value++;
        }

        private void reset() {
            this.value = this.valueInPreviousInterval = 0;
        }

        private long newInterval() {
            this.valueInPreviousInterval = (this.valueInPreviousInterval + this.value) / 2;
            this.value = 0;
            return this.valueInPreviousInterval;
        }

        @Override
        public String toString() {
            return Long.toString(this.value);
        }
    }

    private static final double htRequestCachePollutionDegreeHighThreshold = 0.25;
    private static final double htRequestCachePollutionDegreeLowThreshold = 0.005;
}
