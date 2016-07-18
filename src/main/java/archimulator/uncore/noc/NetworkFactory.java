/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.noc;

import archimulator.common.NoCEnvironment;
import archimulator.uncore.noc.routing.OddEvenRoutingAlgorithm;
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
    /**
     * Create a network using the XY NoC routing algorithm.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return a newly created network using the XY NoC routing algorithm
     */
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

    /**
     * Create a network using the Odd-Even routing and Random selection algorithms.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return the newly created network using the Odd-Even routing and Random selection algorithms
     */
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
                OddEvenRoutingAlgorithm::new
        );
    }

    /**
     * Create a network using the Odd-Even routing and Buffer-Level selection algorithms.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return a newly created network using the Odd-Even routing and Buffer-Level selection algorithms
     */
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
                OddEvenRoutingAlgorithm::new
        );
    }

    /**
     * Create a network using the Odd-Even routing and Neighbor-on-Path (NoP) selection algorithms.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return a newly created network using the Odd-Even routing and Neighbor-on-Path (NoP) selection algorithms
     */
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
                OddEvenRoutingAlgorithm::new
        );
    }

    /**
     * Create a network using the Odd-Even routing and Neighbor-on-Path (NoP) selection algorithms.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return the newly created network using the Odd-Even routing and Neighbor-on-Path (NoP) selection algorithms
     */
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
                OddEvenRoutingAlgorithm::new
        );

        switch (environment.getConfig().getAntPacketTraffic()) {
            case "uniform":
                new UniformTrafficGenerator(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (src, dest, size) -> new ForwardAntPacket(network, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "transpose":
                new TransposeTrafficGenerator(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (src, dest, size) -> new ForwardAntPacket(network, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
            case "hotspot":
                new HotspotTrafficGenerator(
                        network,
                        environment.getConfig().getAntPacketInjectionRate(),
                        (src, dest, size) -> new ForwardAntPacket(network, src, dest, size, () -> {}),
                        environment.getConfig().getAntPacketSize(),
                        -1
                );
                break;
        }

        return network;
    }

    /**
     * Create a network.
     *
     * @param environment the environment
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param numNodes the number of nodes
     * @return a newly created network
     */
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
