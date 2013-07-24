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
import archimulator.sim.uncore.helperThread.HelperThreadL2CacheRequestProfilingHelper;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;

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
        private long numMainThreadL2CacheHits;
        private long numMainThreadL2CacheMisses;

        private long numHelperThreadL2CacheHits;
        private long numHelperThreadL2CacheMisses;

        private long numRedundantHitToTransientTagHelperThreadL2CacheRequests;
        private long numRedundantHitToCacheHelperThreadL2CacheRequests;

        private long numTimelyHelperThreadL2CacheRequests;
        private long numLateHelperThreadL2CacheRequests;

        private long numBadHelperThreadL2CacheRequests;

        private long numUglyHelperThreadL2CacheRequests;

        private double helperThreadL2CacheRequestAccuracy;
        private double helperThreadL2CacheRequestRedundancy;
        private double helperThreadL2CacheRequestEarliness;
        private double helperThreadL2CacheRequestLateness;
        private double helperThreadL2CacheRequestPollution;

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

        this.intervals = new ArrayList<Interval>();

        this.currentInterval = new Interval();

        simulation.getCycleAccurateEventQueue().getPerCycleEvents().add(new Action() {
            @Override
            public void apply() {
                if (simulation.getType() != SimulationType.FAST_FORWARD) {
                    numCyclesElapsed++;

                    if (numCyclesElapsed == numCyclesElapsedPerInterval) {
                        currentInterval.onCompleted();
                        intervals.add(currentInterval);

                        numCyclesElapsed = 0;
                        currentInterval = new Interval();
                    }
                }
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheHitEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheHitEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheHitEvent event) {
                currentInterval.numMainThreadL2CacheHits++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.MainThreadL2CacheMissEvent event) {
                currentInterval.numMainThreadL2CacheMisses++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheHitEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheHitEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheHitEvent event) {
                currentInterval.numHelperThreadL2CacheHits++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheMissEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheMissEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.HelperThreadL2CacheMissEvent event) {
                currentInterval.numHelperThreadL2CacheMisses++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToTransientTagHelperThreadL2CacheRequestEvent event) {
                currentInterval.numRedundantHitToTransientTagHelperThreadL2CacheRequests++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.RedundantHitToCacheHelperThreadL2CacheRequestEvent event) {
                currentInterval.numRedundantHitToCacheHelperThreadL2CacheRequests++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.TimelyHelperThreadL2CacheRequestEvent event) {
                currentInterval.numTimelyHelperThreadL2CacheRequests++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.LateHelperThreadL2CacheRequestEvent event) {
                currentInterval.numLateHelperThreadL2CacheRequests++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.UglyHelperThreadL2CacheRequestEvent event) {
                currentInterval.numUglyHelperThreadL2CacheRequests++;
            }
        });

        simulation.getBlockingEventDispatcher().addListener(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent.class, new Action1<HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent>() {
            @Override
            public void apply(HelperThreadL2CacheRequestProfilingHelper.BadHelperThreadL2CacheRequestEvent event) {
                currentInterval.numBadHelperThreadL2CacheRequests++;
            }
        });
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "intervalHelper") {{
            for (int i = 0; i < intervals.size(); i++) {
                Interval interval = intervals.get(i);
                getChildren().add(new ReportNode(this, "numMainThreadL2CacheHits[" + i + "]", interval.numMainThreadL2CacheHits + ""));
                getChildren().add(new ReportNode(this, "numMainThreadL2CacheMisses[" + i + "]", interval.numMainThreadL2CacheMisses + ""));

                getChildren().add(new ReportNode(this, "numHelperThreadL2CacheHits[" + i + "]", interval.numHelperThreadL2CacheHits + ""));
                getChildren().add(new ReportNode(this, "numHelperThreadL2CacheMisses[" + i + "]", interval.numHelperThreadL2CacheMisses + ""));

                getChildren().add(new ReportNode(this, "numRedundantHitToTransientTagHelperThreadL2CacheRequests[" + i + "]", interval.numRedundantHitToTransientTagHelperThreadL2CacheRequests + ""));
                getChildren().add(new ReportNode(this, "numRedundantHitToCacheHelperThreadL2CacheRequests[" + i + "]", interval.numRedundantHitToCacheHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numTimelyHelperThreadL2CacheRequests[" + i + "]", interval.numTimelyHelperThreadL2CacheRequests + ""));
                getChildren().add(new ReportNode(this, "numLateHelperThreadL2CacheRequests[" + i + "]", interval.numLateHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numBadHelperThreadL2CacheRequests[" + i + "]", interval.numBadHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "numUglyHelperThreadL2CacheRequests[" + i + "]", interval.numUglyHelperThreadL2CacheRequests + ""));

                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestAccuracy[" + i + "]", interval.helperThreadL2CacheRequestAccuracy + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestRedundancy[" + i + "]", interval.helperThreadL2CacheRequestRedundancy + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestEarliness[" + i + "]", interval.helperThreadL2CacheRequestEarliness + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestLateness[" + i + "]", interval.helperThreadL2CacheRequestLateness + ""));
                getChildren().add(new ReportNode(this, "helperThreadL2CacheRequestPollution[" + i + "]", interval.helperThreadL2CacheRequestPollution + ""));
            }
        }});
    }
}
