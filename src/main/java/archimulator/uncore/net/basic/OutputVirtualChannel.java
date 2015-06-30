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
 * Output virtual channel.
 *
 * @author Min Cai
 */
public class OutputVirtualChannel {
    private Direction port;
    private int num;

    private boolean available;

    private List<Flit> outputBuffer;

    /**
     * Create an output virtual channel.
     *
     * @param port the port
     * @param num the number
     */
    public OutputVirtualChannel(Direction port, int num) {
        this.port = port;
        this.num = num;

        this.available = true;

        this.outputBuffer = new ArrayList<>();
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
     * Get the number.
     *
     * @return the number
     */
    public int getNum() {
        return num;
    }

    /**
     * Get a boolean value indicating whether it is available or not.
     *
     * @return a boolean value indicating whether it is available or not
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Set a boolean value indicating whether it is available or not.
     *
     * @param available a boolean value indicating whether it is available or not
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Get the output buffer.
     *
     * @return the output buffer
     */
    public List<Flit> getOutputBuffer() {
        return outputBuffer;
    }
}
