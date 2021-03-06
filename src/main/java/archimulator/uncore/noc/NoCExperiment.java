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
import archimulator.uncore.noc.prediction.RouterCongestionStatusPredictionHelper;
import archimulator.uncore.noc.routers.FlitState;
import archimulator.uncore.noc.selection.aco.ACOSelectionAlgorithm;
import archimulator.uncore.noc.selection.aco.Pheromone;
import archimulator.uncore.noc.traffics.HotspotTrafficGenerator;
import archimulator.uncore.noc.traffics.TransposeTrafficGenerator;
import archimulator.uncore.noc.traffics.UniformTrafficGenerator;
import archimulator.util.dateTime.DateHelper;
import archimulator.util.event.BlockingEvent;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * NoC experiment.
 *
 * @author Min Cai
 */
public class NoCExperiment extends Experiment<NoCExperimentConfig> implements NoCEnvironment, Reportable {
    /**
     * NoC experiment started event.
     */
    public class NoCExperimentStartedEvent implements BlockingEvent {
    }

    /**
     * NoC experiment advance one cycle event.
     */
    public class NoCExperimentAdvanceOneCycleEvent implements BlockingEvent {
    }

    /**
     * NoC experiment ended event.
     */
    public class NoCExperimentEndedEvent implements BlockingEvent {
    }

    private String outputDirectory;

    private Random random;

    private int numNodes;

    private int maxCycles;

    private int maxPackets;

    private boolean noDrain;

    private long beginTime;

    private long endTime;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;

    private Network network;

    private RouterCongestionStatusPredictionHelper routerCongestionStatusPredictionHelper;

    /**
     * Create a NoC experiment.
     */
    public NoCExperiment(String outputDirectory, int numNodes, int maxCycles, int maxPackets, boolean noDrain) {
        super(new NoCExperimentConfig());

        this.outputDirectory = outputDirectory;
        this.numNodes = numNodes;
        this.maxCycles = maxCycles;
        this.maxPackets = maxPackets;
        this.noDrain = noDrain;

        this.random = this.getConfig().getRandSeed() != -1 ? new Random(this.getConfig().getRandSeed()) : new Random();

        this.cycleAccurateEventQueue = new CycleAccurateEventQueue();

        this.blockingEventDispatcher = new BlockingEventDispatcher<>();
    }

