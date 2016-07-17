package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Odd-even turn based routing algorithm.
 *
 * @author Min Cai
 */
public class OddEvenTurnBasedRoutingAlgorithm extends AbstractRoutingAlgorithm {
    public OddEvenTurnBasedRoutingAlgorithm(Node node) {
        super(node);
    }

    @Override
    public List<Direction> nextHop(int src, int dest, int parent) {
        List<Direction> directions = new ArrayList<>();

        int c0 = getNode().getX();
        int c1 = getNode().getY();

        int s0 = Node.getX(getNode().getNetwork(), src);
        int s1 = Node.getY(getNode().getNetwork(), src);

        int d0 = Node.getX(getNode().getNetwork(), dest);
        int d1 = Node.getY(getNode().getNetwork(), dest);

        int e0 = d0 - c0;
        int e1 = -(d1 - c1);

        if(e0 == 0) {
            if(e1 > 0) {
                directions.add(Direction.NORTH);
            } else {
                directions.add(Direction.SOUTH);
            }
        } else {
            if(e0 > 0) {
                if(e1 == 0) {
                    directions.add(Direction.EAST);
                } else {
                    if(c0 % 2 == 1 || c0 == s0) {
                        if(e1 > 0) {
                            directions.add(Direction.NORTH);
                        } else {
                            directions.add(Direction.SOUTH);
                        }
                    }

                    if(d0 % 2 == 1 || e0 != 1) {
                        directions.add(Direction.EAST);
                    }
                }
            } else {
                directions.add(Direction.WEST);
                if(c0 % 2 == 0) {
                    if(e1 > 0) {
                        directions.add(Direction.NORTH);
                    }
                    if(e1 < 0) {
                        directions.add(Direction.SOUTH);
                    }
                }
            }
        }

        return directions;
    }
}
