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
package archimulator.sim.uncore.cache.Interval;

import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationType;
import archimulator.sim.common.report.ReportNode;
import archimulator.sim.common.report.Reportable;
import archimulator.sim.core.Thread;
import archimulator.sim.core.event.DynamicInstructionCommittedEvent;
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;
import archimulator.sim.uncore.mlp.BLPProfilingHelper;
import archimulator.sim.uncore.mlp.MLPProfilingHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval helper.
 *
 * @author Min Cai
 */
public class IntervalHelper implements Reportable {
    /**
     * Interval.
     */
    private class Interval {
        private long numMainThreadDynamicInstructionsCommitted;
        private long numHelperThreadDynamicInstructionsCommitted;

        private long numMainThreadL2CacheHits;
        private long numMainThreadL2CacheMisses;

        private long numHelperThreadL2CacheHits;
        private long numHelperThreadL2CacheMisses;

        private long numRedundantHitToTransientTagHelperThreadL2CacheRequests;
        private long numRedundantHitToCacheHelperThreadL2CacheRequests;

        private long numTimelyHelperThreadL2CacheRequests;
        private long numLateHelperThreadL2CacheRequests;

        private long numBadHelperThreadL2CacheRequests;

        private long numEarlyHelperThreadL2CacheRequests;

        private long numUglyHelperThreadL2CacheRequests;

        private double helperThreadL2CacheRequestAccuracy;
        private double helperThreadL2CacheRequestRedundancy;
        private double helperThreadL2CacheRequestEarliness;
        private double helperThreadL2CacheRequestLateness;
        private double helperThreadL2CacheRequestPollution;

        private double mainThreadIpc;
        private double helperThreadIpc;

        private double mainThreadCpi;
        private double helperThreadCpi;

        private double mainThreadMpki;
        private double helperThreadMpki;

        private double l2MissMlpCosts;
        private double numL2MissMlpSamples;

        private double dramBankAccessBlpCosts;
        private double numDramBankAccessBlpSamples;

        /**
         * Handle when this interval is completed.
         */
        public void onCompleted() {
            long numTotalHelperThreadL2CacheRequests = numHelperThreadL2CacheHits + numHelperThreadL2CacheMisses;
            long numUsefulHelperThreadL2CacheRequests = numTimelyHelperThreadL2CacheRequests + numLateHelperThreadL2CacheRequests;

            helperThreadL2CacheRequestAccuracy = (double) numUsefulHelperThreadL2CacheRequests / numTotalHelperThreadL2CacheRequests;
            helperThreadL2CacheRequestRedundancy = (double) (numRedundantHitToTransientTagHelperThreadL2CacheRequests + numRedundantHitToCacheHelperThreadL2CacheRequests) / numUsefulHelperThreadL2CacheRequests;
            helperThreadL2CacheRequestEarliness = (double) numUglyHelperThreadL2CacheRequests / numUsefulHelperThreadL2CacheRequests;
            helperThreadL2CacheRequestLateness = (double) numLateHelperThreadL2CacheRequests / numUsefulHelperThreadL2CacheRequests;
            helperThreadL2CacheRequestPollution = (double) numBadHelperThreadL2CacheRequests / numUsefulHelperThreadL2CacheRequests;

            mainThreadIpc = (double) numMainThreadDynamicInstructionsCommitted / numCyclesElapsedPerInterval;
            helperThreadIpc = (double) numHelperThreadDynamicInstructionsCommitted / numCyclesElapsedPerInterval;

            mainThreadCpi = (double) numCyclesElapsedPerInterval / numMainThreadDynamicInstructionsCommitted;
            helperThreadCpi = (double) numCyclesElapsedPerInterval / numHelperThreadDynamicInstructionsCommitted;

            mainThreadMpki = (double) numMainThreadL2CacheMisses / ((double) numMainThreadDynamicInstructionsCommitted / 1000);
            helperThreadMpki = (double) numHelperThreadL2CacheMisses / ((double) numHelperThreadDynamicInstructionsCommitted / 1000);
        }
    }

    private int numCyclesElapsedPerInterval;

    private int numCyclesElapsed;

    private List<Interval> intervals;

    private Interval currentInterval;

