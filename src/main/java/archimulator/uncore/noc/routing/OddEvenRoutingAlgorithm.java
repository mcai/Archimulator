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
import archimulator.uncore.noc.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Odd-even routing algorithm.
 *
 * @author Min Cai
 */
public class OddEvenRoutingAlgorithm extends AbstractRoutingAlgorithm {
    /**
     * Create an odd-even routing algorithm.
     *
     * @param node the parent node
     */
    public OddEvenRoutingAlgorithm(Node node) {
        super(node);
    }

    /**
     * Get the list of directions for deriving the next hop of the upcoming packet.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param parent the parent node ID
     * @return the next hop
     */
    @Override
    public List<Direction> nextHop(int src, int dest, int parent) {
        List<Direction> directions = new ArrayList<>();

        int c0 = getNode().getX();
        int c1 = getNode().getY();

        int s0 = Node.getX(getNode().getNetwork(), src);
        int s1 = Node.getY(getNode().getNetwork(), src);

        int d0 = Node.getX(getNode().getNetwork(), dest);
        int d1 = Node.getY(getNode().getNetwork(), dest);

        int e0 = d0 - c0;
        int e1 = -(d1 - c1);

        if(e0 == 0) {
            if(e1 > 0) {
                directions.add(Direction.NORTH);
            } else {
                directions.add(Direction.SOUTH);
            }
        } else {
            if(e0 > 0) {
                if(e1 == 0) {
                    directions.add(Direction.EAST);
                } else {
                    if(c0 % 2 == 1 || c0 == s0) {
                        if(e1 > 0) {
                            directions.add(Direction.NORTH);
                        } else {
                            directions.add(Direction.SOUTH);
                        }
                    }

                    if(d0 % 2 == 1 || e0 != 1) {
                        directions.add(Direction.EAST);
                    }
                }
            } else {
                directions.add(Direction.WEST);
                if(c0 % 2 == 0) {
                    if(e1 > 0) {
                        directions.add(Direction.NORTH);
                    }
                    if(e1 < 0) {
                        directions.add(Direction.SOUTH);
                    }
                }
            }
        }

        return directions;
    }
}
