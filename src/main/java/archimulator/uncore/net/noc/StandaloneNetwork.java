package archimulator.uncore.net.noc;

import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.routing.RoutingAlgorithmFactory;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Standalone network.
 *
 * @param <NodeT> the node type
 * @param <RoutingAlgorithmT> the routing algorithm type
 * @author Min Cai
 */
public class StandaloneNetwork<NodeT extends Node,  RoutingAlgorithmT extends RoutingAlgorithm>
        extends Network<NodeT, RoutingAlgorithmT> {
    StandaloneNetwork(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes,
            NodeFactory<NodeT> nodeFactory,
            RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory
    ) {
        super(settings, cycleAccurateEventQueue, numNodes, nodeFactory, routingAlgorithmFactory);
    }

    @Override
    public boolean simulateAtCurrentCycle() {
        return true;
    }
}
