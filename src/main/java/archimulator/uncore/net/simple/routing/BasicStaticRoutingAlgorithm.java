/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.net.simple.routing;

import archimulator.uncore.net.Net;
import archimulator.uncore.net.simple.BasicRoute;
import archimulator.uncore.net.simple.SimpleNet;
import archimulator.uncore.net.simple.NetLink;
import archimulator.uncore.net.simple.Route;
import archimulator.uncore.net.simple.node.NetNode;
import archimulator.uncore.net.simple.port.OutPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic static routing algorithm.
 *
 * @author Min Cai
 */
public class BasicStaticRoutingAlgorithm implements RoutingAlgorithm {
    private SimpleNet net;
    private Map<NetNode, Map<NetNode, BasicRoute>> routes;

    /**
     * Create a basic static routing algorithm.
     *
     * @param net the parent net
     */
    public BasicStaticRoutingAlgorithm(SimpleNet net) {
        this.net = net;
        this.routes = new HashMap<>();

        this.calculateRoutes();
    }

    @Override
    public Route getRoute(NetNode nodeFrom, NetNode nodeTo) {
        return getRoutes(nodeFrom).get(nodeTo);
    }

    /**
     * Calculate the routes.
     */
    private void calculateRoutes() {
        Map<Route, NetNode> routes = new HashMap<>();

        List<NetNode> nodes = new ArrayList<>();
        nodes.add(this.net.getSwitchNode());
        nodes.addAll(this.net.getEndPointNodes().values());

        /* Initialize table with infinite costs */
        for (NetNode nodeFrom : nodes) {
            for (NetNode nodeTo : nodes) {
                BasicRoute route = new BasicRoute(nodeFrom, nodeTo);
                route.setCost(nodeFrom == nodeTo ? 0 : nodes.size());
                getRoutes(nodeFrom).put(nodeTo, route);
            }
        }

        /* Set 1-jump connections */
        for (NetNode node : nodes) {
            node.getOutPorts().stream().filter(outPort -> outPort.getLink() != null).forEach(outPort -> {
                NetLink link = outPort.getLink();
                BasicRoute route = getRoutes(node).get(link.getPortTo().getNode());
                route.setCost(1);
                routes.put(route, link.getPortTo().getNode());
            });
        }

        /* Calculate shortest paths Floyd-Warshall algorithm.*/
        for (NetNode k : nodes) {
            for (NetNode i : nodes) {
                for (NetNode j : nodes) {
                    BasicRoute routeIk = getRoutes(i).get(k);
                    BasicRoute routeKj = getRoutes(k).get(j);
                    BasicRoute routeIj = getRoutes(i).get(j);

                    if (routeIk.getCost() + routeKj.getCost() < routeIj.getCost()) {
                        routeIj.setCost(routeIk.getCost() + routeKj.getCost());
                        routes.put(routeIj, k);
                    }
                }
            }
        }

        /* Calculate net port values */
        for (NetNode nodeI : nodes) {
            for (NetNode nodeJ : nodes) {
                BasicRoute route = getRoutes(nodeI).get(nodeJ);

                NetNode nodeNext = routes.get(route);
                if (nodeNext == null) {
                    route.setOutPort(null);
                    continue;
                }

                while (getRoutes(nodeI).get(nodeNext).getCost() > 1) {
                    nodeNext = routes.get(getRoutes(nodeI).get(nodeNext));
                }

                for (OutPort port : nodeI.getOutPorts()) {
                    NetLink link = port.getLink();
                    if (link != null && link.getPortTo().getNode() == nodeNext) {
                        route.setOutPort(port);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the routes for the specified node.
     *
     * @return the routes for the specified node
     */
    private Map<NetNode, BasicRoute> getRoutes(NetNode node) {
        if(!routes.containsKey(node)) {
            routes.put(node, new HashMap<>());
        }

        return routes.get(node);
    }

    /**
     * Get the net.
     *
     * @return the net
     */
    public Net getNet() {
        return net;
    }
}
