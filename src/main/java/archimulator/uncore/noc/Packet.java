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
package archimulator.uncore.noc;

import archimulator.uncore.noc.routers.Flit;
import archimulator.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet.
 *
 * @author Min Cai
 */
public abstract class Packet {
    private Network network;

    private long id;

    private long beginCycle;
    private long endCycle;

    private int src;
    private int dest;

    private int size;

    private Runnable onCompletedCallback;

    private List<Pair<Integer, Long>> memory;

    private List<Flit> flits;

    /**
     * Create a packet.
     *
     * @param network the parent network
     * @param src the source node ID
     * @param dest the destination node ID
     * @param size the size of the packet
     * @param onCompletedCallback the callback function which is to be called on completion
     */
    public Packet(Network network, int src, int dest, int size, Runnable onCompletedCallback) {
        this.network = network;

        this.id = this.network.currentPacketId++;

        this.beginCycle = this.network.getCycleAccurateEventQueue().getCurrentCycle();
        this.endCycle = -1;

        this.src = src;
        this.dest = dest;

        this.size = size;

        int numFlits = (int) Math.ceil((double)(this.size) / this.network.getEnvironment().getConfig().getLinkWidth());
        if(numFlits > this.network.getEnvironment().getConfig().getMaxInputBufferSize()) {
            throw new IllegalArgumentException(
                    String.format("Number of flits (%d) in a packet cannot be greater than max input buffer size (%d)",
                            numFlits, this.network.getEnvironment().getConfig().getMaxInputBufferSize()
                    )
            );
        }

        this.onCompletedCallback = onCompletedCallback;

        this.memory = new ArrayList<>();

        this.flits = new ArrayList<>();
    }

    /**
     * Get a string representation of the packet.
     *
     * @return a string representation of the packet
     */
    @Override
    public String toString() {
        return String.format("Packet{id=%d, src=%d, dest=%d, beginCycle=%d, delay=%d, size=%d, memory=%s}",
                id, src, dest, beginCycle, delay(), size, memory);
    }

    /**
     * Get the delay of the packet.
     *
     * @return the delay of the packet
     */
    public int delay() {
        return this.endCycle != -1 ? (int)(this.endCycle - this.beginCycle) : -1;
    }

    /**
     * Memorize.
     *
     * @param currentNodeId the current node ID
     */
    public void memorize(int currentNodeId) {
        for(Pair<Integer, Long> entry : this.memory) {
            if(entry.getFirst() == currentNodeId) {
                throw new IllegalArgumentException(String.format("%d", currentNodeId));
            }
        }

        this.memory.add(new Pair<>(currentNodeId, this.network.getCycleAccurateEventQueue().getCurrentCycle()));
    }

    /**
     * Get the list of hops.
     *
     * @return the list of hops
     */
    public int hops() {
        return this.memory.size();
    }

    /**
     * Get a boolean value indicating whether the packet has payload or not.
     *
     * @return a boolean value indicating whether the packet has payload or not
     */
    public abstract boolean hasPayload();

    /**
     * Get the network.
     *
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Get the ID of the packet.
     *
     * @return the ID of the packet
     */
    public long getId() {
        return id;
    }

    /**
     * Get the cycle when the packet is generated at the source node.
     *
     * @return the cycle when the packet is generated at the source node
     */
    public long getBeginCycle() {
        return beginCycle;
    }

    /**
     * Get the cycle when the packet arrives at the destination node.
     *
     * @return the cycle when the packet arrives at the destination node
     */
    public long getEndCycle() {
        return endCycle;
    }

    /**
     * Set the cycle when the packet arrives at the destination node.
     *
     * @param endCycle the cycle when the packet arrives at the destination node
     */
    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    /**
     * Get the source node ID.
     *
     * @return the source node ID
     */
    public int getSrc() {
        return src;
    }

    /**
     * Get the destination node ID.
     *
     * @return the destination node ID
     */
    public int getDest() {
        return dest;
    }

    /**
     * Get the source node ID.
     *
     * @return the source node ID
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the callback function which is to be called upon completion.
     *
     * @return the callback function which is to be called upon completion
     */
    public Runnable getOnCompletedCallback() {
        return onCompletedCallback;
    }

    /**
     * Get the memory.
     *
     * @return the memory
     */
    public List<Pair<Integer, Long>> getMemory() {
        return memory;
    }

    /**
     * Get the list of flits.
     *
     * @return the list of flits
     */
    public List<Flit> getFlits() {
        return flits;
    }
}
