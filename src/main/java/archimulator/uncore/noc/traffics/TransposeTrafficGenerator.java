package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routing.RoutingAlgorithm;

/**
 * Transpose traffic generator.
 *
 * @param <NodeT>             the node type
 * @param <RoutingAlgorithmT> the routing algorithm type
 * @param <PacketT>           the packet type
 * @author Min Cai
 */
public class TransposeTrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet>
        extends SyntheticTrafficGenerator<NodeT, RoutingAlgorithmT, PacketT> {
    /**
     * Create a transpose traffic generator.
     *
     * @param network the parent network
     * @param packetInjectionRate the packet injection rate
     * @param packetFactory the packet factory
     * @param packetSize the size of a packet
     * @param maxPackets the maximum number of packets to be generated
     */
    public TransposeTrafficGenerator(
            Network<NodeT, RoutingAlgorithmT> network,
            double packetInjectionRate,
            PacketFactory<PacketT> packetFactory,
            int packetSize,
            long maxPackets
    ) {
        super(network, packetInjectionRate, packetFactory, packetSize, maxPackets);
    }

    /**
     * Get the destination node ID for the specified source node ID.
     *
     * @param src the source node ID
     * @return the destination node ID for the specified source node ID
     */
    @Override
    protected int dest(int src) {
        int srcX = Node.getX(this.getNetwork(), src);
        int srcY = Node.getY(this.getNetwork(), src);

        int destX = this.getNetwork().getWidth() - 1 - srcY;
        int destY = this.getNetwork().getWidth() - 1 - srcX;

        return destY * this.getNetwork().getWidth() + destX;
    }
}
