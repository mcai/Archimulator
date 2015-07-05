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

/**
 * Abstract route computation component.
 *
 * @author Min Cai
 */
public abstract class AbstractRouteComputation {
    private Router router;

    /**
     * Create an abstract route computation component.
     *
     * @param router the router
     */
    public AbstractRouteComputation(Router router) {
        this.router = router;
    }

    /**
     * The route calculation (RC) stage. Calculate the permissible routes for every first flit in each ivc.
     */
    public abstract void stageRouteCalculation();

    /**
     * Get the corresponding output port for the specified input port and input VC.
     *
     * @param inputPort the input port
     * @param ivc the input virtual channel
     * @return the corresponding output port for the specified input port and input VC
     */
    public abstract Port getRoute(Port inputPort, int ivc);

    /**
     * Get the parent router.
     *
     * @return the parent router
     */
    public Router getRouter() {
        return router;
    }
}
