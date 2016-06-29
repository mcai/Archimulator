package archimulator.uncore.net.noc.selection;

import archimulator.uncore.net.noc.Direction;
import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

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
        return directions.get(this.getNetwork().getSettings().getRandom().nextInt(directions.size()));
    }
}
