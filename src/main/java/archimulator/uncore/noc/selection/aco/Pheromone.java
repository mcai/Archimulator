package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;

/**
 * Pheromone.
 *
 * @author Min Cai
 */
public class Pheromone {
    private ACORoutingTable routingTable;
    private int dest;
    private Direction direction;
    private double value;

    public Pheromone(ACORoutingTable routingTable, int dest, Direction direction, double value) {
        this.routingTable = routingTable;
        this.dest = dest;
        this.direction = direction;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Pheromone{node.id=%s, dest=%d, direction=%s, value=%s}",
                routingTable.getNode().getId(), dest, direction, value);
    }

    public ACORoutingTable getRoutingTable() {
        return routingTable;
    }

    public int getDest() {
        return dest;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
