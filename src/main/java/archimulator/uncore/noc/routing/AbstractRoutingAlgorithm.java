package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Node;

/**
 * Abstract routing algorithm.
 *
 * @author Min Cai
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {
    private Node node;

    public AbstractRoutingAlgorithm(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
