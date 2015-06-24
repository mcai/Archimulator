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
package archimulator.uncore.net.simple.port;

import archimulator.uncore.net.simple.common.NetLink;
import archimulator.uncore.net.simple.node.NetNode;

/**
 * Net port.
 *
 * @author Min Cai
 */
public abstract class NetPort {
    private NetNode node;
    private NetLink link;

    /**
     * Create a net port.
     *
     * @param node the node
     */
    public NetPort(NetNode node) {
        this.node = node;
    }

    /**
     * Get the node.
     *
     * @return the node
     */
    public NetNode getNode() {
        return node;
    }

    /**
     * Get the link.
     *
     * @return the link
     */
    public NetLink getLink() {
        return link;
    }

    /**
     * Set the link.
     *
     * @param link the link
     */
    public void setLink(NetLink link) {
        this.link = link;
    }
}
