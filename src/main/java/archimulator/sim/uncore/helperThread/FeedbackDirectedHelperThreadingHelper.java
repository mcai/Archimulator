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
package archimulator.sim.uncore.helperThread;

import archimulator.model.ContextMapping;
import archimulator.sim.common.Simulation;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadIntervalAwareLRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.util.IntervalStat;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.math.SaturatingCounter;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;
import java.util.TreeMap;

/**
 * Feedback directed helper threading helper.
 *
 * @author Min Cai
 */
public class FeedbackDirectedHelperThreadingHelper {
    private Simulation simulation;

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

    private SummaryStatistics statAccuracy;
    private SummaryStatistics statLateness;
    private SummaryStatistics statPollution;
    private SummaryStatistics statMemoryBandwidthContention;

    private Map<HelperThreadL2CacheRequestAccuracy, Long> accuracyDistribution;
    private Map<HelperThreadL2CacheRequestLateness, Long> latenessDistribution;
    private Map<HelperThreadL2CacheRequestPollution, Long> pollutionDistribution;
    private Map<HelperThreadAggressiveness, Long> aggressivenessDistribution;
    private Map<HelperThreadL2CacheRequestPollutionForInsertionPolicy, Long> pollutionForInsertionPolicyDistribution;

    /**
     * Create a feedback directed helper threading helper.
     *
     * @param simulation the simulation
     */
    public FeedbackDirectedHelperThreadingHelper(Simulation simulation) {
        this.simulation = simulation;

        final DirectoryController l2CacheController = simulation.getProcessor().getMemoryHierarchy().getL2CacheController();

        this.numEvictedL2CacheLinesPerInterval = l2CacheController.getCache().getNumSets() * l2CacheController.getCache().getAssociativity() / 2;

        this.pollutionForInsertionPolicy = HelperThreadL2CacheRequestPollutionForInsertionPolicy.MEDIUM;

        this.numTotalHelperThreadL2CacheRequestsStat = new IntervalStat();

        this.numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numRedundantHitToCacheHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numTimelyHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numLateHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numBadHelperThreadL2CacheRequestsStat = new IntervalStat();
        this.numUglyHelperThreadL2CacheRequestsStat = new IntervalStat();

        this.helperThreadingAggressivenessCounter = new SaturatingCounter(1, 1, 5, 1);

        this.statAccuracy = new SummaryStatistics();
        this.statLateness = new SummaryStatistics();
        this.statPollution = new SummaryStatistics();
        this.statMemoryBandwidthContention = new SummaryStatistics();

        this.accuracyDistribution = new TreeMap<HelperThreadL2CacheRequestAccuracy, Long>();
        this.latenessDistribution = new TreeMap<HelperThreadL2CacheRequestLateness, Long>();
        this.pollutionDistribution = new TreeMap<HelperThreadL2CacheRequestPollution, Long>();
        this.aggressivenessDistribution = new TreeMap<HelperThreadAggressiveness, Long>();
        this.pollutionForInsertionPolicyDistribution = new TreeMap<HelperThreadL2CacheRequestPollutionForInsertionPolicy, Long>();

        l2CacheController.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                int numPendingMemoryAccesses = l2CacheController.getNumPendingMemoryAccesses();
                if (numPendingMemoryAccesses > 0) {
                    statMemoryBandwidthContention.addValue(numPendingMemoryAccesses);
                }
            }
        });

        //TODO: should not be hardcoded!!!
        if (!(l2CacheController.getCache().getReplacementPolicy() instanceof HelperThreadIntervalAwareLRUPolicy)) {
            return;
        }

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent param) {
                numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent param) {
                numRedundantHitToCacheHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent param) {
                numTimelyHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent param) {
                numLateHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent event) {
                numBadHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent param) {
                numUglyHelperThreadL2CacheRequestsStat.inc();
                numTotalHelperThreadL2CacheRequestsStat.inc();
            }
        });

        l2CacheController.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, new Action1<GeneralCacheControllerLineReplacementEvent>() {
            @Override
            public void apply(GeneralCacheControllerLineReplacementEvent event) {
                if (event.getCacheController() == l2CacheController) {
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

    /**
     * New interval.
     */
    private void newInterval() {
        long numTotalHelperThreadL2CacheRequests = this.numTotalHelperThreadL2CacheRequestsStat.newInterval();
        long numRedundantHitToTransientTagHelperThreadL2CacheRequests = this.numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat.newInterval();
        long numRedundantHitToCacheHelperThreadL2CacheRequests = this.numRedundantHitToCacheHelperThreadL2CacheRequestsStat.newInterval();
        long numTimelyHelperThreadL2CacheRequests = this.numTimelyHelperThreadL2CacheRequestsStat.newInterval();
        long numLateHelperThreadL2CacheRequests = this.numLateHelperThreadL2CacheRequestsStat.newInterval();
        long numBadHelperThreadL2CacheRequests = this.numBadHelperThreadL2CacheRequestsStat.newInterval();
        long numUglyHelperThreadL2CacheRequests = this.numUglyHelperThreadL2CacheRequestsStat.newInterval();

        double accuracyRatio = (double) (numTimelyHelperThreadL2CacheRequests + numLateHelperThreadL2CacheRequests) / numTotalHelperThreadL2CacheRequests;
        double redundancyRatio = (double) (numRedundantHitToTransientTagHelperThreadL2CacheRequests + numRedundantHitToCacheHelperThreadL2CacheRequests) / numTotalHelperThreadL2CacheRequests;
        double latenessRatio = (double) numLateHelperThreadL2CacheRequests / numTotalHelperThreadL2CacheRequests;
        double pollutionRatio = (double) numBadHelperThreadL2CacheRequests / numTotalHelperThreadL2CacheRequests;

        HelperThreadL2CacheRequestAccuracy accuracy = getAccuracy(accuracyRatio);
        HelperThreadL2CacheRequestLateness lateness = getLateness(latenessRatio);
        HelperThreadL2CacheRequestPollution pollution = getPollution(pollutionRatio);

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

        ContextMapping contextMapping = this.simulation.getProcessor().getCores().get(0).getThreads().get(0).getContext().getProcess().getContextMapping();
        contextMapping.setHelperThreadLookahead(aggressiveness.getLookahead());
        contextMapping.setHelperThreadStride(aggressiveness.getStride());

        this.pollutionForInsertionPolicy = getPollutionForInsertionPolicy(pollutionRatio);

        this.statAccuracy.addValue(accuracyRatio);
        this.statLateness.addValue(latenessRatio);
        this.statPollution.addValue(pollutionRatio);

        if (!this.accuracyDistribution.containsKey(accuracy)) {
            this.accuracyDistribution.put(accuracy, 1L);
        }

        if (!this.latenessDistribution.containsKey(lateness)) {
            this.latenessDistribution.put(lateness, 1L);
        }

        if (!this.pollutionDistribution.containsKey(pollution)) {
            this.pollutionDistribution.put(pollution, 1L);
        }

        if (!this.aggressivenessDistribution.containsKey(aggressiveness)) {
            this.aggressivenessDistribution.put(aggressiveness, 1L);
        }

        if (!this.pollutionForInsertionPolicyDistribution.containsKey(pollutionForInsertionPolicy)) {
            this.pollutionForInsertionPolicyDistribution.put(pollutionForInsertionPolicy, 1L);
        }

        this.accuracyDistribution.put(accuracy, this.accuracyDistribution.get(accuracy) + 1);
        this.latenessDistribution.put(lateness, this.latenessDistribution.get(lateness) + 1);
        this.pollutionDistribution.put(pollution, this.pollutionDistribution.get(pollution) + 1);
        this.aggressivenessDistribution.put(aggressiveness, this.aggressivenessDistribution.get(aggressiveness) + 1);
        this.pollutionForInsertionPolicyDistribution.put(pollutionForInsertionPolicy, this.pollutionForInsertionPolicyDistribution.get(pollutionForInsertionPolicy) + 1);
    }

    /**
     * Get the aggressiveness.
     *
     * @return the aggressiveness
     */
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

    /**
     * Get the aggressiveness tuning direction.
     *
     * @param accuracy  the accuracy
     * @param lateness  the lateness
     * @param pollution the pollution
     * @return the aggressiveness tuning direction
     */
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

    /**
     * Get the accuracy.
     *
     * @param accuracyRatio the accuracy ratio
     * @return the accuracy
     */
    private HelperThreadL2CacheRequestAccuracy getAccuracy(double accuracyRatio) {
        if (accuracyRatio > helperThreadL2CacheRequestAccuracyHighThreshold) {
            return HelperThreadL2CacheRequestAccuracy.HIGH;
        } else if (accuracyRatio < helperThreadL2CacheRequestAccuracyLowThreshold) {
            return HelperThreadL2CacheRequestAccuracy.LOW;
        } else {
            return HelperThreadL2CacheRequestAccuracy.MEDIUM;
        }
    }

    /**
     * Get the lateness.
     *
     * @param latenessRatio the lateness ratio
     * @return the lateness
     */
    private HelperThreadL2CacheRequestLateness getLateness(double latenessRatio) {
        if (latenessRatio > helperThreadL2CacheRequestLatenessThreshold) {
            return HelperThreadL2CacheRequestLateness.LATE;
        } else {
            return HelperThreadL2CacheRequestLateness.NOT_LATE;
        }
    }

    /**
     * Get the pollution.
     *
     * @param pollutionRatio the pollution ratio
     * @return the pollution
     */
    private HelperThreadL2CacheRequestPollution getPollution(double pollutionRatio) {
        if (pollutionRatio > helperThreadL2CacheRequestPollutionThreshold) {
            return HelperThreadL2CacheRequestPollution.POLLUTING;
        } else {
            return HelperThreadL2CacheRequestPollution.NOT_POLLUTING;
        }
    }

    /**
     * Get the pollution for insertion policy.
     *
     * @param pollutionForInsertionPolicyRatio
     *         the pollution for insertion policy ratio
     * @return the pollution for insertion policy
     */
    private HelperThreadL2CacheRequestPollutionForInsertionPolicy getPollutionForInsertionPolicy(double pollutionForInsertionPolicyRatio) {
        if (pollutionForInsertionPolicyRatio > helperThreadL2CacheRequestPollutionHighThresholdForInsertionPolicy) {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.HIGH;
        } else if (pollutionForInsertionPolicyRatio < helperThreadL2CacheRequestPollutionLowThresholdForInsertionPolicy) {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.LOW;
        } else {
            return HelperThreadL2CacheRequestPollutionForInsertionPolicy.MEDIUM;
        }
    }

    /**
     * Get the number of evicted L2 cache lines per interval.
     *
     * @return the number of evicted L2 cache lines per interval
     */
    public int getNumEvictedL2CacheLinesPerInterval() {
        return numEvictedL2CacheLinesPerInterval;
    }

    /**
     * Get the number of intervals.
     *
     * @return the number of intervals
     */
    public long getNumIntervals() {
        return numIntervals;
    }

    /**
     * Get the interval stat of the total number of helper thread L2 cache requests.
     *
     * @return the interval stat of the total number of helper thread L2 cache requests
     */
    public IntervalStat getNumTotalHelperThreadL2CacheRequestsStat() {
        return numTotalHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the interval stat of the number of redundant "hit to transient tag" helper thread L2 cache requests
     */
    public IntervalStat getNumRedundantHitToTransientTagHelperThreadL2CacheRequestsStat() {
        return numRedundantHitToTransientTagHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the interval stat of the number of redundant "hit to cache" helper thread L2 cache requests
     */
    public IntervalStat getNumRedundantHitToCacheHelperThreadL2CacheRequestsStat() {
        return numRedundantHitToCacheHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of timely helper thread L2 cache requests.
     *
     * @return the interval stat of the number of timely helper thread L2 cache requests
     */
    public IntervalStat getNumTimelyHelperThreadL2CacheRequestsStat() {
        return numTimelyHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of late helper thread L2 cache requests.
     *
     * @return the interval stat of the number of late helper thread L2 cache requests
     */
    public IntervalStat getNumLateHelperThreadL2CacheRequestsStat() {
        return numLateHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of bad helper thread L2 cache requests.
     *
     * @return the interval stat of the number of bad helper thread L2 cache requests
     */
    public IntervalStat getNumBadHelperThreadL2CacheRequestsStat() {
        return numBadHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the interval stat of the number of ugly helper thread L2 cache requests.
     *
     * @return the interval stat of the number of ugly helper thread L2 cache requests
     */
    public IntervalStat getNumUglyHelperThreadL2CacheRequestsStat() {
        return numUglyHelperThreadL2CacheRequestsStat;
    }

    /**
     * Get the pollution for insertion policy.
     *
     * @return the pollution for insertion policy
     */
    public HelperThreadL2CacheRequestPollutionForInsertionPolicy getPollutionForInsertionPolicy() {
        return pollutionForInsertionPolicy;
    }

    /**
     * Get the summary statistics of accuracy.
     *
     * @return the summary statistics of accuracy
     */
    public SummaryStatistics getStatAccuracy() {
        return statAccuracy;
    }

    /**
     * Get the summary statistics of lateness.
     *
     * @return the summary statistics of lateness
     */
    public SummaryStatistics getStatLateness() {
        return statLateness;
    }

    /**
     * Get the summary statistics of pollution.
     *
     * @return the summary statistics of pollution
     */
    public SummaryStatistics getStatPollution() {
        return statPollution;
    }

    /**
     * Get the summary statistics of memory bandwidth contention.
     *
     * @return the summary statistics of memory bandwidth contention
     */
    public SummaryStatistics getStatMemoryBandwidthContention() {
        return statMemoryBandwidthContention;
    }

    /**
     * Get the distribution of accuracy per interval.
     *
     * @return the distribution of accuracy per interval
     */
    public Map<HelperThreadL2CacheRequestAccuracy, Long> getAccuracyDistribution() {
        return accuracyDistribution;
    }

    /**
     * Get the distribution of lateness per interval.
     *
     * @return the distribution of lateness per interval
     */
    public Map<HelperThreadL2CacheRequestLateness, Long> getLatenessDistribution() {
        return latenessDistribution;
    }

    /**
     * Get the distribution of pollution per interval.
     *
     * @return the distribution of pollution per interval
     */
    public Map<HelperThreadL2CacheRequestPollution, Long> getPollutionDistribution() {
        return pollutionDistribution;
    }

    /**
     * Get the distribution of aggressiveness per interval.
     *
     * @return the distribution of aggressiveness per interval
     */
    public Map<HelperThreadAggressiveness, Long> getAggressivenessDistribution() {
        return aggressivenessDistribution;
    }

    /**
     * Get the distribution of pollution for insertion policy per interval.
     *
     * @return the distribution of pollution for insertion policy per interval
     */
    public Map<HelperThreadL2CacheRequestPollutionForInsertionPolicy, Long> getPollutionForInsertionPolicyDistribution() {
        return pollutionForInsertionPolicyDistribution;
    }

    /**
     * Helper thread L2 cache request accuracy.
     */
    public enum HelperThreadL2CacheRequestAccuracy {
        /**
         * High.
         */
        HIGH,

        /**
         * Medium.
         */
        MEDIUM,

        /**
         * Low.
         */
        LOW
    }

    /**
     * Helper thread L2 cache request lateness.
     */
    public enum HelperThreadL2CacheRequestLateness {
        /**
         * Late.
         */
        LATE,

        /**
         * Not late.
         */
        NOT_LATE
    }

    /**
     * Helper thread L2 cache request pollution.
     */
    public enum HelperThreadL2CacheRequestPollution {
        /**
         * Not polluting.
         */
        NOT_POLLUTING,

        /**
         * Polluting.
         */
        POLLUTING
    }

    /**
     * Helper thread L2 cache request pollution for insertion policy.
     */
    public enum HelperThreadL2CacheRequestPollutionForInsertionPolicy {
        /**
         * High.
         */
        HIGH,

        /**
         * Medium.
         */
        MEDIUM,

        /**
         * Low.
         */
        LOW
    }

    /**
     * Helper thread aggressiveness tuning direction.
     */
    public enum HelperThreadAggressivenessTuningDirection {
        /**
         * Increment.
         */
        INCREMENT,

        /**
         * No change.
         */
        NO_CHANGE,

        /**
         * Decrement.
         */
        DECREMENT
    }

    /**
     * Helper threading aggressiveness.
     */
    public enum HelperThreadAggressiveness {
        /**
         * Very conservative.
         */
        VERY_CONSERVATIVE(4, 1),

        /**
         * Conservative.
         */
        CONSERVATIVE(8, 1),

        /**
         * Middle of  the road.
         */
        MIDDLE_OF_THE_ROAD(16, 2),

        /**
         * Aggressive.
         */
        AGGRESSIVE(32, 4),

        /**
         * Very aggressive.
         */
        VERY_AGGRESSIVE(64, 4);

        private int lookahead;
        private int stride;

        /**
         * Create a helper thread aggressiveness.
         *
         * @param lookahead the lookahead
         * @param stride    the stride
         */
        private HelperThreadAggressiveness(int lookahead, int stride) {
            this.lookahead = lookahead;
            this.stride = stride;
        }

        /**
         * Get the lookahead.
         *
         * @return the lookahead
         */
        public int getLookahead() {
            return lookahead;
        }

        /**
         * Get the stride.
         *
         * @return the stride
         */
        public int getStride() {
            return stride;
        }
    }

    private static final double helperThreadL2CacheRequestAccuracyHighThreshold = 0.75;
    private static final double helperThreadL2CacheRequestAccuracyLowThreshold = 0.4;
    private static final double helperThreadL2CacheRequestLatenessThreshold = 0.01;
    private static final double helperThreadL2CacheRequestPollutionThreshold = 0.005;

    private static final double helperThreadL2CacheRequestPollutionHighThresholdForInsertionPolicy = 0.25;
    private static final double helperThreadL2CacheRequestPollutionLowThresholdForInsertionPolicy = 0.005;
}
