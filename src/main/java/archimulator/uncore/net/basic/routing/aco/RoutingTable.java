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

import java.util.*;

/**
 * Routing table.
 *
 * @author Min Cai
 */
public class RoutingTable {
    private ACORouting routing;
    private Map<Router, List<Pheromone>> pheromones;

    private Random random;

    /**
     * Create a routing table.
     *
     * @param routing the ACO routing
     */
    public RoutingTable(ACORouting routing) {
        this.routing = routing;
        this.pheromones = new HashMap<>();

        this.random = new Random(13);
    }

    /**
     * Add a router entry.
     *
     * @param destination    the destination router
     * @param next           the next router
     * @param pheromoneValue the pheromone value
     */
    public void addEntry(Router destination, Router next, double pheromoneValue) {
        Pheromone pheromone = new Pheromone(next, pheromoneValue);

        if (!this.pheromones.containsKey(destination)) {
            this.pheromones.put(destination, new ArrayList<>());
        }

        this.pheromones.get(destination).add(pheromone);
    }

    /**
     * Calculate a randomly chosen destination router for the specified source router.
     *
     * @param source the source router
     * @return a randomly chosen destination router for the specified source router
     */
    public Router calculateDestination(Router source) {
        int i;

        do {
            i = random.nextInt(this.routing.getNet().getRouters().size());
        } while (i == source.getId());

        return this.routing.getNet().getRouters().get(i);
    }

    /**
     * Calculate the next hop router for the specified destination and parent routers.
     *
     * @param destination the destination router
     * @param parent      the parent router
     * @return the next hop router for the specified destination and parent routers
     */
    public Router calculateNext(Router destination, Router parent) {
        List<Pheromone> pheromones = this.pheromones.get(destination);

        //calculate probability range for parent link
        double lRange = 0.0d;
        double uRange = 0.0d;
        for (Pheromone pheromone : pheromones) {
            Router next = pheromone.getNeighbor();
            double pheromoneValue = pheromone.getValue();

            if (next == parent) {
                uRange = lRange + pheromoneValue;
                break;
            }

            lRange += pheromoneValue;
        }

        if (uRange == 0.0d) {
            uRange = 1.0d;
        }

        //dead end, loopback
        if (lRange == 0.0d && uRange == 1.0d) {
            return parent;
        }

        //generate random probability value, out of range of parent link
        double tempDouble;
        do {
            tempDouble = random.nextDouble();
        } while (tempDouble >= lRange && tempDouble < uRange);

        //find next hop router corresponding to this range of probability
        lRange = 0.0d;
        uRange = 0.0d;
        for (Pheromone pheromone : pheromones) {
            Router next = pheromone.getNeighbor();
            double pheromoneValue = pheromone.getValue();

            uRange += pheromoneValue;
            if (tempDouble >= lRange && tempDouble < uRange) {
                return next;
            }
            lRange = uRange;
        }

        throw new IllegalArgumentException();
    }

    /**
     * Update the routing table by incrementing or evaporating pheromone values as per the AntNet algorithm.
     *
     * @param destination the destination router
     * @param next        the next router
     */
    public void update(Router destination, Router next) {
        List<Pheromone> pheromones = this.pheromones.get(destination);

        for (Pheromone pheromone : pheromones) {
            double oldPheromoneValue = pheromone.getValue();
            if (pheromone.getNeighbor() == next) {
                pheromone.setValue(oldPheromoneValue + ACORouting.REINFORCEMENT_FACTOR * (1 - oldPheromoneValue));
            } else {
                pheromone.setValue((1 - ACORouting.REINFORCEMENT_FACTOR) * oldPheromoneValue);
            }
        }
    }

    /**
     * Get the ACO routing.
     *
     * @return the ACO routing
     */
    public ACORouting getRouting() {
        return routing;
    }

    /**
     * Get the map of pheromones.
     *
     * @return the map of pheromones
     */
    public Map<Router, List<Pheromone>> getPheromones() {
        return pheromones;
    }
}
