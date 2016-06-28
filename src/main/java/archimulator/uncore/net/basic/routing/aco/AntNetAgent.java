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
package archimulator.uncore.net.basic.routing.aco;

import archimulator.uncore.net.basic.Router;

import java.util.Collection;

/**
 * Ant net agent.
 *
 * @author Min Cai
 */
public class AntNetAgent {
    private Router router;
    private ACORouting routing;
    private RoutingTable routingTable;

    /**
     * Create an ant net agent.
     *
     * @param router  the parent router
     * @param routing the ACO routing
     */
    public AntNetAgent(Router router, ACORouting routing) {
        this.router = router;
        this.routing = routing;
        this.routingTable = new RoutingTable(this);

        this.initializeRoutingTable();

        this.router.getNet().getCycleAccurateEventQueue().getPerCycleEvents().add(() -> {
            if (this.router.getNet().getCycleAccurateEventQueue().getCurrentCycle() % 10000 == 0) {
                this.createAndSendForwardAntPacket();
            }
        });
    }

    /**
     * Initialize the routing table.
     */
    private void initializeRoutingTable() {
        Collection<Router> neighbors = this.router.getLinks().values();

        for (Router router : this.router.getNet().getRouters()) {
            if (this.router != router) {
                double pheromoneValue = 1.0d / neighbors.size();

                for (Router neighbor : neighbors) {
                    this.routingTable.addEntry(router, neighbor, pheromoneValue);
                }
            }
        }
    }

    /**
     * Create and send a forward ant packet.
     */
    private void createAndSendForwardAntPacket() {
        Router destination = this.routingTable.calculateRandomDestination(this.router);

        AntPacket packet = new AntPacket(this.routing, AntPacketType.FORWARD_ANT, this.router, destination);
        this.memorize(packet);

        Router neighbor = this.routingTable.calculateNeighbor(destination, this.router);
        this.sendPacket(packet, neighbor);
    }

    /**
     * On receiving an ant packet.
     *
     * @param packet the received packet
     * @param parent the parent router
     */
    private void receiveAntPacket(AntPacket packet, Router parent) {
        if (packet.getType() == AntPacketType.FORWARD_ANT) {
            this.memorize(packet);
            if (this.getRouter() != packet.getTo()) {
                this.forwardAntPacket(packet, parent);
            } else {
                this.createAndSendBackwardAntPacket(packet);
            }
        } else {
            this.updateRoutingTable(packet);
            if (this.getRouter() != packet.getTo()) {
                this.backwardAntPacket(packet);
            }
        }
    }

    /**
     * Send the specified ant packet to the neighbor router as determined by the AntNet algorithm.
     *
     * @param packet the ant packet
     * @param parent the parent router
     */
    private void forwardAntPacket(AntPacket packet, Router parent) {
        Router neighbor = this.routingTable.calculateNeighbor(packet.getTo(), parent);
        this.sendPacket(packet, neighbor);
    }

    /**
     * Create and send a backward ant packet.
     *
     * @param packet the original forward ant packet
     */
    private void createAndSendBackwardAntPacket(AntPacket packet) {
        Router temp = packet.getFrom();
        packet.setFrom(packet.getTo());
        packet.setTo(temp);

        packet.setType(AntPacketType.BACKWARD_ANT);

        int index = packet.getMemory().size() - 2;
        this.sendPacket(packet, packet.getMemory().get(index));
    }

    /**
     * Send the specified backward ant packet to the neighbor router as determined from the memory.
     *
     * @param packet the backward ant packet
     */
    private void backwardAntPacket(AntPacket packet) {
        int index = packet.getMemory().lastIndexOf(this.router) - 1;
        this.sendPacket(packet, packet.getMemory().get(index));
    }

    /**
     * Update the routing table.
     *
     * @param packet the ant packet
     */
    private void updateRoutingTable(AntPacket packet) {
        int index = packet.getMemory().indexOf(this.router) + 1;
        Router neighbor = packet.getMemory().get(index);

        for (int i = index; i < packet.getMemory().size(); i++) {
            Router destination = packet.getMemory().get(i);
            this.routingTable.update(destination, neighbor);
        }
    }

    /**
     * Build the memory of the specified forward ant packet.
     *
     * @param packet the ant packet
     */
    private void memorize(AntPacket packet) {
        packet.getMemory().add(this.router);
    }

    /**
     * Send the packet to the neighbor router.
     *
     * @param packet   the packet
     * @param neighbor the neighbor router
     */
    private void sendPacket(AntPacket packet, Router neighbor) {
        this.router.getNet().transfer(
                this.router,
                neighbor,
                8,
                () -> this.routing.getAgents().get(neighbor).receiveAntPacket(packet, this.router)
        );
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Get the ACO routing.
     *
     * @return the ACO routing
     */
    public ACORouting getRouting() {
        return routing;
    }

    /**
     * Get the routing table.
     *
     * @return the routing table
     */
    public RoutingTable getRoutingTable() {
        return routingTable;
    }
}
