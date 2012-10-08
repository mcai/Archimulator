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
package archimulator.sim.core.bpred;

import archimulator.sim.core.Thread;

/**
 *
 * @author Min Cai
 */
public abstract class DynamicBranchPredictor extends BranchPredictor {
    private BranchTargetBuffer branchTargetBuffer;
    private ReturnAddressStack returnAddressStack;

    /**
     *
     * @param thread
     * @param name
     * @param type
     * @param branchTargetBufferNumSets
     * @param branchTargetBufferAssociativity
     * @param returnAddressStackSize
     */
    public DynamicBranchPredictor(Thread thread, String name, BranchPredictorType type, int branchTargetBufferNumSets, int branchTargetBufferAssociativity, int returnAddressStackSize) {
        super(thread, name, type);

        this.branchTargetBuffer = new BranchTargetBuffer(branchTargetBufferNumSets, branchTargetBufferAssociativity);
        this.returnAddressStack = new ReturnAddressStack(returnAddressStackSize);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     *
     * @return
     */
    public BranchTargetBuffer getBranchTargetBuffer() {
        return branchTargetBuffer;
    }

    /**
     *
     * @return
     */
    public ReturnAddressStack getReturnAddressStack() {
        return returnAddressStack;
    }
}
