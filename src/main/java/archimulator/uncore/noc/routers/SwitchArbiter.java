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

import archimulator.uncore.noc.util.RoundRobinArbiter;

import java.util.ArrayList;
import java.util.List;

/**
 * Switch arbiter.
 *
 * @author Min Cai
 */
public class SwitchArbiter extends RoundRobinArbiter<OutputPort, InputVirtualChannel> {
    private List<InputVirtualChannel> inputVirtualChannels;

    /**
     * Create a switch arbiter.
     *
     * @param outputPort the output port
     */
    public SwitchArbiter(OutputPort outputPort) {
        super(outputPort);
    }

    /**
     * Get the list of input virtual channels.
     *
     * @return the list of input virtual channels
     */
    @Override
    protected List<InputVirtualChannel> getRequesters() {
        if(inputVirtualChannels == null) {
            inputVirtualChannels = new ArrayList<>();

            for(InputPort inputPort : this.getResource().getRouter().getInputPorts().values()) {
                inputVirtualChannels.addAll(inputPort.getVirtualChannels());
            }
        }

        return inputVirtualChannels;
    }

    /**
     * Get a boolean value indicating whether the output port is available or not.
     *
     * @param outputPort the output port
     * @return a boolean value indicating whether the output port is available or not
     */
    @Override
    protected boolean resourceAvailable(OutputPort outputPort) {
        return true;
    }

    /**
     * Get a boolean value indicating whether the input virtual channel has flits to be processed or not.
     *
     * @param inputVirtualChannel the input virtual channel
     * @return a boolean value indicating whether the input virtual channel has flits to be processed or not
     */
    @Override
    protected boolean requesterHasRequests(InputVirtualChannel inputVirtualChannel) {
        if(inputVirtualChannel.getOutputVirtualChannel() != null
                && inputVirtualChannel.getOutputVirtualChannel().getOutputPort() == this.getResource()) {
            Flit flit = inputVirtualChannel.getInputBuffer().peek();
            return flit != null
                    && (flit.isHead() && flit.getState() == FlitState.VIRTUAL_CHANNEL_ALLOCATION
                    || !flit.isHead() && flit.getState() == FlitState.INPUT_BUFFER);
        }

        return false;
    }
}
