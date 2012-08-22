/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.uncore.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NetNode {
    private Net net;
    private String name;

    private List<InPort> inPorts;
    private List<OutPort> outPorts;
    private Crossbar crossbar;

    private Map<NetNode, RoutingEntry> routingEntries;

    public NetNode(Net net, String name, int numInPorts, int inBufferSize, int numOutPorts, int outBufferSize, int bandwidth) {
        this.net = net;
        this.name = name;

        this.inPorts = new ArrayList<InPort>();
        for (int i = 0; i < numInPorts; i++) {
            this.inPorts.add(new InPort(this, i, inBufferSize));
        }

        this.outPorts = new ArrayList<OutPort>();
        for (int i = 0; i < numOutPorts; i++) {
            this.outPorts.add(new OutPort(this, i, outBufferSize));
        }

        this.crossbar = new Crossbar(this, bandwidth);

        this.routingEntries = new HashMap<NetNode, RoutingEntry>();
    }

    public InPort findFreeInPort() {
        return this.findFreePort(this.inPorts);
    }

    public OutPort findFreeOutPort() {
        return this.findFreePort(this.outPorts);
    }

    protected <NetPortT extends NetPort> NetPortT findFreePort(List<NetPortT> ports) {
        for (NetPortT port : ports) {
            if (port.getLink() == null) {
                return port;
            }
        }

        return null;
    }

    public OutPort getPort(NetNode destinationNode) {
        return this.routingEntries.get(destinationNode).getOutPort();
    }

    public Net getNet() {
        return net;
    }

    public String getName() {
        return name;
    }

    public List<OutPort> getOutPorts() {
        return outPorts;
    }

    public List<InPort> getInPorts() {
        return inPorts;
    }

    public Crossbar getCrossbar() {
        return crossbar;
    }

    public Map<NetNode, RoutingEntry> getRoutingEntries() {
        return routingEntries;
    }
}
