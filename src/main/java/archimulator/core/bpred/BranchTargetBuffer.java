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

import java.util.ArrayList;
import java.util.List;

/**
 * Branch target buffer.
 *
 * @author Min Cai
 */
public class BranchTargetBuffer {
    private List<List<BranchTargetBufferEntry>> entries;

    /**
     * Create a branch target buffer.
     *
     * @param numSets       the number of sets
     * @param associativity associativity
     */
    public BranchTargetBuffer(int numSets, int associativity) {
        this.entries = new ArrayList<>();

        for (int i = 0; i < numSets; i++) {
            List<BranchTargetBufferEntry> entriesPerSet = new ArrayList<>();

            for (int j = 0; j < associativity; j++) {
                entriesPerSet.add(new BranchTargetBufferEntry());
            }

            this.entries.add(entriesPerSet);
        }
    }

    /**
     * Lookup the branch target buffer entry for the specified branch address.
     *
     * @param branchAddress the branch address
     * @return the branch target buffer entry matching the specified branch address
     */
    public BranchTargetBufferEntry lookup(int branchAddress) {
        int set = this.getSet(branchAddress);

        for (BranchTargetBufferEntry branchTargetBufferEntry : this.entries.get(set)) {
            if (branchTargetBufferEntry.getSource() == branchAddress) {
                return branchTargetBufferEntry;
            }
        }

        return null;
    }

    /**
     * Update.
     *
     * @param branchAddress the branch address
     * @param branchTarget  the branch target
     * @param taken         a value indicating whether the branch is taken or not
     * @return the branch target buffer entry matching the specified branch address
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

    /**
     * Get the set index for the specified branch address
     *
     * @param branchAddress the branch address
     * @return the set index for the specified branch address
     */
    private int getSet(int branchAddress) {
        return branchAddress >> BranchPredictor.BRANCH_SHIFT & (this.entries.size() - 1);
    }
}
