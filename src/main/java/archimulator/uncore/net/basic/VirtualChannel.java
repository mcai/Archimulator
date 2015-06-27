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

import org.apache.commons.collections.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual channel.
 *
 * @author Min Cai
 */
public class VirtualChannel {
    private int num;

    private Port port;

    private MultiKeyMap routes;

    private List<Flit> inputBuffer;
    private List<Flit> outputBuffer;

    private boolean available;

    private VirtualChannel outputVirtualChannel;

    private OutputPort fixedRoute;

    private int credit;

    /**
     * Create a virtual channel.
     *
     * @param port the port
     * @param num the number
     */
    public VirtualChannel(Port port, int num) {
        this.port = port;
        this.num = num;
        this.routes = new MultiKeyMap();

        this.inputBuffer = new ArrayList<>();
        this.outputBuffer = new ArrayList<>();

        this.available = true;

        this.credit = 10;
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
     * Get a boolean value indicating whether it is available or not.
     *
     * @return boolean value indicating whether is is available or not
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
     * Set the route.
     *
     * @param i the index
     * @param direction the direction
     * @param route the route
     */
    public void setRoute(int i, Direction direction, boolean route) {
        this.routes.put(i, direction, route);
    }

    /**
     * Clear the routes.
     */
    public void clearRoutes() {
        this.routes.clear();
    }

    /**
     * Get the route.
     *
     * @param i the index
     * @param direction the direction
     * @return the route
     */
    public boolean getRoute(int i, Direction direction) {
        return this.routes.containsKey(i, direction) && (boolean) this.routes.get(i, direction);
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
     * Get the output buffer.
     *
     * @return the output buffer
     */
    public List<Flit> getOutputBuffer() {
        return outputBuffer;
    }

    /**
     * Get the output virtual channel.
     *
     * @return the output virtual channel
     */
    public VirtualChannel getOutputVirtualChannel() {
        return outputVirtualChannel;
    }

    /**
     * Set the output virtual channel.
     *
     * @param outputVirtualChannel the output virtual channel
     */
    public void setOutputVirtualChannel(VirtualChannel outputVirtualChannel) {
        this.outputVirtualChannel = outputVirtualChannel;
    }

    /**
     * Get the fixed route.
     *
     * @return the fixed route
     */
    public OutputPort getFixedRoute() {
        return fixedRoute;
    }

    /**
     * Set the fixed route.
     *
     * @param fixedRoute the fixed route
     */
    public void setFixedRoute(OutputPort fixedRoute) {
        this.fixedRoute = fixedRoute;
    }

    /**
     * Get the credit.
     *
     * @return the credit
     */
    public int getCredit() {
        return credit;
    }

    /**
     * Set the credit.
     *
     * @param credit the credit
     */
    public void setCredit(int credit) {
        this.credit = credit;
    }
}
