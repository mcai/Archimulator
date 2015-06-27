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

import java.util.ArrayList;
import java.util.List;

/**
 * Port.
 *
 * @author Min Cai
 */
public class Port {
    private Router router;
    private Direction direction;
    private List<VirtualChannel> virtualChannels;

    /**
     * Create a port.
     *
     * @param router the router
     * @param direction the direction
     */
    public Port(Router router, Direction direction) {
        this.router = router;
        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for(int i = 0; i < this.router.getNet().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new VirtualChannel(this, i));
        }
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
     * Get the direction.
     *
     * @return the direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Get the list of virtual channels.
     *
     * @return the list of virtual channels
     */
    public List<VirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }
}
