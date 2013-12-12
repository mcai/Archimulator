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
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.uncore.cache.replacement.helperThread.HelperThreadIntervalAwareLRUPolicy;
import archimulator.sim.uncore.coherence.event.GeneralCacheControllerLineReplacementEvent;
import archimulator.sim.uncore.coherence.msi.controller.DirectoryController;
import archimulator.util.IntervalCounter;
import net.pickapack.math.SaturatingCounter;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;
import java.util.TreeMap;

/**
 * Feedback directed helper threading helper.
 *
 * @author Min Cai
 */
public class FeedbackDirectedHelperThreadingHelper implements Reportable {
    private Simulation simulation;

    private int numEvictedL2LinesPerInterval;

    private long numIntervals;
    private int numEvictedL2Lines;

    private IntervalCounter numTotalHelperThreadL2RequestsStat;

    private IntervalCounter numRedundantHitToTransientTagHelperThreadL2RequestsStat;
    private IntervalCounter numRedundantHitToCacheHelperThreadL2RequestsStat;
    private IntervalCounter numTimelyHelperThreadL2RequestsStat;
    private IntervalCounter numLateHelperThreadL2RequestsStat;
    private IntervalCounter numBadHelperThreadL2RequestsStat;
    private IntervalCounter numEarlyHelperThreadL2RequestsStat;
    private IntervalCounter numUglyHelperThreadL2RequestsStat;

    private HelperThreadL2RequestPollutionForInsertionPolicy pollutionForInsertionPolicy;

    private SaturatingCounter helperThreadingAggressivenessCounter;

    private SummaryStatistics statAccuracy;
    private SummaryStatistics statLateness;
    private SummaryStatistics statPollution;
    private SummaryStatistics statMemoryBandwidthContention;

    private Map<HelperThreadL2RequestAccuracy, Long> accuracyDistribution;
    private Map<HelperThreadL2RequestLateness, Long> latenessDistribution;
    private Map<HelperThreadL2RequestPollution, Long> pollutionDistribution;
    private Map<HelperThreadAggressiveness, Long> aggressivenessDistribution;
    private Map<HelperThreadL2RequestPollutionForInsertionPolicy, Long> pollutionForInsertionPolicyDistribution;

