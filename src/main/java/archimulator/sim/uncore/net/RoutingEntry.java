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

public class RoutingEntry {
    private NetNode srcNode;
    private NetNode destNode;
    private int cost;
    private OutPort outPort;

    public RoutingEntry(NetNode srcNode, NetNode destNode) {
        this.srcNode = srcNode;
        this.destNode = destNode;
    }

    public NetNode getSrcNode() {
        return srcNode;
    }

    public NetNode getDestNode() {
        return destNode;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public OutPort getOutPort() {
        return outPort;
    }

    public void setOutPort(OutPort outPort) {
        this.outPort = outPort;
    }
}
