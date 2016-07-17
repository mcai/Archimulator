package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Node;

/**
 * Routing algorithm factory.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithmFactory {
    RoutingAlgorithm create(Node node);
}
