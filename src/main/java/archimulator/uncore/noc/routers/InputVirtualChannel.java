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

/**
 * Input virtual channel.
 *
 * @author Min Cai
 */
public class InputVirtualChannel {
    private InputPort inputPort;
    private int id;
    private InputBuffer inputBuffer;
    private Direction route;
    private OutputVirtualChannel outputVirtualChannel;

    /**
     * Create an input virtual channel.
     *
     * @param inputPort the parent input port
     * @param id the input virtual channel ID
     */
    public InputVirtualChannel(InputPort inputPort, int id) {
        this.inputPort = inputPort;

        this.id = id;

        this.inputBuffer = new InputBuffer(this);

        this.route = null;

        this.outputVirtualChannel = null;
    }

    /**
     * Get the input port.
     *
     * @return the input port
     */
    public InputPort getInputPort() {
        return inputPort;
    }

    /**
     * Get the ID.
     *
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the input buffer.
     *
     * @return the input buffer
     */
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    /**
     * Get the route.
     *
     * @return the router
     */
    public Direction getRoute() {
        return route;
    }

    /**
     * Set the route.
     *
     * @param route the route
     */
    public void setRoute(Direction route) {
        this.route = route;
    }

    /**
     * Get the output virtual channel.
     *
     * @return the output virtual channel
     */
    public OutputVirtualChannel getOutputVirtualChannel() {
        return outputVirtualChannel;
    }

    /**
     * Set the output virtual channel.
     *
     * @param outputVirtualChannel the output virtual channel
     */
    public void setOutputVirtualChannel(OutputVirtualChannel outputVirtualChannel) {
        this.outputVirtualChannel = outputVirtualChannel;
    }
}
