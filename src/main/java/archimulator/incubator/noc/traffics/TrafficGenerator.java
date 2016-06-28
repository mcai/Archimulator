package archimulator.incubator.noc.traffics;

import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

import java.util.List;

/**
 * Traffic generator.
 *
 * @author Min Cai
 */
public abstract class TrafficGenerator<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm, PacketT extends Packet> {
    private Network<NodeT, RoutingAlgorithmT> network;
    private double packetInjectionRate;
    private PacketFactory<PacketT> packetFactory;
    private int packetSize;
    private long maxPackets;

    public TrafficGenerator(
            Network<NodeT, RoutingAlgorithmT> network,
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

        this.network.getCycleAccurateEventQueue().getPerCycleEvents().add(this::generateTraffic);
    }

    private void generateTraffic() {
        for(Node node : this.getSrcNodes()) {
            if(!this.network.isAcceptPacket() || this.maxPackets != -1 && this.network.getNumPacketsReceived() > this.maxPackets) {
                break;
            }

            boolean valid = this.network.getExperiment().getRandom().nextDouble() <= this.packetInjectionRate;
            if(valid) {
                int src = node.getId();
                int dest = this.dest(src);

                if(src != dest) {
                    this.injectPacketWithDelay(src, dest);
                }
            }
        }
    }

    protected List<NodeT> getSrcNodes() {
        return this.network.getNodes();
    }

    protected abstract int dest(int src);

    protected void injectPacketWithDelay(int src, int dest) {
        this.network.getCycleAccurateEventQueue().schedule(this, () -> {
            this.network.receive(this.packetFactory.create(this.network, src, dest, this.packetSize));
        }, 1);
    }

    public Network<NodeT, RoutingAlgorithmT> getNetwork() {
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
