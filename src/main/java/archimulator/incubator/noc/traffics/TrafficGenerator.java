package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.RoutingAlgorithm;

/**
 * Traffic generator.
 *
 * @author Min Cai
 */
public class TrafficGenerator<PacketT extends Packet> {
    private Network<? extends Node, ? extends RoutingAlgorithm> network;
    private double packetInjectionRate;
    private PacketFactory<PacketT> packetFactory;
    private int packetSize;
    private long maxPackets;

    public TrafficGenerator(
            Network<? extends Node, ? extends RoutingAlgorithm> network,
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
    }

    public Network<? extends Node, ? extends RoutingAlgorithm> getNetwork() {
        return network;
    }

    public double getPacketInjectionRate() {
        return packetInjectionRate;
    }

    public PacketFactory<PacketT> getPacketFactory() {
        return packetFactory;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public long getMaxPackets() {
        return maxPackets;
    }
}
