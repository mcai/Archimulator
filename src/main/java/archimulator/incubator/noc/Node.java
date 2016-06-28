package archimulator.incubator.noc;

import archimulator.incubator.noc.routers.InputVirtualChannel;
import archimulator.incubator.noc.routers.Router;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

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

    public static int getX(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        return id % network.getWidth();
    }

    public static int getY(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        return (id - id % network.getWidth()) / network.getWidth();
    }

    @Override
    public String toString() {
        return String.format("Node{id=%d, x=%d, y=%d, neighbors=%s}", id, x, y, neighbors);
    }

    public void handleDestArrived(Packet packet, InputVirtualChannel inputVirtualChannel) {
        packet.memorize(this.id);

        packet.setEndCycle(this.network.getCycleAccurateEventQueue().getCurrentCycle());

        this.network.getInflightPackets().remove(packet);
        this.network.logPacketTransmitted(packet);

        if(packet.getOnCompletedCallback() != null) {
            packet.getOnCompletedCallback().apply();
        }
    }

    public Direction doRouteCalculation(Packet packet, InputVirtualChannel inputVirtualChannel) {
        int parent = !packet.getMemory().isEmpty() ? packet.getMemory().get(packet.getMemory().size() - 1).getFirst() : -1;

        packet.memorize(this.id);

        List<Direction> directions =
                this.network.getRoutingAlgorithm().nextHop(this, packet.getSrc(), packet.getDest(), parent);

        return this.select(packet.getSrc(), packet.getDest(), inputVirtualChannel.getId(), directions);
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
