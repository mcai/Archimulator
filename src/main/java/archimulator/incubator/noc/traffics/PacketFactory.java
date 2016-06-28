package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

public interface PacketFactory<PacketT extends Packet> {
    PacketT create(Network<? extends Node, ? extends RoutingAlgorithm> network, int src, int dest, int size);
}
