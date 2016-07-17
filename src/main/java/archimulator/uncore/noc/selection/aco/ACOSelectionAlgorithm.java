package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;
import archimulator.uncore.noc.routers.Router;
import archimulator.uncore.noc.selection.AbstractSelectionAlgorithm;
import archimulator.util.Pair;

import java.util.List;

public class ACOSelectionAlgorithm extends AbstractSelectionAlgorithm {
    private PheromoneTable pheromoneTable;

    public ACOSelectionAlgorithm(Node node) {
        super(node);

        this.pheromoneTable = new PheromoneTable(this);

        double pheromoneValue = 1.0 / this.getNode().getNeighbors().size();

        for (int i = 0; i < this.getNode().getNetwork().getNumNodes(); i++) {
            if (this.getNode().getId() != i) {
                for (Direction direction : this.getNode().getNeighbors().keySet()) {
                    this.pheromoneTable.append(i, direction, pheromoneValue);
                }
            }
        }
    }

    @Override
    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        if (packet instanceof AntPacket) {
            if (((AntPacket) packet).isForward()) {
                packet.memorize(this.getNode().getId());

                this.createAndSendBackwardAntPacket((ForwardAntPacket) packet);
            } else {
                this.updateRoutingTable((BackwardAntPacket) packet, inputVirtualChannel);
            }

            packet.setEndCycle(this.getNode().getNetwork().getCycleAccurateEventQueue().getCurrentCycle());

            this.getNode().getNetwork().getInflightPackets().remove(packet);
            this.getNode().getNetwork().logPacketTransmitted(packet);

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
                if (this.getNode().getId() != packet.getSrc()) {
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
                this.getNode().getNetwork().getEnvironment().getConfig().getAntPacketSize(),
                null);

        newPacket.getMemory().addAll(packet.getMemory());

        this.getNode().getNetwork().getCycleAccurateEventQueue().schedule(
                this, () -> this.getNode().getNetwork().receive(newPacket), 1
        );
    }

    private Direction backwardAntPacket(BackwardAntPacket packet) {
        int i;

        for(i = packet.getMemory().size() - 1; i > 0; i--) {
            Pair<Integer, Long> entry = packet.getMemory().get(i);
            if(entry.getFirst() == this.getNode().getId()) {
                break;
            }
        }

        int prev = packet.getMemory().get(i - 1).getFirst();

        for (Direction direction : this.getNode().getNeighbors().keySet()) {
            int neighbor = this.getNode().getNeighbors().get(direction);
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
            if(entry.getFirst() == this.getNode().getId()) {
                break;
            }
        }

        for(int j = i + 1; j < packet.getMemory().size(); j++) {
            int dest = packet.getMemory().get(j).getFirst();
            this.pheromoneTable.update(dest, inputVirtualChannel.getInputPort().getDirection());
        }
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        double maxProbability = -1.0;
        Direction bestDirection = null;

        for(Direction direction : directions) {
            Pheromone pheromone = this.pheromoneTable.getPheromones().get(dest).get(direction);
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();
            int freeSlots = neighborRouter.freeSlots(direction.getReflexDirection(), ivc);

            double alpha = this.getNode().getNetwork().getEnvironment().getConfig().getAcoSelectionAlpha();
            double qTotal = this.getNode().getNetwork().getEnvironment().getConfig().getMaxInputBufferSize();
            int n = this.getNode().getNeighbors().size();

            double probability = (pheromone.getValue() + alpha * ((double) (freeSlots) / qTotal)) / (1 + alpha * (n - 1));
            if(probability > maxProbability) {
                maxProbability = probability;
                bestDirection = direction;
            }
        }

        return bestDirection;
    }

    public PheromoneTable getPheromoneTable() {
        return pheromoneTable;
    }
}
