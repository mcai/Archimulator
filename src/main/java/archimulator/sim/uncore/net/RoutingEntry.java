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

/**
 *
 * @author Min Cai
 */
public class RoutingEntry {
    private NetNode sourceNode;
    private NetNode destinationNode;
    private int cost;
    private OutPort outPort;

    /**
     *
     * @param sourceNode
     * @param destinationNode
     */
    public RoutingEntry(NetNode sourceNode, NetNode destinationNode) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
    }

    /**
     *
     * @return
     */
    public NetNode getSourceNode() {
        return sourceNode;
    }

    /**
     *
     * @return
     */
    public NetNode getDestinationNode() {
        return destinationNode;
    }

    /**
     *
     * @return
     */
    public int getCost() {
        return cost;
    }

    /**
     *
     * @param cost
     */
    public void setCost(int cost) {
        this.cost = cost;
    }

    /**
     *
     * @return
     */
    public OutPort getOutPort() {
        return outPort;
    }

    /**
     *
     * @param outPort
     */
    public void setOutPort(OutPort outPort) {
        this.outPort = outPort;
    }
}
