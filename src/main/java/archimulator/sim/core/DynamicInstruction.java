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
package archimulator.sim.core;

import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.StaticInstructionType;
import org.apache.commons.lang.StringUtils;

/**
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

    private boolean missedInL2Cache;
    private int cyclesSpentAtHeadOfReorderBuffer;

    /**
     *
     * @param thread
     * @param pc
     * @param staticInstruction
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

        this.missedInL2Cache = false;
        this.cyclesSpentAtHeadOfReorderBuffer = 0;
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public Thread getThread() {
        return thread;
    }

    /**
     *
     * @return
     */
    public int getPc() {
        return pc;
    }

    /**
     *
     * @return
     */
    public StaticInstruction getStaticInstruction() {
        return staticInstruction;
    }

    /**
     *
     * @return
     */
    public int getEffectiveAddress() {
        return effectiveAddress;
    }

    /**
     *
     * @return
     */
    public int getEffectiveAddressBase() {
        return effectiveAddressBase;
    }

    /**
     *
     * @return
     */
    public int getEffectiveAddressDisplacement() {
        return effectiveAddressDisplacement;
    }

    /**
     *
     * @return
     */
    public boolean isUseStackPointerAsEffectiveAddressBase() {
        return useStackPointerAsEffectiveAddressBase;
    }

    /**
     *
     * @return
     */
    public boolean isMissedInL2Cache() {
        return missedInL2Cache;
    }

    //TODO: set value
    /**
     *
     * @param missedInL2Cache
     */
    public void setMissedInL2Cache(boolean missedInL2Cache) {
        this.missedInL2Cache = missedInL2Cache;
    }

    /**
     *
     * @return
     */
    public int getCyclesSpentAtHeadOfReorderBuffer() {
        return cyclesSpentAtHeadOfReorderBuffer;
    }

    /**
     *
     * @param cyclesSpentAtHeadOfReorderBuffer
     */
    public void setCyclesSpentAtHeadOfReorderBuffer(int cyclesSpentAtHeadOfReorderBuffer) {
        this.cyclesSpentAtHeadOfReorderBuffer = cyclesSpentAtHeadOfReorderBuffer;
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
