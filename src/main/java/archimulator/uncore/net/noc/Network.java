package archimulator.uncore.net.noc;

import archimulator.uncore.net.NoCNet;
import archimulator.uncore.net.noc.routers.FlitState;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.routing.RoutingAlgorithmFactory;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.*;

/**
 * Network.
 *
 * @author  Min Cai
 */
public abstract class Network<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm> {
    long currentPacketId;

    private NoCNet settings;

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

    private Map<Class<? extends Packet>, Long> numPacketsReceivedPerType;
    private Map<Class<? extends Packet>, Long> numPacketsTransmittedPerType;

    private Map<Class<? extends Packet>, Long> totalPacketDelaysPerType;
    private Map<Class<? extends Packet>, Long> maxPacketDelayPerType;

    private Map<Class<? extends Packet>, Long> totalPacketHopsPerType;
    private Map<Class<? extends Packet>, Long> maxPacketHopsPerType;

    private Map<FlitState, Long> numFlitPerStateDelaySamples;
    private Map<FlitState, Long> totalFlitPerStateDelays;
    private Map<FlitState, Long> maxFlitPerStateDelay;

    public Network(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes,
            NodeFactory<NodeT> nodeFactory,
            RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory
    ) {
        this.currentPacketId = 0;

        this.settings = settings;

        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.numNodes = numNodes;

        this.width = (int) Math.sqrt(this.numNodes);

        this.nodeFactory = nodeFactory;

        this.nodes = new ArrayList<>();

        for(int i = 0; i < this.numNodes; i++) {
            this.nodes.add(this.nodeFactory.createNode(this, i));
        }

        if(this.width * this.width != this.numNodes) {
            throw new RuntimeException("Only 2D meshes are supported");
        }

        this.routingAlgorithmFactory = routingAlgorithmFactory;

        this.routingAlgorithm = this.routingAlgorithmFactory.createRoutingAlgorithm();

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

        this.numFlitPerStateDelaySamples = new HashMap<>();
        this.totalFlitPerStateDelays = new HashMap<>();
        this.maxFlitPerStateDelay = new HashMap<>();
    }

    public abstract boolean simulateAtCurrentCycle();

    public void logPacketReceived(Packet packet) {
        this.numPacketsReceived++;

        if(packet.hasPayload()) {
            this.numPayloadPacketsReceived++;
        }

        Class<? extends Packet> packetCls = packet.getClass();

        if(!this.numPacketsReceivedPerType.containsKey(packetCls)) {
            this.numPacketsReceivedPerType.put(packetCls, 0L);
        }

        this.numPacketsReceivedPerType.put(packetCls,
                this.numPacketsReceivedPerType.get(packetCls) + 1);
    }

    public void logPacketTransmitted(Packet packet) {
        this.numPacketsTransmitted++;

        if(packet.hasPayload()) {
            this.numPayloadPacketsTransmitted++;
        }

        Class<? extends Packet> packetCls = packet.getClass();

        if(!this.numPacketsTransmittedPerType.containsKey(packetCls)) {
            this.numPacketsTransmittedPerType.put(packetCls, 0L);
        }

        this.numPacketsTransmittedPerType.put(packetCls,
                this.numPacketsTransmittedPerType.get(packetCls) + 1);

        this.totalPacketDelays += packet.delay();
        this.totalPacketHops += packet.hops();

        if(packet.hasPayload()) {
            this.totalPayloadPacketDelays += packet.delay();
            this.totalPayloadPacketHops += packet.hops();
        }

        if(!this.totalPacketDelaysPerType.containsKey(packetCls)) {
            this.totalPacketDelaysPerType.put(packetCls, 0L);
        }

        if(!this.totalPacketHopsPerType.containsKey(packetCls)) {
            this.totalPacketHopsPerType.put(packetCls, 0L);
        }

        this.totalPacketDelaysPerType.put(packetCls,
                this.totalPacketDelaysPerType.get(packetCls) + packet.delay());

        this.totalPacketHopsPerType.put(packetCls,
                this.totalPacketHopsPerType.get(packetCls) + packet.hops());

        this.maxPacketDelay = Math.max(this.maxPacketDelay, packet.delay());
        this.maxPacketHops = Math.max(this.maxPacketHops, packet.hops());

        if(packet.hasPayload()) {
            this.maxPayloadPacketDelay = Math.max(this.maxPayloadPacketDelay, packet.delay());
            this.maxPayloadPacketHops = Math.max(this.maxPayloadPacketHops, packet.hops());
        }

        if(!this.maxPacketDelayPerType.containsKey(packetCls)) {
            this.maxPacketDelayPerType.put(packetCls, 0L);
        }

        if(!this.maxPacketHopsPerType.containsKey(packetCls)) {
            this.maxPacketHopsPerType.put(packetCls, 0L);
        }

        this.maxPacketDelayPerType.put(packetCls,
                Math.max(this.maxPacketDelayPerType.get(packetCls), packet.delay()));
        this.maxPacketHopsPerType.put(packetCls,
                Math.max(this.maxPacketHopsPerType.get(packetCls), packet.hops()));
    }

