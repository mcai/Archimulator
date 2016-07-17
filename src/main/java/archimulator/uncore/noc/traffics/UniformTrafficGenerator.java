package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;

/**
 * Uniform traffic generator.
 *
 * @author Min Cai
 */
public class UniformTrafficGenerator
        extends SyntheticTrafficGenerator {
    /**
     * Create a uniform traffic generator.
     *
     * @param network the parent network
     * @param packetInjectionRate the packet injection rate
     * @param packetFactory the packet factory
     * @param packetSize the size of a packet
     * @param maxPackets the maximum number of packets to be generated
     */
    public UniformTrafficGenerator(
            Network network,
            double packetInjectionRate,
            PacketFactory packetFactory,
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
        return this.getNetwork().randDest(src);
    }
}
