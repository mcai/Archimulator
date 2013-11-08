/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.sim.os.event;

import archimulator.sim.os.Context;
import net.pickapack.io.buffer.CircularByteBuffer;

/**
 * Wait for file descriptor criterion.
 *
 * @author Min Cai
 */
public class WaitForFileDescriptorCriterion implements SystemEventCriterion {
    private CircularByteBuffer buffer;

    private int address;
    private int size;
    private int pufds;

    public boolean needProcess(Context context) {
        return !this.getBuffer().isEmpty();
    }

    /**
     * Get the buffer.
     *
     * @return the buffer
     */
    public CircularByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * Set the buffer.
     *
     * @param buffer the buffer
     */
    public void setBuffer(CircularByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Get the address.
     *
     * @return the address
     */
    public int getAddress() {
        return address;
    }

    /**
     * Set the address.
     *
     * @param address the address
     */
    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Get the size of the buffer.
     *
     * @return the size of the buffer
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the size of the buffer.
     *
     * @param size the size of the buffer
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Get the pufds.
     *
     * @return the pufds
     */
    public int getPufds() {
        return pufds;
    }

    /**
     * Set the pufds.
     *
     * @param pufds the pufds
     */
    public void setPufds(int pufds) {
        this.pufds = pufds;
    }
}
