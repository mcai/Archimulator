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
package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Direction;

import java.util.List;

/**
 * Routing algorithm.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithm {
    /**
     * Get the list of directions for deriving the next hop of the upcoming packet.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param parent the parent node ID
     * @return the next hop
     */
    List<Direction> nextHop(int src, int dest, int parent);
}
