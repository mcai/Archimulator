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
import archimulator.util.math.SaturatingCounter;
import archimulator.util.Reference;

public class CombinedBranchPredictor extends DynamicBranchPredictor {
    private TwoBitBranchPredictor bimod;
    private TwoBitBranchPredictor meta;
    private TwoLevelBranchPredictor twoLevel;

    public CombinedBranchPredictor(Thread thread, String name, CombinedBranchPredictorConfig config) {
        super(thread, name, BranchPredictorType.COMBINED, config.getBtbSets(), config.getBtbAssoc(), config.getRetStackSize());

        this.bimod = new TwoBitBranchPredictor(thread, name + ".twoBit", config.getBimodSize(), 0, 0, 0);
        this.meta = new TwoBitBranchPredictor(thread, name + ".meta", config.getMetaSize(), 0, 0, 0);
        this.twoLevel = new TwoLevelBranchPredictor(thread, name + ".twoLevel", config.getL1Size(), config.getL2Size(), config.getShiftWidth(), config.isXor(), 0, 0, 0);
    }

    @Override
    public int predict(int baddr, int btarget, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        if (mnemonic.getType() == StaticInstructionType.CONDITIONAL) {
            SaturatingCounter counterBimod = this.bimod.getIndex(baddr);
            SaturatingCounter counterMeta = this.meta.getIndex(baddr);
            SaturatingCounter counterTwoLevel = this.twoLevel.getIndex(baddr);

            dirUpdate.setCounterMeta(counterMeta);
            dirUpdate.setMeta(counterMeta.isTaken());
            dirUpdate.setBimod(counterBimod.isTaken());
            dirUpdate.setTwoLevel(counterTwoLevel.isTaken());

            if (counterMeta.isTaken()) {
                dirUpdate.setCounterDir1(counterTwoLevel);
                dirUpdate.setCounterDir2(counterBimod);
            } else {
                dirUpdate.setCounterDir1(counterBimod);
                dirUpdate.setCounterDir2(counterTwoLevel);
            }
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

        this.twoLevel.updateTable(baddr, taken);

        dirUpdate.getCounterDir1().update(taken);
        dirUpdate.getCounterDir2().update(taken);

        if (dirUpdate.getCounterMeta() != null) {
            if (dirUpdate.isBimod() != dirUpdate.isTwoLevel()) {
                dirUpdate.getCounterMeta().update(dirUpdate.isTwoLevel() == taken);
            }
        }

        this.getBranchTargetBuffer().update(baddr, btarget, taken);
    }

    public TwoBitBranchPredictor getBimod() {
        return bimod;
    }

    public TwoBitBranchPredictor getMeta() {
        return meta;
    }

    public TwoLevelBranchPredictor getTwoLevel() {
        return twoLevel;
    }
}
