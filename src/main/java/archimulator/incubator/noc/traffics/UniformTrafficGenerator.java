package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

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
