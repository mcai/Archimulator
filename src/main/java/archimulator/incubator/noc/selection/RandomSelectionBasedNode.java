package archimulator.incubator.noc.selection;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

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
        return directions.get(this.getNetwork().getExperiment().getRandom().nextInt(directions.size()));
    }
}
