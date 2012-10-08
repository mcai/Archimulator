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

import archimulator.sim.common.SimulationEvent;
import archimulator.sim.core.Thread;

/**
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
     *
     * @param thread
     * @param virtualPc
     * @param physicalAddress
     * @param physicalTag
     * @param type
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
     *
     * @return
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     *
     * @return
     */
    public int getVirtualPc() {
        return virtualPc;
    }

    /**
     *
     * @return
     */
    public int getPhysicalAddress() {
        return physicalAddress;
    }

    /**
     *
     * @return
     */
    public int getPhysicalTag() {
        return physicalTag;
    }

    /**
     *
     * @return
     */
    public MemoryHierarchyAccessType getType() {
        return type;
    }
}
