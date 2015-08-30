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

import archimulator.isa.StaticInstruction;
import archimulator.isa.StaticInstructionType;
import org.apache.commons.lang.StringUtils;

/**
 * Dynamic instruction.
 *
 * @author Min Cai
 */
public class DynamicInstruction extends MemoryHierarchyDynamicInstruction {
    private StaticInstruction staticInstruction;

    private int effectiveAddressBase;
    private int effectiveAddressDisplacement;

    private boolean useStackPointerAsEffectiveAddressBase;

    private int numCyclesSpentAtHeadOfReorderBuffer;

    /**
     * Create a dynamic instruction.
     *
     * @param thread            the thread
     * @param pc                the value of the program counter (PC)
     * @param staticInstruction the static instruction
     */
    public DynamicInstruction(Thread thread, int pc, StaticInstruction staticInstruction) {
        super(thread, pc, -1);

        this.staticInstruction = staticInstruction;

        if (this.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.LOAD || this.getStaticInstruction().getMnemonic().getType() == StaticInstructionType.STORE) {
            this.effectiveAddress = StaticInstruction.getEffectiveAddress(this.getThread().getContext(), this.staticInstruction.getMachineInstruction());
            this.effectiveAddressBase = StaticInstruction.getEffectiveAddressBase(this.getThread().getContext(), this.staticInstruction.getMachineInstruction());
            this.effectiveAddressDisplacement = StaticInstruction.getEffectiveAddressDisplacement(this.staticInstruction.getMachineInstruction());

            this.useStackPointerAsEffectiveAddressBase = StaticInstruction.useStackPointerAsEffectiveAddressBase(this.staticInstruction.getMachineInstruction());
        }

        this.numCyclesSpentAtHeadOfReorderBuffer = 0;
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
                getId(), getThread().getName(), getPc(), staticInstruction.getMnemonic(),
                StringUtils.join(staticInstruction.getInputDependencies(), ", "), StringUtils.join(staticInstruction.getOutputDependencies(), ", "),
                effectiveAddress, effectiveAddressBase, effectiveAddressDisplacement, useStackPointerAsEffectiveAddressBase);
    }

}
