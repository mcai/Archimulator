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
package archimulator.util.ai.aco;

import archimulator.util.event.BlockingEvent;

import java.util.List;

/**
 * The event occurred when a new path is generated.
 *
 * @author Min Cai
 */
public class PathGeneratedEvent implements BlockingEvent {
    private Ant ant;
    private List<Node> path;
    private List<Edge> pathEdges;
    private double pathCost;

    /**
     * Create an event when a new path is generated.
     *
     * @param ant       the ant
     * @param path      the path
     * @param pathEdges the path of edges
     * @param pathCost  the path cost
     */
    public PathGeneratedEvent(Ant ant, List<Node> path, List<Edge> pathEdges, double pathCost) {
        this.ant = ant;
        this.path = path;
        this.pathEdges = pathEdges;
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
    public List<Node> getPath() {
        return path;
    }

    /**
     * Get the path of edges.
     *
     * @return the path of edges
     */
    public List<Edge> getPathEdges() {
        return pathEdges;
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
