package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routing.RoutingAlgorithm;

/**
 * Packet factory.
 *
 * @param <PacketT> the packet type
 * @author Min Cai
 */
public interface PacketFactory<PacketT extends Packet> {
    /**
     * Create a packet.
     *
     * @param network the parent network
     * @param src the source node ID
     * @param dest the destination node ID
     * @param size the size of the packet
     * @return the newly created packet
     */
    PacketT create(Network<? extends Node, ? extends RoutingAlgorithm> network, int src, int dest, int size);
}
