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
 * Input virtual channel.
 *
 * @author Min Cai
 */
public class InputVirtualChannel {
    private Port port;
    private int num;

    private Map<Integer, Set<Port>> routes;

    private List<Flit> inputBuffer;

    private Port outputPort;
    private int outputVirtualChannel;

    /**
     * Create an input virtual channel.
     *
     * @param port the port
     * @param num the number
     */
    public InputVirtualChannel(Port port, int num) {
        this.port = port;
        this.num = num;

        this.routes = new HashMap<>();
        this.routes.put(0, new HashSet<>());
        this.routes.put(1, new HashSet<>());

        this.inputBuffer = new ArrayList<>();

        this.outputPort = null;
        this.outputVirtualChannel = -1;
    }

    /**
     * Get the port.
     *
     * @return the port
     */
    public Port getPort() {
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
     * Set the route.
     *
     * @param i the index
     * @param port the direction
     * @param route the route
     */
    public void setRoute(int i, Port port, boolean route) {
        if(route) {
            this.routes.get(i).add(port);
        }
        else {
            this.routes.get(i).remove(port);
        }
    }

    /**
     * Clear the routes.
     */
    public void clearRoutes() {
        this.routes.get(0).clear();
        this.routes.get(1).clear();
    }

    /**
     * Get the route.
     *
     * @param i the index
     * @param port the direction
     * @return the route
     */
    public boolean getRoute(int i, Port port) {
        return this.routes.get(i).contains(port);
    }

    /**
     * Get the input buffer.
     *
     * @return the input buffer
     */
    public List<Flit> getInputBuffer() {
        return inputBuffer;
    }

    /**
     * Get the output port.
     *
     * @return the output port
     */
    public Port getOutputPort() {
        return outputPort;
    }

    /**
     * Set the output port.
     *
     * @param outputPort the output port
     */
    public void setOutputPort(Port outputPort) {
        this.outputPort = outputPort;
    }

    /**
     * Get the output virtual channel number.
     *
     * @return the output virtual channel number
     */
    public int getOutputVirtualChannel() {
        return outputVirtualChannel;
    }

    /**
     * Set the output virtual channel number.
     *
     * @param outputVirtualChannel the output virtual channel number
     */
    public void setOutputVirtualChannel(int outputVirtualChannel) {
        this.outputVirtualChannel = outputVirtualChannel;
    }
}
