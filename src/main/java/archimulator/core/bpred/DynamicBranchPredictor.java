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

/**
 * Dynamic branch predictor.
 *
 * @author Min Cai
 */
public abstract class DynamicBranchPredictor extends BranchPredictor {
    private BranchTargetBuffer branchTargetBuffer;
    private ReturnAddressStack returnAddressStack;

    /**
     * Create a dynamic branch predictor.
     *
     * @param thread                    the thread
     * @param name                      the name of the dynamic branch predictor
     * @param type                      the type of the dynamic branch predictor
     * @param branchTargetBufferNumSets the number of sets in the branch target buffer
     * @param branchTargetBufferAssociativity
     *                                  the associativity in the branch target buffer
     * @param returnAddressStackSize    the return address stack size
     */
    public DynamicBranchPredictor(
            Thread thread,
            String name,
            BranchPredictorType type,
            int branchTargetBufferNumSets,
            int branchTargetBufferAssociativity,
            int returnAddressStackSize
    ) {
        super(thread, name, type);

        this.branchTargetBuffer = new BranchTargetBuffer(branchTargetBufferNumSets, branchTargetBufferAssociativity);
        this.returnAddressStack = new ReturnAddressStack(returnAddressStackSize);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     * Get the branch target buffer.
     *
     * @return the branch target buffer
     */
    public BranchTargetBuffer getBranchTargetBuffer() {
        return branchTargetBuffer;
    }

    /**
     * Get the return address stack.
     *
     * @return the return address stack
     */
    public ReturnAddressStack getReturnAddressStack() {
        return returnAddressStack;
    }
}
