package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Network;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routing.RoutingAlgorithm;

import java.util.List;

/**
 * Random selection based node.
 *
 * @author Min Cai
 */
public class RandomSelectionBasedNode extends Node {
    public RandomSelectionBasedNode(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        super(network, id);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        return directions.get(this.getNetwork().getMemoryHierarchy().getRandom().nextInt(directions.size()));
    }
}
