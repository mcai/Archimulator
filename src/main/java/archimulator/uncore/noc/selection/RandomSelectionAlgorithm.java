package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;

import java.util.List;

/**
 * Random selection algorithm.
 *
 * @author Min Cai
 */
public class RandomSelectionAlgorithm extends AbstractSelectionAlgorithm {
    public RandomSelectionAlgorithm(Node node) {
        super(node);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        return directions.get(this.getNode().getNetwork().getEnvironment().getRandom().nextInt(directions.size()));
    }
}
