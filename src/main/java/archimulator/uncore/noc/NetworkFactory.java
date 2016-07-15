package archimulator.uncore.noc;

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
    private static Network<? extends Node, ? extends RoutingAlgorithm> aco(
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        return new Network<>(
                memoryHierarchy,
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

    private static Network<? extends Node, ? extends RoutingAlgorithm> random(
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                memoryHierarchy,
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
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                memoryHierarchy,
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
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        return new Network<>(
                memoryHierarchy,
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

    private static Network<? extends Node, ? extends RoutingAlgorithm> xy(
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network<>(
                memoryHierarchy,
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

    public static Network<? extends Node, ? extends RoutingAlgorithm> setupNetwork(
            NoCMemoryHierarchy memoryHierarchy,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        Network<? extends Node, ? extends RoutingAlgorithm> network;

        switch (memoryHierarchy.getExperiment().getConfig().getRouting()) {
            case "xy":
                network = xy(memoryHierarchy, cycleAccurateEventQueue, numNodes);
                break;
            case "oddEven":
                switch (memoryHierarchy.getExperiment().getConfig().getSelection()) {
                    case "random":
                        network = random(memoryHierarchy, cycleAccurateEventQueue, numNodes);
                        break;
                    case "bufferLevel":
                        network = bufferLevel(memoryHierarchy, cycleAccurateEventQueue, numNodes);
                        break;
                    case "neighborOnPath":
                        network = neighborOnPath(memoryHierarchy, cycleAccurateEventQueue, numNodes);
                        break;
                    case "aco":
                        network = aco(memoryHierarchy, cycleAccurateEventQueue, numNodes);
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
