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
package archimulator.uncore.net;

import archimulator.common.Experiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationEvent;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.AbstractMemoryHierarchy;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.routers.FlitState;
import archimulator.uncore.net.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.net.noc.selection.aco.ACONode;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * NoC memory hierarchy.
 *
 * @author Min Cai
 */
public class NoCMemoryHierarchy extends AbstractMemoryHierarchy implements Reportable {
    private NoCNet net;

    /**
     * Create a basic memory hierarchy.
     *
     * @param experiment              the experiment
     * @param simulation              the simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public NoCMemoryHierarchy(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.net = new NoCNet(this);
    }

    @Override
    public Net getNet(MemoryDevice from, MemoryDevice to) {
        return net;
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> network = net.getNetwork();

            getChildren().add(new ReportNode(this, "numPacketsReceived", network.getNumPacketsReceived() + ""));
            getChildren().add(new ReportNode(this, "numPacketsTransmitted", network.getNumPacketsTransmitted() + ""));
            getChildren().add(new ReportNode(this, "throughput", network.throughput() + ""));
            getChildren().add(new ReportNode(this, "averagePacketDelay", network.averagePacketDelay() + ""));
            getChildren().add(new ReportNode(this, "averagePacketHops", network.averagePacketHops() + ""));
            getChildren().add(new ReportNode(this, "maxPacketDelay", network.getMaxPacketDelay() + ""));
            getChildren().add(new ReportNode(this, "maxPacketHops", network.getMaxPacketHops() + ""));

            getChildren().add(new ReportNode(this, "numPayloadPacketsReceived", network.getNumPayloadPacketsReceived() + ""));
            getChildren().add(new ReportNode(this, "numPayloadPacketsTransmitted", network.getNumPayloadPacketsTransmitted() + ""));
            getChildren().add(new ReportNode(this, "payloadThroughput", network.payloadThroughput() + ""));
            getChildren().add(new ReportNode(this, "averagePayloadPacketDelay", network.averagePayloadPacketDelay() + ""));
            getChildren().add(new ReportNode(this, "averagePayloadPacketHops", network.averagePayloadPacketHops() + ""));
            getChildren().add(new ReportNode(this, "maxPayloadPacketDelay", network.getMaxPayloadPacketDelay() + ""));
            getChildren().add(new ReportNode(this, "maxPayloadPacketHops", network.getMaxPayloadPacketHops() + ""));

            for(FlitState state : FlitState.values()) {
                getChildren().add(new ReportNode(this, String.format("averageFlitPerStateDelay::%s", state),
                        network.averageFlitPerStateDelay(state) + ""));
            }

            for(FlitState state : FlitState.values()) {
                getChildren().add(new ReportNode(this, String.format("maxFlitPerStateDelay::%s", state),
                        network.getMaxFlitPerStateDelay().containsKey(state) ? network.getMaxFlitPerStateDelay().get(state) + "" : 0.0 + ""));
            }
        }});
    }
}
