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
package archimulator.uncore.net.basic;

import archimulator.uncore.net.basic.routing.Routing;
import archimulator.uncore.net.basic.routing.ShortestPathFirstRouting;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Route computation component.
 *
 * @author Min Cai
 */
public class RouteComputation {
    private Router router;
    private EnumMap<Port, List<Port>> routes;

    private Routing routing;

    /**
     * Create a route computation component.
     *
     * @param router the router
     */
    public RouteComputation(Router router) {
        this.router = router;

        this.routes = new EnumMap<>(Port.class);
        this.routes.put(Port.LOCAL, new ArrayList<>());
        this.routes.put(Port.LEFT, new ArrayList<>());
        this.routes.put(Port.RIGHT, new ArrayList<>());
        this.routes.put(Port.UP, new ArrayList<>());
        this.routes.put(Port.DOWN, new ArrayList<>());

        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.getRouter().getNet().getNumVirtualChannels(); ivc++) {
                this.routes.get(inputPort).add(null);
            }
        }

//        this.routing = new XYRouting();
        this.routing = new ShortestPathFirstRouting(this.router.getNet());
    }

    /**
     * The route calculation (RC) stage. Calculate the permissible routes for every first flit in each ivc.
     */
    public void stageRouteCalculation() {
        for(Port inputPort : Port.values()) {
            for(int ivc = 0; ivc < this.getRouter().getNet().getNumVirtualChannels(); ivc++) {
                if(this.getRouter().getInputBuffers().get(inputPort).get(ivc).isEmpty()) {
                    continue;
                }

                Flit flit = this.getRouter().getInputBuffers().get(inputPort).get(ivc).get(0);
                if(flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER) {
                    this.routes.get(inputPort).set(ivc, null);
                    if(flit.getDestination() == this.getRouter()) {
                        this.routes.get(inputPort).set(ivc, Port.LOCAL);
                    }
                    else {
                        this.routes.get(inputPort).set(ivc, this.routing.getOutputPort(this.router, flit));
                    }
                    flit.setState(FlitState.ROUTE_CALCULATION);
                }
            }
        }
    }

    /**
     * Get the corresponding output port for the specified input port and input VC.
     *
     * @param inputPort the input port
     * @param ivc the input virtual channel
     * @return the corresponding output port for the specified input port and input VC
     */
    public Port getRoute(Port inputPort, int ivc) {
        return this.routes.get(inputPort).get(ivc);
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }
}
