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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.sim.isa.memory.datastore;

import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.isa.memory.Memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class BasicMemoryDataStore extends BasicSimulationObject implements MemoryDataStore {
    private transient Map<Integer, ByteBuffer> bbs;
    private Memory memory;

    public BasicMemoryDataStore(Memory memory) {
        super(memory);
        this.memory = memory;

        this.bbs = new HashMap<Integer, ByteBuffer>();
    }

    public void create(int pageId) {
//        this.bbs.put(pageId, ByteBuffer.allocateDirect(Memory.getPageSize()).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
        this.bbs.put(pageId, ByteBuffer.allocate(Memory.getPageSize()).order(memory.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));
    }

    public void access(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        ByteBuffer bb = getByteBuffer(pageId);
        bb.position(displacement);

        if (write) {
            bb.put(buf, offset, size);
        } else {
            bb.get(buf, offset, size);
        }
    }

    private ByteBuffer getByteBuffer(int pageId) {
        return this.bbs.get(pageId);
    }
}
