package archimulator.incubator.noc;

import archimulator.incubator.noc.routers.FlitState;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.*;

/**
 * Network.
 *
 * @author  Min Cai
 */
public class Network<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm> {
    public static long currentPacketId;

    private Experiment experiment;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private int numNodes;

    private List<NodeT> nodes;

    private NodeFactory<NodeT> nodeFactory;

    private RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory;

    private int width;

    private List<Packet> inflightPackets;

    private boolean acceptPacket;

    private long numPacketsReceived;
    private long numPacketsTransmitted;

    private long totalPacketDelays;
    private long maxPacketDelay;

    private long totalPacketHops;
    private long maxPacketHops;

    private long numPayloadPacketsReceived;
    private long numPayloadPacketsTransmitted;

    private long totalPayloadPacketDelays;
    private long maxPayloadPacketDelay;

    private long totalPayloadPacketHops;
    private long maxPayloadPacketHops;

    private Map<? extends Packet, Long> numPacketsReceivedPerType;
    private Map<? extends Packet, Long> numPacketsTransmittedPerType;

    private Map<? extends Packet, Long> totalPacketDelaysPerType;
    private Map<? extends Packet, Long> maxPacketDelayPerType;

    private Map<? extends Packet, Long> totalPacketHopsPerType;
    private Map<? extends Packet, Long> maxPacketHopsPerType;

    private EnumMap<FlitState, Long> numFlitPerStateDelaySamples;
    private EnumMap<FlitState, Long> totalFlitPerStateDelays;
    private EnumMap<FlitState, Long> maxFlitPerStateDelay;

    private EnumMap<FlitState, Long> numHeadFlitPerStateDelaySamples;
    private EnumMap<FlitState, Long> totalHeadFlitPerStateDelays;
    private EnumMap<FlitState, Long> maxHeadFlitPerStateDelay;

    private EnumMap<FlitState, Long> numBodyFlitPerStateDelaySamples;
    private EnumMap<FlitState, Long> totalBodyFlitPerStateDelays;
    private EnumMap<FlitState, Long> maxBodyFlitPerStateDelay;

    private EnumMap<FlitState, Long> numTailFlitPerStateDelaySamples;
    private EnumMap<FlitState, Long> totalTailFlitPerStateDelays;
    private EnumMap<FlitState, Long> maxTailFlitPerStateDelay;


    static {
        currentPacketId = 0;
    }

    public Network(
            Experiment experiment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes,
            NodeFactory<NodeT> nodeFactory,
            RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory
    ) {
        this.experiment = experiment;

        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.numNodes = numNodes;

        this.width = (int) Math.sqrt(this.numNodes);

        this.nodeFactory = nodeFactory;

        this.routingAlgorithmFactory = routingAlgorithmFactory;

        this.nodes = new ArrayList<>();

        for(int i = 0; i < this.numNodes; i++) {
            this.nodes.add(this.nodeFactory.createNode(this, i));
        }

        if(this.width * this.width != this.numNodes) {
            throw new RuntimeException("Only 2D meshes are supported");
        }

        this.inflightPackets = new ArrayList<>();

        this.acceptPacket = true;

        this.numPacketsReceived = 0;
        this.numPacketsTransmitted = 0;

        this.totalPacketDelays = 0;
        this.maxPacketDelay = 0;

        this.totalPacketHops = 0;
        this.maxPacketHops = 0;

        this.numPayloadPacketsReceived = 0;
        this.numPayloadPacketsTransmitted = 0;

        this.totalPayloadPacketDelays = 0;
        this.maxPayloadPacketDelay = 0;

        this.totalPayloadPacketHops = 0;
        this.maxPayloadPacketHops = 0;

        this.numPacketsReceivedPerType = new HashMap<>();
        this.numPacketsTransmittedPerType = new HashMap<>();

        this.totalPacketDelaysPerType = new HashMap<>();
        this.maxPacketDelayPerType = new HashMap<>();

        this.totalPacketHopsPerType = new HashMap<>();
        this.maxPacketHopsPerType = new HashMap<>();

        this.numFlitPerStateDelaySamples = new EnumMap<>(FlitState.class);
        this.totalFlitPerStateDelays = new EnumMap<>(FlitState.class);
        this.maxFlitPerStateDelay = new EnumMap<>(FlitState.class);

        this.numHeadFlitPerStateDelaySamples = new EnumMap<>(FlitState.class);
        this.totalHeadFlitPerStateDelays = new EnumMap<>(FlitState.class);
        this.maxHeadFlitPerStateDelay = new EnumMap<>(FlitState.class);

        this.numBodyFlitPerStateDelaySamples = new EnumMap<>(FlitState.class);
        this.totalBodyFlitPerStateDelays = new EnumMap<>(FlitState.class);
        this.maxBodyFlitPerStateDelay = new EnumMap<>(FlitState.class);

        this.numTailFlitPerStateDelaySamples = new EnumMap<>(FlitState.class);
        this.totalTailFlitPerStateDelays = new EnumMap<>(FlitState.class);
        this.maxTailFlitPerStateDelay = new EnumMap<>(FlitState.class);
    }

    public boolean isInWarmupPhase() {
        return this.experiment.getConfig().getWarmupCycles() > 0
                && this.cycleAccurateEventQueue.getCurrentCycle() < this.experiment.getConfig().getWarmupCycles();
    }

    public void logPacketReceived(Packet packet) {
        //TODO
    }

    public void logPacketTransmitted(Packet packet) {
        //TODO
    }

    public void logFlitPerStateDelay(boolean head, boolean tail, FlitState state, int delay) {
        //TODO
    }

    public void receive(Packet packet) {
        //TODO
    }

    public int randDest(int src) {
        return -1; //TODO
    }

    public double throughput() {
        return 0.0; //TODO
    }

    public double averagePacketDelay() {
        return 0.0; //TODO
    }

    //TODO...

    public Experiment getExperiment() {
        return experiment;
    }

    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return cycleAccurateEventQueue;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public List<NodeT> getNodes() {
        return nodes;
    }

    public int getWidth() {
        return width;
    }

    public NodeFactory<NodeT> getNodeFactory() {
        return nodeFactory;
    }

    public RoutingAlgorithmFactory<RoutingAlgorithmT> getRoutingAlgorithmFactory() {
        return routingAlgorithmFactory;
    }
}
