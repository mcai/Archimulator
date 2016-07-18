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
package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.Packet;
import archimulator.uncore.noc.routers.InputVirtualChannel;

import java.util.List;

/**
 * Abstract selection algorithm.
 *
 * @author Min Cai
 */
public class AbstractSelectionAlgorithm implements SelectionAlgorithm {
    private Node node;

    /**
     * Create an abstract selection algorithm for the specified node.
     *
     * @param node the parent node
     */
    public AbstractSelectionAlgorithm(Node node) {
        this.node = node;
    }

    /**
     * Handle the event when a packet has arrived at its destination node.
     *
     * @param packet the packet
     * @param inputVirtualChannel the input virtual channel
     */
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

    /**
     * Do route calculation for a packet.
     *
     * @param packet the packet
     * @param inputVirtualChannel input virtual channel
     * @return the newly calculated output direction for routing the packet
     */
    @Override
    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        int parent = !packet.getMemory().isEmpty() ? packet.getMemory().get(packet.getMemory().size() - 1).getFirst() : -1;

        packet.memorize(this.node.getId());

        List<Direction> directions =
                this.node.getRoutingAlgorithm().nextHop(packet.getSrc(), packet.getDest(), parent);

        return this.select(packet.getSrc(), packet.getDest(), inputVirtualChannel.getId(), directions);
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
        return directions.get(0);
    }

    /**
     * Get the parent node.
     *
     * @return the parent node
     */
    public Node getNode() {
        return node;
    }
}
