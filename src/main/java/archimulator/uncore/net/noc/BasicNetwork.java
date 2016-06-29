package archimulator.uncore.net.noc;

import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.routing.RoutingAlgorithmFactory;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Basic network.
 *
 * @param <NodeT> the node type
 * @param <RoutingAlgorithmT> the routing algorithm type
 */
public class BasicNetwork<NodeT extends Node, RoutingAlgorithmT extends RoutingAlgorithm> extends Network<NodeT, RoutingAlgorithmT> {
    public BasicNetwork(NoCSettings settings, CycleAccurateEventQueue cycleAccurateEventQueue, int numNodes, NodeFactory<NodeT> nodeFactory, RoutingAlgorithmFactory<RoutingAlgorithmT> routingAlgorithmFactory) {
        super(settings, cycleAccurateEventQueue, numNodes, nodeFactory, routingAlgorithmFactory);
    }

    @Override
    public boolean simulateAtCurrentCycle() {
        return true;
    }
}