    /**
     * Simulate.
     */
    @Override
    protected void simulate() {
        Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

        this.network = NetworkFactory.create(this, this.cycleAccurateEventQueue, this.numNodes);

        switch (this.getConfig().getDataPacketTraffic()) {
            case "uniform":
                new UniformTrafficGenerator(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (src, dest, size) -> new DataPacket(network, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (src, dest, size) -> new DataPacket(network, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (src, dest, size) -> new DataPacket(network, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
        }

        this.routerCongestionStatusPredictionHelper = new RouterCongestionStatusPredictionHelper(this.network);

        this.blockingEventDispatcher.dispatch(new NoCExperimentStartedEvent());

        this.beginTime = DateHelper.toTick(new Date());

        while ((this.getMaxCycles() == -1 || cycleAccurateEventQueue.getCurrentCycle() < this.getMaxCycles())
                && (this.getMaxPackets() == -1 || network.getNumPacketsReceived() < this.getMaxPackets())) {
            cycleAccurateEventQueue.advanceOneCycle();
            this.blockingEventDispatcher.dispatch(new NoCExperimentAdvanceOneCycleEvent());
        }

        if (!this.isNoDrain()) {
            network.setAcceptPacket(false);

            while (network.getNumPacketsReceived() != network.getNumPacketsTransmitted()) {
                cycleAccurateEventQueue.advanceOneCycle();
                this.blockingEventDispatcher.dispatch(new NoCExperimentAdvanceOneCycleEvent());
            }
        }

        this.endTime = DateHelper.toTick(new Date());

        this.collectStats();

        Logger.info(Logger.SIMULATION, "Simulation completed successfully.", this.getCycleAccurateEventQueue().getCurrentCycle());

        this.blockingEventDispatcher.dispatch(new NoCExperimentEndedEvent());
    }

    /**
     * Collect the statistics.
     */
    private void collectStats() {
        final List<ExperimentStat> stats = new ArrayList<>();

        ReportNode rootReportNode = new ReportNode(null, "");

        this.dumpStats(rootReportNode);

        rootReportNode.traverse(node -> stats.add(new ExperimentStat("", node.getPath(), node.getValue())));

        this.getStats().addAll(stats);
    }

    /**
     * Dump the statistics into the specified report node.
     *
     * @param reportNode the report node
     */
    @Override
    public void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, "simulation") {{
            getChildren().add(new ReportNode(this, "beginTimeAsString", getBeginTimeAsString()));
            getChildren().add(new ReportNode(this, "endTimeAsString", getEndTimeAsString()));
            getChildren().add(new ReportNode(this, "duration", getDuration()));
            getChildren().add(new ReportNode(this, "durationInSeconds", getDurationInSeconds() + ""));

            getChildren().add(new ReportNode(this, "cycleAccurateEventQueue/currentCycle", getCycleAccurateEventQueue().getCurrentCycle() + ""));
        }});

        reportNode.getChildren().add(new ReportNode(reportNode, "noc") {{
            routerCongestionStatusPredictionHelper.dumpStats(this);

            getChildren().add(
                    new ReportNode(
                            this,
                            "numPacketsReceived",
                            String.format("%d", network.getNumPacketsReceived())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "numPacketsTransmitted",
                            String.format("%d", network.getNumPacketsTransmitted())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "throughput",
                            String.format("%s", network.throughput())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "averagePacketDelay",
                            String.format("%s", network.averagePacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "averagePacketHops",
                            String.format("%s", network.averagePacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "maxPacketDelay",
                            String.format("%d", network.getMaxPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "maxPacketHops",
                            String.format("%d", network.getMaxPacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "numPayloadPacketsReceived",
                            String.format("%d", network.getNumPayloadPacketsReceived())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "numPayloadPacketsTransmitted",
                            String.format("%d", network.getNumPayloadPacketsTransmitted())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "payloadThroughput",
                            String.format("%s", network.payloadThroughput())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "averagePayloadPacketDelay",
                            String.format("%s", network.averagePayloadPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "averagePayloadPacketHops",
                            String.format("%s", network.averagePayloadPacketHops())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "maxPayloadPacketDelay",
                            String.format("%d", network.getMaxPayloadPacketDelay())
                    )
            );

            getChildren().add(
                    new ReportNode(
                            this,
                            "maxPayloadPacketHops",
                            String.format("%d", network.getMaxPayloadPacketHops())
                    )
            );

            for (FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("averageFlitPerStateDelay::%s", state),
                                String.format("%s", network.averageFlitPerStateDelay(state))
                        )
                );
            }

            for (FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("maxFlitPerStateDelay::%s", state),
                                network.getMaxFlitPerStateDelay().containsKey(state) ? String.format("%d", network.getMaxFlitPerStateDelay().get(state)) : String.format("%s", 0.0)
                        )
                );
            }

            if (getConfig().getSelection().equals("aco")) {
                for (Node srcNode : getNetwork().getNodes()) {
                    for (Node destNode : getNetwork().getNodes()) {
                        if (srcNode != destNode) {
                            ACOSelectionAlgorithm selectionAlgorithm =
                                    (ACOSelectionAlgorithm) srcNode.getSelectionAlgorithm();

                            for (Pheromone pheromone :
                                    selectionAlgorithm.getPheromoneTable().getPheromones().get(destNode.getId()).values()) {
                                getChildren().add(
                                        new ReportNode(
                                                this,
                                                String.format("node_%d.pheromones[node_%d][%s]", srcNode.getId(), destNode.getId(), pheromone.getDirection()),
                                                String.format("%s", pheromone.getValue())
                                        )
                                );
                            }
                        }
                    }
                }

            }
        }});
    }

    /**
     * Get the network.
     *
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Get the router congestion status prediction helper.
     *
     * @return the router congestion status prediction helper
     */
    public RouterCongestionStatusPredictionHelper getRouterCongestionStatusPredictionHelper() {
        return routerCongestionStatusPredictionHelper;
    }

    /**
     * Get the output directory.
     *
     * @return the output directory
     */
    @Override
    protected String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Get the random object.
     *
     * @return the random object
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
        return true;
    }

    /**
     * Get the number of nodes.
     *
     * @return the number of nodes
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Get the time in ticks when the simulation begins.
     *
     * @return the time in ticks when the simulation begins
     */
    public long getBeginTime() {
        return beginTime;
    }

    /**
     * Get the time in ticks when the simulation ends.
     *
     * @return the time in ticks when the simulation ends
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Get the string representation of the time when the simulation begins.
     *
     * @return the string representation of the time when the simulation begins
     */
    public String getBeginTimeAsString() {
        return DateHelper.toString(beginTime);
    }

    /**
     * Get the string representation of the time when the simulation ends.
     *
     * @return the string representation of the time when the simulation ends
     */
    public String getEndTimeAsString() {
        return DateHelper.toString(endTime);
    }

    /**
     * Get the duration in seconds that the simulation lasts.
     *
     * @return the duration in seconds that the simulation lasts
     */
    public long getDurationInSeconds() {
        return (this.getEndTime() - this.getBeginTime()) / 1000;
    }

    /**
     * Get the string representation of the duration that the simulation lasts.
     *
     * @return the string representation of the duration that the simulation lasts
     */
    public String getDuration() {
        return DurationFormatUtils.formatDurationHMS(this.getEndTime() - this.getBeginTime());
    }

    /**
     * Get the cycle accurate event queue.
     *
     * @return the cycle accurate event queue
     */
    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return cycleAccurateEventQueue;
    }

    /**
     * Get the blocking event dispatcher.
     *
     * @return the blocking event dispatcher
     */
    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
        return blockingEventDispatcher;
    }

    /**
     * Get the maximum number of cycles to be simulated.
     *
     * @return the maximum number of cycles to be simulated
     */
    public int getMaxCycles() {
        return maxCycles;
    }

    /**
     * Get the maximum number of packets to be simulated.
     *
     * @return the maximum number of packets to be simulated
     */
    public int getMaxPackets() {
        return maxPackets;
    }

    /**
     * Get a boolean value indicating whether draining  packets is disabled or not.
     *
     * @return a boolean value indicating whether draining packets is disabled or not
     */
    public boolean isNoDrain() {
        return noDrain;
    }
}
