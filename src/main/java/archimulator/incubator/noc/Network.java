package archimulator.incubator.noc;

import archimulator.incubator.noc.routers.FlitState;
import archimulator.incubator.noc.routing.RoutingAlgorithm;
import archimulator.incubator.noc.routing.RoutingAlgorithmFactory;
import archimulator.util.event.CycleAccurateEventQueue;
import javaslang.collection.LinkedHashMap;
import javaslang.collection.List;
import javaslang.collection.Map;

/**
 * Network.
 *
 * @author  Min Cai
 */
public class Network<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm> {
    long currentPacketId;

    private Experiment experiment;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private int numNodes;

    private List<NodeT> nodes;

    private NodeFactory<NodeT> nodeFactory;

    private RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory;

    private RoutingAlgorithmT routingAlgorithm;

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

    private Map<FlitState, Long> numFlitPerStateDelaySamples;
    private Map<FlitState, Long> totalFlitPerStateDelays;
    private Map<FlitState, Long> maxFlitPerStateDelay;

    private Map<FlitState, Long> numHeadFlitPerStateDelaySamples;
    private Map<FlitState, Long> totalHeadFlitPerStateDelays;
    private Map<FlitState, Long> maxHeadFlitPerStateDelay;

    private Map<FlitState, Long> numBodyFlitPerStateDelaySamples;
    private Map<FlitState, Long> totalBodyFlitPerStateDelays;
    private Map<FlitState, Long> maxBodyFlitPerStateDelay;

    private Map<FlitState, Long> numTailFlitPerStateDelaySamples;
    private Map<FlitState, Long> totalTailFlitPerStateDelays;
    private Map<FlitState, Long> maxTailFlitPerStateDelay;

    public Network(
            Experiment experiment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes,
            NodeFactory<NodeT> nodeFactory,
            RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory
    ) {
        this.currentPacketId = 0;

        this.experiment = experiment;

        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.numNodes = numNodes;

        this.width = (int) Math.sqrt(this.numNodes);

        this.nodeFactory = nodeFactory;

        this.nodes = List.empty();

        for(int i = 0; i < this.numNodes; i++) {
            this.nodes.append(this.nodeFactory.createNode(this, i));
        }

        if(this.width * this.width != this.numNodes) {
            throw new RuntimeException("Only 2D meshes are supported");
        }

        this.routingAlgorithmFactory = routingAlgorithmFactory;

        this.routingAlgorithm = this.routingAlgorithmFactory.createRoutingAlgorithm();

        this.inflightPackets = List.empty();

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

        this.numPacketsReceivedPerType = LinkedHashMap.empty();
        this.numPacketsTransmittedPerType = LinkedHashMap.empty();

        this.totalPacketDelaysPerType = LinkedHashMap.empty();
        this.maxPacketDelayPerType = LinkedHashMap.empty();

        this.totalPacketHopsPerType = LinkedHashMap.empty();
        this.maxPacketHopsPerType = LinkedHashMap.empty();

        this.numFlitPerStateDelaySamples = LinkedHashMap.empty();
        this.totalFlitPerStateDelays = LinkedHashMap.empty();
        this.maxFlitPerStateDelay = LinkedHashMap.empty();

        this.numHeadFlitPerStateDelaySamples = LinkedHashMap.empty();
        this.totalHeadFlitPerStateDelays = LinkedHashMap.empty();
        this.maxHeadFlitPerStateDelay = LinkedHashMap.empty();

        this.numBodyFlitPerStateDelaySamples = LinkedHashMap.empty();
        this.totalBodyFlitPerStateDelays = LinkedHashMap.empty();
        this.maxBodyFlitPerStateDelay = LinkedHashMap.empty();

        this.numTailFlitPerStateDelaySamples = LinkedHashMap.empty();
        this.totalTailFlitPerStateDelays = LinkedHashMap.empty();
        this.maxTailFlitPerStateDelay = LinkedHashMap.empty();
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

    public double averagePacketHops() {
        return 0.0; //TODO
    }

    public double payloadThroughput() {
        return 0.0; //TODO
    }

    public double averagePayloadPacketDelay() {
        return 0.0; //TODO
    }

    public double averagePayloadPacketHops() {
        return 0.0; //TODO
    }

    public double throughputPerType(Class<? extends Packet> packetCls) {
        return 0.0; //TODO
    }

    public double averagePacketDelayPerType(Class<? extends Packet> packetCls) {
        return 0.0; //TODO
    }

    public double averagePacketHopsPerType(Class<? extends Packet> packetCls) {
        return 0.0; //TODO
    }

    public double averageFlitPerStateDelay(FlitState state) {
        return 0.0; //TODO
    }

    public double averageHeadFlitPerStateDelay(FlitState state) {
        return 0.0; //TODO
    }

    public double averageBodyFlitPerStateDelay(FlitState state) {
        return 0.0; //TODO
    }

    public double averageTailFlitPerStateDelay(FlitState state) {
        return 0.0; //TODO
    }

    public void dumpState() {
        //TODO
    }

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

    public NodeFactory<NodeT> getNodeFactory() {
        return nodeFactory;
    }

    public RoutingAlgorithmFactory<RoutingAlgorithmT> getRoutingAlgorithmFactory() {
        return routingAlgorithmFactory;
    }

    public RoutingAlgorithmT getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    public int getWidth() {
        return width;
    }

    public List<Packet> getInflightPackets() {
        return inflightPackets;
    }

    public boolean isAcceptPacket() {
        return acceptPacket;
    }

    public long getNumPacketsReceived() {
        return numPacketsReceived;
    }

    public long getNumPacketsTransmitted() {
        return numPacketsTransmitted;
    }
}
