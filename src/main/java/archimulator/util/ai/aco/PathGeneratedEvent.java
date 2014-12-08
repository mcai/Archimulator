/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.util.ai.aco;

import net.pickapack.event.BlockingEvent;

import java.util.List;

/**
 * The event occurred when a new path is generated.
 *
 * @author Min Cai
 */
public class PathGeneratedEvent implements BlockingEvent {
    private Ant ant;
    private List<Edge> path;
    private double pathCost;

    /**
     * Create an event when a new path is generated.
     *
     * @param ant the ant
     * @param path the path
     * @param pathCost the path cost
     */
    public PathGeneratedEvent(Ant ant, List<Edge> path, double pathCost) {
        this.ant = ant;
        this.path = path;
        this.pathCost = pathCost;
    }

    /**
     * Get the ant.
     *
     * @return the ant
     */
    public Ant getAnt() {
        return ant;
    }

    /**
     * Get the path.
     *
     * @return the path
     */
    public List<Edge> getPath() {
        return path;
    }

    /**
     * Get the path cost.
     *
     * @return the path cost
     */
    public double getPathCost() {
        return pathCost;
    }
}
