package archimulator.incubator.noc;

/**
 * Routing algorithm factory.
 *
 * @author Min Cai
 */
public interface RoutingAlgorithmFactory<RoutingAlgorithmT extends RoutingAlgorithm> {
    RoutingAlgorithmT createRoutingAlgorithm();
}
