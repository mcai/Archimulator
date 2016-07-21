/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;
import archimulator.uncore.noc.routers.Router;
import archimulator.uncore.noc.selection.AbstractSelectionAlgorithm;
import archimulator.util.Pair;

import java.util.List;

/**
 * Ant colony optimization (ACO) based selection algorithm.
 *
 * @author Min Cai
 */
public class ACOSelectionAlgorithm extends AbstractSelectionAlgorithm {
    private PheromoneTable pheromoneTable;

    /**
     * Create an ant colony optimization (ACO) based selection algorithm.
     *
     * @param node the parent node
     */
    public ACOSelectionAlgorithm(Node node) {
        super(node);

        this.pheromoneTable = new PheromoneTable(this.getNode());

        double pheromoneValue = 1.0 / this.getNode().getNeighbors().size();

        for (int i = 0; i < this.getNode().getNetwork().getNumNodes(); i++) {
            if (this.getNode().getId() != i) {
                for (Direction direction : this.getNode().getNeighbors().keySet()) {
                    this.pheromoneTable.append(i, direction, pheromoneValue);
                }
            }
        }
    }

    /**
     * Handle the event when a packet has arrived at its destination node.
     *
     * @param packet the packet
     * @param inputVirtualChannel the input virtual channel
     */
    @Override
    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        if (packet instanceof AntPacket) {
            if (((AntPacket) packet).isForward()) {
                packet.memorize(this.getNode().getId());

                this.createAndSendBackwardAntPacket((ForwardAntPacket) packet);
            } else {
                this.updatePheromoneTable((BackwardAntPacket) packet, inputVirtualChannel);
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

    /**
     * Do route calculation for a packet.
     *
     * @param packet the packet
     * @param inputVirtualChannel input virtual channel
     * @return the newly calculated output direction for routing the packet
     */
    @Override
    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        if (packet instanceof AntPacket) {
            if (((AntPacket) packet).isForward()) {
                return super.doRouteCalculation(packet, inputVirtualChannel);
            } else {
                if (this.getNode().getId() != packet.getSrc()) {
                    this.updatePheromoneTable((BackwardAntPacket) packet, inputVirtualChannel);
                }

                return this.backwardAntPacket((BackwardAntPacket) packet);
            }
        } else {
            return super.doRouteCalculation(packet, inputVirtualChannel);
        }
    }

    /**
     * Create and send a backward ant packet for the forward ant packet.
     *
     * @param packet the forward ant packet
     */
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

    /**
     * Send the backward ant packet to the next hop.
     *
     * @param packet the backward ant packet
     * @return the next hop that is calculated for the backward ant packet
     */
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

    /**
     * Update the pheromone table for the specified backward ant packet.
     *
     * @param packet the backward ant packet
     * @param inputVirtualChannel the input virtual channel
     */
    private void updatePheromoneTable(BackwardAntPacket packet, InputVirtualChannel inputVirtualChannel) {
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

    /**
     * Select the best output direction from a list of candidate output directions.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param ivc the input virtual channel ID
     * @param directions the list of candidate output directions
     * @return the best output direction selected from a list of candidate output directions
     */
    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        double maxProbability = -1.0;
        Direction bestDirection = null;

        for(Direction direction : directions) {
            Pheromone pheromone = this.pheromoneTable.getPheromones().get(dest).get(direction);
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();
            int freeSlots = neighborRouter.getFreeSlots(direction.getReflexDirection(), ivc);

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

    /**
     * Get the pheromone table.
     *
     * @return the pheromone table
     */
    public PheromoneTable getPheromoneTable() {
        return pheromoneTable;
    }
}
