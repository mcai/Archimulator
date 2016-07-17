package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Hotspot traffic generator.
 *
 * @author Min Cai
 */
public class HotspotTrafficGenerator extends UniformTrafficGenerator {
    private List<Integer> hotspots;

    /**
     * Create a hotspot traffic generator.
     *
     * @param network the parent network
     * @param packetInjectionRate the packet injection rate
     * @param packetFactory the packet factory
     * @param packetSize the size of a packet
     * @param maxPackets the maximum number of packets to be generated
     */
    public HotspotTrafficGenerator(
            Network network,
            double packetInjectionRate,
            PacketFactory packetFactory,
            int packetSize,
            long maxPackets
    ) {
        super(network, packetInjectionRate, packetFactory, packetSize, maxPackets);

        int numHotspots = this.getNetwork().getEnvironment().getRandom().nextInt(this.getNetwork().getWidth()) + 1;

        this.hotspots = new ArrayList<>();

        for(int i = 0; i < numHotspots; i++) {
            int hotspot = this.getNetwork().getEnvironment().getRandom().nextInt(this.getNetwork().getNumNodes());

            if(!this.hotspots.contains(hotspot)) {
                this.hotspots.add(hotspot);
            }
        }
    }

    /**
     * Get the list of source nodes.
     *
     * @return the list of source nodes
     */
    @Override
    protected List<Node> getSrcNodes() {
        List<Node> srcNodes = new ArrayList<>();

        srcNodes.addAll(this.getNetwork().getNodes());

        for(int hotspot : this.hotspots) {
            srcNodes.add(this.getNetwork().getNodes().get(hotspot));
        }

        return srcNodes;
    }
}
