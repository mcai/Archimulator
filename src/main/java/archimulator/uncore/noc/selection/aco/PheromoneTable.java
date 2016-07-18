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
import archimulator.uncore.noc.Node;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pheromone table.
 *
 * @author Min Cai
 */
public class PheromoneTable {
    private Node node;

    private Map<Integer, Map<Direction, Pheromone>> pheromones;

    /**
     * Create a pheromone table for the specified node.
     *
     * @param node the parent node
     */
    public PheromoneTable(Node node) {
        this.node = node;

        this.pheromones = new TreeMap<>();
    }

    /**
     * Append a pheromone table entry to the table.
     *
     * @param dest the destination node ID
     * @param direction the direction
     * @param pheromoneValue the pheromone value
     */
    public void append(int dest, Direction direction, double pheromoneValue) {
        Pheromone pheromone = new Pheromone(this, dest, direction, pheromoneValue);

        if(!this.pheromones.containsKey(dest)) {
            this.pheromones.put(dest, new EnumMap<>(Direction.class));
        }

        this.pheromones.get(dest).put(direction, pheromone);
    }

    /**
     * Update the specified pheromone table entry.
     *
     * @param dest the destination node ID
     * @param direction the direction
     */
    public void update(int dest, Direction direction) {
        for(Pheromone pheromone : this.pheromones.get(dest).values()) {
            if(pheromone.getDirection() == direction) {
                pheromone.setValue(
                        pheromone.getValue()
                                + this.node.getNetwork().getEnvironment().getConfig().getReinforcementFactor() * (1 - pheromone.getValue())
                );
            } else {
                pheromone.setValue(
                        pheromone.getValue()
                                - this.node.getNetwork().getEnvironment().getConfig().getReinforcementFactor() * pheromone.getValue()
                );
            }
        }
    }

    /**
     * Get the parent node.
     *
     * @return the parent node
     */
    public Node getNode() {
        return node;
    }

    /**
     * Get the map of destination-direction-pheromones.
     *
     * @return the map of pheromones
     */
    public Map<Integer, Map<Direction, Pheromone>> getPheromones() {
        return pheromones;
    }
}
