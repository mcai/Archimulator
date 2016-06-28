package archimulator.incubator.noc.routing;

/**
 * Routing algorithm factory.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithmFactory<RoutingAlgorithmT extends RoutingAlgorithm> {
    RoutingAlgorithmT createRoutingAlgorithm();
}
