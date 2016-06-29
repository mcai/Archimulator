package archimulator.uncore.net.noc;

/**
 * Node factory.
 *
 * @author Min Cai
 */
public interface NodeFactory<NodeT extends Node> {
    NodeT createNode(Network<NodeT, ?> network, int i);
}
