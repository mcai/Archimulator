package archimulator.uncore.noc;

import archimulator.common.NoCEnvironment;
import archimulator.uncore.noc.routers.FlitState;
import archimulator.uncore.noc.routing.RoutingAlgorithmFactory;
import archimulator.uncore.noc.selection.SelectionAlgorithmFactory;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Network.
 *
 * @author  Min Cai
 */
public class Network {
    long currentPacketId;

    private NoCEnvironment environment;

    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private int numNodes;

    private List<Node> nodes;

    private RoutingAlgorithmFactory routingAlgorithmFactory;

    private SelectionAlgorithmFactory selectionAlgorithmFactory;

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

    /**
     * Create a network.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @param selectionAlgorithmFactory the node factory
     * @param routingAlgorithmFactory the routing algorithm factory
     */
    public Network(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes,
            SelectionAlgorithmFactory selectionAlgorithmFactory,
            RoutingAlgorithmFactory routingAlgorithmFactory
    ) {
        this.environment = environment;

        this.currentPacketId = 0;

        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.numNodes = numNodes;

        this.width = (int) Math.sqrt(this.numNodes);

        if(this.width * this.width != this.numNodes) {
            throw new RuntimeException("Only 2D meshes are supported");
        }

        this.selectionAlgorithmFactory = selectionAlgorithmFactory;

        this.routingAlgorithmFactory = routingAlgorithmFactory;

        this.nodes = new ArrayList<>();

        for(int i = 0; i < this.numNodes; i++) {
            Node node = new Node(this, i);

            node.setSelectionAlgorithm(this.selectionAlgorithmFactory.create(node));

            node.setRoutingAlgorithm(this.routingAlgorithmFactory.create(node));

            this.nodes.add(node);
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

        this.numFlitPerStateDelaySamples = new HashMap<>();
        this.totalFlitPerStateDelays = new HashMap<>();
        this.maxFlitPerStateDelay = new HashMap<>();
    }

    /**
     * Log the event when a packet is received.
     *
     * @param packet the packet that is received
     */
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

    /**
     * Log the event when a packet is transmitted.
     *
     * @param packet the packet that is transmitted
     */
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

    /**
     * Log the flit per-state delay.
     *
     * @param head a boolean value indicating whether the flit is head flit or not
     * @param tail a boolean value indicating whether the flit is tail flit or not
     * @param state the flit state
     * @param delay the delay that the flit is spent in the state
     */
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

    /**
     * Receives a packet.
     *
     * @param packet the packet that is received
     * @return a boolean value indicating whether the packet is received or not
     */
    public boolean receive(Packet packet) {
        if(!this.nodes.get(packet.getSrc()).getRouter().injectPacket(packet)) {
            this.cycleAccurateEventQueue.schedule(this, () -> receive(packet), 1);
            return false;
        }

        this.inflightPackets.add(packet);
        this.logPacketReceived(packet);

        return true;
    }

    /**
     * Calculate a random destination node ID for the specified source node ID.
     *
     * @param src the source node ID
     * @return a newly calculated random destination node ID for the specified source node ID
     */
    public int randDest(int src) {
        while (true) {
            int i = this.environment.getRandom().nextInt(this.numNodes);
            if(i != src) {
                return i;
            }
        }
    }

    /**
     * Get the throughput.
     *
     * @return the throughput
     */
    public double throughput() {
        return (double) this.numPacketsTransmitted / this.cycleAccurateEventQueue.getCurrentCycle() / this.numNodes;
    }

    /**
     * Get the average packet delay.
     *
     * @return the average packet delay
     */
    public double averagePacketDelay() {
        if(this.numPacketsTransmitted > 0) {
            return (double) this.totalPacketDelays / this.numPacketsTransmitted;
        }

        return 0.0;
    }

    /**
     * Get the average number of hops that a packet traverses.
     *
     * @return the average number of hops that a packet traverses
     */
    public double averagePacketHops() {
        if(this.numPacketsTransmitted > 0) {
            return (double) this.totalPacketHops / this.numPacketsTransmitted;
        }

        return 0.0;
    }

    /**
     * Get the payload throughput.
     *
     * @return the payload throughput
     */
    public double payloadThroughput() {
        return (double) this.numPayloadPacketsTransmitted / this.cycleAccurateEventQueue.getCurrentCycle() / this.numNodes;
    }

    /**
     * Get the average payload packet delay.
     *
     * @return the average payload packet delay
     */
    public double averagePayloadPacketDelay() {
        if(this.numPayloadPacketsTransmitted > 0) {
            return (double) this.totalPayloadPacketDelays / this.numPayloadPacketsTransmitted;
        }

        return 0.0;
    }

    /**
     * Get the average number of hops that a payload packet traverses.
     *
     * @return the average number of hops that a payload packet traverses
     */
    public double averagePayloadPacketHops() {
        if(this.numPayloadPacketsTransmitted > 0) {
            return (double) this.totalPayloadPacketHops / this.numPayloadPacketsTransmitted;
        }

        return 0.0;
    }

    /**
     * Get the throughput for the specified packet type.
     *
     * @param packetCls the packet type
     * @return the throughput for the specified packet type
     */
    public double throughputPerType(Class<? extends Packet> packetCls) {
        return (double) this.numPacketsTransmittedPerType.get(packetCls)
                / this.cycleAccurateEventQueue.getCurrentCycle()
                / this.numNodes;
    }

    /**
     * Get the average delay for the specified packet type.
     *
     * @param packetCls the packet type
     * @return the average delay for the specified packet type
     */
    public double averagePacketDelayPerType(Class<? extends Packet> packetCls) {
        if(this.numPacketsTransmittedPerType.get(packetCls) > 0) {
            return (double) this.totalPacketDelaysPerType.get(packetCls)
                    / this.numPacketsTransmittedPerType.get(packetCls);
        }

        return 0.0;
    }

    /**
     * Get the average number of hops that a packet of the specified type traverses.
     *
     * @param packetCls the packet type
     * @return the average number of hops that a packet of the specified type traverses
     */
    public double averagePacketHopsPerType(Class<? extends Packet> packetCls) {
        if(this.numPacketsTransmittedPerType.get(packetCls) > 0) {
            return (double) this.totalPacketHopsPerType.get(packetCls)
                    / this.numPacketsTransmittedPerType.get(packetCls);
        }

        return 0.0;
    }

    /**
     * Get the average delay that a flit spends in the specified state.
     *
     * @param state the flit state
     * @return the average delay that a flit spends in the specified state
     */
    public double averageFlitPerStateDelay(FlitState state) {
        if(this.numFlitPerStateDelaySamples.containsKey(state) && this.numFlitPerStateDelaySamples.get(state) > 0) {
            return (double) this.totalFlitPerStateDelays.get(state)
                    / this.numFlitPerStateDelaySamples.get(state);
        }

        return 0.0;
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
     * Get the number of nodes.
     *
     * @return the number of nodes
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Get the list of nodes.
     *
     * @return the list of nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Get the routing algorithm factory.
     *
     * @return the routing algorithm factory
     */
    public RoutingAlgorithmFactory getRoutingAlgorithmFactory() {
        return routingAlgorithmFactory;
    }

    /**
     * Get the selection algorithm factory.
     *
     * @return the selection algorithm factory
     */
    public SelectionAlgorithmFactory getSelectionAlgorithmFactory() {
        return selectionAlgorithmFactory;
    }

    /**
     * Get the width.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the list of inflight packets.
     *
     * @return the list of inflight packets
     */
    public List<Packet> getInflightPackets() {
        return inflightPackets;
    }

    /**
     * Get a boolean value indicating whether the network can accept new packets or not.
     *
     * @return a boolean value indicating whether the network can accept new packets or not
     */
    public boolean isAcceptPacket() {
        return acceptPacket;
    }

    /**
     * Set a boolean value indicating whether the network can accept new packets or not.
     *
     * @param acceptPacket a boolean value indicating whether the network can accept new packets or not
     */
    public void setAcceptPacket(boolean acceptPacket) {
        this.acceptPacket = acceptPacket;
    }

    /**
     * Get the number of packets that the network has received.
     *
     * @return the number of packets that the network has received
     */
    public long getNumPacketsReceived() {
        return numPacketsReceived;
    }

    /**
     * Get the number of packets that the network has transmitted.
     *
     * @return the number of packets that the network has transmitted
     */
    public long getNumPacketsTransmitted() {
        return numPacketsTransmitted;
    }

    /**
     * Get the maximum delay that a packet experiences.
     *
     * @return the maximum delay that a packet experiences
     */
    public long getMaxPacketDelay() {
        return maxPacketDelay;
    }

    /**
     * Get the maximum number of hops that a packet traverses.
     *
     * @return the maximum number of hops that a packet traverses
     */
    public long getMaxPacketHops() {
        return maxPacketHops;
    }

    /**
     * Get the number of payload packets that the network has received.
     *
     * @return the number of payload packets that the network has received
     */
    public long getNumPayloadPacketsReceived() {
        return numPayloadPacketsReceived;
    }

    /**
     * Get the number of payload packets that the network has transmitted.
     *
     * @return the number of payload packets that the network has transmitted
     */
    public long getNumPayloadPacketsTransmitted() {
        return numPayloadPacketsTransmitted;
    }

    /**
     * Get the maximum delay that a payload packet experiences.
     *
     * @return the maximum delay that a payload packet experiences
     */
    public long getMaxPayloadPacketDelay() {
        return maxPayloadPacketDelay;
    }

    /**
     * Get the maximum number of hops that a payload packet traverses.
     *
     * @return the maximum number of hops that a payload packet traverses
     */
    public long getMaxPayloadPacketHops() {
        return maxPayloadPacketHops;
    }

    /**
     * Get the map of the number of packets that the network has received.
     *
     * @return the map of the number of packets that the network has received
     */
    public Map<Class<? extends Packet>, Long> getNumPacketsReceivedPerType() {
        return numPacketsReceivedPerType;
    }

    /**
     * Get the map of the number of packets that the network has transmitted.
     *
     * @return the map of the number of packets that the network has transmitted
     */
    public Map<Class<? extends Packet>, Long> getNumPacketsTransmittedPerType() {
        return numPacketsTransmittedPerType;
    }

    /**
     * Get the map of the maximum delay that a packet experiences.
     *
     * @return the map of the maximum delay that a packet experiences
     */
    public Map<Class<? extends Packet>, Long> getMaxPacketDelayPerType() {
        return maxPacketDelayPerType;
    }

    /**
     * Get the map of the maximum number of hops that a packet traverses.
     *
     * @return the map of the maximum number of hops that a packet traverses
     */
    public Map<Class<? extends Packet>, Long> getMaxPacketHopsPerType() {
        return maxPacketHopsPerType;
    }

    /**
     * Get the map of the maximum per-state delay that a flit spends.
     *
     * @return the map of the maximum per-state delay that a flit spends
     */
    public Map<FlitState, Long> getMaxFlitPerStateDelay() {
        return maxFlitPerStateDelay;
    }

    /**
     * Get the environment.
     *
     * @return the environment
     */
    public NoCEnvironment getEnvironment() {
        return environment;
    }
}
