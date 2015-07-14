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
    public static final double REINFORCEMENT_FACTOR = 0.05;

    private AntNetAgent agent;
    private Map<Router, List<Pheromone>> pheromones;

    private Random random;

    /**
     * Create a routing table.
     *
     * @param agent the ACO routing
     */
    public RoutingTable(AntNetAgent agent) {
        this.agent = agent;
        this.pheromones = new HashMap<>();

        this.random = new Random(13);
    }

    /**
     * Add a router entry.
     *
     * @param destination    the destination router
     * @param neighbor       the neighbor router
     * @param pheromoneValue the pheromone value
     */
    public void addEntry(Router destination, Router neighbor, double pheromoneValue) {
        Pheromone pheromone = new Pheromone(neighbor, pheromoneValue);

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
    public Router calculateRandomDestination(Router source) {
        int size = this.agent.getRouter().getNet().getRouters().size();

        int i;

        do {
            i = random.nextInt(size);
        } while (i == source.getId());

        return this.agent.getRouter().getNet().getRouters().get(i);
    }

    /**
     * Calculate the neighbor (next hop) router for the specified destination and parent routers.
     *
     * @param destination the destination router
     * @return the next hop router for the specified destination and parent routers
     */
    public Router calculateNeighbor(Router destination) {
        if(destination == this.agent.getRouter()) {
            throw new IllegalArgumentException();
        }

        List<Pheromone> pheromonesPerDestination = this.pheromones.get(destination);

        double maxPheromoneValue = 0;
        Router maxPheromoneNeighbor = null;

        for(Pheromone pheromone : pheromonesPerDestination) {
            Router neighbor = pheromone.getNeighbor();
            double pheromoneValue = pheromone.getValue();

            if(pheromoneValue > maxPheromoneValue) {
                maxPheromoneValue = pheromoneValue;
                maxPheromoneNeighbor = neighbor;
            }
        }

        if(maxPheromoneNeighbor == null) {
            throw new IllegalArgumentException();
        }

        return maxPheromoneNeighbor;
    }

    /**
     * Update the routing table by incrementing or evaporating pheromone values as per the AntNet algorithm.
     *
     * @param destination the destination router
     * @param neighbor    the neighbor router
     */
    public void update(Router destination, Router neighbor) {
        List<Pheromone> pheromonesPerDestination = this.pheromones.get(destination);

        for (Pheromone pheromone : pheromonesPerDestination) {
            double oldPheromoneValue = pheromone.getValue();
            if (pheromone.getNeighbor() == neighbor) {
                pheromone.setValue(oldPheromoneValue + REINFORCEMENT_FACTOR * (1 - oldPheromoneValue));
            } else {
                pheromone.setValue((1 - REINFORCEMENT_FACTOR) * oldPheromoneValue);
            }
        }
    }

    /**
     * Get the ant net agent.
     *
     * @return the ant net agent
     */
    public AntNetAgent getAgent() {
        return agent;
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
