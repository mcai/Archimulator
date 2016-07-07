/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
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

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numMainThreadDynamicInstructionsCommitted/%d", i),
                                String.format("%d", interval.numMainThreadDynamicInstructionsCommitted)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numHelperThreadDynamicInstructionsCommitted/%d", i),
                                String.format("%d", interval.numHelperThreadDynamicInstructionsCommitted)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numMainThreadL2Hits/%d", i),
                                String.format("%d", interval.numMainThreadL2Hits)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numMainThreadL2Misses/%d", i),
                                String.format("%d", interval.numMainThreadL2Misses)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numHelperThreadL2Hits/%d", i),
                                String.format("%d", interval.numHelperThreadL2Hits)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numHelperThreadL2Misses/%d", i),
                                String.format("%d", interval.numHelperThreadL2Misses)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numRedundantHitToTransientTagHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numRedundantHitToTransientTagHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numRedundantHitToCacheHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numRedundantHitToCacheHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numTimelyHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numTimelyHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numLateHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numLateHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numBadHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numBadHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numEarlyHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numEarlyHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numUglyHelperThreadL2Requests/%d", i),
                                String.format("%d", interval.numUglyHelperThreadL2Requests)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("mainThreadIpc/%d", i),
                                String.format("%s", interval.mainThreadIpc)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("helperThreadIpc/%d", i),
                                String.format("%s", interval.helperThreadIpc)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("mainThreadCpi/%d", i),
                                String.format("%s", interval.mainThreadCpi)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("helperThreadCpi/%d", i),
                                String.format("%s", interval.helperThreadCpi)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("mainThreadMpki/%d", i),
                                String.format("%s", interval.mainThreadMpki)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("helperThreadMpki/%d", i),
                                String.format("%s", interval.helperThreadMpki)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("averageL2MissMlpCost/%d", i),
                                String.format(
                                        "%s",
                                        interval.numL2MissMlpSamples == 0 ? 0 : interval.l2MissMlpCosts / interval.numL2MissMlpSamples
                                )
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numL2MissMlpSamples/%d", i),
                                String.format("%s", interval.numL2MissMlpSamples)
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("averageDramBankAccessBlpCost/%d", i),
                                String.format(
                                        "%s",
                                        interval.numDramBankAccessBlpSamples == 0 ? 0 : interval.dramBankAccessBlpCosts / interval.numDramBankAccessBlpSamples
                                )
                        )
                );

                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("numDramBankAccessBlpSamples/%d", i),
                                String.format("%s", interval.numDramBankAccessBlpSamples)
                        )
                );
            }
        }});
    }
}
