/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core.bpred;

import archimulator.core.Thread;
import archimulator.isa.Mnemonic;
import archimulator.isa.StaticInstructionType;
import archimulator.util.Reference;
import archimulator.util.math.SaturatingCounter;

public class TwoLevelBranchPredictor extends DynamicBranchPredictor {
    private int l1Size;
    private int l2Size;
    private int shiftWidth;
    private boolean xor;
    private int[] shiftRegs;
    private SaturatingCounter[] l2Table;

    public TwoLevelBranchPredictor(Thread thread, String name, int l1Size, int l2Size, int shiftWidth, boolean xor, int btbSets, int btbAssoc, int retStackSize) {
        super(thread, name, BranchPredictorType.TWO_LEVEL, btbSets, btbAssoc, retStackSize);

        this.l1Size = l1Size;
        this.l2Size = l2Size;
        this.shiftWidth = shiftWidth;
        this.xor = xor;

        this.shiftRegs = new int[this.l1Size];
        this.l2Table = new SaturatingCounter[this.l2Size];

        int flipflop = 1;
        for (int cnt = 0; cnt < this.l2Size; cnt++) {
            this.l2Table[cnt] = new SaturatingCounter(0, 2, 3, flipflop);
            flipflop = 3 - flipflop;
        }
    }

    public TwoLevelBranchPredictor(Thread thread, String name, TwoLevelBranchPredictorConfig config) {
        this(thread, name, config.getL1Size(), config.getL2Size(), config.getShiftWidth(), config.isXor(), config.getBtbSets(), config.getBtbAssoc(), config.getRetStackSize());
    }

    @Override
    public int predict(int baddr, int btarget, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            dirUpdate.setCounterDir1(getIndex(baddr));
        }

        returnAddressStackRecoverIndex.set(this.getReturnAddressStack().getTopOfStack());

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_RETURN && this.getReturnAddressStack().getSize() > 0) {
            dirUpdate.setRas(true);
            return this.getReturnAddressStack().pop();
        }

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_CALL && this.getReturnAddressStack().getSize() > 0) {
            this.getReturnAddressStack().push(baddr);
        }

        BranchTargetBufferEntry btbEntry = this.getBranchTargetBuffer().lookup(baddr);

        if (mnemonic.getType() != StaticInstructionType.CONDITIONAL) {
            return btbEntry != null ? btbEntry.getTarget() : 1;
        }

        if (!dirUpdate.getCounterDir1().isTaken()) {
            return 0;
        }

        return btbEntry != null ? btbEntry.getTarget() : 1;
    }

    @Override
    public void update(int baddr, int btarget, boolean taken, boolean predTaken, boolean correct, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate) {
        super.update(baddr, btarget, taken, predTaken, correct, mnemonic, dirUpdate);

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_RETURN) {
            if (!dirUpdate.isRas()) {
                return;
            }
        }

        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            this.updateTable(baddr, taken);
        }

        dirUpdate.getCounterDir1().update(taken);

        this.getBranchTargetBuffer().update(baddr, btarget, taken);
    }

    private int hash(int baddr) {
        int l1Index = (baddr >> BranchPredictor.BRANCH_SHIFT) & (this.l1Size - 1);
        int l2Index = this.shiftRegs[l1Index];

        if (this.xor) {
            l2Index = (l2Index ^ (baddr >> BranchPredictor.BRANCH_SHIFT)) & ((1 << this.shiftWidth) - 1) | ((baddr >> BranchPredictor.BRANCH_SHIFT) << this.shiftWidth);
        } else {
            l2Index |= (baddr >> BranchPredictor.BRANCH_SHIFT) << this.shiftWidth;
        }

        l2Index &= (this.l2Size - 1);

        return l2Index;
    }

    public SaturatingCounter getIndex(int baddr) {
        return this.l2Table[this.hash(baddr)];
    }

    public void updateTable(int baddr, boolean taken) {
        int l1Index = (baddr >> BranchPredictor.BRANCH_SHIFT) & (this.l1Size - 1);
        int shiftReg = (this.shiftRegs[l1Index] << 1) | (taken ? 1 : 0);
        this.shiftRegs[l1Index] = shiftReg & ((1 << this.shiftWidth) - 1);
    }
}
