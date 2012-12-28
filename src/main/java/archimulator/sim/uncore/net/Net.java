/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.uncore.net;

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.uncore.MemoryHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import net.pickapack.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Net.
 *
 * @author Min Cai
 */
public abstract class Net extends BasicSimulationObject {
    /**
     * The map of the destination devices to the End point nodes.
     */
    protected Map<MemoryDevice, EndPointNode> endPointNodes;

    /**
     * The switch node.
     */
    protected SwitchNode switchNode;

    /**
     * Create a net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    public Net(MemoryHierarchy memoryHierarchy) {
        super(memoryHierarchy);

        this.endPointNodes = new HashMap<MemoryDevice, EndPointNode>();

        this.setup(memoryHierarchy);
        this.calculateRoutes();
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    protected abstract void setup(MemoryHierarchy memoryHierarchy);

    /**
     * Calculate the routes.
     */
    private void calculateRoutes() {
        Map<RoutingEntry, NetNode> routes = new HashMap<RoutingEntry, NetNode>();

        List<NetNode> nodes = new ArrayList<NetNode>();
        nodes.add(this.switchNode);
        nodes.addAll(this.endPointNodes.values());

        /* Initialize table with infinite costs */
        for (NetNode sourceNode : nodes) {
            for (NetNode destinationNode : nodes) {
                RoutingEntry routingEntry = new RoutingEntry(sourceNode, destinationNode);
                routingEntry.setCost(sourceNode == destinationNode ? 0 : nodes.size());
                sourceNode.getRoutingEntries().put(routingEntry.getDestinationNode(), routingEntry);
            }
        }

        /* Set 1-jump connections */
        for (NetNode node : nodes) {
            for (OutPort outPort : node.getOutPorts()) {
                if (outPort.getLink() != null) {
                    NetLink link = outPort.getLink();
                    final RoutingEntry routingEntry = node.getRoutingEntries().get(link.getDestinationPort().getNode());
                    routingEntry.setCost(1);
                    routes.put(routingEntry, link.getDestinationPort().getNode());
                }
            }
        }

        /* Calculate shortest paths Floyd-Warshall algorithm.*/
        for (NetNode nodeK : nodes) {
            for (NetNode nodeI : nodes) {
                for (NetNode nodeJ : nodes) {
                    RoutingEntry routingEntryIK = nodeI.getRoutingEntries().get(nodeK);
                    RoutingEntry routingEntryKJ = nodeK.getRoutingEntries().get(nodeJ);
                    RoutingEntry routingEntryIJ = nodeI.getRoutingEntries().get(nodeJ);

                    if (routingEntryIK.getCost() + routingEntryKJ.getCost() < routingEntryIJ.getCost()) {
                        routingEntryIJ.setCost(routingEntryIK.getCost() + routingEntryKJ.getCost());
                        routes.put(routingEntryIJ, nodeK);
                    }
                }
            }
        }

        /* Calculate net port values */
        for (NetNode nodeI : nodes) {
            for (NetNode nodeJ : nodes) {
                RoutingEntry routingEntry = nodeI.getRoutingEntries().get(nodeJ);

                NetNode nodeNext = routes.get(routingEntry);
                if (nodeNext == null) {
                    routingEntry.setOutPort(null);
                    continue;
                }

                while (nodeI.getRoutingEntries().get(nodeNext).getCost() > 1) {
                    nodeNext = routes.get(nodeI.getRoutingEntries().get(nodeNext));
                }

                for (OutPort port : nodeI.getOutPorts()) {
                    NetLink link = port.getLink();
                    if (link != null && link.getDestinationPort().getNode() == nodeNext) {
                        routingEntry.setOutPort(port);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Create a bi-directional link between the specified two nodes.
     *
     * @param node1 the first node
     * @param node2 the second node
     * @param bandwidth the bandwidth of the bi-directional link
     */
    protected void createBidirectionalLink(NetNode node1, NetNode node2, int bandwidth) {
        new NetLink(node1, node2, bandwidth);
        new NetLink(node2, node1, bandwidth);
    }

    /**
     * Transfer a message of the specified size from the source device to the destination device.
     *
     * @param sourceDevice the source device
     * @param destinationDevice the destination device
     * @param size the size
     * @param onCompletedCallback the callback action performed when the transfer is completed
     */
    public void transfer(MemoryDevice sourceDevice, MemoryDevice destinationDevice, int size, Action onCompletedCallback) {
        EndPointNode sourceNode = this.endPointNodes.get(sourceDevice);
        EndPointNode destinationNode = this.endPointNodes.get(destinationDevice);
        OutPort port = sourceNode.getPort(destinationNode);
        port.toLink(new NetMessage(this, sourceNode, destinationNode, size, onCompletedCallback));
    }
}
