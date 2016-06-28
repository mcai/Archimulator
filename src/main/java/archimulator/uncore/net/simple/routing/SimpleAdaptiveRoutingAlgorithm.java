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
package archimulator.uncore.net.simple.routing;

import archimulator.uncore.net.Net;
import archimulator.uncore.net.simple.Route;
import archimulator.uncore.net.simple.node.NetNode;

/**
 * Simple adaptive routing algorithm.
 *
 * @author Min Cai
 */
public class SimpleAdaptiveRoutingAlgorithm implements RoutingAlgorithm {
    private Net net;

    /**
     * Create a simple adaptive routing algorithm.
     *
     * @param net the parent net
     */
    public SimpleAdaptiveRoutingAlgorithm(Net net) {
        this.net = net;
    }

    @Override
    public Route getRoute(NetNode nodeFrom, NetNode nodeTo) {
        //TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Get the parent net.
     * @return parent net
     */
    public Net getNet() {
        return net;
    }
}
