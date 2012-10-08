/*******************************************************************************
 * Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).
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
 * MERCHANpTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.os.event;

import archimulator.sim.os.Context;
import net.pickapack.io.buffer.CircularByteBuffer;

/**
 *
 * @author Min Cai
 */
public class WaitFileDescriptorCriterion implements SystemEventCriterion {
    private CircularByteBuffer buffer;

    private int address;
    private int size;
    private int pufds;

    /**
     *
     * @param context
     * @return
     */
    public boolean needProcess(Context context) {
        return !this.getBuffer().isEmpty();
    }

    /**
     *
     * @return
     */
    public CircularByteBuffer getBuffer() {
        return buffer;
    }

    /**
     *
     * @param buffer
     */
    public void setBuffer(CircularByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     *
     * @return
     */
    public int getAddress() {
        return address;
    }

    /**
     *
     * @param address
     */
    public void setAddress(int address) {
        this.address = address;
    }

    /**
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     *
     * @return
     */
    public int getPufds() {
        return pufds;
    }

    /**
     *
     * @param pufds
     */
    public void setPufds(int pufds) {
        this.pufds = pufds;
    }
}
