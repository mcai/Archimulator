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

import archimulator.uncore.net.basic.Router;

/**
 * Memory.
 *
 * @author Min Cai
 */
public class Memory {
    private Router router;
    private double tripTime;

    /**
     * Create a memory.
     *
     * @param router   the router
     * @param tripTime the trip time
     */
    public Memory(Router router, double tripTime) {
        this.router = router;
        this.tripTime = tripTime;
    }

    /**
     * Get the router.
     *
     * @return the router
     */
    public Router getRouter() {
        return router;
    }

    /**
     * Set the router.
     *
     * @param router the router
     */
    public void setRouter(Router router) {
        this.router = router;
    }

    /**
     * Get the trip time.
     *
     * @return the trip time
     */
    public double getTripTime() {
        return tripTime;
    }

    /**
     * Set the trip time.
     *
     * @param tripTime the trip time
     */
    public void setTripTime(double tripTime) {
        this.tripTime = tripTime;
    }
}
