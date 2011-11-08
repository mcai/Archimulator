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
package archimulator.isa.memory;

import archimulator.isa.memory.bigMemory.MemoryDataStore;
import archimulator.os.Kernel;

public abstract class AbstractMemory extends Memory {
    public AbstractMemory(Kernel kernel, String simulationDirectory, boolean littleEndian, int processId) {
        super(kernel, simulationDirectory, littleEndian, processId);
    }

    @Override
    protected void onPageCreated(int id) {
        this.getDataStore().create(id);
    }

    @Override
    protected void doPageAccess(int pageId, int displacement, byte[] buf, int offset, int size, boolean write) {
        this.getDataStore().access(pageId, displacement, buf, offset, size, write);
    }

    protected abstract MemoryDataStore getDataStore();
}
