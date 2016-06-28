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
package archimulator.uncore.net.simple.node;

import archimulator.uncore.net.simple.Crossbar;
import archimulator.uncore.net.simple.SimpleNet;
import archimulator.uncore.net.simple.port.InPort;
import archimulator.uncore.net.simple.port.NetPort;
import archimulator.uncore.net.simple.port.OutPort;

import java.util.ArrayList;
import java.util.List;

/**
 * Net node.
 *
 * @author Min Cai
 */
public abstract class NetNode {
    private SimpleNet net;
    private String name;

    private List<InPort> inPorts;
    private List<OutPort> outPorts;
    private Crossbar crossbar;

    /**
     * Create a net node.
     *
     * @param net           the parent net
     * @param name          the name of the net node
     * @param numInPorts    the number of in ports
     * @param inBufferSize  the size of the in buffer
     * @param numOutPorts   the number of out ports
     * @param outBufferSize the size of the out buffer
     * @param bandwidth     the bandwidth of the net node
     */
    public NetNode(SimpleNet net, String name, int numInPorts, int inBufferSize, int numOutPorts, int outBufferSize, int bandwidth) {
        this.net = net;
        this.name = name;

        this.inPorts = new ArrayList<>();
        for (int i = 0; i < numInPorts; i++) {
            this.inPorts.add(new InPort(this, i, inBufferSize));
        }

        this.outPorts = new ArrayList<>();
        for (int i = 0; i < numOutPorts; i++) {
            this.outPorts.add(new OutPort(this, i, outBufferSize));
        }

        this.crossbar = new Crossbar(this, bandwidth);
    }

    /**
     * Find a free in port.
     *
     * @return a free in port if any exists; otherwise null
     */
    public InPort findFreeInPort() {
        return this.findFreePort(this.inPorts);
    }

    /**
     * Find a free out port.
     *
     * @return a free out port if any exists; otherwise null
     */
    public OutPort findFreeOutPort() {
        return this.findFreePort(this.outPorts);
    }

    /**
     * Find a free port among the specified list of ports.
     *
     * @param <NetPortT> port type
     * @param ports      the list of ports to be searched
     * @return a free port found among the specified list of ports if any exists; otherwise null
     */
    protected <NetPortT extends NetPort> NetPortT findFreePort(List<NetPortT> ports) {
        for (NetPortT port : ports) {
            if (port.getLink() == null) {
                return port;
            }
        }

        return null;
    }

    /**
     * Get the out port for the specified destination node.
     *
     * @param nodeTo the destination node
     * @return the out port for the specified destination node
     */
    public OutPort getPort(NetNode nodeTo) {
        return this.getNet().getRoutingAlgorithm().getRoute(this, nodeTo).getOutPort();
    }

    /**
     * Get the parent net.
     *
     * @return the parent net
     */
    public SimpleNet getNet() {
        return net;
    }

    /**
     * Get the name of the net node.
     *
     * @return the name of the net node
     */
    public String getName() {
        return name;
    }

    /**
     * Get the list of the out ports.
     *
     * @return the list of the out ports
     */
    public List<OutPort> getOutPorts() {
        return outPorts;
    }

    /**
     * Get the list of the in ports.
     *
     * @return the list of the in ports
     */
    public List<InPort> getInPorts() {
        return inPorts;
    }

    /**
     * Get the cross bar.
     *
     * @return the cross bar
     */
    public Crossbar getCrossbar() {
        return crossbar;
    }
}
