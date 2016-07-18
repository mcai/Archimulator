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
package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Packet;

/**
 * Ant packet.
 *
 * @author Min Cai
 */
public abstract class AntPacket extends Packet {
    private boolean forward;

    /**
     * Create an ant packet.
     *
     * @param network the network
     * @param src the source node ID
     * @param dest the destination node ID
     * @param size the size
     * @param forward a boolean value indicating whether the ant packet is forward ant packet or not
     * @param onCompletedCallback a boolean value indicating whether the ant packet is forward ant packet or not
     */
    public AntPacket(Network network, int src, int dest, int size, boolean forward, Runnable onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);

        this.forward = forward;
    }

    /**
     * Get a boolean value indicating whether the ant packet is forward ant packet or not.
     *
     * @return a boolean value indicating whether the ant packet is forward ant packet or not
     */
    public boolean isForward() {
        return forward;
    }
}
