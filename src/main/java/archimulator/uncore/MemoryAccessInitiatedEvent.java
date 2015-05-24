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

import archimulator.common.SimulationEvent;
import archimulator.core.Thread;

/**
 * The event fired when a memory access is initiated.
 *
 * @author Min Cai
 */
public class MemoryAccessInitiatedEvent extends SimulationEvent {
    private int threadId;
    private int virtualPc;
    private int physicalAddress;
    private int physicalTag;
    private MemoryHierarchyAccessType type;

    /**
     * Create an event when a memory access is initiated.
     *
     * @param thread          the thread
     * @param virtualPc       the virtual address of the program counter (PC)
     * @param physicalAddress the physical address of the data under access
     * @param physicalTag     the physical tag of the data under access
     * @param type            the memory hierarchy access type
     */
    public MemoryAccessInitiatedEvent(Thread thread, int virtualPc, int physicalAddress, int physicalTag, MemoryHierarchyAccessType type) {
        super(thread);
        this.threadId = thread.getId();
        this.physicalAddress = physicalAddress;
        this.virtualPc = virtualPc;
        this.physicalTag = physicalTag;
        this.type = type;
    }

    /**
     * Get the ID of the thread.
     *
     * @return the ID of the thread
     */
    public int getThreadId() {
        return threadId;
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
     * Get the memory hierarchy access type.
     *
     * @return the memory hierarchy access type
     */
    public MemoryHierarchyAccessType getType() {
        return type;
    }
}
