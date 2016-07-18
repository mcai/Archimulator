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

import archimulator.uncore.noc.Direction;

/**
 * Pheromone.
 *
 * @author Min Cai
 */
public class Pheromone {
    private PheromoneTable pheromoneTable;
    private int dest;
    private Direction direction;
    private double value;

    /**
     * Create a pheromone.
     *
     * @param pheromoneTable the parent pheromone table
     * @param dest the destination node ID
     * @param direction the direction
     * @param value the pheromone value
     */
    public Pheromone(PheromoneTable pheromoneTable, int dest, Direction direction, double value) {
        this.pheromoneTable = pheromoneTable;
        this.dest = dest;
        this.direction = direction;
        this.value = value;
    }

    /**
     * Get a string representation of the pheromone.
     *
     * @return a string representation of the pheromone
     */
    @Override
    public String toString() {
        return String.format("Pheromone{node.id=%s, dest=%d, direction=%s, value=%s}",
                pheromoneTable.getNode().getId(), dest, direction, value);
    }

    /**
     * Get the parent pheromone table.
     *
     * @return the parent pheromone table
     */
    public PheromoneTable getPheromoneTable() {
        return pheromoneTable;
    }

    /**
     * Get the destination node ID.
     *
     * @return the destination node ID
     */
    public int getDest() {
        return dest;
    }

    /**
     * Get the direction.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the pheromone value.
     *
     * @return the pheromone value
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the pheromone value.
     *
     * @param value the pheromone value
     */
    public void setValue(double value) {
        this.value = value;
    }
}
