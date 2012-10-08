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

import net.pickapack.action.Action;

/**
 *
 * @author Min Cai
 */
public class NetMessage {
    private long id;
    private NetNode sourceNode;
    private NetNode destinationNode;
    private int size;
    private Action onCompletedCallback;

    private long beginCycle;

    /**
     *
     * @param net
     * @param sourceNode
     * @param destinationNode
     * @param size
     * @param onCompletedCallback
     * @param beginCycle
     */
    public NetMessage(Net net, NetNode sourceNode, NetNode destinationNode, int size, Action onCompletedCallback, long beginCycle) {
        this.id = net.getSimulation().currentNetMessageId++;

        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.size = size;
        this.onCompletedCallback = onCompletedCallback;

        this.beginCycle = beginCycle;
    }

    /**
     *
     * @param endCycle
     */
    public void complete(long endCycle) {
        this.onCompletedCallback.apply();

//        System.out.printf("%s -> %s: size: %d, latency: %d%n", sourceNode.getName(), destinationNode.getName(), size, (endCycle - beginCycle));
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
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
    public int getSize() {
        return size;
    }

    /**
     *
     * @return
     */
    public long getBeginCycle() {
        return beginCycle;
    }
}
