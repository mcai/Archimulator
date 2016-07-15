package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routing.RoutingAlgorithm;

import java.util.List;

/**
 * Synthetic traffic generator.
 *
 * @author Min Cai
 */
public abstract class SyntheticTrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet> {
    private Network<NodeT, RoutingAlgorithmT> network;
    private double packetInjectionRate;
    private PacketFactory<PacketT> packetFactory;
    private int packetSize;
    private long maxPackets;

    /**
     * Create a synthetic traffic generator.
     *
     * @param network the parent network
     * @param packetInjectionRate the packet injection rate
     * @param packetFactory the packet factory
     * @param packetSize the packet size
     * @param maxPackets the maximum number of packets to be generated
     */
    public SyntheticTrafficGenerator(
            Network<NodeT, RoutingAlgorithmT> network,
            double packetInjectionRate,
            PacketFactory<PacketT> packetFactory,
            int packetSize,
            long maxPackets
    ) {
        this.network = network;
        this.packetInjectionRate = packetInjectionRate;
        this.packetFactory = packetFactory;
        this.packetSize = packetSize;
        this.maxPackets = maxPackets;

        this.network.getCycleAccurateEventQueue().getPerCycleEvents().add(this::generateTraffic);
    }

    /**
     * Generate traffic.
     */
    private void generateTraffic() {
        for(Node node : this.getSrcNodes()) {
            if(!this.network.isAcceptPacket() || this.maxPackets != -1 && this.network.getNumPacketsReceived() > this.maxPackets) {
                break;
            }

            boolean valid = this.network.getEnvironment().getRandom().nextDouble() <= this.packetInjectionRate;
            if(valid) {
                int src = node.getId();
                int dest = this.dest(src);

                if(src != dest) {
                    this.injectPacketWithDelay(src, dest);
                }
            }
        }
    }

    /**
     * Get the list of source nodes.
     *
     * @return the list of source nodes
     */
    protected List<NodeT> getSrcNodes() {
        return this.network.getNodes();
    }

    /**
     * Get the destination node ID for the specified source node ID.
     *
     * @param src the source node ID
     * @return the destination node ID for the specified source node ID
     */
    protected abstract int dest(int src);

    /**
     * Inject a packet with one cycle delay.
     *
     * @param src the source node ID
     * @param dest the target node ID
     */
    protected void injectPacketWithDelay(int src, int dest) {
        this.network.getCycleAccurateEventQueue().schedule(this, () -> {
            this.network.receive(this.packetFactory.create(this.network, src, dest, this.packetSize));
        }, 1);
    }

    /**
     * Get the parent network.
     *
     * @return the parent network
     */
    public Network<NodeT, RoutingAlgorithmT> getNetwork() {
        return network;
    }

    /**
     * Get the packet injection rate.
     *
     * @return the packet injection rate
     */
    public double getPacketInjectionRate() {
        return packetInjectionRate;
    }

    /**
     * Get the packet factory.
     *
     * @return the packet factory
     */
    public PacketFactory<PacketT> getPacketFactory() {
        return packetFactory;
    }

    /**
     * Get the packet size.
     *
     * @return the packet size
     */
    public int getPacketSize() {
        return packetSize;
    }

    /**
     * Get the maximum number of packets.
     *
     * @return the maximum number of packets
     */
    public long getMaxPackets() {
        return maxPackets;
    }
}
