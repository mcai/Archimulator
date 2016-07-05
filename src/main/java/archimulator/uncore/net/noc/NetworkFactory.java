package archimulator.uncore.net.noc;

import archimulator.uncore.net.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.net.noc.routing.RoutingAlgorithm;
import archimulator.uncore.net.noc.routing.XYRoutingAlgorithm;
import archimulator.uncore.net.noc.selection.BufferLevelSelectionBasedNode;
import archimulator.uncore.net.noc.selection.NeighborOnPathSelectionBasedNode;
import archimulator.uncore.net.noc.selection.RandomSelectionBasedNode;
import archimulator.uncore.net.noc.selection.aco.ACONode;
import archimulator.uncore.net.noc.selection.aco.ForwardAntPacket;
import archimulator.uncore.net.noc.traffics.HotspotTrafficGenerator;
import archimulator.uncore.net.noc.traffics.TransposeTrafficGenerator;
import archimulator.uncore.net.noc.traffics.UniformTrafficGenerator;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Network factory.
 *
 * @author Min Cai
 */
public class NetworkFactory {
    public static Network<? extends Node, ? extends RoutingAlgorithm> aco(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    ) {
        Network<ACONode, OddEvenTurnBasedRoutingAlgorithm> network =
                new StandaloneNetwork<>(
                        settings,
                        cycleAccurateEventQueue,
                        settings.getConfig().getNumNodes(),
                        new NodeFactory<ACONode>() {
                            @Override
                            public ACONode createNode(Network<ACONode, ?> network, int i) {
                                return new ACONode(network, i);
                            }
                        },
                        OddEvenTurnBasedRoutingAlgorithm::new);

        switch (settings.getConfig().getTraffic()) {
            case "uniform":
                new UniformTrafficGenerator<>(
                        network,
                        settings.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        settings.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator<>(
                        network,
                        settings.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        settings.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator<>(
                        network,
                        settings.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        settings.getConfig().getAntPacketSize(),
                        -1
                );
                break;
        }

        return network;
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> random(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getConfig().getNumNodes(),
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> bufferLevel(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getConfig().getNumNodes(),
                new NodeFactory<BufferLevelSelectionBasedNode>() {
                    @Override
                    public BufferLevelSelectionBasedNode createNode(Network<BufferLevelSelectionBasedNode, ?> network, int i) {
                        return new BufferLevelSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> neighborOnPath(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    ) {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getConfig().getNumNodes(),
                new NodeFactory<NeighborOnPathSelectionBasedNode>() {
                    @Override
                    public NeighborOnPathSelectionBasedNode createNode(Network<NeighborOnPathSelectionBasedNode, ?> network, int i) {
                        return new NeighborOnPathSelectionBasedNode(network, i);
                    }
                },
                OddEvenTurnBasedRoutingAlgorithm::new);
    }

    public static Network<? extends Node, ? extends RoutingAlgorithm> xy(
            NoCSettings settings,
            CycleAccurateEventQueue cycleAccurateEventQueue
    )  {
        return new StandaloneNetwork<>(
                settings,
                cycleAccurateEventQueue,
                settings.getConfig().getNumNodes(),
                new NodeFactory<RandomSelectionBasedNode>() {
                    @Override
                    public RandomSelectionBasedNode createNode(Network<RandomSelectionBasedNode, ?> network, int i) {
                        return new RandomSelectionBasedNode(network, i);
                    }
                },
                XYRoutingAlgorithm::new);
    }
}
