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
 * Output port.
 *
 * @author Min Cai
 */
public class OutputPort extends Port {
    private boolean switchAvailable;
    private boolean linkAvailable;

    /**
     * Create an output port.
     *
     * @param router the router
     * @param direction the direction
     */
    public OutputPort(Router router, Direction direction) {
        super(router, direction);

        this.switchAvailable = true;
        this.linkAvailable = true;
    }

    /**
     * Get a boolean value indicating whether the switch is available or not.
     *
     * @return a boolean value indicating whether the switch is available or not
     */
    public boolean isSwitchAvailable() {
        return switchAvailable;
    }

    /**
     * Set a boolean value indicating whether the switch is available or not.
     *
     * @param switchAvailable a boolean value indicating whether the switch is available or not
     */
    public void setSwitchAvailable(boolean switchAvailable) {
        this.switchAvailable = switchAvailable;
    }

    /**
     * Get a boolean value indicating whether the link is available or not.
     *
     * @return a boolean value indicating whether the link is available or not
     */
    public boolean isLinkAvailable() {
        return linkAvailable;
    }

    /**
     * Set a boolean value indicating whether the link is available or not.
     *
     * @param linkAvailable a boolean value indicating whether the link is available or not
     */
    public void setLinkAvailable(boolean linkAvailable) {
        this.linkAvailable = linkAvailable;
    }
}
