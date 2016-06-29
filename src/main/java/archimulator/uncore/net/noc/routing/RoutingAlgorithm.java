package archimulator.uncore.net.noc.routing;

import archimulator.uncore.net.noc.Direction;
import archimulator.uncore.net.noc.Node;

import java.util.List;

/**
 * Routing algorithm.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithm {
    List<Direction> nextHop(Node node, int src, int dest, int parent);
}
