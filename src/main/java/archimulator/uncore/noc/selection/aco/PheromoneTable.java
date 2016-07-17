package archimulator.uncore.noc.selection.aco;

import archimulator.uncore.noc.Direction;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pheromone table.
 *
 * @author Min Cai
 */
public class PheromoneTable {
    private ACOSelectionAlgorithm selectionAlgorithm;

    private Map<Integer, Map<Direction, Pheromone>> pheromones;

    public PheromoneTable(ACOSelectionAlgorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;

        this.pheromones = new TreeMap<>();
    }

    public void append(int dest, Direction direction, double pheromoneValue) {
        Pheromone pheromone = new Pheromone(this, dest, direction, pheromoneValue);

        if(!this.pheromones.containsKey(dest)) {
            this.pheromones.put(dest, new EnumMap<>(Direction.class));
        }

        this.pheromones.get(dest).put(direction, pheromone);
    }

    public void update(int dest, Direction direction) {
        for(Pheromone pheromone : this.pheromones.get(dest).values()) {
            if(pheromone.getDirection() == direction) {
                pheromone.setValue(
                        pheromone.getValue()
                                + this.selectionAlgorithm.getNode().getNetwork().getEnvironment().getConfig().getReinforcementFactor() * (1 - pheromone.getValue())
                );
            } else {
                pheromone.setValue(
                        pheromone.getValue()
                                - this.selectionAlgorithm.getNode().getNetwork().getEnvironment().getConfig().getReinforcementFactor() * pheromone.getValue()
                );
            }
        }
    }

    public ACOSelectionAlgorithm getSelectionAlgorithm() {
        return selectionAlgorithm;
    }

    public Map<Integer, Map<Direction, Pheromone>> getPheromones() {
        return pheromones;
    }
}
