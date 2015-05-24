/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore;

import archimulator.core.DynamicInstruction;
import archimulator.core.Thread;
import archimulator.util.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory hierarchy access.
 *
 * @author Min Cai
 */
public class MemoryHierarchyAccess {
    private long id;
    private DynamicInstruction dynamicInstruction;
    private Thread thread;
    private MemoryHierarchyAccessType type;

    private int virtualPc;
    private int physicalAddress;
    private int physicalTag;

    private Action onCompletedCallback;
    private List<MemoryHierarchyAccess> aliases;

    private long beginCycle;
    private long endCycle;

    /**
     * Create a memory hierarchy access.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param thread              the thread
     * @param type                the type of the memory hierarchy access
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param physicalAddress     the physical address of the data under access
     * @param physicalTag         the physical tag of the data under access
     * @param onCompletedCallback the callback action performed when the access is completed
     */
    public MemoryHierarchyAccess(DynamicInstruction dynamicInstruction, Thread thread, MemoryHierarchyAccessType type, int virtualPc, int physicalAddress, int physicalTag, Action onCompletedCallback) {
        this.id = thread.getSimulation().currentMemoryHierarchyAccessId++;

        this.dynamicInstruction = dynamicInstruction;

        this.thread = thread;
        this.type = type;
        this.virtualPc = virtualPc;
        this.physicalAddress = physicalAddress;
        this.physicalTag = physicalTag;

        this.onCompletedCallback = onCompletedCallback;

        this.aliases = new ArrayList<>();

        this.beginCycle = thread.getCycleAccurateEventQueue().getCurrentCycle();
    }

    /**
     * Act on when the access is completed.
     */
    public void complete() {
        this.endCycle = this.thread.getCycleAccurateEventQueue().getCurrentCycle();
        this.onCompletedCallback.apply();
        this.onCompletedCallback = null;
    }

    /**
     * Get the ID of the memory hierarchy access.
     *
     * @return the ID of the memory hierarchy access
     */
    public long getId() {
        return id;
    }

    /**
     * Get the dynamic instruction.
     *
     * @return the dynamic instruction
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    /**
     * Get the thread.
     *
     * @return the thread
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Get the memory hierarchy access type.
     *
     * @return the memory hierarchy access type
     */
    public MemoryHierarchyAccessType getType() {
        return type;
    }

    /**
     * Get the virtual address of the program counter (PC).
     *
     * @return the virtual address of the program counter (PC)
     */
    public int getVirtualPc() {
        return virtualPc;
    }

    /**
     * Get the physical address of the data under access.
     *
     * @return the physical address of the data under access
     */
    public int getPhysicalAddress() {
        return physicalAddress;
    }

    /**
     * Get the physical tag of the data under access.
     *
     * @return the physical tag of the data under access
     */
    public int getPhysicalTag() {
        return physicalTag;
    }

    /**
     * Get the list of aliases.
     *
     * @return the list of aliases
     */
    public List<MemoryHierarchyAccess> getAliases() {
        return aliases;
    }

    /**
     * Get the time in cycles when the access begins.
     *
     * @return the time in cycles when the access begins
     */
    public long getBeginCycle() {
        return beginCycle;
    }

    /**
     * Get the time in cycles when the access ends.
     *
     * @return the time in cycles when the access ends
     */
    public long getEndCycle() {
        return endCycle;
    }

    /**
     * Get the time in cycles spent servicing the access.
     *
     * @return the time in cycles spent servicing the access
     */
    public int getCycles() {
        return (int) (endCycle - beginCycle);
    }

    @Override
    public String toString() {
//        return String.format("%d %s %s @ {virtualPc=0x%08x, physicalAddress=0x%08x, physicalTag=0x%08x, dynamicInstruction=%s}", id, thread.getName(), type, virtualPc, physicalAddress, physicalTag, dynamicInstruction);
//        return String.format("%s 0x%08x %s {id=%d, physicalTag=0x%08x}", thread.getName(), virtualPc, type, id, physicalTag);
        return String.format("%s {%d} 0x%08x: 0x%08x %s", thread.getName(), id, virtualPc, physicalTag, type);
    }
}
