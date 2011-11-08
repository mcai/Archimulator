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

public class TwoBitBranchPredictor extends DynamicBranchPredictor {
    private int size;
    private SaturatingCounter[] table;

    public TwoBitBranchPredictor(Thread thread, String name, int bimodSize, int btbSets, int btbAssoc, int retStackSize) {
        super(thread, name, BranchPredictorType.TWO_BIT, btbSets, btbAssoc, retStackSize);

        this.size = bimodSize;

        this.table = new SaturatingCounter[this.size];

        int flipflop = 1;
        for (int i = 0; i < this.size; i++) {
            this.table[i] = new SaturatingCounter(0, 2, 3, flipflop);
            flipflop = 3 - flipflop;
        }
    }

    public TwoBitBranchPredictor(Thread thread, String name, TwoBitBranchPredictorConfig config) {
        this(thread, name, config.getBimodSize(), config.getBtbSets(), config.getBtbAssoc(), config.getRetStackSize());
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

        if (mnemonic.getType() != StaticInstructionType.CONDITIONAL || dirUpdate.getCounterDir1().isTaken()) {
            BranchTargetBufferEntry branchTargetBufferEntry = this.getBranchTargetBuffer().lookup(baddr);
            return branchTargetBufferEntry != null ? branchTargetBufferEntry.getTarget() : 1;
        } else {
            return 0;
        }
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
            dirUpdate.getCounterDir1().update(taken);
        }

        this.getBranchTargetBuffer().update(baddr, btarget, taken);
    }

    public SaturatingCounter getIndex(int baddr) {
        return this.table[this.hash(baddr)];
    }

    private int hash(int baddr) {
        return (baddr >> 19) ^ (baddr >> BranchPredictor.BRANCH_SHIFT) & (this.size - 1);
    }
}
