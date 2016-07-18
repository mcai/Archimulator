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
package archimulator.uncore.noc.traffics;

import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;

/**
 * Transpose traffic generator.
 *
 * @author Min Cai
 */
public class TransposeTrafficGenerator extends SyntheticTrafficGenerator {
    /**
     * Create a transpose traffic generator.
     *
     * @param network the parent network
     * @param packetInjectionRate the packet injection rate
     * @param packetFactory the packet factory
     * @param packetSize the size of a packet
     * @param maxPackets the maximum number of packets to be generated
     */
    public TransposeTrafficGenerator(
            Network network,
            double packetInjectionRate,
            PacketFactory packetFactory,
            int packetSize,
            long maxPackets
    ) {
        super(network, packetInjectionRate, packetFactory, packetSize, maxPackets);
    }

    /**
     * Get the destination node ID for the specified source node ID.
     *
     * @param src the source node ID
     * @return the destination node ID for the specified source node ID
     */
    @Override
    protected int dest(int src) {
        int srcX = Node.getX(this.getNetwork(), src);
        int srcY = Node.getY(this.getNetwork(), src);

        int destX = this.getNetwork().getWidth() - 1 - srcY;
        int destY = this.getNetwork().getWidth() - 1 - srcX;

        return destY * this.getNetwork().getWidth() + destX;
    }
}
