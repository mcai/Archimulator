package archimulator.incubator.noc.routing;

import archimulator.incubator.noc.Direction;
import archimulator.incubator.noc.Node;
import javaslang.collection.List;

/**
 * Routing algorithm.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithm {
    List<Direction> nextHop(Node node, int src, int dest, int parent);
}
