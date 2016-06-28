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
package archimulator.uncore.net.basic;

/**
 * Flit.
 *
 * @author Min Cai
 */
public class Flit {
    private BasicNet net;

    private Packet packet;

    private int num;

    private Router source;
    private Router destination;

    private boolean head;
    private boolean tail;

    private FlitState state = FlitState.INIT;

    private long timestamp;

    /**
     * Create a flit.
     *
     * @param net the net
     * @param packet the packet
     * @param source the source router
     * @param destination the destination router
     */
    public Flit(BasicNet net, Packet packet, Router source, Router destination) {
        this.net = net;
        this.packet = packet;

        this.source = source;
        this.destination = destination;

        this.timestamp = this.net.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Get the net.
     *
     * @return the net
     */
    public BasicNet getNet() {
        return net;
    }

    /**
     * Get the packet.
     *
     * @return the packet
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Get the number.
     *
     * @return the number
     */
    public int getNum() {
        return num;
    }

    /**
     * Set the number.
     *
     * @param num the number
     */
    public void setNum(int num) {
        this.num = num;
    }

    /**
     * Get a boolean value indicating whether it is head or not.
     *
     * @return a boolean value indicating whether it is head or not
     */
    public boolean isHead() {
        return head;
    }

    /**
     * Set a boolean value indicating whether it is head or not.
     *
     * @param head a boolean value indicating whether it is head or not
     */
    public void setHead(boolean head) {
        this.head = head;
    }

    /**
     * Get a boolean value indicating whether it is tail or not.
     *
     * @return a boolean value indicating whether it is tail or not
     */
    public boolean isTail() {
        return tail;
    }

    /**
     * Set a boolean value indicating whether it is tail or not.
     *
     * @param tail a boolean value indicating whether it is tail or not
     */
    public void setTail(boolean tail) {
        this.tail = tail;
    }

    /**
     * Get the state.
     *
     * @return the state
     */
    public FlitState getState() {
        return state;
    }

    /**
     * Set the state.
     *
     * @param state the state
     */
    public void setState(FlitState state) {
        this.state = state;
    }

    /**
     * Get the timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the source.
     *
     * @return the source
     */
    public Router getSource() {
        return source;
    }

    /**
     * Get the destination.
     *
     * @return the destination
     */
    public Router getDestination() {
        return destination;
    }
}
