package archimulator.uncore.net.noc.traffics;

import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

public interface PacketFactory<PacketT extends Packet> {
    PacketT create(Network<? extends Node, ? extends RoutingAlgorithm> network, int src, int dest, int size);
}
