package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routers.Router;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer level selection based node.
 *
 * @author Min Cai
 */
public class BufferLevelSelectionAlgorithm extends AbstractSelectionAlgorithm {
    public BufferLevelSelectionAlgorithm(Node node) {
        super(node);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        List<Direction> bestDirections = new ArrayList<>();

        double maxFreeSlots = -1.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();
            int freeSlots = neighborRouter.freeSlots(direction.getReflexDirection(), ivc);

            if(freeSlots > maxFreeSlots) {
                maxFreeSlots = freeSlots;
                bestDirections.clear();
                bestDirections.add(direction);
            } else if(freeSlots == maxFreeSlots) {
                bestDirections.add(direction);
            }
        }

        if(!bestDirections.isEmpty()) {
            return bestDirections.get(this.getNode().getNetwork().getEnvironment().getRandom().nextInt(bestDirections.size()));
        }

        return super.select(src, dest, ivc, directions);
    }
}
