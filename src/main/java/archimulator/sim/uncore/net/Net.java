/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.model.base.BasicSimulationObject;
import archimulator.sim.uncore.CacheHierarchy;
import archimulator.sim.uncore.MemoryDevice;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Net extends BasicSimulationObject {
    protected Map<MemoryDevice, EndPointNode> endPointNodes;
    protected SwitchNode switchNode;

    public Net(CacheHierarchy cacheHierarchy) {
        super(cacheHierarchy);

        this.endPointNodes = new HashMap<MemoryDevice, EndPointNode>();

        this.setup(cacheHierarchy);
        this.calculateRoutes();
    }

    protected abstract void setup(CacheHierarchy cacheHierarchy);

    private void calculateRoutes() {
        Map<RoutingEntry, NetNode> routes = new HashMap<RoutingEntry, NetNode>();

        List<NetNode> nodes = new ArrayList<NetNode>();
        nodes.add(this.switchNode);
        nodes.addAll(this.endPointNodes.values());

        /* Initialize table with infinite costs */
        for (NetNode srcNode : nodes) {
            for (NetNode destNode : nodes) {
                RoutingEntry routingEntry = new RoutingEntry(srcNode, destNode);
                routingEntry.setCost(srcNode == destNode ? 0 : nodes.size());
                srcNode.getRoutingEntries().put(routingEntry.getDestNode(), routingEntry);
            }
        }

        /* Set 1-jump connections */
        for (NetNode node : nodes) {
            for (OutPort outPort : node.getOutPorts()) {
                if (outPort.getLink() != null) {
                    NetLink link = outPort.getLink();
                    final RoutingEntry routingEntry = node.getRoutingEntries().get(link.getDestPort().getNode());
                    routingEntry.setCost(1);
                    routes.put(routingEntry, link.getDestPort().getNode());
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
                    if (link != null && link.getDestPort().getNode() == nodeNext) {
                        routingEntry.setOutPort(port);
                        break;
                    }
                }
            }
        }
    }

    protected void createBidirectionalLink(NetNode node1, NetNode node2, int bandwidth) {
        new NetLink(node1, node2, bandwidth);
        new NetLink(node2, node1, bandwidth);
    }

    public void transfer(final MemoryDevice srcDevice, final MemoryDevice destDevice, final int size, final Action onCompletedCallback) {
        EndPointNode srcNode = this.endPointNodes.get(srcDevice);
        EndPointNode destNode = this.endPointNodes.get(destDevice);
        OutPort port = srcNode.getPort(destNode);
        port.toLink(new NetMessage(srcNode, destNode, size, onCompletedCallback, this.getCycleAccurateEventQueue().getCurrentCycle()));
    }
}
