package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;

/**
 * Pheromone.
 *
 * @author Min Cai
 */
public class Pheromone {
    private PheromoneTable pheromoneTable;
    private int dest;
    private Direction direction;
    private double value;

    /**
     * Create a pheromone.
     *
     * @param pheromoneTable the parent pheromone table
     * @param dest the destination node ID
     * @param direction the direction
     * @param value the pheromone value
     */
    public Pheromone(PheromoneTable pheromoneTable, int dest, Direction direction, double value) {
        this.pheromoneTable = pheromoneTable;
        this.dest = dest;
        this.direction = direction;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Pheromone{node.id=%s, dest=%d, direction=%s, value=%s}",
                pheromoneTable.getNode().getId(), dest, direction, value);
    }

    /**
     * Get the parent pheromone table.
     *
     * @return the parent pheromone table
     */
    public PheromoneTable getPheromoneTable() {
        return pheromoneTable;
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
