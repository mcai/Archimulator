/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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

import java.util.ArrayList;
import java.util.List;

/**
 * Return address stack.
 *
 * @author Min Cai
 */
public class ReturnAddressStack {
    private int size;
    private int topOfStack;
    private List<BranchTargetBufferEntry> entries;

    /**
     * Create a return address stack.
     *
     * @param size the size of the return address stack
     */
    public ReturnAddressStack(int size) {
        this.size = size;

        this.entries = new ArrayList<BranchTargetBufferEntry>();
        for (int i = 0; i < this.size; i++) {
            this.entries.add(new BranchTargetBufferEntry());
        }

        this.topOfStack = this.size - 1;
    }

    /**
     * Recover.
     *
     * @param returnAddressStackRecoverIndex the return address stack recover index
     */
    public void recover(int returnAddressStackRecoverIndex) {
        this.topOfStack = returnAddressStackRecoverIndex;
    }

    /**
     * Push.
     *
     * @param branchAddress the branch address
     */
    public void push(int branchAddress) {
        this.topOfStack = (this.topOfStack + 1) % this.size;
        this.entries.get(this.topOfStack).setTarget(branchAddress + 8);
    }

    /**
     * Pop.
     *
     * @return the branch target address at the top of the stack
     */
    public int pop() {
        int target = this.entries.get(this.topOfStack).getTarget();
        this.topOfStack = (this.topOfStack + this.size - 1) % this.size;
        return target;
    }

    /**
     * Get the size of the return address stack.
     *
     * @return the size of the return address stack
     */
    public int getSize() {
        return size;
    }

    /**
     * Get the top of the stack.
     *
     * @return the top of the stack
     */
    public int getTopOfStack() {
        return this.size > 0 ? this.topOfStack : 0;
    }
}
