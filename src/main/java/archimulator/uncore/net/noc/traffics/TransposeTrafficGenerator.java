package archimulator.uncore.net.noc.traffics;

import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

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
