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

import java.util.*;

/**
 * XY Route computation component.
 *
 * @author Min Cai
 */
public class XYRouteComputation extends AbstractRouteComputation {
    private EnumMap<Port, List<Port>> routes;

    /**
     * Create an XY route computation component.
     *
     * @param router the parent router
     */
    public XYRouteComputation(Router router) {
        super(router);

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
    }

    /**
     * The route calculation (RC) stage. Calculate the permissible routes for every first flit in each ivc.
     */
    @Override
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
                        if(this.getRouter().getX() > flit.getDestination().getX()) {
                            this.routes.get(inputPort).set(ivc, Port.LEFT);
                        }
                        else if(this.getRouter().getX() < flit.getDestination().getX()) {
                            this.routes.get(inputPort).set(ivc, Port.RIGHT);
                        }
                        else if(this.getRouter().getY() > flit.getDestination().getY()) {
                            this.routes.get(inputPort).set(ivc, Port.UP);
                        }
                        else if(this.getRouter().getY() < flit.getDestination().getY()) {
                            this.routes.get(inputPort).set(ivc, Port.DOWN);
                        }
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
    @Override
    public Port getRoute(Port inputPort, int ivc) {
        return this.routes.get(inputPort).get(ivc);
    }
}
