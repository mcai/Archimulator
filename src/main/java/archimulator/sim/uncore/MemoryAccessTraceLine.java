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
package archimulator.sim.uncore;

public class MemoryAccessTraceLine {
    private int threadId;
    private int virtualPc;
    private int physicalAddress;
    private MemoryHierarchyAccessType type;

    private long beginCycle;
    private long endCycle;

    public MemoryAccessTraceLine(int threadId, int virtualPc, int physicalAddress, MemoryHierarchyAccessType type) {
        this.threadId = threadId;
        this.virtualPc = virtualPc;
        this.physicalAddress = physicalAddress;
        this.type = type;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getVirtualPc() {
        return virtualPc;
    }

    public int getPhysicalAddress() {
        return physicalAddress;
    }

    public MemoryHierarchyAccessType getType() {
        return type;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public void setBeginCycle(long beginCycle) {
        this.beginCycle = beginCycle;
    }

    public long getEndCycle() {
        return endCycle;
    }

    public void setEndCycle(long endCycle) {
        this.endCycle = endCycle;
    }

    public int getCycles() {
        return (int) (this.endCycle - this.beginCycle);
    }

    @Override
    public String toString() {
        return String.format("%d 0x%08x 0x%08x %s", threadId, virtualPc, physicalAddress, type);
    }
}
