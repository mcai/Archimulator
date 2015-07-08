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
package archimulator.uncore.net.basic.routing;

import archimulator.uncore.net.basic.BasicNet;
import archimulator.uncore.net.basic.Flit;
import archimulator.uncore.net.basic.Port;
import archimulator.uncore.net.basic.Router;

import java.util.HashMap;
import java.util.Map;

/**
 * Shortest path first (SPF) routing.
 *
 * @author Min Cai
 */
//TODO: should be instantiated per net other than per router.
public class ShortestPathFirstRouting implements Routing {
    private BasicNet net;
    private Map<Router, Map<Router, Route>> routes;
    private boolean initialized;

    /**
     * Create a shortest path first (SPF) routing.
     *
     * @param net the parent net
     */
    public ShortestPathFirstRouting(BasicNet net) {
        this.net = net;
        this.routes = new HashMap<>();
    }

    /**
     * Calculate the routes.
     */
    private void calculateRoutes() {
        Map<Route, Router> routes = new HashMap<>();

        /* Initialize table with infinite costs */
        for (Router source : this.net.getRouters()) {
            for (Router destination : this.net.getRouters()) {
                Route route = new Route(source, destination);
                route.setCost(source == destination ? 0 : this.net.getRouters().size());
                getRoutes(source).put(destination, route);
            }
        }

        /* Set 1-jump connections */
        for (Router source : this.net.getRouters()) {
            source.getLinks().keySet().stream().forEach(outPort -> {
                Router destination = source.getLinks().get(outPort);
                Route route = getRoutes(source).get(destination);
                route.setCost(1);
                routes.put(route, destination);
            });
        }

        /* Calculate shortest paths Floyd-Warshall algorithm.*/
        for (Router k : this.net.getRouters()) {
            for (Router i : this.net.getRouters()) {
                for (Router j : this.net.getRouters()) {
                    Route routeIk = getRoutes(i).get(k);
                    Route routeKj = getRoutes(k).get(j);
                    Route routeIj = getRoutes(i).get(j);

                    if (routeIk.getCost() + routeKj.getCost() < routeIj.getCost()) {
                        routeIj.setCost(routeIk.getCost() + routeKj.getCost());
                        routes.put(routeIj, k);
                    }
                }
            }
        }

        /* Calculate net port values */
        for (Router i : this.net.getRouters()) {
            for (Router j : this.net.getRouters()) {
                Route route = getRoutes(i).get(j);

                Router next = routes.get(route);
                if (next == null) {
                    route.setOutputPort(null);
                    continue;
                }

                while (getRoutes(i).get(next).getCost() > 1) {
                    next = routes.get(getRoutes(i).get(next));
                }

                for (Port port : Port.values()) {
                    if (i.getLinks().containsKey(port) && i.getLinks().get(port) == next) {
                        route.setOutputPort(port);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Get the routes for the specified router.
     *
     * @return the routes for the specified router
     */
    private Map<Router, Route> getRoutes(Router node) {
        if (!routes.containsKey(node)) {
            routes.put(node, new HashMap<>());
        }

        return routes.get(node);
    }

    /**
     * Get the output port for the specified router and flit.
     *
     * @param router the router
     * @param flit the flit
     * @return the output port for the specified router and flit
     */
    @Override
    public Port getOutputPort(Router router, Flit flit) {
        if (!this.initialized) {
            this.calculateRoutes();
            this.initialized = true;
        }

        return this.routes.get(router).get(flit.getDestination()).getOutputPort();
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
     * Route.
     */
    private class Route {
        private Router source;
        private Router destination;
        private int cost;
        private Port outputPort;

        /**
         * Create a route.
         *
         * @param source the source router
         * @param destination the destination router
         */
        public Route(Router source, Router destination) {
            this.source = source;
            this.destination = destination;
        }

        /**
         * Get the source router.
         *
         * @return the source router
         */
        public Router getSource() {
            return source;
        }

        /**
         * Get the destination router.
         *
         * @return the destination router
         */
        public Router getDestination() {
            return destination;
        }

        /**
         * Get the cost.
         *
         * @return the cost
         */
        public int getCost() {
            return cost;
        }

        /**
         * Set the cost.
         *
         * @param cost the cost
         */
        public void setCost(int cost) {
            this.cost = cost;
        }

        /**
         * Get the output port.
         *
         * @return the output port
         */
        public Port getOutputPort() {
            return outputPort;
        }

        /**
         * Set the output port.
         *
         * @param outputPort the output port
         */
        public void setOutputPort(Port outputPort) {
            this.outputPort = outputPort;
        }
    }
}
