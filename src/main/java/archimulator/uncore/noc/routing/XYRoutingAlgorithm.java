package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * XY routing algorithm.
 *
 * @author Min Cai
 */
public class XYRoutingAlgorithm extends AbstractRoutingAlgorithm {
    public XYRoutingAlgorithm(Node node) {
        super(node);
    }

    @Override
    public List<Direction> nextHop(int src, int dest, int parent) {
        List<Direction> directions = new ArrayList<>();

        int destX = Node.getX(getNode().getNetwork(), dest);
        int destY = Node.getY(getNode().getNetwork(), dest);

        if(destX != getNode().getX()) {
            if(destX > getNode().getX()) {
                directions.add(Direction.EAST);
            } else {
                directions.add(Direction.WEST);
            }
        } else {
            if(destY > getNode().getY()) {
                directions.add(Direction.SOUTH);
            } else {
                directions.add(Direction.NORTH);
            }
        }

        return directions;
    }
}
