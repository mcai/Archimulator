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

import archimulator.model.ContextMapping;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.LRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;
import net.pickapack.action.Action1;
import net.pickapack.math.SaturatingCounter;

import java.io.Serializable;

//TODO: handle the cases of non-helper thread benchmarks
/**
 * Helper thread interval aware least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadIntervalAwareLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    private int numEvictedL2CacheLinesPerInterval;

    private long numIntervals;
    private int numEvictedL2CacheLines;

    private IntervalStat numTotalHelperThreadL2CacheRequestsStat;

    private IntervalStat numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat;
    private IntervalStat numRedundantHitToCacheHelperThreadL2CacheRequestsStat;
    private IntervalStat numTimelyHelperThreadL2CacheRequestsStat;
    private IntervalStat numLateHelperThreadL2CacheRequestsStat;
    private IntervalStat numBadHelperThreadL2CacheRequestsStat;
    private IntervalStat numUglyHelperThreadL2CacheRequestsStat;

    private HelperThreadL2CacheRequestPollutionForInsertionPolicy pollutionForInsertionPolicy;

    private SaturatingCounter helperThreadingAggressivenessCounter;

    public HelperThreadIntervalAwareLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);

        this.numEvictedL2CacheLinesPerInterval = cache.getNumSets() * cache.getAssociativity() / 2;

        this.pollutionForInsertionPolicy = HelperThreadL2CacheRequestPollutionForInsertionPolicy.MEDIUM;

        this.numTotalHelperThreadL2CacheRequestsStat = new IntervalStat();

        this.numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numRedundantHitToCacheHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numTimelyHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numLateHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numBadHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numUglyHelperThreadL2CacheRequestsStat = new IntervalStat();

        this.helperThreadingAggressivenessCounter = new SaturatingCounter(1, 1, 5, 1);

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent param) {
                numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent param) {
                numRedundantHitToCacheHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent param) {
                numTimelyHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent param) {
                numLateHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent event) {
                numBadHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent param) {
                numUglyHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        cache.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, new Action1<GeneralCacheControllerLineReplacementEvent>() {
            @Override
            public void apply(GeneralCacheControllerLineReplacementEvent event) {
                if (event.getCacheController().getCache() == getCache()) {
                    numEvictedL2CacheLines++;

                    if (numEvictedL2CacheLines == numEvictedL2CacheLinesPerInterval) {
                        newInterval();

                        numEvictedL2CacheLines = 0;
                        numIntervals++;
                    }
                }
            }
        });
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (HelperThreadingHelper.isHelperThread(access.getThread().getId())) {
            int newStackPosition;

            switch (this.pollutionForInsertionPolicy) {
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

    private void newInterval() {
        long numTotalHelperThreadL2CacheRequests = this.numTotalHelperThreadL2CacheRequestsStat.newInterval();

        long numRedundantHitToTransientTagHelperThreadL2CacheRequests = this.numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat.newInterval();
        long numRedundantHitToCacheHelperThreadL2CacheRequests = this.numRedundantHitToCacheHelperThreadL2CacheRequestsStat.newInterval();
        long numTimelyHelperThreadL2CacheRequests = this.numTimelyHelperThreadL2CacheRequestsStat.newInterval();
        long numLateHelperThreadL2CacheRequests = this.numLateHelperThreadL2CacheRequestsStat.newInterval();
        long numBadHelperThreadL2CacheRequests = this.numBadHelperThreadL2CacheRequestsStat.newInterval();
        long numUglyHelperThreadL2CacheRequests = this.numUglyHelperThreadL2CacheRequestsStat.newInterval();

        double accuracyValue = (double) (numTimelyHelperThreadL2CacheRequests + numLateHelperThreadL2CacheRequests) / numTotalHelperThreadL2CacheRequests;
        double redundancyValue = (double) (numRedundantHitToTransientTagHelperThreadL2CacheRequests + numRedundantHitToCacheHelperThreadL2CacheRequests) / numTotalHelperThreadL2CacheRequests;
        double latenessValue = (double) numLateHelperThreadL2CacheRequests / numTotalHelperThreadL2CacheRequests;
        double pollutionValue = (double) numBadHelperThreadL2CacheRequests / numTotalHelperThreadL2CacheRequests;

        HelperThreadL2CacheRequestAccuracy accuracy = getAccuracy(accuracyValue);
        HelperThreadL2CacheRequestLateness lateness = getLateness(latenessValue);
        HelperThreadL2CacheRequestPollution pollution = getPollution(pollutionValue);

        HelperThreadAggressivenessTuningDirection aggressivenessTuningDirection = getAggressivenessTuningDirection(accuracy, lateness, pollution);

        switch (aggressivenessTuningDirection) {
            case INCREMENT:
                this.helperThreadingAggressivenessCounter.update(true);
                break;
            case NO_CHANGE:
                break;
            case DECREMENT:
                this.helperThreadingAggressivenessCounter.update(false);
                break;
        }

        HelperThreadAggressiveness aggressiveness = getAggressiveness();

        ContextMapping contextMapping = getCache().getSimulation().getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess().getContextMapping();
        contextMapping.setHelperThreadLookahead(aggressiveness.getLookahead());
        contextMapping.setHelperThreadStride(aggressiveness.getStride());

        this.pollutionForInsertionPolicy = getPollutionForInsertionPolicy(pollutionValue);
    }

    public HelperThreadAggressiveness getAggressiveness() {
        int aggressivenessCounterValue = this.helperThreadingAggressivenessCounter.getValue();

        if (aggressivenessCounterValue == 1) {
            return HelperThreadAggressiveness.VERY_CONSERVATIVE;
        } else if (aggressivenessCounterValue == 2) {
            return HelperThreadAggressiveness.CONSERVATIVE;
        } else if (aggressivenessCounterValue == 3) {
            return HelperThreadAggressiveness.MIDDLE_OF_THE_ROAD;
        } else if (aggressivenessCounterValue == 4) {
            return HelperThreadAggressiveness.AGGRESSIVE;
        } else if (aggressivenessCounterValue == 5) {
            return HelperThreadAggressiveness.VERY_AGGRESSIVE;
        }

        throw new IllegalArgumentException();
    }

    private HelperThreadAggressivenessTuningDirection getAggressivenessTuningDirection(
            HelperThreadL2CacheRequestAccuracy accuracy,
            HelperThreadL2CacheRequestLateness lateness,
            HelperThreadL2CacheRequestPollution pollution
    ) {
        if (accuracy == HelperThreadL2CacheRequestAccuracy.HIGH) {
            if (lateness == HelperThreadL2CacheRequestLateness.LATE) {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                }
            } else {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.NO_CHANGE;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            }
        } else if (accuracy == HelperThreadL2CacheRequestAccuracy.MEDIUM) {
            if (lateness == HelperThreadL2CacheRequestLateness.LATE) {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            } else {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.NO_CHANGE;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            }
        } else {
            if (lateness == HelperThreadL2CacheRequestLateness.LATE) {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            } else {
                if (pollution == HelperThreadL2CacheRequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.NO_CHANGE;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            }
        }
    }

    private HelperThreadL2CacheRequestAccuracy getAccuracy(double value) {
        if (value > helperThreadL2CacheRequestAccuracyHighThreshold) {
            return HelperThreadL2CacheRequestAccuracy.HIGH;
        } else if (value < helperThreadL2CacheRequestAccuracyLowThreshold) {
            return HelperThreadL2CacheRequestAccuracy.LOW;
        } else {
            return HelperThreadL2CacheRequestAccuracy.MEDIUM;
        }
    }

    private HelperThreadL2CacheRequestLateness getLateness(double value) {
        if (value > helperThreadL2CacheRequestLatenessThreshold) {
            return HelperThreadL2CacheRequestLateness.LATE;
        } else {
            return HelperThreadL2CacheRequestLateness.NOT_LATE;
        }
    }

    private HelperThreadL2CacheRequestPollution getPollution(double value) {
        if (value > helperThreadL2CacheRequestPollutionThreshold) {
            return HelperThreadL2CacheRequestPollution.POLLUTING;
        } else {
            return HelperThreadL2CacheRequestPollution.NOT_POLLUTING;
        }
    }

    private HelperThreadL2CacheRequestPollutionForInsertionPolicy getPollutionForInsertionPolicy(double value) {
        if (value > helperThreadL2CacheRequestPollutionHighThresholdForInsertionPolicy) {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.HIGH;
        } else if (value < helperThreadL2CacheRequestPollutionLowThresholdForInsertionPolicy) {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.LOW;
        } else {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.MEDIUM;
        }
    }

    public int getNumEvictedL2CacheLinesPerInterval() {
        return numEvictedL2CacheLinesPerInterval;
    }

    public long getNumIntervals() {
        return numIntervals;
    }

    public IntervalStat getNumTotalHelperThreadL2CacheRequestsStat() {
        return numTotalHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumRedundantHitToTransientTagHelperThreadL2CacheRequestsStat() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumRedundantHitToCacheHelperThreadL2CacheRequestsStat() {
        return numRedundantHitToCacheHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumTimelyHelperThreadL2CacheRequestsStat() {
        return numTimelyHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumLateHelperThreadL2CacheRequestsStat() {
        return numLateHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumBadHelperThreadL2CacheRequestsStat() {
        return numBadHelperThreadL2CacheRequestsStat;
    }

    public IntervalStat getNumUglyHelperThreadL2CacheRequestsStat() {
        return numUglyHelperThreadL2CacheRequestsStat;
    }

    private enum HelperThreadL2CacheRequestAccuracy {
        HIGH,
        MEDIUM,
        LOW
    }

    private enum HelperThreadL2CacheRequestLateness {
        LATE,
        NOT_LATE
    }

    private enum HelperThreadL2CacheRequestPollution {
        NOT_POLLUTING,
        POLLUTING
    }

    private enum HelperThreadL2CacheRequestPollutionForInsertionPolicy {
        HIGH,
        MEDIUM,
        LOW
    }

    private enum HelperThreadAggressivenessTuningDirection {
        INCREMENT,
        NO_CHANGE,
        DECREMENT
    }

    private enum HelperThreadAggressiveness {
        VERY_CONSERVATIVE(4, 1),
        CONSERVATIVE(8, 1),
        MIDDLE_OF_THE_ROAD(16, 2),
        AGGRESSIVE(32, 4),
        VERY_AGGRESSIVE(64, 4);

        private int lookahead;
        private int stride;

        private HelperThreadAggressiveness(int lookahead, int stride) {
            this.lookahead = lookahead;
            this.stride = stride;
        }

        public int getLookahead() {
            return lookahead;
        }

        public int getStride() {
            return stride;
        }
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

    private static final double helperThreadL2CacheRequestAccuracyHighThreshold = 0.75;
    private static final double helperThreadL2CacheRequestAccuracyLowThreshold = 0.4;
    private static final double helperThreadL2CacheRequestLatenessThreshold = 0.01;
    private static final double helperThreadL2CacheRequestPollutionThreshold = 0.005;

    private static final double helperThreadL2CacheRequestPollutionHighThresholdForInsertionPolicy = 0.25;
    private static final double helperThreadL2CacheRequestPollutionLowThresholdForInsertionPolicy = 0.005;
}
