/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.core.bpred;

import archimulator.core.Thread;
import archimulator.isa.Mnemonic;
import archimulator.isa.StaticInstructionType;
import archimulator.util.Reference;
import archimulator.util.math.SaturatingCounter;

/**
 * Two level branch predictor.
 *
 * @author Min Cai
 */
public class TwoLevelBranchPredictor extends DynamicBranchPredictor {
    private int l1Size;
    private int l2Size;
    private int shiftWidth;
    private boolean xor;
    private int[] shiftRegs;
    private SaturatingCounter[] l2Table;

    /**
     * Create a two level branch predictor.
     *
     * @param thread the thread
     * @param name the name
     * @param l1Size the L1 cache size
     * @param l2Size the L2 cache size
     * @param shiftWidth the shift width
     * @param xor the xor
     * @param branchTargetBufferNumSets the number of sets in the branch target buffer
     * @param branchTargetBufferAssociativity the associativity of the branch target buffer
     * @param returnAddressStackSize the size of the return address stack
     */
    public TwoLevelBranchPredictor(Thread thread, String name, int l1Size, int l2Size, int shiftWidth, boolean xor, int branchTargetBufferNumSets, int branchTargetBufferAssociativity, int returnAddressStackSize) {
        super(thread, name, BranchPredictorType.TWO_LEVEL, branchTargetBufferNumSets, branchTargetBufferAssociativity, returnAddressStackSize);

        this.l1Size = l1Size;
        this.l2Size = l2Size;
        this.shiftWidth = shiftWidth;
        this.xor = xor;

        this.shiftRegs = new int[this.l1Size];
        this.l2Table = new SaturatingCounter[this.l2Size];

        int flipFlop = 1;
        for (int cnt = 0; cnt < this.l2Size; cnt++) {
            this.l2Table[cnt] = new SaturatingCounter(0, 2, 3, flipFlop);
            flipFlop = 3 - flipFlop;
        }
    }

    /**
     * Create a two level branch predictor.
     *
     * @param thread the thread
     * @param name the name
     */
    public TwoLevelBranchPredictor(Thread thread, String name) {
        this(
                thread,
                name,
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorL1Size(),
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorL2Size(),
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorShiftWidth(),
                thread.getExperiment().getConfig().isTwoLevelBranchPredictorXor(),
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorBranchTargetBufferNumSets(),
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorBranchTargetBufferAssociativity(),
                thread.getExperiment().getConfig().getTwoLevelBranchPredictorReturnAddressStackSize()
        );
    }

    @Override
    public int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            branchPredictorUpdate.setCounterDir1(getIndex(branchAddress));
        }

        returnAddressStackRecoverIndex.set(this.getReturnAddressStack().getTopOfStack());

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_RETURN && this.getReturnAddressStack().getSize() > 0) {
            branchPredictorUpdate.setRas(true);
            return this.getReturnAddressStack().pop();
        }

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_CALL && this.getReturnAddressStack().getSize() > 0) {
            this.getReturnAddressStack().push(branchAddress);
        }

        BranchTargetBufferEntry branchTargetBufferEntry = this.getBranchTargetBuffer().lookup(branchAddress);

        if (mnemonic.getType() != StaticInstructionType.CONDITIONAL) {
            return branchTargetBufferEntry != null ? branchTargetBufferEntry.getTarget() : 1;
        }

        if (!branchPredictorUpdate.getCounterDir1().isTaken()) {
            return 0;
        }

        return branchTargetBufferEntry != null ? branchTargetBufferEntry.getTarget() : 1;
    }

    @Override
    public void update(int branchAddress, int branchTarget, boolean taken, boolean predictedTaken, boolean correct, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate) {
        super.update(branchAddress, branchTarget, taken, predictedTaken, correct, mnemonic, branchPredictorUpdate);

        if (mnemonic.getType() == StaticInstructionType.FUNCTION_RETURN) {
            if (!branchPredictorUpdate.isRas()) {
                return;
            }
        }

        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            this.updateTable(branchAddress, taken);
        }

        branchPredictorUpdate.getCounterDir1().update(taken);

        this.getBranchTargetBuffer().update(branchAddress, branchTarget, taken);
    }

    private int hash(int branchAddress) {
        int l1Index = (branchAddress >> BranchPredictor.BRANCH_SHIFT) & (this.l1Size - 1);
        int l2Index = this.shiftRegs[l1Index];

        if (this.xor) {
            l2Index = (l2Index ^ (branchAddress >> BranchPredictor.BRANCH_SHIFT)) & ((1 << this.shiftWidth) - 1) | ((branchAddress >> BranchPredictor.BRANCH_SHIFT) << this.shiftWidth);
        } else {
            l2Index |= (branchAddress >> BranchPredictor.BRANCH_SHIFT) << this.shiftWidth;
        }

        l2Index &= (this.l2Size - 1);

        return l2Index;
    }

    /**
     * Get the index for the specified branch address.
     *
     * @param branchAddress the branch address
     * @return the index for the specified branch address
     */
    public SaturatingCounter getIndex(int branchAddress) {
        return this.l2Table[this.hash(branchAddress)];
    }

    /**
     * Update the table.
     *
     * @param branchAddress the branch address
     * @param taken a value indicating whether it is taken or not
     */
    public void updateTable(int branchAddress, boolean taken) {
        int l1Index = (branchAddress >> BranchPredictor.BRANCH_SHIFT) & (this.l1Size - 1);
        int shiftReg = (this.shiftRegs[l1Index] << 1) | (taken ? 1 : 0);
        this.shiftRegs[l1Index] = shiftReg & ((1 << this.shiftWidth) - 1);
    }
}
