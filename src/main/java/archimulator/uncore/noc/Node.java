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

    public static int getX(Network network, int id) {
        return id % network.getWidth();
    }

    public static int getY(Network network, int id) {
        return (id - id % network.getWidth()) / network.getWidth();
    }

    @Override
    public String toString() {
        return String.format("Node{id=%d, x=%d, y=%d, neighbors=%s}", id, x, y, neighbors);
    }

    public Network getNetwork() {
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

    public RoutingAlgorithm getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    public void setRoutingAlgorithm(RoutingAlgorithm routingAlgorithm) {
        this.routingAlgorithm = routingAlgorithm;
    }

    public SelectionAlgorithm getSelectionAlgorithm() {
        return selectionAlgorithm;
    }

    public void setSelectionAlgorithm(SelectionAlgorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
    }
}
