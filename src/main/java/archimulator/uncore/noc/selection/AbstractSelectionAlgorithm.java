package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;

import java.util.List;

public class AbstractSelectionAlgorithm implements SelectionAlgorithm {
    private Node node;

    public AbstractSelectionAlgorithm(Node node) {
        this.node = node;
    }

    @Override
    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        packet.memorize(this.node.getId());

        packet.setEndCycle(this.node.getNetwork().getCycleAccurateEventQueue().getCurrentCycle());

        this.node.getNetwork().getInflightPackets().remove(packet);
        this.node.getNetwork().logPacketTransmitted(packet);

        if(packet.getOnCompletedCallback() != null) {
            packet.getOnCompletedCallback().run();
        }
    }

    @Override
    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        int parent = !packet.getMemory().isEmpty() ? packet.getMemory().get(packet.getMemory().size() - 1).getFirst() : -1;

        packet.memorize(this.node.getId());

        List<Direction> directions =
                this.node.getNetwork().getRoutingAlgorithm().nextHop(this.node, packet.getSrc(), packet.getDest(), parent);

        return this.select(packet.getSrc(), packet.getDest(), inputVirtualChannel.getId(), directions);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        return directions.get(0);
    }

    public Node getNode() {
        return node;
    }
}
