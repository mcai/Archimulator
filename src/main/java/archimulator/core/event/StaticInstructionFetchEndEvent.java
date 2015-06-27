/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core.event;

import archimulator.common.SimulationEvent;
import archimulator.common.SimulationObject;

/**
 * Static instruction fetch end event.
 *
 * @author Min Cai
 */
public class StaticInstructionFetchEndEvent extends SimulationEvent {
    private long currentCycle;
    private int threadId;
    private int pc;

    /**
     * Create a static instruction fetch end event.
     *
     * @param sender the sender simulation object
     * @param currentCycle t the current cycle
     * @param threadId the thread ID
     * @param pc the PC address
     */
    public StaticInstructionFetchEndEvent(SimulationObject sender, long currentCycle, int threadId, int pc) {
        super(sender);
        this.currentCycle = currentCycle;
        this.threadId = threadId;
        this.pc = pc;
    }

    public long getCurrentCycle() {
        return currentCycle;
    }

    public int getThreadId() {
        return threadId;
    }

    public int getPc() {
        return pc;
    }

    @Override
    public String toString() {
        return String.format("StaticInstructionFetchEndEvent{currentCycle=%d, threadId=%d, pc=0x%08x}", currentCycle, threadId, pc);
    }
}
