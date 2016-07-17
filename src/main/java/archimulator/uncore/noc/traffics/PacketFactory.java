package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Packet;

/**
 * Packet factory.
 *
 * @author Min Cai
 */
public interface PacketFactory {
    /**
     * Create a packet.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param size the size of the packet
     * @return the newly created packet
     */
    Packet create(int src, int dest, int size);
}
