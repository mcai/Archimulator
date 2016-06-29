package archimulator.uncore.net.noc.traffics;

import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

public class UniformTrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet>
        extends TrafficGenerator<NodeT, RoutingAlgorithmT, PacketT> {
    public UniformTrafficGenerator(
            Network<NodeT, RoutingAlgorithmT> network,
            double packetInjectionRate,
            PacketFactory<PacketT> packetFactory,
            int packetSize,
            long maxPackets
    ) {
        super(network, packetInjectionRate, packetFactory, packetSize, maxPackets);
    }

    @Override
    protected int dest(int src) {
        return this.getNetwork().randDest(src);
    }
}
