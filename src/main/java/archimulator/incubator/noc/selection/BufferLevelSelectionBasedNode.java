package archimulator.incubator.noc.selection;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.routers.Router;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Buffer level selection based node.
 *
 * @author Min Cai
 */
public class BufferLevelSelectionBasedNode extends Node {
    public BufferLevelSelectionBasedNode(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        super(network, id);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        List<Direction> bestDirections = new ArrayList<>();

        double maxFreeSlots = -1.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNetwork().getNodes().get(this.getNeighbors().get(direction)).getRouter();
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
            return bestDirections.get(this.getNetwork().getSettings().getRandom().nextInt(bestDirections.size()));
        }

        return super.select(src, dest, ivc, directions);
    }
}
