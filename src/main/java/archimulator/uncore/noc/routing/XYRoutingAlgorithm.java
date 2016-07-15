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
public class XYRoutingAlgorithm implements RoutingAlgorithm {
    @Override
    public List<Direction> nextHop(Node node, int src, int dest, int parent) {
        List<Direction> directions = new ArrayList<>();

        int destX = Node.getX(node.getNetwork(), dest);
        int destY = Node.getY(node.getNetwork(), dest);

        if(destX != node.getX()) {
            if(destX > node.getX()) {
                directions.add(Direction.EAST);
            } else {
                directions.add(Direction.WEST);
            }
        } else {
            if(destY > node.getY()) {
                directions.add(Direction.SOUTH);
            } else {
                directions.add(Direction.NORTH);
            }
        }

//        System.out.println(String.format("[%d] node#%d::next_hop(dest=%d, parent=%d, neighbors=%s) = %s",
//                node.getNetwork().getCycleAccurateEventQueue().getCurrentCycle(),
//                node.getId(),
//                dest,
//                parent,
//                node.getNeighbors(),
//                directions));

        return directions;
    }
}
