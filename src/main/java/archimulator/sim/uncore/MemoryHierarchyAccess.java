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

import archimulator.sim.base.simulation.Simulation;
import archimulator.sim.core.DynamicInstruction;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

public class MemoryHierarchyAccess {
    private long id;
    private DynamicInstruction dynamicInst;
    private MemoryHierarchyThread thread;
    private MemoryHierarchyAccessType type;

    private int virtualPc;
    private int physicalAddress;
    private int physicalTag;

    private Action onCompletedCallback;
    private List<MemoryHierarchyAccess> aliases;

    private long beginCycle;
    private long endCycle;

    public MemoryHierarchyAccess(DynamicInstruction dynamicInst, MemoryHierarchyThread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback, long beginCycle) {
        this.id = Simulation.currentMemoryHierarchyAccessId++;

        this.dynamicInst = dynamicInst;

        this.thread = thread;
        this.type = type;
        this.virtualPc = virtualPc;
        this.physicalAddress = physicalAddress;
        this.physicalTag = physicalTag;

        this.onCompletedCallback = onCompletedCallback;

        this.aliases = new ArrayList<MemoryHierarchyAccess>();

        this.beginCycle = beginCycle;
    }

    public void complete(long currentCycle) {
        this.endCycle = currentCycle;
        this.onCompletedCallback.apply();
        this.onCompletedCallback = null;
    }

    public long getId() {
        return id;
    }

    public DynamicInstruction getDynamicInst() {
        return dynamicInst;
    }

    public MemoryHierarchyThread getThread() {
        return thread;
    }

    public MemoryHierarchyAccessType getType() {
        return type;
    }

    public int getVirtualPc() {
        return virtualPc;
    }

    public int getPhysicalAddress() {
        return physicalAddress;
    }

    public int getPhysicalTag() {
        return physicalTag;
    }

    public List<MemoryHierarchyAccess> getAliases() {
        return aliases;
    }

    public long getBeginCycle() {
        return beginCycle;
    }

    public long getEndCycle() {
        return endCycle;
    }

    public int getCycles() {
        return (int) (endCycle - beginCycle);
    }

    @Override
    public String toString() {
//        return String.format("%d %s %s @ {virtualPc=0x%08x, physicalAddress=0x%08x, physicalTag=0x%08x, dynamicInst=%s}", id, thread.getName(), type, virtualPc, physicalAddress, physicalTag, dynamicInst);
//        return String.format("%s 0x%08x %s {id=%d, physicalTag=0x%08x}", thread.getName(), virtualPc, type, id, physicalTag);
        return String.format("%s 0x%08x: 0x%08x %s", thread.getName(), virtualPc, physicalTag, type);
    }
}
