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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package archimulator.isa.memory;

import archimulator.isa.memory.bigMemory.MemoryDataStore;
import archimulator.isa.memory.bigMemory.BigMemoryDataStore;
import archimulator.os.Kernel;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public class ProfiledBigMemory extends Memory {
    private transient Map<Integer, ByteBuffer> bbs;
    private MemoryDataStore dataStore;

    public ProfiledBigMemory(Kernel kernel, String simulationDirectory, boolean littleEndian, int processId) {
        super(kernel, simulationDirectory, littleEndian, processId);

        this.bbs = new HashMap<Integer, ByteBuffer>();

        this.dataStore = new BigMemoryDataStore(this);
    }

    @Override
    protected void onPageCreated(int id) {
        this.bbs.put(id, ByteBuffer.allocateDirect(getPageSize()).order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN));

        this.dataStore.create(id);
    }

    @Override
    protected void doPageAccess(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        if(write) {
            ByteBuffer bb = this.bbs.get(pageId);
            bb.position(displacement);
            bb.put(buf, offset, size);

            this.dataStore.access(pageId, displacement, buf, offset, size, write);
        }
        else {
            byte[] buf2 = buf.clone();

            ByteBuffer bb = this.bbs.get(pageId);
            bb.position(displacement);
            bb.get(buf2, offset, size);

            if(pageId == 1513) {
                System.out.println();
            }

            this.dataStore.access(pageId, displacement, buf, offset, size, write);

            for(int i = offset; i < offset + size; i++) {
                if(buf[i] != buf2[i]) {
                this.dataStore.access(pageId, displacement, buf, offset, size, write);
                    throw new IllegalArgumentException();
                }
            }

//            if(!Arrays.equals(buf, buf2)) {
//                this.dataStore.access(pageId, displacement, buf, offset, size, write);
//                throw new IllegalArgumentException();
//            }
        }

//        ByteBuffer bb = this.bbs.get(pageId);
//        bb.position(displacement);
//
//        if (write) {
//            bb.put(buf, offset, size);
//        } else {
//            bb.get(buf, offset, size);
//        }
//
//        this.dataStore.access(pageId, displacement, buf, offset, size, write);
    }
}
