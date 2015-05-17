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
import java.util.Comparator;
import java.util.List;

/**
 * Ant.
 *
 * @author Min Cai
 */
public class Ant {
    private ACOHelper acoHelper;
    private String name;
    private Node initialNode;
    private Node node;

    private List<Node> path;
    private List<Edge> pathEdges;
    private double pathCost;

    /**
     * Create an ant.
     *
     * @param acoHelper the ACO helper
     * @param name      the name
     * @param node      the initial node at which the ant begins
     */
    public Ant(ACOHelper acoHelper, String name, Node node) {
        this.acoHelper = acoHelper;
        this.name = name;
        this.initialNode = node;
        this.node = node;

        this.path = new ArrayList<>();
        this.pathEdges = new ArrayList<>();
        this.pathCost = 0;

        acoHelper.getCycleAccurateEventQueue().schedule(this, this::forward, 1);
    }

    /**
     * Go forward.
     */
    protected void forward() {
        Node nextNodeToVisit = getNextNodeToVisit();

        if (nextNodeToVisit != null) {
            Edge edge = this.getAcoHelper().getEdge(this.node, nextNodeToVisit);

            this.path.add(nextNodeToVisit);
            this.pathEdges.add(edge);
            this.pathCost += edge.getWeight();

            this.node = nextNodeToVisit;

//            getAcoHelper().getCycleAccurateEventQueue().schedule(this, this::forward, (int)edge.getCost() + 1);
            getAcoHelper().getCycleAccurateEventQueue().schedule(this, this::forward, 1);
        } else {
            this.pathEdges.forEach(Edge::deposit);

            this.getAcoHelper().getBlockingEventDispatcher().dispatch(new PathGeneratedEvent(this, this.path, pathEdges, this.getPathCost()));

            this.reset();

            getAcoHelper().getCycleAccurateEventQueue().schedule(this, this::forward, 1);
        }
    }

    /**
     * Reset.
     */
    public void reset() {
        this.node = this.getInitialNode();
        this.path.clear();
        this.pathEdges.clear();
        this.pathCost = 0;
    }

    /**
     * Get the next node to visit.
     *
     * @return the next node to visit
     */
    protected Node getNextNodeToVisit() {
        return this.getNode().getEdges().stream().map(edge -> edge.getOtherNode(this.getNode())).filter(node -> node != this.getInitialNode() && !this.path.contains(node))
                .max(Comparator.comparing(this::getP)).orElseGet(() -> null);
    }

    /**
     * Get the p value for the specified edge.
     *
     * @param oppositeNode the other node
     * @return the p value for the specified edge
     */
    private double getP(Node oppositeNode) {
        Edge edge = this.getAcoHelper().getEdge(this.getNode(), oppositeNode);

        double eta = getEta(edge);

        double a = Math.pow(edge.getPheromone(), this.getAcoHelper().getAlpha()) * Math.pow(eta, this.getAcoHelper().getBeta());
        double sum = this.getNode().getEdges().stream().mapToDouble(e -> Math.pow(e.getPheromone(), this.getAcoHelper().getAlpha()) * Math.pow(getEta(e), this.getAcoHelper().getBeta())).sum();

        return a / sum;
    }

    /**
     * Get the eta for the specified edge.
     *
     * @param edge the edge
     * @return the eta for the specified edge
     */
    private double getEta(Edge edge) {
        return 1 - edge.getWeight() / getNode().getEdges().stream().mapToDouble(Edge::getWeight).sum();
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
     * Get the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the initial node.
     *
     * @return the initial node
     */
    public Node getInitialNode() {
        return initialNode;
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
    public List<Node> getPath() {
        return path;
    }

    /**
     * Get the cost of the path.
     *
     * @return the cost of the path
     */
    public double getPathCost() {
        return this.pathCost;
    }

    @Override
    public String toString() {
        return String.format("Ant{name='%s', node=%s}", name, node);
    }
}
