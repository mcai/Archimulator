package archimulator.incubator.noc.selection.aco;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.Packet;
import archimulator.incubator.noc.routers.InputVirtualChannel;
import archimulator.incubator.noc.routers.Router;
import archimulator.incubator.noc.routing.RoutingAlgorithm;
import javaslang.Function1;
import javaslang.Tuple2;
import javaslang.collection.List;

public class ACONode extends Node {
    private ACORoutingTable routingTable;

    public ACONode(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        super(network, id);

        this.routingTable = new ACORoutingTable(this);

        double pheromoneValue = 1.0 / this.getNeighbors().size();

        for (int i = 0; i < this.getNetwork().getNumNodes(); i++) {
            if (this.getId() != i) {
                for (Direction direction : this.getNeighbors().keySet()) {
                    this.routingTable.append(i, direction, pheromoneValue);
                }
            }
        }
    }

    @Override
    public String toString() {
        return String.format("ACONode{id=%d, x=%d, y=%d, neighbors=%s}", getId(), getX(), getY(), getNeighbors());
    }

    @Override
    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        if (packet instanceof AntPacket) {
            if (((AntPacket) packet).isForward()) {
                packet.memorize(this.getId());

                this.createAndSendBackwardAntPacket((ForwardAntPacket) packet);
            } else {
                this.updateRoutingTable((BackwardAntPacket) packet, inputVirtualChannel);
            }

            packet.setEndCycle(this.getNetwork().getCycleAccurateEventQueue().getCurrentCycle());

            this.getNetwork().getInflightPackets().remove(packet);
            this.getNetwork().logPacketTransmitted(packet);

            if (packet.getOnCompletedCallback() != null) {
                packet.getOnCompletedCallback().apply();
            }
        } else {
            super.handleDestArrived(packet, inputVirtualChannel);
        }
    }

    @Override
    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        if (packet instanceof AntPacket) {
            if (((AntPacket) packet).isForward()) {
                return super.doRouteCalculation(packet, inputVirtualChannel);
            } else {
                if (this.getId() != packet.getSrc()) {
                    this.updateRoutingTable((BackwardAntPacket) packet, inputVirtualChannel);
                }

                return this.backwardAntPacket((BackwardAntPacket) packet);
            }
        } else {
            return super.doRouteCalculation(packet, inputVirtualChannel);
        }
    }

    private void createAndSendBackwardAntPacket(ForwardAntPacket packet) {
        BackwardAntPacket newPacket = new BackwardAntPacket(
                packet.getNetwork(),
                packet.getDest(),
                packet.getSrc(),
                this.getNetwork().getExperiment().getConfig().getAntPacketSize(),
                null);

        newPacket.getMemory().appendAll(packet.getMemory());

        this.getNetwork().getCycleAccurateEventQueue().schedule(
                this, () -> this.getNetwork().receive(newPacket), 1
        );
    }

    private Direction backwardAntPacket(BackwardAntPacket packet) {
        int i = packet.getMemory().lastIndexWhere(x -> x._1() == this.getId());

        int prev = packet.getMemory().get(i - 1)._1();

        for (Direction direction : this.getNeighbors().keySet()) {
            int neighbor = this.getNeighbors().get(direction).get();
            if (neighbor == prev) {
                return direction;
            }
        }

        throw new IllegalArgumentException();
    }

    private void updateRoutingTable(BackwardAntPacket packet, InputVirtualChannel inputVirtualChannel) {
        int i = packet.getMemory().indexWhere(x -> x._1() == this.getId());

        for (int dest : packet.getMemory().slice(i + 1, packet.getMemory().size() - 1).map(Tuple2::_1)) {
            this.routingTable.update(dest, inputVirtualChannel.getInputPort().getDirection());
        }
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        return directions.maxBy((Function1<Direction, Double>) direction -> {
            Pheromone pheromone = this.routingTable.getPheromones().get(dest).get().get(direction).get();
            Router neighborRouter = this.getNetwork().getNodes().get(ACONode.this.getNeighbors().get(direction).get()).getRouter();
            int freeSlots = neighborRouter.freeSlots(direction.getReflexDirection(), ivc);

            double alpha = this.getNetwork().getExperiment().getConfig().getAcoSelectionAlpha();
            double qTotal = this.getNetwork().getExperiment().getConfig().getMaxInputBufferSize();
            int n = this.getNeighbors().size();

            return (pheromone.getValue() + alpha * ((double) (freeSlots) / qTotal)) / (1 + alpha * (n - 1));
        }).get();
    }

    public ACORoutingTable getRoutingTable() {
        return routingTable;
    }
}
