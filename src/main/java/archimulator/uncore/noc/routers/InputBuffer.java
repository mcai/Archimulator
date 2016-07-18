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

import java.util.ArrayList;
import java.util.List;

/**
 * Input buffer.
 *
 * @author Min Cai
 */
public class InputBuffer {
    private InputVirtualChannel inputVirtualChannel;

    private List<Flit> flits;

    /**
     * Create an input buffer.
     *
     * @param inputVirtualChannel the parent input virtual channel
     */
    public InputBuffer(InputVirtualChannel inputVirtualChannel) {
        this.inputVirtualChannel = inputVirtualChannel;
        this.flits = new ArrayList<>();
    }

    /**
     * Append the specified flit into the input buffer.
     *
     * @param flit the flit
     */
    public void append(Flit flit) {
        if(this.flits.size() + 1 >
                this.inputVirtualChannel.getInputPort().getRouter().getNode().getNetwork().getEnvironment().getConfig().getMaxInputBufferSize()) {
            throw new IllegalArgumentException();
        }

        this.flits.add(flit);
    }

    /**
     * Peek.
     *
     * @return the first flit in the input buffer
     */
    public Flit peek() {
        return !this.flits.isEmpty() ? this.flits.get(0) : null;
    }

    /**
     * Pop.
     *
     * Remove the first flit in the input buffer
     */
    public void pop() {
        if(!this.flits.isEmpty()) {
            this.flits.remove(0);
        }
    }

    /**
     * Get a boolean value indicating whether the input buffer is full or not.
     *
     * @return a boolean value indicating whether the input buffer is full or not
     */
    public boolean full() {
        return this.flits.size() >=
                this.inputVirtualChannel.getInputPort().getRouter().getNode().getNetwork().getEnvironment().getConfig().getMaxInputBufferSize();
    }

    /**
     * Get the size of the input buffer.
     *
     * @return the size of the input buffer
     */
    public int size() {
        return this.flits.size();
    }

    /**
     * Get the input virtual channel.
     *
     * @return the input virtual channel
     */
    public InputVirtualChannel getInputVirtualChannel() {
        return inputVirtualChannel;
    }
}
