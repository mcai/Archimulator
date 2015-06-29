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
 * Credit.
 *
 * @author Min Cai
 */
public class Credit {
    private Direction port;
    private int virtualChannel;
    private boolean ready;

    /**
     * Create a credit.
     *
     * @param port the port
     * @param virtualChannel the virtual channel
     */
    public Credit(Direction port, int virtualChannel) {
        this.port = port;
        this.virtualChannel = virtualChannel;
    }

    /**
     * Get the port.
     *
     * @return the port
     */
    public Direction getPort() {
        return port;
    }

    /**
     * Get the virtual channel.
     *
     * @return the virtual channel
     */
    public int getVirtualChannel() {
        return virtualChannel;
    }

    /**
     * Get a boolean value indicating whether it is ready or not.
     *
     * @return a boolean value indicating whether it is ready or not
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Set a boolean value indicating whether it is ready or not.
     *
     * @param ready a boolean value indicating whether it is ready or not
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
