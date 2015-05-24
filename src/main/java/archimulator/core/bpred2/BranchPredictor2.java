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
package archimulator.core.bpred2;

/**
 * Branch predictor.
 *
 * @author Min Cai
 */
public interface BranchPredictor2 {
    /**
     * Predict.
     *
     * @param branchInfo the branch information object
     * @return the result branch update object
     */
    BranchUpdate predict(BranchInfo branchInfo);

    /**
     * Update.
     *
     * @param branchInfo   the branch information object
     * @param branchUpdate the branch update object
     * @param taken        a boolean value indicating whether the branch is taken or not
     * @param target       the branch target address
     */
    void update(BranchInfo branchInfo, BranchUpdate branchUpdate, boolean taken, int target);
}
