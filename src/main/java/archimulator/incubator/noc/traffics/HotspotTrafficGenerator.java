package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routing.RoutingAlgorithm;
import javaslang.collection.List;

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

        int numHotspots = this.getNetwork().getExperiment().getRandom().nextInt(this.getNetwork().getWidth()) + 1;

        this.hotspots = List.empty();

        for(int i = 0; i < numHotspots; i++) {
            int hotspot = this.getNetwork().getExperiment().getRandom().nextInt(this.getNetwork().getNumNodes());

            if(!this.hotspots.contains(hotspot)) {
                this.hotspots.append(hotspot);
            }
        }
    }

    @Override
    protected List<NodeT> getSrcNodes() {
        List<NodeT> hotspotNodes = this.hotspots.map(i -> this.getNetwork().getNodes().get(i));
        return this.getNetwork().getNodes().appendAll(hotspotNodes);
    }
}
