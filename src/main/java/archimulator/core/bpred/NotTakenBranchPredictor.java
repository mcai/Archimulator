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

/**
 * The "Always not taken" branch predictor.
 *
 * @author Min Cai
 */
public class NotTakenBranchPredictor extends BranchPredictor {
    /**
     * Create an "always not taken" branch predictor.
     *
     * @param thread the thread
     * @param name   the name of the branch predictor
     */
    public NotTakenBranchPredictor(Thread thread, String name) {
        super(thread, name, BranchPredictorType.NOT_TAKEN);
    }

    @Override
    public int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        return mnemonic.getType() != StaticInstructionType.CONDITIONAL ? branchTarget : branchAddress + 4;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
