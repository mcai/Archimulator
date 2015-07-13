/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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

import java.util.List;

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
        this.routingTable = new RoutingTable(this.routing);

        this.initializeRoutingTable();

        this.createAndSendForwardAntPacket();
    }

    /**
     * Initialize the routing table.
     */
    private void initializeRoutingTable() {
        for (Router router : this.router.getNet().getRouters()) {
            if (this.router != router) {
                List<Router> neighbors = this.routing.getNeighbors(this.router);
                for (Router neighbor : neighbors) {
                    double pheromoneValue = 1.0d / neighbors.size();
                    this.routingTable.addEntry(router, neighbor, pheromoneValue);
                }
            }
        }
    }

    /**
     * Create and send a forward ant.
     */
    public void createAndSendForwardAntPacket() {
        Router destination = this.routingTable.calculateRandomDestination(this.router);

        AntPacket packet = new AntPacket(this.routing, AntPacketType.FORWARD_ANT, this.router, destination);
        packet.getMemory().add(this.router);

        Router next = this.routingTable.calculateNext(destination, this.router);

        if (next == this.router) {
            return;
        }

        this.sendPacket(packet, next);
    }

    /**
     * On receiving an ant packet.
     *
     * @param packet the received packet
     */
    public void receiveAntPacket(AntPacket packet) {
        if (packet.getType() == AntPacketType.FORWARD_ANT) {
            this.memorize(packet);
            if (this.getRouter() != packet.getDestination()) {
                this.forwardAntPacket(packet);
            } else {
                this.createAndSendBackwardAntPacket(packet);
            }
        } else {
            this.updateRoutingTable(packet);
            if (this.getRouter() != packet.getDestination()) {
                this.backwardAntPacket(packet);
            }
            else {
                this.createAndSendForwardAntPacket();
            }
        }
    }

    /**
     * Build the memory of the specified forward ant.
     *
     * @param packet the ant packet
     */
    public void memorize(AntPacket packet) {
        packet.getMemory().add(this.router);
    }

    /**
     * Send the specified ant packet to the next hop router as determined by the AntNet algorithm.
     *
     * @param packet the ant packet
     */
    public void forwardAntPacket(AntPacket packet) {
        Router parent = packet.getSource();

        //TODO: parent is not the current router
        Router next = this.routingTable.calculateNext(packet.getDestination(), this.router);
        if (next == this.router || next == parent) {
            return;
        }

        this.sendPacket(packet, next);
    }

    /**
     * Create and send a backward ant packet.
     *
     * @param packet the original forward ant packet
     */
    public void createAndSendBackwardAntPacket(AntPacket packet) {
        Router temp = packet.getSource();
        packet.setSource(packet.getDestination());
        packet.setDestination(temp);

        int index = packet.getMemory().size() - 2;

        packet.setType(AntPacketType.BACKWARD_ANT);

        this.sendPacket(packet, packet.getMemory().get(index));
    }

    /**
     * Send the specified backward ant packet to the next hop router as determined from the memory.
     *
     * @param packet the backward ant packet
     */
    public void backwardAntPacket(AntPacket packet) {
        int index = -1;

        for (int i = packet.getMemory().size() - 1; i >= 0; i--) {
            if (packet.getMemory().get(i) == this.router) {
                index = i - 1;
                break;
            }
        }

        packet.setType(AntPacketType.BACKWARD_ANT);

        this.sendPacket(packet, packet.getMemory().get(index));
    }

    /**
     * Update the routing table.
     *
     * @param packet the ant packet
     */
    public void updateRoutingTable(AntPacket packet) {
        int i;
        for (i = 0; packet.getMemory().get(i) != this.router; i++) ;
        i++;
        Router next = packet.getMemory().get(i);

        for (int index = i; index < packet.getMemory().size(); index++) {
            Router destination = packet.getMemory().get(index);
            this.routingTable.update(destination, next);
        }
    }

    /**
     * Send the packet to the next hop.
     *
     * @param packet the packet
     * @param nextHop the next hop
     */
    private void sendPacket(AntPacket packet, Router nextHop) {
        this.router.getNet().getCycleAccurateEventQueue().schedule(
                this,
                () -> this.routing.getAgents().get(nextHop).receiveAntPacket(packet),
                10
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
