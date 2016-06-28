package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

public class TransposeTrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet>
        extends TrafficGenerator<NodeT, RoutingAlgorithmT, PacketT> {
    public TransposeTrafficGenerator(
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
        int srcX = Node.getX(this.getNetwork(), src);
        int srcY = Node.getY(this.getNetwork(), src);

        int destX = this.getNetwork().getWidth() - 1 - srcY;
        int destY = this.getNetwork().getWidth() - 1 - srcX;

        return destY * this.getNetwork().getWidth() + destX;
    }
}
