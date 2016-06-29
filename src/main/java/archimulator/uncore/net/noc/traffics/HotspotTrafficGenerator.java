package archimulator.uncore.net.noc.traffics;

import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.Packet;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Hotspot traffic generator.
 *
 * @param <PacketT> packet type
 * @author Min Cai
 */
public class HotspotTrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet>
        extends UniformTrafficGenerator<NodeT, RoutingAlgorithmT, PacketT> {
    private List<Integer> hotspots;

    public HotspotTrafficGenerator(
            Network<NodeT, RoutingAlgorithmT> network,
            double packetInjectionRate,
            PacketFactory<PacketT> packetFactory,
            int packetSize,
            long maxPackets
    ) {
        super(network, packetInjectionRate, packetFactory, packetSize, maxPackets);

        int numHotspots = this.getNetwork().getSettings().getRandom().nextInt(this.getNetwork().getWidth()) + 1;

        this.hotspots = new ArrayList<>();

        for(int i = 0; i < numHotspots; i++) {
            int hotspot = this.getNetwork().getSettings().getRandom().nextInt(this.getNetwork().getNumNodes());

            if(!this.hotspots.contains(hotspot)) {
                this.hotspots.add(hotspot);
            }
        }
    }

    @Override
    protected List<NodeT> getSrcNodes() {
        List<NodeT> srcNodes = new ArrayList<>();

        srcNodes.addAll(this.getNetwork().getNodes());

        for(int hotspot : this.hotspots) {
            srcNodes.add(this.getNetwork().getNodes().get(hotspot));
        }

        return srcNodes;
    }
}
