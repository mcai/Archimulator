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

import archimulator.util.math.SaturatingCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * A branch predictor test implementation.
 *
 * @author Min Cai
 */
public class BranchPredictor2Impl implements BranchPredictor2 {
    private static final int HISTORY_LENGTH = 15;
    private static final int TABLE_BITS = 15;

    private int history;
    private List<SaturatingCounter> tab;

    /**
     * Create a branch predictor test implementation.
     */
    public BranchPredictor2Impl() {
        this.history = 0;

        this.tab = new ArrayList<>();
        for (int i = 0; i < 1 << TABLE_BITS; i++) {
            this.tab.add(new SaturatingCounter(0, 2, 3, 0));
        }
    }

    /**
     * Predict.
     *
     * @param branchInfo the branch information object
     * @return the result branch update object
     */
    public BranchUpdate predict(BranchInfo branchInfo) {
        MyBranchUpdate branchUpdate = new MyBranchUpdate();

        if ((branchInfo.getBranchFlags() & BranchInfo.BR_CONDITIONAL) != 0) {
            branchUpdate.setIndex((history << (TABLE_BITS - HISTORY_LENGTH)) ^ (branchInfo.getAddress() & ((1 << TABLE_BITS) - 1)));
            branchUpdate.setPredictedDirection(this.tab.get(branchUpdate.getIndex()).isTaken());
        } else {
            branchUpdate.setPredictedDirection(true);
        }
        branchUpdate.setPredictedTarget(0);
        return branchUpdate;
    }

    /**
     * Update.
     *
     * @param branchInfo   the branch information object
     * @param branchUpdate the branch update object
     * @param taken        a value indicating whether the branch is taken or not
     * @param target       the branch target address
     */
    public void update(BranchInfo branchInfo, BranchUpdate branchUpdate, boolean taken, int target) {
        if (!(branchUpdate instanceof MyBranchUpdate)) {
            throw new IllegalArgumentException();
        }

        if ((branchInfo.getBranchFlags() & BranchInfo.BR_CONDITIONAL) != 0) {
            SaturatingCounter counter = this.tab.get(((MyBranchUpdate) branchUpdate).getIndex());
            counter.update(taken);
            this.history <<= 1;
            this.history |= taken ? 1 : 0;
            this.history &= (1 << HISTORY_LENGTH) - 1;
        }
    }

    /**
     * A custom branch update object.
     */
    private static class MyBranchUpdate extends BranchUpdate {
        private int index;

        /**
         * Create a custom branch update object.
         */
        private MyBranchUpdate() {
        }

        /**
         * Get the index.
         *
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Set the index.
         *
         * @param index the index
         */
        public void setIndex(int index) {
            this.index = index;
        }
    }
}
