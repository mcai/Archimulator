package archimulator.uncore.noc;

import archimulator.common.NoCEnvironment;
import archimulator.uncore.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.noc.routing.RoutingAlgorithm;
import archimulator.uncore.noc.routing.XYRoutingAlgorithm;
import archimulator.uncore.noc.selection.BufferLevelSelectionBasedNode;
import archimulator.uncore.noc.selection.NeighborOnPathSelectionBasedNode;
import archimulator.uncore.noc.selection.RandomSelectionBasedNode;
import archimulator.uncore.noc.selection.aco.ACONode;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Network factory.
 *
 * @author Min Cai
 */
public class NetworkFactory {
    private static Network<? extends Node, ? extends RoutingAlgorithm> xy(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                XYRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> random(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> bufferLevel(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                new NodeFactory<BufferLevelSelectionBasedNode>() {
                    @Override
                    public BufferLevelSelectionBasedNode createNode(Network<BufferLevelSelectionBasedNode, ?> network, int i) {
                        return new BufferLevelSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> neighborOnPath(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        return new Network<>(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                new NodeFactory<NeighborOnPathSelectionBasedNode>() {
                    @Override
                    public NeighborOnPathSelectionBasedNode createNode(Network<NeighborOnPathSelectionBasedNode, ?> network, int i) {
                        return new NeighborOnPathSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> aco(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        return new Network<>(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                new NodeFactory<ACONode>() {
                    @Override
                    public ACONode createNode(Network<ACONode, ?> network1, int i) {
                        return new ACONode(network1, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> setupNetwork(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        Network<? extends Node, ? extends RoutingAlgorithm> network;

        switch (environment.getConfig().getRouting()) {
            case "xy":
                network = xy(environment, cycleAccurateEventQueue, numNodes);
                break;
            case "oddEven":
                switch (environment.getConfig().getSelection()) {
                    case "random":
                        network = random(environment, cycleAccurateEventQueue, numNodes);
                        break;
                    case "bufferLevel":
                        network = bufferLevel(environment, cycleAccurateEventQueue, numNodes);
                        break;
                    case "neighborOnPath":
                        network = neighborOnPath(environment, cycleAccurateEventQueue, numNodes);
                        break;
                    case "aco":
                        network = aco(environment, cycleAccurateEventQueue, numNodes);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }

        return network;
    }
}
