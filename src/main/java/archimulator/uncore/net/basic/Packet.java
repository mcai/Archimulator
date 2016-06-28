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

import archimulator.util.action.Action;

/**
 * Packet.
 *
 * @author Min Cai
 */
public class Packet {
    private BasicNet net;

    private long id;

    private Router from;
    private Router to;

    private int size;
    private Action onCompletedCallback;

    /**
     * Create a packet.
     *
     * @param net the net
     * @param from the source router
     * @param to the destination router
     * @param size the size of the packet
     * @param onCompletedCallback the callback performed on completion
     */
    public Packet(BasicNet net, Router from, Router to, int size, Action onCompletedCallback) {
        this.net = net;
        this.id = net.currentRequestId++;
        this.from = from;
        this.to = to;
        this.size = size;
        this.onCompletedCallback = onCompletedCallback;
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
     * Get the ID of the packet.
     *
     * @return the ID of the packet
     */
    public long getId() {
        return id;
    }

    /**
     * Get the source router.
     *
     * @return the source router
     */
    public Router getFrom() {
        return from;
    }

    /**
     * Get the destination router.
     *
     * @return the destination router
     */
    public Router getTo() {
        return to;
    }

    /**
     * Get the size of the packet.
     *
     * @return the size of the packet
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the callback performed on completion.
     *
     * @return the callback performed on completion
     */
    public Action getOnCompletedCallback() {
        return onCompletedCallback;
    }
}
