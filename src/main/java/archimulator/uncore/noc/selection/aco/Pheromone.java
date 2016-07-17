package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;

/**
 * Pheromone.
 *
 * @author Min Cai
 */
public class Pheromone {
    private PheromoneTable routingTable;
    private int dest;
    private Direction direction;
    private double value;

    /**
     * Create a pheromone.
     *
     * @param routingTable the parent routing table
     * @param dest the destination node ID
     * @param direction the direction
     * @param value the pheromone value
     */
    public Pheromone(PheromoneTable routingTable, int dest, Direction direction, double value) {
        this.routingTable = routingTable;
        this.dest = dest;
        this.direction = direction;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Pheromone{node.id=%s, dest=%d, direction=%s, value=%s}",
                routingTable.getSelectionAlgorithm().getNode().getId(), dest, direction, value);
    }

    /**
     * Get the parent routing table.
     *
     * @return the parent routing table
     */
    public PheromoneTable getRoutingTable() {
        return routingTable;
    }

    /**
     * Get the destination node ID.
     *
     * @return the destination node ID
     */
    public int getDest() {
        return dest;
    }

    /**
     * Get the direction.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the pheromone value.
     *
     * @return the pheromone value
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the pheromone value.
     *
     * @param value the pheromone value
     */
    public void setValue(double value) {
        this.value = value;
    }
}
