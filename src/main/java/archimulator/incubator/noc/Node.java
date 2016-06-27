package archimulator.incubator.noc;

import archimulator.incubator.noc.routers.InputVirtualChannel;
import archimulator.incubator.noc.routers.Router;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Node.
 *
 * @author Min Cai
 */
public abstract class Node {
    private Network<? extends Node, ? extends RoutingAlgorithm> network;

    private int id;

    private int x;
    private int y;

    private Map<Direction, Integer> neighbors;

    private Router router;

    public Node(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        this.network = network;

        this.id = id;

        this.x = this.id % this.network.getWidth();
        this.y = (this.id - this.id % this.network.getWidth()) / this.network.getWidth();

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

    @Override
    public String toString() {
        return String.format("Node{id=%d, x=%d, y=%d, neighbors=%s}", id, x, y, neighbors);
    }

    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        //TODO
    }

    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        return null; //TODO
    }

    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        return directions.get(0);
    }

    public Network<? extends Node, ? extends RoutingAlgorithm> getNetwork() {
        return network;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Map<Direction, Integer> getNeighbors() {
        return neighbors;
    }

    public Router getRouter() {
        return router;
    }
}
