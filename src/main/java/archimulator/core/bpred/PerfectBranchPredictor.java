/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.util.Reference;

/**
 * Perfect branch predictor.
 *
 * @author Min Cai
 */
public class PerfectBranchPredictor extends BranchPredictor {
    /**
     * Create a perfect branch predictor.
     *
     * @param thread the thread
     * @param name   the name of the branch predictor
     */
    public PerfectBranchPredictor(Thread thread, String name) {
        super(thread, name, BranchPredictorType.PERFECT);
    }

    @Override
    public int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate, Reference<Integer> returnAddressStackRecoverIndex) {
        return this.getThread().getContext().getRegisterFile().getNnpc();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }
}
