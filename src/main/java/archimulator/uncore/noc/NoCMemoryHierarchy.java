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
package archimulator.uncore.noc;

import archimulator.common.*;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.AbstractMemoryHierarchy;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.Net;
import archimulator.uncore.coherence.msi.controller.L1IController;
import archimulator.uncore.noc.routers.FlitState;
import archimulator.uncore.noc.prediction.RouterCongestionStatusPredictionHelper;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * NoC memory hierarchy.
 *
 * @author Min Cai
 */
public class NoCMemoryHierarchy extends AbstractMemoryHierarchy implements Net, NoCEnvironment, Reportable {
    private Network network;

    private RouterCongestionStatusPredictionHelper routerCongestionStatusPredictionHelper;

    private Map<SimulationObject, Integer> devicesToNodeIds;

    private Random random;

    /**
     * Create a basic memory hierarchy.
     *
     * @param experiment              the experiment
     * @param simulation              the simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public NoCMemoryHierarchy(CPUExperiment experiment, Simulation simulation, BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.devicesToNodeIds = new HashMap<>();

        int i = 0;

        for (L1IController l1IController : this.getL1IControllers()) {
            this.devicesToNodeIds.put(l1IController, i);
            this.devicesToNodeIds.put(
                    this.getL1DControllers().get(
                            this.getL1IControllers().indexOf(l1IController)
                    ),
                    i);

            i++;
        }

        this.devicesToNodeIds.put(this.getL2Controller(), i);

        i++;

        this.devicesToNodeIds.put(this.getMemoryController(), i);

        i++;

        int width = (int) Math.sqrt(i);
        if (width * width != i) {
            i = (width + 1) * (width + 1);
        }

        this.network = NetworkFactory.create(this, this.getCycleAccurateEventQueue(), i);

        this.routerCongestionStatusPredictionHelper = new RouterCongestionStatusPredictionHelper(this.network);

        this.getExperiment().getConfig().setMaxInputBufferSize(this.getL2Controller().getCache().getLineSize() + 8);

        this.random = this.getExperiment().getConfig().getRandSeed() != -1 ? new Random(this.getExperiment().getConfig().getRandSeed()) : new Random();
    }

    /**
     * Get the net for the specified source and destination devices.
     *
     * @param from the source device
     * @param to   the destination device
     * @return the net for the specified source and destination devices
     */
    @Override
    public Net getNet(MemoryDevice from, MemoryDevice to) {
        return this;
    }

    /**
     * Transfer a message of the specified size from the source device to the destination device.
     *
     * @param deviceFrom        the source device
     * @param deviceTo   the destination device
     * @param size                the size
     * @param onCompletedCallback the callback action performed when the transfer is completed
     */
    @Override
    public void transfer(MemoryDevice deviceFrom, MemoryDevice deviceTo, int size, Runnable onCompletedCallback) {
        int src = this.devicesToNodeIds.get(deviceFrom);
        int dest = this.devicesToNodeIds.get(deviceTo);

        DataPacket packet = new DataPacket(this.network, src, dest, size, onCompletedCallback);

        this.getCycleAccurateEventQueue().schedule(this, () -> this.network.receive(packet), 1);
    }

    /**
     * Dump the statistics into the specified report node.
     *
     * @param reportNode the report node
     */
    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            routerCongestionStatusPredictionHelper.dumpStats(this);

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/numPacketsReceived",
                            String.format("%d", network.getNumPacketsReceived())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/numPacketsTransmitted",
                            String.format("%d", network.getNumPacketsTransmitted())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/throughput",
                            String.format("%s", network.throughput())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/averagePacketDelay",
                            String.format("%s", network.averagePacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/averagePacketHops",
                            String.format("%s", network.averagePacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/maxPacketDelay",
                            String.format("%d", network.getMaxPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/maxPacketHops",
                            String.format("%d", network.getMaxPacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/numPayloadPacketsReceived",
                            String.format("%d", network.getNumPayloadPacketsReceived())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/numPayloadPacketsTransmitted",
                            String.format("%d", network.getNumPayloadPacketsTransmitted())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/payloadThroughput",
                            String.format("%s", network.payloadThroughput())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/averagePayloadPacketDelay",
                            String.format("%s", network.averagePayloadPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/averagePayloadPacketHops",
                            String.format("%s", network.averagePayloadPacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/maxPayloadPacketDelay",
                            String.format("%d", network.getMaxPayloadPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "noc/maxPayloadPacketHops",
                            String.format("%d", network.getMaxPayloadPacketHops())
                    )
            );

            for(FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("noc/averageFlitPerStateDelay::%s", state),
                                String.format("%s", network.averageFlitPerStateDelay(state))
                        )
                );
            }

            for(FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("noc/maxFlitPerStateDelay::%s", state),
                                network.getMaxFlitPerStateDelay().containsKey(state) ? String.format("%d", network.getMaxFlitPerStateDelay().get(state)) : String.format("%s", 0.0)
                        )
                );
            }
        }});
    }

    /**
     * Get the name of the memory hierarchy.
     *
     * @return the name of the memory hierarchy
     */
    @Override
    public String getName() {
        return "net";
    }

    /**
     * Get the config.
     *
     * @return the config
     */
    @Override
    public NoCConfig getConfig() {
        return getExperiment().getConfig();
    }

    /**
     * Get the random.
     *
     * @return the random
     */
    @Override
    public Random getRandom() {
        return random;
    }

    /**
     * Get a boolean value indicating whether it is currently in the detailed simulation mode or not.
     *
     * @return a boolean value indicating whether it is currently in the detailed simulation mode or not
     */
    @Override
    public boolean isInDetailedSimulationMode() {
        return this.getSimulation().getType() == SimulationType.MEASUREMENT
                || this.getSimulation().getType() == SimulationType.WARMUP;
    }

    /**
     * Get the network.
     *
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }
}