    /**
     * Create a feedback directed helper threading helper.
     *
     * @param simulation the simulation
     */
    public FeedbackDirectedHelperThreadingHelper(Simulation simulation) {
        this.simulation = simulation;

        final DirectoryController l2Controller = simulation.getProcessor().getMemoryHierarchy().getL2Controller();

        this.numEvictedL2LinesPerInterval = l2Controller.getCache().getNumSets() * l2Controller.getCache().getAssociativity() / 2;

        this.pollutionForInsertionPolicy = HelperThreadL2RequestPollutionForInsertionPolicy.MEDIUM;

        this.numTotalHelperThreadL2RequestsStat = new IntervalCounter();

        this.numRedundantHitToTransientTagHelperThreadL2RequestsStat = new IntervalCounter();
        this.numRedundantHitToCacheHelperThreadL2RequestsStat = new IntervalCounter();
        this.numTimelyHelperThreadL2RequestsStat = new IntervalCounter();
        this.numLateHelperThreadL2RequestsStat = new IntervalCounter();
        this.numBadHelperThreadL2RequestsStat = new IntervalCounter();
        this.numEarlyHelperThreadL2RequestsStat = new IntervalCounter();
        this.numUglyHelperThreadL2RequestsStat = new IntervalCounter();

        this.helperThreadingAggressivenessCounter = new SaturatingCounter(1, 1, 5, 1);

        this.statAccuracy = new SummaryStatistics();
        this.statLateness = new SummaryStatistics();
        this.statPollution = new SummaryStatistics();
        this.statMemoryBandwidthContention = new SummaryStatistics();

        this.accuracyDistribution = new TreeMap<>();
        this.latenessDistribution = new TreeMap<>();
        this.pollutionDistribution = new TreeMap<>();
        this.aggressivenessDistribution = new TreeMap<>();
        this.pollutionForInsertionPolicyDistribution = new TreeMap<>();

        l2Controller.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            int numPendingMemoryAccesses = l2Controller.getNumPendingMemoryAccesses();
            if (numPendingMemoryAccesses > 0) {
                statMemoryBandwidthContention.addValue(numPendingMemoryAccesses);
            }
        });

        //TODO: should not be hardcoded!!!
        if (!(l2Controller.getCache().getReplacementPolicy() instanceof HelperThreadIntervalAwareLRUPolicy)) {
            return;
        }

        l2Controller.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2RequestEvent.class, event -> {
            switch (event.getQuality()) {
                case REDUNDANT_HIT_TO_TRANSIENT_TAG:
                    numRedundantHitToTransientTagHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case REDUNDANT_HIT_TO_CACHE:
                    numRedundantHitToCacheHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case TIMELY:
                    numTimelyHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case LATE:
                    numLateHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case BAD:
                    numBadHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case EARLY:
                    numEarlyHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                case UGLY:
                    numUglyHelperThreadL2RequestsStat.increment();
                    numTotalHelperThreadL2RequestsStat.increment();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });

        l2Controller.getBlockingEventDispatcher().addListener(GeneralCacheControllerLineReplacementEvent.class, event -> {
            if (event.getCacheController() == l2Controller) {
                numEvictedL2Lines++;

                if (numEvictedL2Lines == numEvictedL2LinesPerInterval) {
                    newInterval();

                    numEvictedL2Lines = 0;
                    numIntervals++;
                }
            }
        });
    }

    /**
     * New interval.
     */
    private void newInterval() {
        long numTotalHelperThreadL2Requests = this.numTotalHelperThreadL2RequestsStat.newInterval();
        long numRedundantHitToTransientTagHelperThreadL2Requests = this.numRedundantHitToTransientTagHelperThreadL2RequestsStat.newInterval();
        long numRedundantHitToCacheHelperThreadL2Requests = this.numRedundantHitToCacheHelperThreadL2RequestsStat.newInterval();
        long numTimelyHelperThreadL2Requests = this.numTimelyHelperThreadL2RequestsStat.newInterval();
        long numLateHelperThreadL2Requests = this.numLateHelperThreadL2RequestsStat.newInterval();
        long numBadHelperThreadL2Requests = this.numBadHelperThreadL2RequestsStat.newInterval();
        long numEarlyHelperThreadL2Requests = this.numEarlyHelperThreadL2RequestsStat.newInterval();
        long numUglyHelperThreadL2Requests = this.numUglyHelperThreadL2RequestsStat.newInterval();

        double accuracyRatio = (double) (numTimelyHelperThreadL2Requests + numLateHelperThreadL2Requests) / numTotalHelperThreadL2Requests;
        double redundancyRatio = (double) (numRedundantHitToTransientTagHelperThreadL2Requests + numRedundantHitToCacheHelperThreadL2Requests) / numTotalHelperThreadL2Requests;
        double latenessRatio = (double) numLateHelperThreadL2Requests / numTotalHelperThreadL2Requests;
        double pollutionRatio = (double) numBadHelperThreadL2Requests / numTotalHelperThreadL2Requests;

        HelperThreadL2RequestAccuracy accuracy = getAccuracy(accuracyRatio);
        HelperThreadL2RequestLateness lateness = getLateness(latenessRatio);
        HelperThreadL2RequestPollution pollution = getPollution(pollutionRatio);

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
            HelperThreadL2RequestAccuracy accuracy,
            HelperThreadL2RequestLateness lateness,
            HelperThreadL2RequestPollution pollution
    ) {
        if (accuracy == HelperThreadL2RequestAccuracy.HIGH) {
            if (lateness == HelperThreadL2RequestLateness.LATE) {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                }
            } else {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.NO_CHANGE;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            }
        } else if (accuracy == HelperThreadL2RequestAccuracy.MEDIUM) {
            if (lateness == HelperThreadL2RequestLateness.LATE) {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.INCREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            } else {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.NO_CHANGE;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            }
        } else {
            if (lateness == HelperThreadL2RequestLateness.LATE) {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                } else {
                    return HelperThreadAggressivenessTuningDirection.DECREMENT;
                }
            } else {
                if (pollution == HelperThreadL2RequestPollution.NOT_POLLUTING) {
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
    private HelperThreadL2RequestAccuracy getAccuracy(double accuracyRatio) {
        if (accuracyRatio > helperThreadL2RequestAccuracyHighThreshold) {
            return HelperThreadL2RequestAccuracy.HIGH;
        } else if (accuracyRatio < helperThreadL2RequestAccuracyLowThreshold) {
            return HelperThreadL2RequestAccuracy.LOW;
        } else {
            return HelperThreadL2RequestAccuracy.MEDIUM;
        }
    }

    /**
     * Get the lateness.
     *
     * @param latenessRatio the lateness ratio
     * @return the lateness
     */
    private HelperThreadL2RequestLateness getLateness(double latenessRatio) {
        if (latenessRatio > helperThreadL2RequestLatenessThreshold) {
            return HelperThreadL2RequestLateness.LATE;
        } else {
            return HelperThreadL2RequestLateness.NOT_LATE;
        }
    }

    /**
     * Get the pollution.
     *
     * @param pollutionRatio the pollution ratio
     * @return the pollution
     */
    private HelperThreadL2RequestPollution getPollution(double pollutionRatio) {
        if (pollutionRatio > helperThreadL2RequestPollutionThreshold) {
            return HelperThreadL2RequestPollution.POLLUTING;
        } else {
            return HelperThreadL2RequestPollution.NOT_POLLUTING;
        }
    }

    /**
     * Get the pollution for insertion policy.
     *
     * @param pollutionForInsertionPolicyRatio
     *         the pollution for insertion policy ratio
     * @return the pollution for insertion policy
     */
    private HelperThreadL2RequestPollutionForInsertionPolicy getPollutionForInsertionPolicy(double pollutionForInsertionPolicyRatio) {
        if (pollutionForInsertionPolicyRatio > helperThreadL2RequestPollutionHighThresholdForInsertionPolicy) {
            return HelperThreadL2RequestPollutionForInsertionPolicy.HIGH;
        } else if (pollutionForInsertionPolicyRatio < helperThreadL2RequestPollutionLowThresholdForInsertionPolicy) {
            return HelperThreadL2RequestPollutionForInsertionPolicy.LOW;
        } else {
            return HelperThreadL2RequestPollutionForInsertionPolicy.MEDIUM;
        }
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "feedbackDirectedHelperThreadingHelper") {{
            getChildren().add(new ReportNode(this, "numEvictedL2LinesPerInterval", getNumEvictedL2LinesPerInterval() + ""));
            getChildren().add(new ReportNode(this, "numIntervals", getNumIntervals() + ""));
            getChildren().add(new ReportNode(this, "numTotalHelperThreadL2RequestsStat", getNumTotalHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2RequestsStat", getNumRedundantHitToTransientTagHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2RequestsStat", getNumRedundantHitToCacheHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2RequestsStat", getNumTimelyHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numLateHelperThreadL2RequestsStat", getNumLateHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numBadHelperThreadL2RequestsStat", getNumBadHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numEarlyHelperThreadL2RequestsStat", getNumEarlyHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "numUglyHelperThreadL2RequestsStat", getNumUglyHelperThreadL2RequestsStat() + ""));
            getChildren().add(new ReportNode(this, "pollutionForInsertionPolicy", getPollutionForInsertionPolicy() + ""));
            getChildren().add(new ReportNode(this, "statAccuracy", getStatAccuracy() + ""));
            getChildren().add(new ReportNode(this, "statLateness", getStatLateness() + ""));
            getChildren().add(new ReportNode(this, "statPollution", getStatPollution() + ""));
            getChildren().add(new ReportNode(this, "statMemoryBandwidthContention", getStatMemoryBandwidthContention() + ""));
            getChildren().add(new ReportNode(this, "accuracyDistribution", getAccuracyDistribution() + ""));
            getChildren().add(new ReportNode(this, "latenessDistribution", getLatenessDistribution() + ""));
            getChildren().add(new ReportNode(this, "pollutionDistribution", getPollutionDistribution() + ""));
            getChildren().add(new ReportNode(this, "aggressivenessDistribution", getAggressivenessDistribution() + ""));
            getChildren().add(new ReportNode(this, "pollutionForInsertionPolicyDistribution", getPollutionForInsertionPolicyDistribution() + ""));
        }});
    }

    /**
     * Get the number of evicted L2 cache lines per interval.
     *
     * @return the number of evicted L2 cache lines per interval
     */
    public int getNumEvictedL2LinesPerInterval() {
        return numEvictedL2LinesPerInterval;
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
    public IntervalCounter getNumTotalHelperThreadL2RequestsStat() {
        return numTotalHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of redundant "hit to transient tag" helper thread L2 cache requests.
     *
     * @return the interval stat of the number of redundant "hit to transient tag" helper thread L2 cache requests
     */
    public IntervalCounter getNumRedundantHitToTransientTagHelperThreadL2RequestsStat() {
        return numRedundantHitToTransientTagHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of redundant "hit to cache" helper thread L2 cache requests.
     *
     * @return the interval stat of the number of redundant "hit to cache" helper thread L2 cache requests
     */
    public IntervalCounter getNumRedundantHitToCacheHelperThreadL2RequestsStat() {
        return numRedundantHitToCacheHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of timely helper thread L2 cache requests.
     *
     * @return the interval stat of the number of timely helper thread L2 cache requests
     */
    public IntervalCounter getNumTimelyHelperThreadL2RequestsStat() {
        return numTimelyHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of late helper thread L2 cache requests.
     *
     * @return the interval stat of the number of late helper thread L2 cache requests
     */
    public IntervalCounter getNumLateHelperThreadL2RequestsStat() {
        return numLateHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of bad helper thread L2 cache requests.
     *
     * @return the interval stat of the number of bad helper thread L2 cache requests
     */
    public IntervalCounter getNumBadHelperThreadL2RequestsStat() {
        return numBadHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of early helper thread L2 cache requests.
     *
     * @return the interval stat of the number of early helper thread L2 cache requests
     */
    public IntervalCounter getNumEarlyHelperThreadL2RequestsStat() {
        return numEarlyHelperThreadL2RequestsStat;
    }

    /**
     * Get the interval stat of the number of ugly helper thread L2 cache requests.
     *
     * @return the interval stat of the number of ugly helper thread L2 cache requests
     */
    public IntervalCounter getNumUglyHelperThreadL2RequestsStat() {
        return numUglyHelperThreadL2RequestsStat;
    }

    /**
     * Get the pollution for insertion policy.
     *
     * @return the pollution for insertion policy
     */
    public HelperThreadL2RequestPollutionForInsertionPolicy getPollutionForInsertionPolicy() {
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
    public Map<HelperThreadL2RequestAccuracy, Long> getAccuracyDistribution() {
        return accuracyDistribution;
    }

    /**
     * Get the distribution of lateness per interval.
     *
     * @return the distribution of lateness per interval
     */
    public Map<HelperThreadL2RequestLateness, Long> getLatenessDistribution() {
        return latenessDistribution;
    }

    /**
     * Get the distribution of pollution per interval.
     *
     * @return the distribution of pollution per interval
     */
    public Map<HelperThreadL2RequestPollution, Long> getPollutionDistribution() {
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
    public Map<HelperThreadL2RequestPollutionForInsertionPolicy, Long> getPollutionForInsertionPolicyDistribution() {
        return pollutionForInsertionPolicyDistribution;
    }

    /**
     * Helper thread L2 cache request accuracy.
     */
    public enum HelperThreadL2RequestAccuracy {
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
    public enum HelperThreadL2RequestLateness {
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
    public enum HelperThreadL2RequestPollution {
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
    public enum HelperThreadL2RequestPollutionForInsertionPolicy {
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

    private static final double helperThreadL2RequestAccuracyHighThreshold = 0.75;
    private static final double helperThreadL2RequestAccuracyLowThreshold = 0.4;
    private static final double helperThreadL2RequestLatenessThreshold = 0.01;
    private static final double helperThreadL2RequestPollutionThreshold = 0.005;

    private static final double helperThreadL2RequestPollutionHighThresholdForInsertionPolicy = 0.25;
    private static final double helperThreadL2RequestPollutionLowThresholdForInsertionPolicy = 0.005;
}
