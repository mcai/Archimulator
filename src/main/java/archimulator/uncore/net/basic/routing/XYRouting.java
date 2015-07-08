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

import archimulator.uncore.net.basic.Flit;
import archimulator.uncore.net.basic.Port;
import archimulator.uncore.net.basic.Router;

/**
 * XY routing.
 *
 * @author Min Cai
 */
public class XYRouting implements Routing {
    /**
     * Get the output port for the specified router and flit.
     *
     * @param router the router
     * @param flit   the flit
     * @return the output port for the specified router and flit
     */
    @Override
    public Port getOutputPort(Router router, Flit flit) {
        int sourceX = router.getX();
        int sourceY = router.getY();

        int destinationX = flit.getDestination().getX();
        int destinationY = flit.getDestination().getY();

        if (sourceX > destinationX) {
            return Port.LEFT;
        } else if (sourceX < destinationX) {
            return Port.RIGHT;
        } else if (sourceY > destinationY) {
            return Port.UP;
        } else if (sourceY < destinationY) {
            return Port.DOWN;
        }

        throw new IllegalArgumentException();
    }
}
