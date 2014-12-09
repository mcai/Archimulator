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
    private ACOHelper acoHelper;
    private Vertex vertexFrom;
    private Vertex vertexTo;

    private double pheromone;
    private double weight;

    /**
     * Create an edge.
     *
     * @param acoHelper the ACO helper
     * @param vertexFrom  the source node
     * @param vertexTo    the destination node
     * @param pheromone the initial pheromone value
     * @param weight    the initial weight
     */
    public Edge(ACOHelper acoHelper, Vertex vertexFrom, Vertex vertexTo, double pheromone, double weight) {
        this.acoHelper = acoHelper;
        this.vertexFrom = vertexFrom;
        this.vertexTo = vertexTo;

        this.pheromone = pheromone;
        this.weight = weight;
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
    public ACOHelper getAcoHelper() {
        return acoHelper;
    }

    /**
     * Get the source node.
     *
     * @return the source node
     */
    private Vertex getVertexFrom() {
        return vertexFrom;
    }

    /**
     * Get the destination node.
     *
     * @return the destination node
     */
    private Vertex getVertexTo() {
        return vertexTo;
    }

    /**
     * Get the other node.
     *
     * @param vertex the node
     * @return the other node
     */
    public Vertex getOtherNode(Vertex vertex) {
        return vertex == this.getVertexFrom() ? this.getVertexTo() : this.getVertexFrom();
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
     * Get the weight.
     *
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Set the cost.
     *
     * @param weight the cost
     */
    public void setWeight(double weight) {
        this.weight = weight;
        this.getAcoHelper().getGraph().setEdgeWeight(this, weight);
    }

    @Override
    public String toString() {
//        return String.format("pheromone=%.4f, weight=%.4f", nodeFrom, nodeTo, pheromone, weight);
        return String.format("%.4f", pheromone);
    }
}
