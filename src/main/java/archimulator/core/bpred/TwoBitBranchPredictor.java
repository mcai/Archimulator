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
package archimulator.core.bpred;

import archimulator.core.Thread;
import archimulator.isa.Mnemonic;
import archimulator.isa.StaticInstructionType;
import archimulator.util.Reference;
import archimulator.util.math.SaturatingCounter;

/**
 * Two bit branch predictor.
 *
 * @author Min Cai
 */
public class TwoBitBranchPredictor extends DynamicBranchPredictor {
    private int size;
    private SaturatingCounter[] table;

    /**
     * Create a two bit branch predictor.
     *
     * @param thread the thread
     * @param name the name
     * @param bimodSize the bimod size
     * @param branchTargetBufferNumSets the number of sets in the branch target buffer
     * @param branchTargetBufferAssociativity the associativity of the branch target buffer
     * @param returnAddressStackSize the size of the return address stack
     */
    public TwoBitBranchPredictor(Thread thread, String name, int bimodSize, int branchTargetBufferNumSets, int branchTargetBufferAssociativity, int returnAddressStackSize) {
        super(thread, name, BranchPredictorType.TWO_BIT, branchTargetBufferNumSets, branchTargetBufferAssociativity, returnAddressStackSize);

        this.size = bimodSize;

        this.table = new SaturatingCounter[this.size];

        int flipFlop = 1;
        for (int i = 0; i < this.size; i++) {
            this.table[i] = new SaturatingCounter(0, 2, 3, flipFlop);
            flipFlop = 3 - flipFlop;
        }
    }

    /**
     * Create a two bit branch predictor.
     *
     * @param thread the thread
     * @param name the name
     */
    public TwoBitBranchPredictor(Thread thread, String name) {
        this(thread, name, thread.getExperiment().getTwoBitBranchPredictorBimodSize(), thread.getExperiment().getTwoBitBranchPredictorBranchTargetBufferNumSets(), thread.getExperiment().getTwoBitBranchPredictorBranchTargetBufferAssociativity(), thread.getExperiment().getTwoBitBranchPredictorReturnAddressStackSize());
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

        if (mnemonic.getType() != StaticInstructionType.CONDITIONAL || branchPredictorUpdate.getCounterDir1().isTaken()) {
            BranchTargetBufferEntry branchTargetBufferEntry = this.getBranchTargetBuffer().lookup(branchAddress);
            return branchTargetBufferEntry != null ? branchTargetBufferEntry.getTarget() : 1;
        } else {
            return 0;
        }
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
            branchPredictorUpdate.getCounterDir1().update(taken);
        }

        this.getBranchTargetBuffer().update(branchAddress, branchTarget, taken);
    }

    /**
     * Get the index of the specified branch address.
     *
     * @param branchAddress the branch address
     * @return the index of the specified branch address
     */
    public SaturatingCounter getIndex(int branchAddress) {
        return this.table[this.hash(branchAddress)];
    }

    private int hash(int branchAddress) {
        return (branchAddress >> 19) ^ (branchAddress >> BranchPredictor.BRANCH_SHIFT) & (this.size - 1);
    }
}
