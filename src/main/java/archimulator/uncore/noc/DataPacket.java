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

/**
 * Data packet.
 *
 * @author Min Cai
 */
public class DataPacket extends Packet {
    /**
     * Create a data packet.
     *
     * @param network the parent network
     * @param src the source node ID
     * @param dest the destination node ID
     * @param size the size of the packet
     * @param onCompletedCallback the callback function which is to be called upon completion
     */
    public DataPacket(Network network, int src, int dest, int size, Runnable onCompletedCallback) {
        super(network, src, dest, size, onCompletedCallback);
    }

    /**
     * Get a boolean value indicating whether the packet has payload or not.
     *
     * @return a boolean value indicating whether the packet has payload or not
     */
    @Override
    public boolean hasPayload() {
        return true;
    }
}
