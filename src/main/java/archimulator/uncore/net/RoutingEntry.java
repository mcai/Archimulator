/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net;

import archimulator.uncore.net.node.NetNode;
import archimulator.uncore.net.port.OutPort;

/**
 * Routing entry.
 *
 * @author Min Cai
 */
public class RoutingEntry {
    private NetNode sourceNode;
    private NetNode destinationNode;
    private int cost;
    private OutPort outPort;

    /**
     * Create a routing entry from the source node to the destination node.
     *
     * @param sourceNode      the source node
     * @param destinationNode the destination node
     */
    public RoutingEntry(NetNode sourceNode, NetNode destinationNode) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
    }

    /**
     * Get the source node.
     *
     * @return the source node
     */
    public NetNode getSourceNode() {
        return sourceNode;
    }

    /**
     * Get the destination node.
     *
     * @return the destination node
     */
    public NetNode getDestinationNode() {
        return destinationNode;
    }

    /**
     * Get the cost.
     *
     * @return the cost
     */
    public int getCost() {
        return cost;
    }

    /**
     * Set the cost.
     *
     * @param cost the cost
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     * Get the out port.
     *
     * @return the out port
     */
    public OutPort getOutPort() {
        return outPort;
    }

    /**
     * Set the out port.
     *
     * @param outPort the out port
     */
    public void setOutPort(OutPort outPort) {
        this.outPort = outPort;
    }
}
