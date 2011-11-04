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
package archimulator.core.bpred2;

public class BranchPredictorTraceRecord {
    private boolean taken;
    private int target;
    private BranchInfo branchInfo;

    public BranchPredictorTraceRecord(boolean taken, int target, BranchInfo branchInfo) {
        this.taken = taken;
        this.target = target;
        this.branchInfo = branchInfo;
    }

    public boolean isTaken() {
        return taken;
    }

    public int getTarget() {
        return target;
    }

    public BranchInfo getBranchInfo() {
        return branchInfo;
    }
}
