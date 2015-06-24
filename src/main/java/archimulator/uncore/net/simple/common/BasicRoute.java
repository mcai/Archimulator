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
package archimulator.uncore.net.simple.common;

import archimulator.uncore.net.simple.node.NetNode;
import archimulator.uncore.net.simple.port.OutPort;

/**
 * Basic route.
 *
 * @author Min Cai
 */
public class BasicRoute implements Route {
    private NetNode nodeFrom;
    private NetNode nodeTo;
    private int cost;
    private OutPort outPort;

    /**
     * Create a basic route from the source node to the destination node.
     *
     * @param nodeFrom      the source node
     * @param nodeTo          the destination node
     */
    public BasicRoute(NetNode nodeFrom, NetNode nodeTo) {
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
    }

    /**
     * Get the source node.
     *
     * @return the source node
     */
    public NetNode getNodeFrom() {
        return nodeFrom;
    }

    /**
     * Get the destination node.
     *
     * @return the destination node
     */
    public NetNode getNodeTo() {
        return nodeTo;
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
    @Override
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