    /**
     * Create an interval helper.
     *
     * @param simulation the simulation
     */
    public IntervalHelper(final Simulation simulation) {
        this.numCyclesElapsedPerInterval = 5000000;

        this.intervals = new ArrayList<>();

        this.currentInterval = new Interval();

        simulation.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (simulation.getType() != SimulationType.FAST_FORWARD) {
                numCyclesElapsed++;

                if (numCyclesElapsed == numCyclesElapsedPerInterval) {
                    currentInterval.onCompleted();
                    intervals.add(currentInterval);

                    numCyclesElapsed = 0;
                    currentInterval = new Interval();
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(DynamicInstructionCommittedEvent.class, event -> {
            Thread thread = event.getDynamicInstruction().getThread();

            if (HelperThreadingHelper.isMainThread(thread)) {
                currentInterval.numMainThreadDynamicInstructionsCommitted++;
            } else if (HelperThreadingHelper.isHelperThread(thread)) {
                currentInterval.numHelperThreadDynamicInstructionsCommitted++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheHitEvent.class, event -> {
            currentInterval.numMainThreadL2CacheHits++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent.class, event -> {
            currentInterval.numMainThreadL2CacheMisses++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheHitEvent.class, event -> {
            currentInterval.numHelperThreadL2CacheHits++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheMissEvent.class, event -> {
            currentInterval.numHelperThreadL2CacheMisses++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheRequestEvent.class, event -> {
            switch (event.getQuality()) {
                case REDUNDANT_HIT_TO_TRANSIENT_TAG:
                    currentInterval.numRedundantHitToTransientTagHelperThreadL2CacheRequests++;
                    break;
                case REDUNDANT_HIT_TO_CACHE:
                    currentInterval.numRedundantHitToCacheHelperThreadL2CacheRequests++;
                    break;
                case TIMELY:
                    currentInterval.numTimelyHelperThreadL2CacheRequests++;
                    break;
                case LATE:
                    currentInterval.numLateHelperThreadL2CacheRequests++;
                    break;
                case BAD:
                    currentInterval.numBadHelperThreadL2CacheRequests++;
                    break;
                case EARLY:
                    currentInterval.numEarlyHelperThreadL2CacheRequests++;
                    break;
                case UGLY:
                    currentInterval.numUglyHelperThreadL2CacheRequests++;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        });

        simulation.getBlockingEventDispatcher().addListener(MLPProfilingHelper.L2MissMLPProfiledEvent.class, event -> {
            currentInterval.l2MissMlpCosts += event.getPendingL2Miss().getMlpCost();
            currentInterval.numL2MissMlpSamples++;
        });

        simulation.getBlockingEventDispatcher().addListener(BLPProfilingHelper.BankAccessBLPProfiledEvent.class, event -> {
            currentInterval.dramBankAccessBlpCosts += event.getPendingDramBankAccess().getBlpCost();
            currentInterval.numDramBankAccessBlpSamples++;
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "intervalHelper") {{
            for (int i = 0; i < intervals.size(); i++) {
                Interval interval = intervals.get(i);
                getChildren().add(new ReportNode(this, "numMainThreadDynamicInstructionsCommitted[" + i + "]", interval.numMainThreadDynamicInstructionsCommitted + ""));
                getChildren().add(new ReportNode(this, "numHelperThreadDynamicInstructionsCommitted[" + i + "]", interval.numHelperThreadDynamicInstructionsCommitted + ""));

                getChildren().add(new ReportNode(this, "numMainThreadL2CacheHits[" + i + "]", interval.numMainThreadL2CacheHits + ""));
                getChildren().add(new ReportNode(this, "numMainThreadL2CacheMisses[" + i + "]", interval.numMainThreadL2CacheMisses + ""));

                getChildren().add(new ReportNode(this, "numHelperThreadL2CacheHits[" + i + "]", interval.numHelperThreadL2CacheHits + ""));
                getChildren().add(new ReportNode(this, "numHelperThreadL2CacheMisses[" + i + "]", interval.numHelperThreadL2CacheMisses + ""));

                getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2CacheRequests[" + i + "]", interval.numRedundantHitToTransientTagHelperThreadL2CacheRequests + ""));
                getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2CacheRequests[" + i + "]", interval.numRedundantHitToCacheHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2CacheRequests[" + i + "]", interval.numTimelyHelperThreadL2CacheRequests + ""));
                getChildren().add(new ReportNode(this, "numLateHelperThreadL2CacheRequests[" + i + "]", interval.numLateHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numBadHelperThreadL2CacheRequests[" + i + "]", interval.numBadHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numEarlyHelperThreadL2CacheRequests[" + i + "]", interval.numEarlyHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numUglyHelperThreadL2CacheRequests[" + i + "]", interval.numUglyHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestAccuracy[" + i + "]", interval.helperThreadL2CacheRequestAccuracy + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestRedundancy[" + i + "]", interval.helperThreadL2CacheRequestRedundancy + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestEarliness[" + i + "]", interval.helperThreadL2CacheRequestEarliness + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestLateness[" + i + "]", interval.helperThreadL2CacheRequestLateness + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestPollution[" + i + "]", interval.helperThreadL2CacheRequestPollution + ""));

                getChildren().add(new ReportNode(this, "mainThreadIpc[" + i + "]", interval.mainThreadIpc + ""));
                getChildren().add(new ReportNode(this, "helperThreadIpc[" + i + "]", interval.helperThreadIpc + ""));

                getChildren().add(new ReportNode(this, "mainThreadCpi[" + i + "]", interval.mainThreadCpi + ""));
                getChildren().add(new ReportNode(this, "helperThreadCpi[" + i + "]", interval.helperThreadCpi + ""));

                getChildren().add(new ReportNode(this, "mainThreadMpki[" + i + "]", interval.mainThreadMpki + ""));
                getChildren().add(new ReportNode(this, "helperThreadMpki[" + i + "]", interval.helperThreadMpki + ""));

                getChildren().add(new ReportNode(this, "averageL2MissMlpCost[" + i + "]", (interval.numL2MissMlpSamples == 0 ? 0 : interval.l2MissMlpCosts / interval.numL2MissMlpSamples) + ""));
                getChildren().add(new ReportNode(this, "numL2MissMlpSamples[" + i + "]", interval.numL2MissMlpSamples + ""));

                getChildren().add(new ReportNode(this, "averageDramBankAccessBlpCost[" + i + "]", (interval.numDramBankAccessBlpSamples == 0 ? 0 : interval.dramBankAccessBlpCosts / interval.numDramBankAccessBlpSamples) + ""));
                getChildren().add(new ReportNode(this, "numDramBankAccessBlpSamples[" + i + "]", interval.numDramBankAccessBlpSamples + ""));
            }
        }});
    }
}
