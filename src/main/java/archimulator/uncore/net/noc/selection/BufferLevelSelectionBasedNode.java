package archimulator.uncore.net.noc.selection;

import archimulator.uncore.net.noc.Direction;
import archimulator.uncore.net.noc.Network;
import archimulator.uncore.net.noc.Node;
import archimulator.uncore.net.noc.routers.Router;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;

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
            return bestDirections.get(this.getNetwork().getMemoryHierarchy().getRandom().nextInt(bestDirections.size()));
        }

        return super.select(src, dest, ivc, directions);
    }
}
