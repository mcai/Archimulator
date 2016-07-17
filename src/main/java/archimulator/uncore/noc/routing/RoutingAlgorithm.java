package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Direction;

import java.util.List;

/**
 * Routing algorithm.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithm {
    List<Direction> nextHop(int src, int dest, int parent);
}
