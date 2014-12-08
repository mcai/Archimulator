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

/**
 * Edge.
 *
 * @author Min Cai
 */
public class Edge {
    private ACOHelper<?> acoHelper;
    private Node nodeFrom;
    private Node nodeTo;

    private double pheromone;
    private double cost;

    /**
     * Create an edge.
     *
     * @param acoHelper the ACO helper
     * @param nodeFrom  the source node
     * @param nodeTo    the destination node
     * @param pheromone the initial pheromone value
     * @param cost      the initial cost
     */
    public Edge(ACOHelper<?> acoHelper, Node nodeFrom, Node nodeTo, double pheromone, double cost) {
        this.acoHelper = acoHelper;
        this.nodeFrom = nodeFrom;
        this.nodeTo = nodeTo;

        this.pheromone = pheromone;
        this.cost = cost;
    }

    /**
     * Deposit.
     */
    public void deposit() {
        this.pheromone += this.acoHelper.getDelta() * (this.acoHelper.getMaxPheromone() - this.pheromone);
    }

    /**
     * Evaporate.
     */
    public void evaporate() {
        this.pheromone *= 1 - this.acoHelper.getP();
    }

    /**
     * Get the ACO helper.
     *
     * @return the ACO helper
     */
    public ACOHelper<?> getAcoHelper() {
        return acoHelper;
    }

    /**
     * Get the source node.
     *
     * @return the source node
     */
    public Node getNodeFrom() {
        return nodeFrom;
    }

    /**
     * Get the destination node.
     *
     * @return the destination node
     */
    public Node getNodeTo() {
        return nodeTo;
    }

    /**
     * Get the pheromone.
     *
     * @return the pheromone
     */
    public double getPheromone() {
        return pheromone;
    }

    /**
     * Get the cost.
     *
     * @return the cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Set the cost.
     *
     * @param cost the cost
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return String.format("Edge{nodeFrom=%s, nodeTo=%s, pheromone=%.4f, cost=%.4f}", nodeFrom, nodeTo, pheromone, cost);
    }
}
