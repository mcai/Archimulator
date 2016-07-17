package archimulator.uncore.noc.selection;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;
import archimulator.uncore.noc.routers.Router;

import java.util.List;

/**
 * Neighbor-on-Path (NoP) selection based node.
 *
 * @author Min Cai
 */
public class NeighborOnPathSelectionAlgorithm extends AbstractSelectionAlgorithm {
    public NeighborOnPathSelectionAlgorithm(Node node) {
        super(node);
    }

    @Override
    public Direction select(int src, int dest, int ivc, List<Direction> directions) {
        double maxScore = -1.0;
        Direction directionWithMaxScore = null;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();

            if(neighborRouter.getNode().getId() == dest) {
                return direction;
            }

            double score = ((NeighborOnPathSelectionAlgorithm) (neighborRouter.getNode().getSelectionAlgorithm())).nopScore(
                    src, dest, this.getNode().getId(), ivc
            );
            if(score > maxScore) {
                maxScore = score;
                directionWithMaxScore = direction;
            }
        }

        return directionWithMaxScore;
    }

    private double nopScore(int src, int dest, int parent, int ivc) {
        List<Direction> directions = this.getNode().getNetwork().getRoutingAlgorithm().nextHop(
                this.getNode(), src, dest, parent
        );

        double score = 0.0;

        for(Direction direction : directions) {
            Router neighborRouter = this.getNode().getNetwork().getNodes().get(this.getNode().getNeighbors().get(direction)).getRouter();
            score += neighborRouter.freeSlots(direction.getReflexDirection(), ivc);
        }

        return score;
    }
}
