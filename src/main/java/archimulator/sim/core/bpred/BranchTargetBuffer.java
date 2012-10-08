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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public class BranchTargetBuffer {
    private List<List<BranchTargetBufferEntry>> entries;

    /**
     *
     * @param numSets
     * @param associativity
     */
    public BranchTargetBuffer(int numSets, int associativity) {
        this.entries = new ArrayList<List<BranchTargetBufferEntry>>();

        for (int i = 0; i < numSets; i++) {
            List<BranchTargetBufferEntry> entriesPerSet = new ArrayList<BranchTargetBufferEntry>();

            for (int j = 0; j < associativity; j++) {
                entriesPerSet.add(new BranchTargetBufferEntry());
            }

            this.entries.add(entriesPerSet);
        }
    }

    /**
     *
     * @param branchAddress
     * @return
     */
    public BranchTargetBufferEntry lookup(int branchAddress) {
        int set = this.getSet(branchAddress);

        for (BranchTargetBufferEntry btbEntry : this.entries.get(set)) {
            if (btbEntry.getSource() == branchAddress) {
                return btbEntry;
            }
        }

        return null;
    }

    /**
     *
     * @param branchAddress
     * @param branchTarget
     * @param taken
     * @return
     */
    public BranchTargetBufferEntry update(int branchAddress, int branchTarget, boolean taken) {
        if (!taken) {
            return null;
        }

        int set = this.getSet(branchAddress);

        if (this.entries.get(set).size() == 1) {
            return this.entries.get(set).get(0);
        }

        BranchTargetBufferEntry entryFound = null;

        for (BranchTargetBufferEntry entry : this.entries.get(set)) {
            if (entry.getSource() == branchAddress) {
                entryFound = entry;
                break;
            }
        }

        if (entryFound == null) {
            entryFound = this.entries.get(set).get(this.entries.get(set).size() - 1); //LRU
            entryFound.setSource(branchAddress);
        }

        entryFound.setTarget(branchTarget);

        this.entries.get(set).remove(entryFound);
        this.entries.get(set).add(0, entryFound); //MRU

        return entryFound;
    }

    private int getSet(int branchAddress) {
        return (branchAddress >> BranchPredictor.BRANCH_SHIFT) & (this.entries.size() - 1);
    }
}
