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
package archimulator.uncore.noc;

import archimulator.uncore.noc.routers.Router;
import archimulator.uncore.noc.routing.RoutingAlgorithm;
import archimulator.uncore.noc.selection.SelectionAlgorithm;

import java.util.EnumMap;
import java.util.Map;

/**
 * Node.
 *
 * @author Min Cai
 */
public class Node {
    private Network network;

    private int id;

    private int x;
    private int y;

    private Map<Direction, Integer> neighbors;

    private Router router;

    private RoutingAlgorithm routingAlgorithm;

    private SelectionAlgorithm selectionAlgorithm;

    /**
     * Create a node.
     *
     * @param network the parent network
     * @param id the node ID
     */
    public Node(Network network, int id) {
        this.network = network;

        this.id = id;

        this.x = getX(network, id);
        this.y = getY(network, id);

        this.neighbors = new EnumMap<>(Direction.class);

        if (this.id / this.network.getWidth() > 0) {
            this.neighbors.put(Direction.NORTH, this.id - this.network.getWidth());
        }

        if( (this.id % this.network.getWidth()) != this.network.getWidth() - 1) {
            this.neighbors.put(Direction.EAST, this.id + 1);
        }

        if(this.id / this.network.getWidth() < this.network.getWidth() - 1) {
            this.neighbors.put(Direction.SOUTH, this.id + this.network.getWidth());
        }

        if(this.id % this.network.getWidth() != 0) {
            this.neighbors.put(Direction.WEST, this.id - 1);
        }

        this.router = new Router(this);
    }

    /**
     * Get the x coordinate.
     *
     * @param network the parent network
     * @param id the ID of the node
     * @return the x coordinate of the specified node
     */
    public static int getX(Network network, int id) {
        return id % network.getWidth();
    }

    /**
     * Get the y coordinate.
     *
     * @param network the parent network
     * @param id the ID of the node
     * @return the y coordinate of the specified node
     */
    public static int getY(Network network, int id) {
        return (id - id % network.getWidth()) / network.getWidth();
    }

    /**
     * Get the string representation of the node.
     *
     * @return the string representation of the node
     */
    @Override
    public String toString() {
        return String.format("Node{id=%d, x=%d, y=%d, neighbors=%s}", id, x, y, neighbors);
    }

    /**
     * Get the parent network.
     *
     * @return the parent network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Get the ID of the node.
     *
     * @return the ID of the node
     */
    public int getId() {
        return id;
    }

    /**
     * Get the x coordinate of the node.
     *
     * @return the y coordinate of the node
     */
    public int getX() {
        return x;
    }

    /**
     * Get the y coordinate of the node.
     *
     * @return the y coordinate of the node
     */
    public int getY() {
        return y;
    }

    /**
     * Get the map of neighbors.
     *
     * @return the map of neighbors
     */
    public Map<Direction, Integer> getNeighbors() {
        return neighbors;
    }

    /**
     * Get the router.
     *
     * @return the router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Get the routing algorithm.
     *
     * @return the routing algorithm
     */
    public RoutingAlgorithm getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    /**
     * Set the routing algorithm.
     *
     * @param routingAlgorithm the routing algorithm
     */
    public void setRoutingAlgorithm(RoutingAlgorithm routingAlgorithm) {
        this.routingAlgorithm = routingAlgorithm;
    }

    /**
     * Get the selection algorithm.
     *
     * @return the selection algorithm
     */
    public SelectionAlgorithm getSelectionAlgorithm() {
        return selectionAlgorithm;
    }

    /**
     * Set the selection algorithm.
     *
     * @param selectionAlgorithm the selection algorithm
     */
    public void setSelectionAlgorithm(SelectionAlgorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
    }
}
