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
package archimulator.uncore.net.simple;

import archimulator.uncore.net.Net;
import archimulator.uncore.net.simple.node.NetNode;
import archimulator.util.action.Action;

/**
 * Net message.
 *
 * @author Min Cai
 */
public class NetMessage {
    private long id;
    private NetNode nodeFrom;
    private NetNode nodeTo;
    private int size;
    private Action onCompletedCallback;

    private long beginCycle;

    /**
     * Create a net message.
     *
     * @param net                 the parent net
     * @param nodeFrom          the source node
     * @param nodeTo     the destination node
     * @param size                the size of the message
     * @param onCompletedCallback the callback action performed when the transfer of the message is completed
     */
    public NetMessage(Net net, NetNode nodeFrom, NetNode nodeTo, int size, Action onCompletedCallback) {
        this.id = net.getSimulation().currentNetMessageId++;

        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;
        this.size = size;
        this.onCompletedCallback = onCompletedCallback;

        this.beginCycle = net.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Complete the transferring of the message.
     */
    public void complete() {
        this.onCompletedCallback.apply();
    }

    /**
     * Get the ID of the message.
     *
     * @return the ID of the message
     */
    public long getId() {
        return id;
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
     * Get the size of the message.
     *
     * @return the size of the message
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the time in cycles when the message begins transferring.
     *
     * @return the time in cycles when the message begins transferring
     */
    public long getBeginCycle() {
        return beginCycle;
    }
}
