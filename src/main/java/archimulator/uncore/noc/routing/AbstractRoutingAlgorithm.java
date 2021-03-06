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
package archimulator.uncore.noc.routing;

import archimulator.uncore.noc.Node;

/**
 * Abstract routing algorithm.
 *
 * @author Min Cai
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {
    private Node node;

    /**
     * Create an abstract routing algorithm for the specified node.
     *
     * @param node the parent node
     */
    public AbstractRoutingAlgorithm(Node node) {
        this.node = node;
    }

    /**
     * Get the parent node.
     *
     * @return the parent node
     */
    public Node getNode() {
        return node;
    }
}
