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
package archimulator.uncore.net.node;

import archimulator.uncore.net.Net;

/**
 * Switch node.
 *
 * @author Min Cai
 */
public class SwitchNode extends NetNode {
    /**
     * Create a switch node.
     *
     * @param net           the parent net
     * @param name          the name of the switch node
     * @param numInPorts    the number of the in ports
     * @param inBufferSize  the size of the in buffer
     * @param numOutPorts   the number of the out ports
     * @param outBufferSize the size of the out buffer
     * @param bandwidth     the bandwidth of the switch node
     */
    public SwitchNode(Net net, String name, int numInPorts, int inBufferSize, int numOutPorts, int outBufferSize, int bandwidth) {
        super(net, name, numInPorts, inBufferSize, numOutPorts, outBufferSize, bandwidth);
    }
}
