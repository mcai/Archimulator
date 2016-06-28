package archimulator.incubator.noc.routing;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Node;
import javaslang.collection.List;

/**
 * XY routing algorithm.
 *
 * @author Min Cai
 */
public class XYRoutingAlgorithm implements RoutingAlgorithm {
    @Override
    public List<Direction> nextHop(Node node, int src, int dest, int parent) {
        List<Direction> directions = List.empty();

        int destX = Node.getX(node.getNetwork(), dest);
        int destY = Node.getY(node.getNetwork(), dest);

        if(destX != node.getX()) {
            if(destX > node.getX()) {
                directions.append(Direction.EAST);
            } else {
                directions.append(Direction.WEST);
            }
        } else {
            if(destY > node.getY()) {
                directions.append(Direction.SOUTH);
            } else {
                directions.append(Direction.NORTH);
            }
        }

        return directions;
    }
}
