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
package archimulator.core;

import archimulator.isa.StaticInstruction;
import archimulator.isa.StaticInstructionType;
import org.apache.commons.lang.StringUtils;

/**
 * Dynamic instruction.
 *
 * @author Min Cai
 */
public class DynamicInstruction {
    private long id;
    private Thread thread;
    private int pc;
    private StaticInstruction staticInstruction;

    private int effectiveAddress;
    private int effectiveAddressBase;
    private int effectiveAddressDisplacement;

    private boolean useStackPointerAsEffectiveAddressBase;

    private boolean missedInL2;
    private int numCyclesSpentAtHeadOfReorderBuffer;

    /**
     * Create a dynamic instruction.
     *
     * @param thread            the thread
     * @param pc                the value of the program counter (PC)
     * @param staticInstruction the static instruction
     */
    public DynamicInstruction(Thread thread, int pc, StaticInstruction staticInstruction) {
        this.id = thread.getSimulation().currentDynamicInstructionId++;
        this.thread = thread;
        this.pc = pc;
        this.staticInstruction = staticInstruction;

        if (this.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD || this.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.STORE) {
            this.effectiveAddress = StaticInstruction.getEffectiveAddress(this.thread.getContext(), this.staticInstruction.getMachineInstruction());
            this.effectiveAddressBase = StaticInstruction.getEffectiveAddressBase(this.thread.getContext(), this.staticInstruction.getMachineInstruction());
            this.effectiveAddressDisplacement = StaticInstruction.getEffectiveAddressDisplacement(this.staticInstruction.getMachineInstruction());

            this.useStackPointerAsEffectiveAddressBase = StaticInstruction.useStackPointerAsEffectiveAddressBase(this.staticInstruction.getMachineInstruction());
        }

        this.missedInL2 = false;
        this.numCyclesSpentAtHeadOfReorderBuffer = 0;
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
     * Get the static instruction.
     *
     * @return the static instruction
     */
    public StaticInstruction getStaticInstruction() {
        return staticInstruction;
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
     * Get the effective address base.
     *
     * @return the effective address base
     */
    public int getEffectiveAddressBase() {
        return effectiveAddressBase;
    }

    /**
     * Get the effective address displacement.
     *
     * @return the effective address displacement
     */
    public int getEffectiveAddressDisplacement() {
        return effectiveAddressDisplacement;
    }

    /**
     * Get a value indicating whether using the stack pointer as the effective address base or not.
     *
     * @return a value indicating whether using the stack pointer as the effective address base or not
     */
    public boolean isUseStackPointerAsEffectiveAddressBase() {
        return useStackPointerAsEffectiveAddressBase;
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

    /**
     * Get the number of cycles the dynamic instruction has spent at the head of the reorder buffer.
     *
     * @return the number of cycles the dynamic instruction has spent at the head of the reorder buffer
     */
    public int getNumCyclesSpentAtHeadOfReorderBuffer() {
        return numCyclesSpentAtHeadOfReorderBuffer;
    }

    /**
     * Set the number of cycles the dynamic instruction has spent at the head of the reorder buffer.
     *
     * @param numCyclesSpentAtHeadOfReorderBuffer
     *         the number of cycles the dynamic instruction has spent at the head of the reorder buffer
     */
    public void setNumCyclesSpentAtHeadOfReorderBuffer(int numCyclesSpentAtHeadOfReorderBuffer) {
        this.numCyclesSpentAtHeadOfReorderBuffer = numCyclesSpentAtHeadOfReorderBuffer;
    }

    @Override
    public String toString() {
        return String.format(
                "DynamicInstruction{id=%d, thread.name=%s, pc=0x%08x, mnemonic=%s, ideps={%s}, odeps={%s}, " +
                        "effectiveAddress=0x%08x, effectiveAddressBase=0x%08x, effectiveAddressDisplacement=0x%08x, useStackPointerAsEffectiveAddressBase=%s}",
                id, thread.getName(), pc, staticInstruction.getMnemonic(),
                StringUtils.join(staticInstruction.getInputDependencies(), ", "), StringUtils.join(staticInstruction.getOutputDependencies(), ", "),
                effectiveAddress, effectiveAddressBase, effectiveAddressDisplacement, useStackPointerAsEffectiveAddressBase);
    }

}
