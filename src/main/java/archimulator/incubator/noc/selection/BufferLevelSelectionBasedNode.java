package archimulator.incubator.noc.selection;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.routers.Router;
import archimulator.incubator.noc.routing.RoutingAlgorithm;
import javaslang.collection.List;

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
        List<Direction> bestDirections = List.empty();

        double maxFreeSlots = -1.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNetwork().getNodes().get(this.getNeighbors().get(direction).get()).getRouter();
            int freeSlots = neighborRouter.freeSlots(direction.getReflexDirection(), ivc);

            if(freeSlots > maxFreeSlots) {
                maxFreeSlots = freeSlots;
                bestDirections = List.of(direction);
            } else if(freeSlots == maxFreeSlots) {
                bestDirections.append(direction);
            }
        }

        if(!bestDirections.isEmpty()) {
            return bestDirections.get(this.getNetwork().getExperiment().getRandom().nextInt(bestDirections.size()));
        }

        return super.select(src, dest, ivc, directions);
    }
}
