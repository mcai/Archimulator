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

import java.util.ArrayList;
import java.util.List;

/**
 * ACO helper.
 *
 * @author Min Cai
 */
public class ACOHelper {
    /**
     * Node.
     */
    public class Node {
        private String name;
        private List<Edge> edges;

        /**
         * Create a node.
         *
         * @param name the name
         */
        public Node(String name) {
            this.name = name;
            this.edges = new ArrayList<>();
        }

        /**
         * Get the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the list of edges.
         *
         * @return the list of edges
         */
        public List<Edge> getEdges() {
            return edges;
        }
    }

    /**
     * Edge.
     */
    public abstract class Edge {
        private Node nodeFrom;
        private Node nodeTo;

        private double pheromone;
        private double cost;

        /**
         * Create an edge.
         *
         * @param nodeFrom the source node
         * @param nodeTo the destination node
         * @param pheromone the initial pheromone value
         * @param cost the initial cost
         */
        public Edge(Node nodeFrom, Node nodeTo, double pheromone, double cost) {
            this.nodeFrom = nodeFrom;
            this.nodeTo = nodeTo;

            this.pheromone = pheromone;
            this.cost = cost;
        }

        /**
         * Deposit.
         */
        public void deposit() {
            this.pheromone += delta * (maxPheromone - this.pheromone);
        }

        /**
         * Evaporate.
         */
        public void evaporate() {
            this.pheromone *= (1 - p);
        }

        /**
         * Set the cost.
         *
         * @param cost the cost
         */
        public void setCost(double cost) {
            this.cost = cost;
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
    }

    /**
     * Ant.
     */
    public abstract class Ant {
        private String name;
        private Node node;
        private List<Edge> path;

        /**
         * Create an ant.
         *
         * @param name the name
         * @param node the initial node at which the ant begins
         */
        public Ant(String name, Node node) {
            this.name = name;
            this.node = node;
            this.path = new ArrayList<>();
        }

        /**
         * Go forward.
         */
        public void forward() {
            Node nextNodeToVisit = getNextNodeToVisit();
            this.path.add(getEdge(this.node, nextNodeToVisit));
            this.node = nextNodeToVisit;
        }

        /**
         * Get the next node to visit.
         *
         * @return the next node to visit
         */
        protected Node getNextNodeToVisit() {
            //TODO
            return null;
        }

        private double getP(Node nodeFrom, Node nodeTo) {
            Edge edge = getEdge(nodeFrom, nodeTo);
            double yita = 1 - edge.getCost() / nodeFrom.getEdges().stream().mapToDouble(Edge::getCost).sum();
            return Math.pow(edge.getPheromone(), getAlpha()) * Math.pow(yita, getBeta()) / nodeFrom.getEdges().stream().mapToDouble(e -> Math.pow(e.getPheromone(), getAlpha()) * Math.pow(yita, getBeta())).sum();
        }

        /**
         * Get the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Get the node where the ant resides.
         *
         * @return the node where the ant resides
         */
        public Node getNode() {
            return node;
        }

        /**
         * Get the list of edges that the ant has visited so far.
         *
         * @return the list of edges that the ant has visited so far
         */
        public List<Edge> getPath() {
            return path;
        }

        /**
         * Get the cost of the path.
         *
         * @return the cost of the path
         */
        public double getPathCost() {
            return this.path.stream().mapToDouble(Edge::getCost).sum();
        }
    }

    private double p;

    private double delta;
    private double maxPheromone;

    private double alpha;
    private double beta;

    private List<Node> nodes;
    private List<Edge> edges;
    private List<Ant> ants;

    /**
     * Create an ACOHelper.
     *  @param p the p value
     * @param delta the delta value
     * @param maxPheromone the max pheromone value
     * @param alpha the alpha value
     * @param beta the beta value
     */
    public ACOHelper(double p, double delta, double maxPheromone, double alpha, double beta) {
        this.p = p;
        this.delta = delta;
        this.maxPheromone = maxPheromone;
        this.alpha = alpha;
        this.beta = beta;

        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.ants = new ArrayList<>();
    }

    /**
     * Get the edge for the specified source and destination nodes.
     *
     * @param nodeFrom the source node
     * @param nodeTo   the destination node
     * @return the edge for the specified source and destination nodes
     */
    public Edge getEdge(Node nodeFrom, Node nodeTo) {
        return nodeFrom.getEdges().stream().filter(edge -> edge.getNodeTo().equals(nodeTo)).findFirst().orElseGet(() -> null);
    }

    /**
     * Get the ant of the shortest path.
     *
     * @return the ant of the shortest path
     */
    public Ant getAntOfShortestPath() {
        double pathCost = Double.MAX_VALUE;
        Ant antOfShortestPath = null;

        for(Ant ant : this.getAnts()) {
            double newPathCost = ant.getPathCost();
            if(newPathCost < pathCost) {
                pathCost = newPathCost;
                antOfShortestPath = ant;
            }
        }

        return antOfShortestPath;
    }

    /**
     * Get the alpha value.
     *
     * @return the alpha value
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Get the beta value.
     *
     * @return the beta value
     */
    public double getBeta() {
        return beta;
    }

    /**
     * Get the list of nodes.
     *
     * @return the list of nodes
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Get the list of edges.
     *
     * @return the list of edges
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * Get the list of ants.
     *
     * @return the list of ants
     */
    public List<Ant> getAnts() {
        return ants;
    }

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ACOHelper acoHelper = new ACOHelper(0.1, 0.5, 1, 0.5, 0.5);

        for(int i = 0; i < 1000; i++) {
            acoHelper.getAnts().forEach(Ant::forward);
            acoHelper.getEdges().forEach(Edge::evaporate);
        }

        Ant antOfShortestPath = acoHelper.getAntOfShortestPath();
        System.out.println(antOfShortestPath);
    }
}
