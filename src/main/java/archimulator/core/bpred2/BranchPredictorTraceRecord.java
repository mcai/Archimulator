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
package archimulator.core.bpred2;

/**
 * Branch predictor trace record.
 *
 * @author Min Cai
 */
public class BranchPredictorTraceRecord {
    private boolean taken;
    private int target;
    private BranchInfo branchInfo;

    /**
     * Create a branch predictor trace record.
     *
     * @param taken      a value indicating whether the branch is taken or not
     * @param target     the target address
     * @param branchInfo the branch information object
     */
    public BranchPredictorTraceRecord(boolean taken, int target, BranchInfo branchInfo) {
        this.taken = taken;
        this.target = target;
        this.branchInfo = branchInfo;
    }

    /**
     * Get a value indicating whether the branch is taken or not.
     *
     * @return a value indicating whether the branch is taken or not
     */
    public boolean isTaken() {
        return taken;
    }

    /**
     * Get the target address.
     *
     * @return the target address
     */
    public int getTarget() {
        return target;
    }

    /**
     * Get the branch information object.
     *
     * @return the branch information object
     */
    public BranchInfo getBranchInfo() {
        return branchInfo;
    }
}
