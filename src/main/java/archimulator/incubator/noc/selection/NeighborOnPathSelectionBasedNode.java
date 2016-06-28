package archimulator.incubator.noc.selection;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Network;
import archimulator.incubator.noc.Node;
import archimulator.incubator.noc.routers.Router;
import archimulator.incubator.noc.routing.RoutingAlgorithm;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Neighbor-on-Path (NoP) selection based node.
 *
 * @author Min Cai
 */
public class NeighborOnPathSelectionBasedNode extends Node {
    public NeighborOnPathSelectionBasedNode(Network<? extends Node, ? extends RoutingAlgorithm> network, int id) {
        super(network, id);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        double maxScore = -1.0;
        Direction directionWithMaxScore = null;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNetwork().getNodes().get(this.getNeighbors().get(direction)).getRouter();

            if(neighborRouter.getNode().getId() == dest) {
                return direction;
            }

            double score = ((NeighborOnPathSelectionBasedNode) (neighborRouter.getNode())).nopScore(src, dest, this.getId(), ivc);
            if(score > maxScore) {
                maxScore = score;
                directionWithMaxScore = direction;
            }
        }

        return directionWithMaxScore;
    }

    private double nopScore(int src, int dest, int parent, int ivc) {
        List<Direction> directions = this.getNetwork().getRoutingAlgorithm().nextHop(
                this, src, dest, parent
        );

        double score = 0.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNetwork().getNodes().get(this.getNeighbors().get(direction)).getRouter();
            score += neighborRouter.freeSlots(direction.getReflexDirection(), ivc);
        }

        return score;
    }
}
