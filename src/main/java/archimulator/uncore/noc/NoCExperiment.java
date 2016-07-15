package archimulator.uncore.noc;

import archimulator.common.Experiment;
import archimulator.common.ExperimentStat;
import archimulator.common.Logger;
import archimulator.common.NoCEnvironment;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.uncore.noc.routers.FlitState;
import archimulator.uncore.noc.routers.prediction.RouterCongestionStatusPredictionHelper;
import archimulator.uncore.noc.routing.RoutingAlgorithm;
import archimulator.uncore.noc.traffics.HotspotTrafficGenerator;
import archimulator.uncore.noc.traffics.TransposeTrafficGenerator;
import archimulator.uncore.noc.traffics.UniformTrafficGenerator;
import archimulator.util.dateTime.DateHelper;
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
    private String outputDirectory;

    private Random random;

    private int numNodes;

    private int maxCycles;

    private int maxPackets;

    private boolean noDrain;

    private long beginTime;

    private long endTime;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private Network<? extends Node, ? extends RoutingAlgorithm> network;

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

        this.network = NetworkFactory.setupNetwork(this, this.cycleAccurateEventQueue, this.numNodes);

        switch (this.getConfig().getDataPacketTraffic()) {
            case "uniform":
                new UniformTrafficGenerator<>(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator<>(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator<>(
                        network,
                        this.getConfig().getDataPacketInjectionRate(),
                        (n, src, dest, size) -> new DataPacket(n, src, dest, size, () -> {}),
                        this.getConfig().getDataPacketSize(),
                        maxPackets
                );
                break;
        }

        this.routerCongestionStatusPredictionHelper = new RouterCongestionStatusPredictionHelper(this.network);
    }

    /**
     * Simulate.
     */
    @Override
    protected void simulate() {
        Logger.info(Logger.SIMULATOR, "", this.cycleAccurateEventQueue.getCurrentCycle());

        this.beginTime = DateHelper.toTick(new Date());

        while ((this.getMaxCycles() == -1 || cycleAccurateEventQueue.getCurrentCycle() < this.getMaxCycles())
                && (this.getMaxPackets() == -1 || network.getNumPacketsReceived() < this.getMaxPackets())) {
            cycleAccurateEventQueue.advanceOneCycle();
        }

        if (!this.isNoDrain()) {
            network.setAcceptPacket(false);

            while(network.getNumPacketsReceived() != network.getNumPacketsTransmitted()) {
                cycleAccurateEventQueue.advanceOneCycle();
            }
        }

        this.endTime = DateHelper.toTick(new Date());

        this.collectStats();

        Logger.info(Logger.SIMULATION, "Simulation completed successfully.", this.getCycleAccurateEventQueue().getCurrentCycle());
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

            for(FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("averageFlitPerStateDelay::%s", state),
                                String.format("%s", network.averageFlitPerStateDelay(state))
                        )
                );
            }

            for(FlitState state : FlitState.values()) {
                getChildren().add(
                        new ReportNode(
                                this,
                                String.format("maxFlitPerStateDelay::%s", state),
                                network.getMaxFlitPerStateDelay().containsKey(state) ? String.format("%d", network.getMaxFlitPerStateDelay().get(state)) : String.format("%s", 0.0)
                        )
                );
            }
        }});
    }

    /**
     * Get the network.
     *
     * @return the network
     */
    public Network<? extends Node, ? extends RoutingAlgorithm> getNetwork() {
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

    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return cycleAccurateEventQueue;
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
