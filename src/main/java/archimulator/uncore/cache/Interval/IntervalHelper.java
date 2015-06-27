/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.Interval;

import archimulator.common.Simulation;
import archimulator.common.SimulationType;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.core.Thread;
import archimulator.core.event.DynamicInstructionCommittedEvent;
import archimulator.uncore.helperThread.HelperThreadL2RequestProfilingHelper;
import archimulator.uncore.helperThread.HelperThreadingHelper;
import archimulator.uncore.mlp.BLPProfilingHelper;
import archimulator.uncore.mlp.MLPProfilingHelper;

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

        private long numMainThreadL2Hits;
        private long numMainThreadL2Misses;

        private long numHelperThreadL2Hits;
        private long numHelperThreadL2Misses;

        private long numRedundantHitToTransientTagHelperThreadL2Requests;
        private long numRedundantHitToCacheHelperThreadL2Requests;

        private long numTimelyHelperThreadL2Requests;
        private long numLateHelperThreadL2Requests;

        private long numBadHelperThreadL2Requests;

        private long numEarlyHelperThreadL2Requests;

        private long numUglyHelperThreadL2Requests;

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
            mainThreadIpc = (double) numMainThreadDynamicInstructionsCommitted / numCyclesElapsedPerInterval;
            helperThreadIpc = (double) numHelperThreadDynamicInstructionsCommitted / numCyclesElapsedPerInterval;

            mainThreadCpi = (double) numCyclesElapsedPerInterval / numMainThreadDynamicInstructionsCommitted;
            helperThreadCpi = (double) numCyclesElapsedPerInterval / numHelperThreadDynamicInstructionsCommitted;

            mainThreadMpki = (double) numMainThreadL2Misses / ((double) numMainThreadDynamicInstructionsCommitted / 1000);
            helperThreadMpki = (double) numHelperThreadL2Misses / ((double) numHelperThreadDynamicInstructionsCommitted / 1000);
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

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.MainThreadL2HitEvent.class, event -> {
            currentInterval.numMainThreadL2Hits++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.MainThreadL2MissEvent.class, event -> {
            currentInterval.numMainThreadL2Misses++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2HitEvent.class, event -> {
            currentInterval.numHelperThreadL2Hits++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2MissEvent.class, event -> {
            currentInterval.numHelperThreadL2Misses++;
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2RequestProfilingHelper.HelperThreadL2RequestEvent.class, event -> {
            switch (event.getQuality()) {
                case REDUNDANT_HIT_TO_TRANSIENT_TAG:
                    currentInterval.numRedundantHitToTransientTagHelperThreadL2Requests++;
                    break;
                case REDUNDANT_HIT_TO_CACHE:
                    currentInterval.numRedundantHitToCacheHelperThreadL2Requests++;
                    break;
                case TIMELY:
                    currentInterval.numTimelyHelperThreadL2Requests++;
                    break;
                case LATE:
                    currentInterval.numLateHelperThreadL2Requests++;
                    break;
                case BAD:
                    currentInterval.numBadHelperThreadL2Requests++;
                    break;
                case EARLY:
                    currentInterval.numEarlyHelperThreadL2Requests++;
                    break;
                case UGLY:
                    currentInterval.numUglyHelperThreadL2Requests++;
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

                getChildren().add(new ReportNode(this, "numMainThreadL2Hits[" + i + "]", interval.numMainThreadL2Hits + ""));
                getChildren().add(new ReportNode(this, "numMainThreadL2Misses[" + i + "]", interval.numMainThreadL2Misses + ""));

                getChildren().add(new ReportNode(this, "numHelperThreadL2Hits[" + i + "]", interval.numHelperThreadL2Hits + ""));
                getChildren().add(new ReportNode(this, "numHelperThreadL2Misses[" + i + "]", interval.numHelperThreadL2Misses + ""));

                getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2Requests[" + i + "]", interval.numRedundantHitToTransientTagHelperThreadL2Requests + ""));
                getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2Requests[" + i + "]", interval.numRedundantHitToCacheHelperThreadL2Requests + ""));

                getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2Requests[" + i + "]", interval.numTimelyHelperThreadL2Requests + ""));
                getChildren().add(new ReportNode(this, "numLateHelperThreadL2Requests[" + i + "]", interval.numLateHelperThreadL2Requests + ""));

                getChildren().add(new ReportNode(this, "numBadHelperThreadL2Requests[" + i + "]", interval.numBadHelperThreadL2Requests + ""));

                getChildren().add(new ReportNode(this, "numEarlyHelperThreadL2Requests[" + i + "]", interval.numEarlyHelperThreadL2Requests + ""));

                getChildren().add(new ReportNode(this, "numUglyHelperThreadL2Requests[" + i + "]", interval.numUglyHelperThreadL2Requests + ""));

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
