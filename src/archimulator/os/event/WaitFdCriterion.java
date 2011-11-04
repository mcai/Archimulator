/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.os.event;

import archimulator.os.Context;
import archimulator.util.io.buffer.CircularByteBuffer;

public class WaitFdCriterion implements SystemEventCriterion {
    private CircularByteBuffer buffer;

    private int address;
    private int size;
    private int pufds;

    public boolean needProcess(Context context) {
        return !this.getBuffer().isEmpty();
    }

    public CircularByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(CircularByteBuffer buffer) {
        this.buffer = buffer;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPufds() {
        return pufds;
    }

    public void setPufds(int pufds) {
        this.pufds = pufds;
    }
}