    public void logFlitPerStateDelay(boolean head, boolean tail, FlitState state, int delay) {
        if(!this.numFlitPerStateDelaySamples.containsKey(state)) {
            this.numFlitPerStateDelaySamples.put(state, 0L);
        }

        this.numFlitPerStateDelaySamples.put(state, this.numFlitPerStateDelaySamples.get(state) + 1);

        if(!this.totalFlitPerStateDelays.containsKey(state)) {
            this.totalFlitPerStateDelays.put(state, 0L);
        }

        this.totalFlitPerStateDelays.put(state, this.totalFlitPerStateDelays.get(state) + delay);

        if(!this.maxFlitPerStateDelay.containsKey(state)) {
            this.maxFlitPerStateDelay.put(state, 0L);
        }

        this.maxFlitPerStateDelay.put(state, Math.max(this.maxFlitPerStateDelay.get(state), delay));
    }

    public boolean receive(Packet packet) {
        if(!this.nodes.get(packet.getSrc()).getRouter().injectPacket(packet)) {
            this.cycleAccurateEventQueue.schedule(this, () -> receive(packet), 1);
            return false;
        }

        this.inflightPackets.add(packet);
        this.logPacketReceived(packet);

        return true;
    }

    public int randDest(int src) {
        while (true) {
            int i = this.settings.getRandom().nextInt(this.numNodes);
            if(i != src) {
                return i;
            }
        }
    }

    public double throughput() {
        return (double) this.numPacketsTransmitted / this.cycleAccurateEventQueue.getCurrentCycle() / this.numNodes;
    }

    public double averagePacketDelay() {
        if(this.numPacketsTransmitted > 0) {
            return (double) this.totalPacketDelays / this.numPacketsTransmitted;
        }

        return 0.0;
    }

    public double averagePacketHops() {
        if(this.numPacketsTransmitted > 0) {
            return (double) this.totalPacketHops / this.numPacketsTransmitted;
        }

        return 0.0;
    }

    public double payloadThroughput() {
        return (double) this.numPayloadPacketsTransmitted / this.cycleAccurateEventQueue.getCurrentCycle() / this.numNodes;
    }

    public double averagePayloadPacketDelay() {
        if(this.numPayloadPacketsTransmitted > 0) {
            return (double) this.totalPayloadPacketDelays / this.numPayloadPacketsTransmitted;
        }

        return 0.0;
    }

    public double averagePayloadPacketHops() {
        if(this.numPayloadPacketsTransmitted > 0) {
            return (double) this.totalPayloadPacketHops / this.numPayloadPacketsTransmitted;
        }

        return 0.0;
    }

    public double throughputPerType(Class<? extends Packet> packetCls) {
        return (double) this.numPacketsTransmittedPerType.get(packetCls)
                / this.cycleAccurateEventQueue.getCurrentCycle()
                / this.numNodes;
    }

    public double averagePacketDelayPerType(Class<? extends Packet> packetCls) {
        if(this.numPacketsTransmittedPerType.get(packetCls) > 0) {
            return (double) this.totalPacketDelaysPerType.get(packetCls)
                    / this.numPacketsTransmittedPerType.get(packetCls);
        }

        return 0.0;
    }

    public double averagePacketHopsPerType(Class<? extends Packet> packetCls) {
        if(this.numPacketsTransmittedPerType.get(packetCls) > 0) {
            return (double) this.totalPacketHopsPerType.get(packetCls)
                    / this.numPacketsTransmittedPerType.get(packetCls);
        }

        return 0.0;
    }

    public double averageFlitPerStateDelay(FlitState state) {
        if(this.numFlitPerStateDelaySamples.containsKey(state) && this.numFlitPerStateDelaySamples.get(state) > 0) {
            return (double) this.totalFlitPerStateDelays.get(state)
                    / this.numFlitPerStateDelaySamples.get(state);
        }

        return 0.0;
    }

    public NoCNet getSettings() {
        return settings;
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

    public void setAcceptPacket(boolean acceptPacket) {
        this.acceptPacket = acceptPacket;
    }

    public long getNumPacketsReceived() {
        return numPacketsReceived;
    }

    public long getNumPacketsTransmitted() {
        return numPacketsTransmitted;
    }

    public long getMaxPacketDelay() {
        return maxPacketDelay;
    }

    public long getMaxPacketHops() {
        return maxPacketHops;
    }

    public long getNumPayloadPacketsReceived() {
        return numPayloadPacketsReceived;
    }

    public long getNumPayloadPacketsTransmitted() {
        return numPayloadPacketsTransmitted;
    }

    public long getMaxPayloadPacketDelay() {
        return maxPayloadPacketDelay;
    }

    public long getMaxPayloadPacketHops() {
        return maxPayloadPacketHops;
    }

    public Map<Class<? extends Packet>, Long> getNumPacketsReceivedPerType() {
        return numPacketsReceivedPerType;
    }

    public Map<Class<? extends Packet>, Long> getNumPacketsTransmittedPerType() {
        return numPacketsTransmittedPerType;
    }

    public Map<Class<? extends Packet>, Long> getMaxPacketDelayPerType() {
        return maxPacketDelayPerType;
    }

    public Map<Class<? extends Packet>, Long> getMaxPacketHopsPerType() {
        return maxPacketHopsPerType;
    }

    public Map<FlitState, Long> getMaxFlitPerStateDelay() {
        return maxFlitPerStateDelay;
    }
}
