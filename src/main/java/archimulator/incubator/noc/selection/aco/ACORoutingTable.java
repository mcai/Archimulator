package archimulator.incubator.noc.selection.aco;

import archimulator.incubator.noc.Direction;
import javaslang.collection.LinkedHashMap;
import javaslang.collection.Map;

/**
 * ACO routing table.
 *
 * @author Min Cai
 */
public class ACORoutingTable {
    private ACONode node;

    private Map<Integer, Map<Direction, Pheromone>> pheromones;

    public ACORoutingTable(ACONode node) {
        this.node = node;

        this.pheromones = LinkedHashMap.empty();
    }

    public void append(int dest, Direction direction, double pheromoneValue) {
        Pheromone pheromone = new Pheromone(this, dest, direction, pheromoneValue);

        if(!this.pheromones.containsKey(dest)) {
            this.pheromones.put(dest, LinkedHashMap.empty());
        }

        this.pheromones.get(dest).get().put(direction, pheromone);
    }

    public void update(int dest, Direction direction) {
        for(Pheromone pheromone : this.pheromones.get(dest).get().values()) {
            if(pheromone.getDirection() == direction) {
                pheromone.setValue(
                        pheromone.getValue()
                                + this.node.getNetwork().getExperiment().getConfig().getReinforcementFactor() * (1 - pheromone.getValue())
                );
            } else {
                pheromone.setValue(
                        pheromone.getValue()
                                - this.node.getNetwork().getExperiment().getConfig().getReinforcementFactor() * pheromone.getValue()
                );
            }
        }
    }

    public ACONode getNode() {
        return node;
    }

    public Map<Integer, Map<Direction, Pheromone>> getPheromones() {
        return pheromones;
    }
}
