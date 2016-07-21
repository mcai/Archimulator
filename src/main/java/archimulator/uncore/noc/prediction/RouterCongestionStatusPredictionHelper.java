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
package archimulator.uncore.noc.prediction;

import archimulator.common.Experiment;
import archimulator.common.SimulationObject;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.cache.prediction.CacheBasedPredictor;
import archimulator.uncore.cache.prediction.Predictor;
import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routers.InputPort;
import archimulator.uncore.noc.routers.InputVirtualChannel;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Router congestion status prediction helper.
 *
 * @author Min Cai
 */
public class RouterCongestionStatusPredictionHelper implements Reportable {
    private Predictor<RouterCongestionStatus> routerCongestionStatusPredictor;

    /**
     * Create a router congestion status prediction helper.
     *
     * @param network the parent network
     */
    public RouterCongestionStatusPredictionHelper(Network network) {
        this.routerCongestionStatusPredictor = new CacheBasedPredictor<>(
                new SimulationObject() {
                    @Override
                    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
                        return null; //TODO
                    }

                    @Override
                    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
                        return network.getCycleAccurateEventQueue();
                    }

                    @Override
                    public Experiment<?> getExperiment() {
                        return null; //TODO
                    }

                    @Override
                    public Object getSimulation() {
                        return null; //TODO
                    }

                    @Override
                    public String getName() {
                        return "noc";
                    }
                },
                "routerCongestionStatusPredictor",
                network.getNumNodes(),
                4,
                16,
                RouterCongestionStatus.NOT_CONGESTED
        );

        network.getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            for(Node node : network.getNodes()) {
                int freeSlots = 0;

                for(InputPort inputPort : node.getRouter().getInputPorts().values()) {
                    for(InputVirtualChannel inputVirtualChannel : inputPort.getVirtualChannels()) {
                        freeSlots += node.getRouter().getFreeSlots(inputPort.getDirection(), inputVirtualChannel.getId());
                    }
                }

                getRouterCongestionStatusPredictor().update(
                        node.getId(),
                        freeSlots > 720 ? RouterCongestionStatus.NOT_CONGESTED : RouterCongestionStatus.CONGESTED
                );
            }
        });
    }

    /**
     * Dump the statistics into the specified report node.
     *
     * @param reportNode the report node
     */
    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "routerCongestionStatusPredictionHelper") {{
            routerCongestionStatusPredictor.dumpStats(this);
        }});
    }

    /**
     * Get the router congestion status predictor.
     *
     * @return the router congestion status predictor
     */
    public Predictor<RouterCongestionStatus> getRouterCongestionStatusPredictor() {
        return routerCongestionStatusPredictor;
    }
}
