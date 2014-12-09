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

import net.pickapack.event.BlockingEvent;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ant Colony Optimization (ACO) helper.
 *
 * @author Min Cai
 */
public class ACOHelper {
    private BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;

    private ListenableUndirectedWeightedGraph<Vertex, Edge> graph;

    private double p;
    private double delta;
    private double maxPheromone;
    private double alpha;
    private double beta;

    private List<Vertex> vertexes;
    private List<Edge> edges;
    private List<Ant> ants;

    private List<Vertex> shortestPath;
    private double shortestPathCost;

    /**
     * Create an Ant Colony Optimization (ACO) helper.
     *
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param p                       the p value
     * @param delta                   the delta value
     * @param maxPheromone            the max pheromone value
     * @param alpha                   the alpha value
     * @param beta                    the beta value
     */
    public ACOHelper(BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, double p, double delta, double maxPheromone, double alpha, double beta) {
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;

        this.graph = new ListenableUndirectedWeightedGraph<>(Edge.class);

        this.p = p;
        this.delta = delta;
        this.maxPheromone = maxPheromone;
        this.alpha = alpha;
        this.beta = beta;

        this.vertexes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.ants = new ArrayList<>();

        this.shortestPath = null;
        this.shortestPathCost = Double.MAX_VALUE;

        this.blockingEventDispatcher.addListener(PathGeneratedEvent.class, event -> {
            if (event.getPathCost() < shortestPathCost) {
                shortestPath = event.getPath();
                shortestPathCost = event.getPathCost();

                System.out.println("New shortest path found: " + shortestPath.stream().map(Vertex::getName).collect(Collectors.toList()));
                System.out.println("New shortest path cost found: " + shortestPathCost);
            }
        });
    }

    /**
     * Create an ACO helper from the specified file.
     *
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param fileName                the file name
     * @param p                       the p value
     * @param delta                   the delta value
     * @param maxPheromone            the max pheromone value
     * @param alpha                   the alpha value
     * @param beta                    the beta value
     * @return the newly created ACO helper
     */
    public static ACOHelper read(BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, String fileName, double p, double delta, double maxPheromone, double alpha, double beta) {
        try {
            ACOHelper acoHelper = new ACOHelper(blockingEventDispatcher, cycleAccurateEventQueue, p, delta, maxPheromone, alpha, beta);

            List<String> lines = IOUtils.readLines(new FileReader(fileName));

            for (String line : lines) {
                String[] parts = line.split(" ");
                if (parts.length == 3 && NumberUtils.isNumber(parts[0].trim()) && NumberUtils.isNumber(parts[1].trim()) && NumberUtils.isNumber(parts[2].trim())) {
                    int i = Integer.parseInt(parts[0].trim());
                    double x = Double.parseDouble(parts[1].trim());
                    double y = Double.parseDouble(parts[2].trim());
                    Vertex vertex = new Vertex(acoHelper, "" + i, x, y);
                    acoHelper.getGraph().addVertex(vertex);
                    acoHelper.getVertexes().add(vertex);
                }
            }

            for (Vertex vertexFrom : acoHelper.getVertexes()) {
                for (Vertex vertexTo : acoHelper.getVertexes()) {
                    if (vertexFrom != vertexTo && acoHelper.getEdge(vertexFrom, vertexTo) == null) {
                        Edge edge = new Edge(acoHelper, vertexFrom, vertexTo, 1, euclideanDistance.compute(new double[]{vertexFrom.getX(), vertexFrom.getY()}, new double[]{vertexTo.getX(), vertexTo.getY()}));
                        acoHelper.getGraph().addEdge(vertexFrom, vertexTo, edge);
                        acoHelper.getEdges().add(edge);
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
     * @param vertexFrom the source node
     * @param vertexTo   the destination node
     * @return the edge for the specified source and destination nodes
     */
    public Edge getEdge(Vertex vertexFrom, Vertex vertexTo) {
        return this.getGraph().getEdge(vertexFrom, vertexTo);
    }

    /**
     * Get the blocking event dispatcher.
     *
     * @return the blocking event dispatcher
     */
    public BlockingEventDispatcher<BlockingEvent> getBlockingEventDispatcher() {
        return blockingEventDispatcher;
    }

    /**
     * Get the cycle accurate event queue.
     *
     * @return the cycle accurate event queue
     */
    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return cycleAccurateEventQueue;
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
     * Get the graph.
     *
     * @return the graph
     */
    public ListenableUndirectedWeightedGraph<Vertex, Edge> getGraph() {
        return graph;
    }

    /**
     * Get the list of nodes.
     *
     * @return the list of nodes
     */
    public List<Vertex> getVertexes() {
        return vertexes;
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
     * Get the list of edges composing the shortest path.
     *
     * @return the list of edges composing the shortest path
     */
    public List<Vertex> getShortestPath() {
        return shortestPath;
    }

    /**
     * Get the cost of the shortest path.
     *
     * @return the cost of the shortest path
     */
    public double getShortestPathCost() {
        return shortestPathCost;
    }

    @Override
    public String toString() {
        return String.format("ACOHelper{p=%s, delta=%s, maxPheromone=%s, alpha=%s, beta=%s}", p, delta, maxPheromone, alpha, beta);
    }

    private static EuclideanDistance euclideanDistance = new EuclideanDistance();

    /**
     * Entry point.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        BlockingEventDispatcher<BlockingEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
        CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

        ACOHelper acoHelper = read(blockingEventDispatcher, cycleAccurateEventQueue, "/home/itecgo/Archimulator/src/main/java/archimulator/util/ai/aco/berlin52.tsp", 0.1, 0.5, 1, 0.5, 0.5);

        cycleAccurateEventQueue.getPerCycleEvents().add(() -> acoHelper.getEdges().forEach(Edge::evaporate));

        for (int i = 0; i < acoHelper.getVertexes().size(); i++) {
            acoHelper.getAnts().add(new Ant(acoHelper, "" + i, acoHelper.getVertexes().get(i)));
        }

        JGraphAdapterFrame jGraphAdapterFrame = new JGraphAdapterFrame(acoHelper);
        jGraphAdapterFrame.setVisible(true);

        cycleAccurateEventQueue.getPerCycleEvents().add(jGraphAdapterFrame::refresh);

        for (; ; ) {
            cycleAccurateEventQueue.advanceOneCycle();
        }
    }
}
