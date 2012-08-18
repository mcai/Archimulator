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

import archimulator.sim.common.Simulation;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.isa.StaticInstructionType;
import org.apache.commons.lang.StringUtils;

public class DynamicInstruction {
    private long id;
    private Thread thread;
    private int pc;
    private StaticInstruction staticInst;

    private int effectiveAddress;
    private int effectiveAddressBase;
    private int effectiveAddressDisplacement;

    private boolean useStackPointerAsEffectiveAddressBase;

    private boolean hasL2Miss;
    private int cyclesSpentAtHeadOfReorderBuffer;

    public DynamicInstruction(Thread thread, int pc, StaticInstruction staticInst) {
        this.id = Simulation.currentDynamicInstructionId++;
        this.thread = thread;
        this.pc = pc;
        this.staticInst = staticInst;

        if (this.getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD || this.getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) {
            this.effectiveAddress = StaticInstruction.getEffectiveAddress(this.thread.getContext(), this.staticInst.getMachInst());
            this.effectiveAddressBase = StaticInstruction.getEffectiveAddressBase(this.thread.getContext(), this.staticInst.getMachInst());
            this.effectiveAddressDisplacement = StaticInstruction.getEffectiveAddressDisplacement(this.staticInst.getMachInst());

            this.useStackPointerAsEffectiveAddressBase = StaticInstruction.useStackPointerAsEffectiveAddressBase(this.staticInst.getMachInst());
        }

        this.hasL2Miss = false;
        this.cyclesSpentAtHeadOfReorderBuffer = 0;
    }

    public long getId() {
        return id;
    }

    public Thread getThread() {
        return thread;
    }

    public int getPc() {
        return pc;
    }

    public StaticInstruction getStaticInst() {
        return staticInst;
    }

    public int getEffectiveAddress() {
        return effectiveAddress;
    }

    public int getEffectiveAddressBase() {
        return effectiveAddressBase;
    }

    public int getEffectiveAddressDisplacement() {
        return effectiveAddressDisplacement;
    }

    public boolean isUseStackPointerAsEffectiveAddressBase() {
        return useStackPointerAsEffectiveAddressBase;
    }

    public boolean isHasL2Miss() {
        return hasL2Miss;
    }

    public void setHasL2Miss(boolean hasL2Miss) {
        this.hasL2Miss = hasL2Miss;
    }

    public int getCyclesSpentAtHeadOfReorderBuffer() {
        return cyclesSpentAtHeadOfReorderBuffer;
    }

    public void setCyclesSpentAtHeadOfReorderBuffer(int cyclesSpentAtHeadOfReorderBuffer) {
        this.cyclesSpentAtHeadOfReorderBuffer = cyclesSpentAtHeadOfReorderBuffer;
    }

    @Override
    public String toString() {
        return String.format(
                "DynamicInstruction{id=%d, thread.name=%s, pc=0x%08x, mnemonic=%s, ideps={%s}, odeps={%s}, " +
                        "effectiveAddress=0x%08x, effectiveAddressBase=0x%08x, effectiveAddressDisplacement=0x%08x, useStackPointerAsEffectiveAddressBase=%s}",
                id, thread.getName(), pc, staticInst.getMnemonic(),
                StringUtils.join(staticInst.getIdeps(), ", "), StringUtils.join(staticInst.getOdeps(), ", "),
                effectiveAddress, effectiveAddressBase, effectiveAddressDisplacement, useStackPointerAsEffectiveAddressBase);
    }

}
