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
package archimulator.uncore.net.common;

import archimulator.common.SimulationEvent;
import archimulator.uncore.net.node.NetNode;

/**
 * A net message end link transfer event.
 *
 * @author Min Cai
 */
public class NetMessageEndLinkTransferEvent extends SimulationEvent {
    private Net net;
    private NetNode nodeFrom;
    private NetNode nodeTo;

    /**
     * Create a net message end link transfer event.
     *
     * @param net the parent net
     * @param nodeFrom the source node
     * @param nodeTo the destination node
     */
    public NetMessageEndLinkTransferEvent(Net net, NetNode nodeFrom, NetNode nodeTo) {
        super(net);

        this.net = net;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
    }

    /**
     * Get the parent net.
     *
     * @return the parent net
     */
    public Net getNet() {
        return net;
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
}
