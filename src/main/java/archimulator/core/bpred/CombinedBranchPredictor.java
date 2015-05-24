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
package archimulator.core.bpred;

import archimulator.core.Thread;
import archimulator.isa.Mnemonic;
import archimulator.isa.StaticInstructionType;
import archimulator.util.Reference;
import archimulator.util.math.SaturatingCounter;

/**
 * Combined branch predictor.
 *
 * @author Min Cai
 */
public class CombinedBranchPredictor extends DynamicBranchPredictor {
    private TwoBitBranchPredictor bimod;
    private TwoBitBranchPredictor meta;
    private TwoLevelBranchPredictor twoLevel;

    /**
     * Create a combined branch predictor.
     *
     * @param thread the thread
     * @param name   the name of the combined branch predictor
     */
    public CombinedBranchPredictor(Thread thread, String name) {
        super(
                thread,
                name,
                BranchPredictorType.COMBINED,
                thread.getExperiment().getCombinedBranchPredictorBranchTargetBufferNumSets(),
                thread.getExperiment().getCombinedBranchPredictorBranchTargetBufferAssociativity(),
                thread.getExperiment().getCombinedBranchPredictorReturnAddressStackSize()
        );

        this.bimod = new TwoBitBranchPredictor(
                thread,
                name + "/bimod",
                thread.getExperiment().getCombinedBranchPredictorBimodSize(),
                0,
                0,
                0
        );
        this.meta = new TwoBitBranchPredictor(
                thread,
                name + "/meta",
                thread.getExperiment().getCombinedBranchPredictorMetaSize(),
                0,
                0,
                0
        );
        this.twoLevel = new TwoLevelBranchPredictor(
                thread,
                name + "/twoLevel",
                thread.getExperiment().getCombinedBranchPredictorL1Size(),
                thread.getExperiment().getCombinedBranchPredictorL2Size(),
                thread.getExperiment().getCombinedBranchPredictorShiftWidth(),
                thread.getExperiment().getCombinedBranchPredictorXor(),
                0,
                0,
                0
        );
    }

    @Override
    public int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            SaturatingCounter counterBimod = this.bimod.getIndex(branchAddress);
            SaturatingCounter counterMeta = this.meta.getIndex(branchAddress);
            SaturatingCounter counterTwoLevel = this.twoLevel.getIndex(branchAddress);

            branchPredictorUpdate.setCounterMeta(counterMeta);
            branchPredictorUpdate.setMeta(counterMeta.isTaken());
            branchPredictorUpdate.setBimod(counterBimod.isTaken());
            branchPredictorUpdate.setTwoLevel(counterTwoLevel.isTaken());

            if (counterMeta.isTaken()) {
                branchPredictorUpdate.setCounterDir1(counterTwoLevel);
                branchPredictorUpdate.setCounterDir2(counterBimod);
            } else {
                branchPredictorUpdate.setCounterDir1(counterBimod);
                branchPredictorUpdate.setCounterDir2(counterTwoLevel);
            }
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

        this.twoLevel.updateTable(branchAddress, taken);

        branchPredictorUpdate.getCounterDir1().update(taken);
        branchPredictorUpdate.getCounterDir2().update(taken);

        if (branchPredictorUpdate.getCounterMeta() != null) {
            if (branchPredictorUpdate.isBimod() != branchPredictorUpdate.isTwoLevel()) {
                branchPredictorUpdate.getCounterMeta().update(branchPredictorUpdate.isTwoLevel() == taken);
            }
        }

        this.getBranchTargetBuffer().update(branchAddress, branchTarget, taken);
    }

    /**
     * Get the bimod branch predictor.
     *
     * @return the bimod branch predictor
     */
    public TwoBitBranchPredictor getBimod() {
        return bimod;
    }

    /**
     * Get the meta branch predictor.
     *
     * @return the meta branch predictor
     */
    public TwoBitBranchPredictor getMeta() {
        return meta;
    }

    /**
     * Get the two level branch predictor.
     *
     * @return the two level branch predictor
     */
    public TwoLevelBranchPredictor getTwoLevel() {
        return twoLevel;
    }
}
