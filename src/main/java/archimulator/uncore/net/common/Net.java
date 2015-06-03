/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
 *
 * This file is part of the Archimulator multicore architectural simulator.
 *
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.uncore.net.common;

import archimulator.common.BasicSimulationObject;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.MemoryHierarchy;
import archimulator.uncore.net.node.EndPointNode;
import archimulator.uncore.net.node.NetNode;
import archimulator.uncore.net.node.SwitchNode;
import archimulator.uncore.net.routing.BasicStaticRoutingAlgorithm;
import archimulator.uncore.net.routing.RoutingAlgorithm;
import archimulator.uncore.net.routing.RoutingAlgorithmType;
import archimulator.uncore.net.routing.SimpleAdaptiveRoutingAlgorithm;
import archimulator.util.action.Action;

import java.util.HashMap;
import java.util.Map;

/**
 * Net.
 *
 * @author Min Cai
 */
public abstract class Net extends BasicSimulationObject {
    private String name;
    private Map<MemoryDevice, EndPointNode> endPointNodes;
    private SwitchNode switchNode;
    private RoutingAlgorithm routingAlgorithm;

    /**
     * Create a net.
     *
     *  @param memoryHierarchy the parent memory hierarchy
     * @param name the name
     */
    public Net(MemoryHierarchy memoryHierarchy, String name) {
        super(memoryHierarchy);

        this.name = name;
        this.endPointNodes = new HashMap<>();

        this.setup(memoryHierarchy);

        switch (this.getRoutingAlgorithmType()) {
            case BASIC_STATIC:
                this.routingAlgorithm = new BasicStaticRoutingAlgorithm(this);
                break;
            case SIMPLE_ADAPTIVE:
                this.routingAlgorithm = new SimpleAdaptiveRoutingAlgorithm(this);
                break;
            default:
                throw new IllegalArgumentException(this.getRoutingAlgorithmType() + "");
        }
    }

    /**
     * Setup the net.
     *
     * @param memoryHierarchy the parent memory hierarchy
     */
    protected abstract void setup(MemoryHierarchy memoryHierarchy);

    /**
     * Create a bi-directional link between the specified two nodes.
     *
     * @param node1     the first node
     * @param node2     the second node
     * @param bandwidth the bandwidth of the bi-directional link
     */
    protected void createBidirectionalLink(NetNode node1, NetNode node2, int bandwidth) {
        new NetLink(node1, node2, bandwidth);
        new NetLink(node2, node1, bandwidth);
    }

    /**
     * Transfer a message of the specified size from the source device to the destination device.
     *
     * @param deviceFrom        the source device
     * @param deviceTo   the destination device
     * @param size                the size
     * @param onCompletedCallback the callback action performed when the transfer is completed
     */
    public void transfer(MemoryDevice deviceFrom, MemoryDevice deviceTo, int size, Action onCompletedCallback) {
        EndPointNode nodeFrom = this.endPointNodes.get(deviceFrom);
        EndPointNode nodeTo = this.endPointNodes.get(deviceTo);
        nodeFrom.getPort(nodeTo).toLink(new NetMessage(this, nodeFrom, nodeTo, size, onCompletedCallback));
    }

    /**
     * Get the name of the net.
     *
     * @return the name of the net
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the map of the destination devices to the end point nodes.
     *
     * @return the map of the destination devices to the end point nodes
     */
    public Map<MemoryDevice, EndPointNode> getEndPointNodes() {
        return endPointNodes;
    }

    /**
     * Get the switch node.
     *
     * @return the switch node
     */
    public SwitchNode getSwitchNode() {
        return switchNode;
    }

    /**
     * Get the routing algorithm.
     *
     * @return the routing algorithm
     */
    public RoutingAlgorithm getRoutingAlgorithm() {
        return routingAlgorithm;
    }

    /**
     * Get the routing algorithm type.
     *
     * @return the routing algorithm type
     */
    public RoutingAlgorithmType getRoutingAlgorithmType() {
        return getExperiment().getRoutingAlgorithmType();
    }

    /**
     * Set the switch node.
     *
     * @param switchNode the switch node
     */
    protected void setSwitchNode(SwitchNode switchNode) {
        this.switchNode = switchNode;
    }
}
