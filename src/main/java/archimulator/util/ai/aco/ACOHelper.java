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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ACO helper.
 *
 * @param <NodeT> the node type
 * @author Min Cai
 */
public class ACOHelper<NodeT extends EuclideanNode> {
    private double p;
    private double delta;
    private double maxPheromone;
    private double alpha;
    private double beta;
    private List<NodeT> nodes;
    private List<Edge> edges;
    private List<Ant> ants;

    /**
     * Create an ACO helper.
     *
     * @param p            the p value
     * @param delta        the delta value
     * @param maxPheromone the max pheromone value
     * @param alpha        the alpha value
     * @param beta         the beta value
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
     * Create an ACO helper from the specified file.
     *
     * @param fileName     the file name
     * @param p            the p value
     * @param delta        the delta value
     * @param maxPheromone the max pheromone value
     * @param alpha        the alpha value
     * @param beta         the beta value
     * @return the newly created ACO helper
     */
    public static ACOHelper<EuclideanNode> read(String fileName, double p, double delta, double maxPheromone, double alpha, double beta) {
        try {
            ACOHelper<EuclideanNode> acoHelper = new ACOHelper<>(p, delta, maxPheromone, alpha, beta);

            List<String> lines = IOUtils.readLines(new FileReader(fileName));

            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length == 3 && NumberUtils.isNumber(parts[0].trim()) && NumberUtils.isNumber(parts[1].trim()) && NumberUtils.isNumber(parts[2].trim())) {
                    int i = Integer.parseInt(parts[0].trim());
                    double x = Double.parseDouble(parts[1].trim());
                    double y = Double.parseDouble(parts[2].trim());
                    acoHelper.getNodes().add(new EuclideanNode(acoHelper, "" + i, x, y));
                }
            }

            for (EuclideanNode nodeFrom : acoHelper.getNodes()) {
                for (EuclideanNode nodeTo : acoHelper.getNodes()) {
                    if (nodeFrom != nodeTo && acoHelper.getEdge(nodeFrom, nodeTo) == null) {
                        acoHelper.getEdges().add(new Edge(acoHelper, nodeFrom, nodeTo, 1, euclideanDistance.compute(new double[]{nodeFrom.getX(), nodeFrom.getY()}, new double[]{nodeTo.getX(), nodeTo.getY()})));
                    }
                }
            }

            return acoHelper;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

        for (Ant ant : this.getAnts()) {
            double newPathCost = ant.getPathCost();
            if (newPathCost < pathCost) {
                pathCost = newPathCost;
                antOfShortestPath = ant;
            }
        }

        return antOfShortestPath;
    }

    @Override
    public String toString() {
        return String.format("ACOHelper{p=%s, delta=%s, maxPheromone=%s, alpha=%s, beta=%s}", p, delta, maxPheromone, alpha, beta);
    }

    /**
     * Get the p value.
     *
     * @return the p value
     */
    public double getP() {
        return p;
    }

    /**
     * Get the delta value.
     *
     * @return the delta value
     */
    public double getDelta() {
        return delta;
    }

    /**
     * Get the max pheromone value.
     *
     * @return the max pheromone value
     */
    public double getMaxPheromone() {
        return maxPheromone;
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
    public List<NodeT> getNodes() {
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

    private static EuclideanDistance euclideanDistance = new EuclideanDistance();

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ACOHelper<EuclideanNode> acoHelper = read("/home/itecgo/Archimulator/src/main/java/archimulator/util/ai/aco/berlin52.tsp", 0.1, 0.5, 1, 0.5, 0.5);

        EuclideanNode firstNode = acoHelper.getNodes().get(0);

        for(int i = 0; i < acoHelper.getNodes().size(); i++) {
            acoHelper.getAnts().add(new Ant(acoHelper, "" + i, firstNode));
        }

        List<Edge> shortestPath;
        double shortestPathCost = Double.MAX_VALUE;

        for (int i = 0; i < 10; i++) {
            acoHelper.getAnts().forEach(Ant::onePass);
            acoHelper.getEdges().forEach(Edge::evaporate);

            Ant antOfShortestPath = acoHelper.getAntOfShortestPath();

            List<Edge> newShortestPath = antOfShortestPath.getPath();
            double newShortestPathCost = antOfShortestPath.getPathCost();

            if(newShortestPathCost < shortestPathCost) {
                shortestPath = newShortestPath;
                shortestPathCost = newShortestPathCost;

                System.out.println("New shortest path found: " + shortestPath.stream().map(edge -> edge.getNodeFrom().getName()).collect(Collectors.toList()));
                System.out.println("New shortest path cost found: " + shortestPathCost);
            }

            acoHelper.getAnts().forEach(Ant::reset);
        }
    }
}
