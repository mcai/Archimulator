package archimulator.uncore.noc;

import archimulator.common.NoCEnvironment;
import archimulator.uncore.noc.routing.OddEvenTurnBasedRoutingAlgorithm;
import archimulator.uncore.noc.routing.XYRoutingAlgorithm;
import archimulator.uncore.noc.selection.BufferLevelSelectionAlgorithm;
import archimulator.uncore.noc.selection.NeighborOnPathSelectionAlgorithm;
import archimulator.uncore.noc.selection.RandomSelectionAlgorithm;
import archimulator.uncore.noc.selection.aco.ACOSelectionAlgorithm;
import archimulator.uncore.noc.selection.aco.ForwardAntPacket;
import archimulator.uncore.noc.traffics.HotspotTrafficGenerator;
import archimulator.uncore.noc.traffics.TransposeTrafficGenerator;
import archimulator.uncore.noc.traffics.UniformTrafficGenerator;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Network factory.
 *
 * @author Min Cai
 */
public class NetworkFactory {
    private static Network xy(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                RandomSelectionAlgorithm::new,
                XYRoutingAlgorithm::new
        );
    }

    private static Network random(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                RandomSelectionAlgorithm::new,
                OddEvenTurnBasedRoutingAlgorithm::new
        );
    }

    private static Network bufferLevel(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    )  {
        return new Network(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                BufferLevelSelectionAlgorithm::new,
                OddEvenTurnBasedRoutingAlgorithm::new
        );
    }

    private static Network neighborOnPath(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        return new Network(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                NeighborOnPathSelectionAlgorithm::new,
                OddEvenTurnBasedRoutingAlgorithm::new
        );
    }

    private static Network aco(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        Network network = new Network(
                environment,
                cycleAccurateEventQueue,
                numNodes,
                ACOSelectionAlgorithm::new,
                OddEvenTurnBasedRoutingAlgorithm::new
        );

        switch (environment.getConfig().getAntPacketTraffic()) {
            case "uniform":
                new UniformTrafficGenerator<>(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator<>(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator<>(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (n, src, dest, size) -> new ForwardAntPacket(n, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
        }

        return network;
    }

    public static Network create(
            NoCEnvironment environment,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            int numNodes
    ) {
        Network network;

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
