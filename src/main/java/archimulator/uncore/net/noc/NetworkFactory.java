package archimulator.uncore.net.noc;

import archimulator.uncore.net.NoCNet;
import archimulator.uncore.net.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.routing.XYRoutingAlgorithm;
import archimulator.uncore.net.noc.selection.BufferLevelSelectionBasedNode;
import archimulator.uncore.net.noc.selection.NeighborOnPathSelectionBasedNode;
import archimulator.uncore.net.noc.selection.RandomSelectionBasedNode;
import archimulator.uncore.net.noc.selection.aco.ACONode;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Network factory.
 *
 * @author Min Cai
 */
public class NetworkFactory {
    private static Network<? extends Node, ? extends RoutingAlgorithm> aco(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    ) {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getExperiment().getConfig().getNumNodes(),
                new NodeFactory<ACONode>() {
                    @Override
                    public ACONode createNode(Network<ACONode, ?> network1, int i) {
                        return new ACONode(network1, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> random(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getExperiment().getConfig().getNumNodes(),
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> bufferLevel(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getExperiment().getConfig().getNumNodes(),
                new NodeFactory<BufferLevelSelectionBasedNode>() {
                    @Override
                    public BufferLevelSelectionBasedNode createNode(Network<BufferLevelSelectionBasedNode, ?> network, int i) {
                        return new BufferLevelSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> neighborOnPath(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    ) {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getExperiment().getConfig().getNumNodes(),
                new NodeFactory<NeighborOnPathSelectionBasedNode>() {
                    @Override
                    public NeighborOnPathSelectionBasedNode createNode(Network<NeighborOnPathSelectionBasedNode, ?> network, int i) {
                        return new NeighborOnPathSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    private static Network<? extends Node, ? extends RoutingAlgorithm> xy(
            NoCNet settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getExperiment().getConfig().getNumNodes(),
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                XYRoutingAlgorithm::new);
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> setupNetwork(NoCNet settings, CycleAccurateEventQueue cycleAccurateEventQueue) {
        Network<? extends Node, ? extends RoutingAlgorithm> network;

        switch (settings.getExperiment().getConfig().getRouting()) {
            case "xy":
                network = xy(settings, cycleAccurateEventQueue);
                break;
            case "oddEven":
                switch (settings.getExperiment().getConfig().getSelection()) {
                    case "random":
                        network = random(settings, cycleAccurateEventQueue);
                        break;
                    case "bufferLevel":
                        network = bufferLevel(settings, cycleAccurateEventQueue);
                        break;
                    case "neighborOnPath":
                        network = neighborOnPath(settings, cycleAccurateEventQueue);
                        break;
                    case "aco":
                        network = aco(settings, cycleAccurateEventQueue);
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
