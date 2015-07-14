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

import archimulator.uncore.net.basic.BasicNet;
import archimulator.uncore.net.basic.Flit;
import archimulator.uncore.net.basic.Port;
import archimulator.uncore.net.basic.Router;
import archimulator.uncore.net.basic.routing.Routing;

import java.util.HashMap;
import java.util.Map;

/**
 * Ant colony optimization (ACO) based routing.
 *
 * @author Min Cai
 */
public class ACORouting implements Routing {
    private BasicNet net;

    private Map<Router, AntNetAgent> agents;

    /**
     * Current ant ID.
     */
    public long currentAntId = 0;

    /**
     * Create an ant colony optimization (ACO) based routing.
     *
     * @param net the parent net
     */
    public ACORouting(BasicNet net) {
        this.net = net;

        this.agents = new HashMap<>();

        for(Router router : this.net.getRouters()) {
            this.agents.put(router, new AntNetAgent(router, this));
        }
    }

    @Override
    public Port getOutputPort(Router router, Flit flit) {
        Router neighbor = this.agents.get(router).getRoutingTable().calculateNeighbor(flit.getDestination(), router);
        for(Port port : router.getLinks().keySet()) {
            if(router.getLinks().get(port) == neighbor) {
                return port;
            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Get the parent net.
     *
     * @return the parent net
     */
    public BasicNet getNet() {
        return net;
    }

    /**
     * Get the map of ant net agents.
     *
     * @return the map of ant net agents
     */
    public Map<Router, AntNetAgent> getAgents() {
        return agents;
    }
}
