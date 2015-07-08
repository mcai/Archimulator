/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net.basic.routing.aco;

import archimulator.uncore.net.basic.Router;

/**
 * Pheromone.
 *
 * @author Min Cai
 */
public class Pheromone {
    private Router neighbor;
    private double value;

    /**
     * Create a pheromone.
     *
     * @param neighbor the neighbor router
     * @param value    the pheromone value
     */
    public Pheromone(Router neighbor, double value) {
        this.neighbor = neighbor;
        this.value = value;
    }

    /**
     * Get the neighbor router.
     *
     * @return the neighbor router
     */
    public Router getNeighbor() {
        return neighbor;
    }

    /**
     * Set the pheromone value.
     *
     * @param value the pheromone value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Get the pheromone value.
     *
     * @return the pheromone value
     */
    public double getValue() {
        return value;
    }
}
