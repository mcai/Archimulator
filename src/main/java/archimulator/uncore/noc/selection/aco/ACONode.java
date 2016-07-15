package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;
import archimulator.uncore.noc.routers.Router;
import archimulator.uncore.noc.routing.RoutingAlgorithm;
import archimulator.util.Pair;

import java.util.List;

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
                packet.getOnCompletedCallback().run();
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
                this.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getAntPacketSize(),
                null);

        newPacket.getMemory().addAll(packet.getMemory());

        this.getNetwork().getCycleAccurateEventQueue().schedule(
                this, () -> this.getNetwork().receive(newPacket), 1
        );
    }

    private Direction backwardAntPacket(BackwardAntPacket packet) {
        int i;

        for(i = packet.getMemory().size() - 1; i > 0; i--) {
            Pair<Integer, Long> entry = packet.getMemory().get(i);
            if(entry.getFirst() == this.getId()) {
                break;
            }
        }

        int prev = packet.getMemory().get(i - 1).getFirst();

        for (Direction direction : this.getNeighbors().keySet()) {
            int neighbor = this.getNeighbors().get(direction);
            if (neighbor == prev) {
                return direction;
            }
        }

        throw new IllegalArgumentException();
    }

    private void updateRoutingTable(BackwardAntPacket packet, InputVirtualChannel inputVirtualChannel) {
        int i;

        for(i = 0; i < packet.getMemory().size() ; i++) {
            Pair<Integer, Long> entry = packet.getMemory().get(i);
            if(entry.getFirst() == this.getId()) {
                break;
            }
        }

        for(int j = i + 1; j < packet.getMemory().size(); j++) {
            int dest = packet.getMemory().get(j).getFirst();
            this.routingTable.update(dest, inputVirtualChannel.getInputPort().getDirection());
        }
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        double maxProbability = -1.0;
        Direction bestDirection = null;

        for(Direction direction : directions) {
            Pheromone pheromone = this.routingTable.getPheromones().get(dest).get(direction);
            Router neighborRouter = this.getNetwork().getNodes().get(this.getNeighbors().get(direction)).getRouter();
            int freeSlots = neighborRouter.freeSlots(direction.getReflexDirection(), ivc);

            double alpha = this.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getAcoSelectionAlpha();
            double qTotal = this.getNetwork().getMemoryHierarchy().getExperiment().getConfig().getMaxInputBufferSize();
            int n = this.getNeighbors().size();

            double probability = (pheromone.getValue() + alpha * ((double) (freeSlots) / qTotal)) / (1 + alpha * (n - 1));
            if(probability > maxProbability) {
                maxProbability = probability;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

    public ACORoutingTable getRoutingTable() {
        return routingTable;
    }
}
