/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.noc.routers;

import archimulator.uncore.noc.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Output port.
 *
 * @author Min Cai
 */
public class OutputPort {
    private Router router;

    private Direction direction;

    private List<OutputVirtualChannel> virtualChannels;

    private SwitchArbiter arbiter;

    /**
     * Create an output port.
     *
     * @param router the parent router
     * @param direction the direction
     */
    public OutputPort(Router router, Direction direction) {
        this.router = router;

        this.direction = direction;

        this.virtualChannels = new ArrayList<>();

        for (int i = 0; i < this.router.getNode().getNetwork().getEnvironment().getConfig().getNumVirtualChannels(); i++) {
            this.virtualChannels.add(new OutputVirtualChannel(this, i));
        }

        this.arbiter = new SwitchArbiter(this);
    }

    /**
     * Get the parent router.
     *
     * @return the parent router
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
     * Get the list of output virtual channels.
     *
     * @return the list of output virtual channels
     */
    public List<OutputVirtualChannel> getVirtualChannels() {
        return virtualChannels;
    }

    /**
     * Get the switch arbiter.
     *
     * @return the switch arbiter
     */
    public SwitchArbiter getArbiter() {
        return arbiter;
    }
}
