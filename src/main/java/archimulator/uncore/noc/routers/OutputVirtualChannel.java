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

/**
 * Output virtual channel.
 *
 * @author Min Cai
 */
public class OutputVirtualChannel {
    private OutputPort outputPort;
    private int id;
    private InputVirtualChannel inputVirtualChannel;
    private int credits;
    private VirtualChannelArbiter arbiter;

    /**
     * Create an output virtual channel.
     *
     * @param outputPort the parent output port
     * @param id the output virtual channel ID
     */
    public OutputVirtualChannel(OutputPort outputPort, int id) {
        this.outputPort = outputPort;

        this.id = id;

        this.inputVirtualChannel = null;

        this.credits = 10;

        this.arbiter = new VirtualChannelArbiter(this);
    }

    /**
     * Get the parent output port.
     *
     * @return the parent output port
     */
    public OutputPort getOutputPort() {
        return outputPort;
    }

    /**
     * Get the output virtual channel ID.
     *
     * @return the output virtual channel ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the connected input virtual channel.
     * @return the connected input virtual channel
     */
    public InputVirtualChannel getInputVirtualChannel() {
        return inputVirtualChannel;
    }

    /**
     * Set the connected input virtual channel.
     *
     * @param inputVirtualChannel the connected input virtual channel
     */
    public void setInputVirtualChannel(InputVirtualChannel inputVirtualChannel) {
        this.inputVirtualChannel = inputVirtualChannel;
    }

    /**
     * Get the number of credits.
     *
     * @return the number of credits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Set the number of credits.
     *
     * @param credits the number of credits
     */
    public void setCredits(int credits) {
        this.credits = credits;
    }

    /**
     * Get the virtual channel arbiter.
     *
     * @return the virtual channel arbiter
     */
    public VirtualChannelArbiter getArbiter() {
        return arbiter;
    }
}
