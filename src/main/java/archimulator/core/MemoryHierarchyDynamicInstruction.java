/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.core;

/**
 * Memory hierarchy dynamic instruction.
 *
 * @author Min Cai
 */
public class MemoryHierarchyDynamicInstruction {
    private long id;
    private Thread thread;
    private int pc;

    protected int effectiveAddress;

    private boolean missedInL2;

    /**
     * Create a memory hierarchy dynamic instruction.
     *
     * @param thread            the thread
     * @param pc                the value of the program counter (PC)
     * @param effectiveAddress  the effective address
     */
    public MemoryHierarchyDynamicInstruction(Thread thread, int pc, int effectiveAddress) {
        this.id = thread.getSimulation().currentDynamicInstructionId++;
        this.thread = thread;
        this.pc = pc;
        this.effectiveAddress = effectiveAddress;

        this.missedInL2 = false;
    }

    /**
     * Get the ID of the dynamic instruction.
     *
     * @return the ID of the dynamic instruction
     */
    public long getId() {
        return id;
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
     * Get the value of the program counter (PC).
     *
     * @return the value of the program counter (PC)
     */
    public int getPc() {
        return pc;
    }

    /**
     * Get the effective address.
     *
     * @return the effective address
     */
    public int getEffectiveAddress() {
        return effectiveAddress;
    }

    /**
     * Get a value indicating whether the dynamic instruction has caused an L2 cache miss or not.
     *
     * @return a value indicating whether the dynamic instruction has caused an L2 cache miss or not
     */
    public boolean isMissedInL2() {
        return missedInL2;
    }

    /**
     * Set a value indicating whether the dynamic instruction has caused an L2 cache miss or not.
     *
     * @param missedInL2 a value indicating whether the dynamic instruction has caused an L2 cache miss or not
     */
    public void setMissedInL2(boolean missedInL2) {
        this.missedInL2 = missedInL2;
    }

    @Override
    public String toString() {
        return String.format("MemoryHierarchyDynamicInstruction{id=%d, thread.name=%s, pc=0x%08x, effectiveAddress=0x%08x}", id, thread.getName(), pc, effectiveAddress);
    }
}
