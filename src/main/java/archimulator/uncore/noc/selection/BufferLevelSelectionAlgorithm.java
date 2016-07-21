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
package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routers.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer level selection algorithm.
 *
 * @author Min Cai
 */
public class BufferLevelSelectionAlgorithm extends AbstractSelectionAlgorithm {
    /**
     * Create a buffer level selection algorithm for the selected node.
     *
     * @param node the parent node
     */
    public BufferLevelSelectionAlgorithm(Node node) {
        super(node);
    }

    /**
     * Select the best output direction from a list of candidate output directions.
     *
     * @param src the source node ID
     * @param dest the destination node ID
     * @param ivc the input virtual channel ID
     * @param directions the list of candidate output directions
     * @return the best output direction selected from a list of candidate output directions
     */
    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        List<Direction> bestDirections = new ArrayList<>();

        double maxFreeSlots = -1.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();
            int freeSlots = neighborRouter.getFreeSlots(direction.getReflexDirection(), ivc);

            if(freeSlots > maxFreeSlots) {
                maxFreeSlots = freeSlots;
                bestDirections.clear();
                bestDirections.add(direction);
            } else if(freeSlots == maxFreeSlots) {
                bestDirections.add(direction);
            }
        }

        if(!bestDirections.isEmpty()) {
            return bestDirections.get(this.getNode().getNetwork().getEnvironment().getRandom().nextInt(bestDirections.size()));
        }

        return super.select(src, dest, ivc, directions);
    }
}
