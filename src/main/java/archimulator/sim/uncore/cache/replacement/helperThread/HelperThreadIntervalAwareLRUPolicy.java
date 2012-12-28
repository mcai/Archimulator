/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.cache.replacement.helperThread;

import archimulator.sim.core.BasicThread;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import net.pickapack.action.Action1;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.Serializable;

/**
 * Helper thread interval aware least recently used (LRU) policy.
 *
 * @author Min Cai
 * @param <StateT> the state type of the parent evictable cache
 */
public class HelperThreadIntervalAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private int evictedL2CacheLinesPerInterval;

    private long intervals;
    private int evictedL2CacheLines;

    private HelperThreadL2CacheRequestPollution helperThreadL2CacheRequestPollution;

    private IntervalStat totalHelperThreadL2CacheRequests;
    private IntervalStat badHelperThreadL2CacheRequests;
    private IntervalStat lateHelperThreadL2CacheRequests;

    public HelperThreadIntervalAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.evictedL2CacheLinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        this.helperThreadL2CacheRequestPollution = HelperThreadL2CacheRequestPollution.MEDIUM;

        this.totalHelperThreadL2CacheRequests = new IntervalStat();
        this.badHelperThreadL2CacheRequests = new IntervalStat();
        this.lateHelperThreadL2CacheRequests = new IntervalStat();

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent param) {
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent param) {
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent param) {
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent param) {
                lateHelperThreadL2CacheRequests.inc();
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent event) {
                badHelperThreadL2CacheRequests.inc();
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent param) {
                totalHelperThreadL2CacheRequests.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, new Action1<GeneralCacheControllerLineReplacementEvent>() {
            @Override
            public void apply(GeneralCacheControllerLineReplacementEvent event) {
                if(event.getCacheController().getCache() == getCache()) {
                    evictedL2CacheLines++;

                    if (evictedL2CacheLines == evictedL2CacheLinesPerInterval) {
                        newInterval();

                        evictedL2CacheLines = 0;
                        intervals++;
                    }
                }
            }
        });
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (BasicThread.isHelperThread(access.getThread().getId())) {
            int newStackPosition;

            switch (this.helperThreadL2CacheRequestPollution) {
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

            this.setStackPosition(set, way, newStackPosition);
        } else {
            super.handleInsertionOnMiss(access, set, way);
        }
    }

    private SummaryStatistics stat = new SummaryStatistics();

    private void newInterval() {
        long numTotalHtRequests = this.totalHelperThreadL2CacheRequests.newInterval();
        long numBadHtRequests = this.badHelperThreadL2CacheRequests.newInterval();
        long numLateHtRequests = this.lateHelperThreadL2CacheRequests.newInterval();

        double pollution = this.getHtRequestPollution(numBadHtRequests, numTotalHtRequests);
        double lateness = this.getHtRequestLateness(numLateHtRequests, numTotalHtRequests);

//        stat.addValue(pollution);
//        stat.addValue(numTotalHtRequests);
        stat.addValue(numBadHtRequests);
//        stat.addValue(numLateHtRequests);

        if (pollution > htRequestCachePollutionDegreeHighThreshold) {
            this.helperThreadL2CacheRequestPollution = HelperThreadL2CacheRequestPollution.HIGH;
        } else if (pollution < htRequestCachePollutionDegreeLowThreshold) {
            this.helperThreadL2CacheRequestPollution = HelperThreadL2CacheRequestPollution.LOW;
        } else {
            this.helperThreadL2CacheRequestPollution = HelperThreadL2CacheRequestPollution.MEDIUM;
        }
    }

    private double getHtRequestPollution(long numBadHtRequests, long numTotalHtRequests) {
        return (double) numBadHtRequests / numTotalHtRequests;
    }

    private double getHtRequestLateness(long numLateHtRequests, long numTotalHtRequests) {
        return (double) numLateHtRequests / numTotalHtRequests;
    }

    public int getEvictedL2CacheLinesPerInterval() {
        return evictedL2CacheLinesPerInterval;
    }

    public long getIntervals() {
        return intervals;
    }

    public IntervalStat getTotalHelperThreadL2CacheRequests() {
        return totalHelperThreadL2CacheRequests;
    }

    public IntervalStat getBadHelperThreadL2CacheRequests() {
        return badHelperThreadL2CacheRequests;
    }

    public IntervalStat getLateHelperThreadL2CacheRequests() {
        return lateHelperThreadL2CacheRequests;
    }

    public SummaryStatistics getStat() {
        return stat;
    }

    private enum HelperThreadL2CacheRequestPollution {
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
