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
package archimulator.sim.core.bpred2;

import net.pickapack.math.SaturatingCounter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class MyBranchPredictor implements BranchPredictor2 {
    private static final int HISTORY_LENGTH = 15;
    private static final int TABLE_BITS = 15;

    private int history;
    private List<SaturatingCounter> tab;

    /**
     *
     */
    public MyBranchPredictor() {
        this.history = 0;

        this.tab = new ArrayList<SaturatingCounter>();
        for (int i = 0; i < 1 << TABLE_BITS; i++) {
            this.tab.add(new SaturatingCounter(0, 2, 3, 0));
        }
    }

    /**
     *
     * @param branchInfo
     * @return
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
     *
     * @param branchInfo
     * @param branchUpdate
     * @param taken
     * @param target
     */
    public void update(BranchInfo branchInfo, BranchUpdate branchUpdate, boolean taken, int target) {
        assert (branchUpdate instanceof MyBranchUpdate);

        if ((branchInfo.getBranchFlags() & BranchInfo.BR_CONDITIONAL) != 0) {
            SaturatingCounter counter = this.tab.get(((MyBranchUpdate) branchUpdate).getIndex());
            counter.update(taken);
            this.history <<= 1;
            this.history |= taken ? 1 : 0;
            this.history &= (1 << HISTORY_LENGTH) - 1;
        }
    }

    private static class MyBranchUpdate extends BranchUpdate {
        private int index;

        private MyBranchUpdate() {
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
