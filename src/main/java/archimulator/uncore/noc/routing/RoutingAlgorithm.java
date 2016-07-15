package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Direction;
import archimulator.uncore.noc.Node;

import java.util.List;

/**
 * Routing algorithm.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithm {
    List<Direction> nextHop(Node node, int src, int dest, int parent);
}
