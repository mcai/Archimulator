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
package archimulator.core.bpred;

import java.util.ArrayList;
import java.util.List;

public class BranchTargetBuffer {
    private final List<List<BranchTargetBufferEntry>> entries;

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

    public BranchTargetBufferEntry lookup(int baddr) {
        int set = this.getSet(baddr);

        for (BranchTargetBufferEntry btbEntry : this.entries.get(set)) {
            if (btbEntry.getSource() == baddr) {
                return btbEntry;
            }
        }

        return null;
    }

    public BranchTargetBufferEntry update(int baddr, int btarget, boolean taken) {
        if (!taken) {
            return null;
        }

        int set = this.getSet(baddr);

        if (this.entries.get(set).size() == 1) {
            return this.entries.get(set).get(0);
        }

        BranchTargetBufferEntry btbEntryFound = null;

        for (BranchTargetBufferEntry btbEntry : this.entries.get(set)) {
            if (btbEntry.getSource() == baddr) {
                btbEntryFound = btbEntry;
                break;
            }
        }

        if (btbEntryFound == null) {
            btbEntryFound = this.entries.get(set).get(this.entries.get(set).size() - 1); //LRU
            btbEntryFound.setSource(baddr);
        }

        btbEntryFound.setTarget(btarget);

        this.entries.get(set).remove(btbEntryFound);
        this.entries.get(set).add(0, btbEntryFound); //MRU

        return btbEntryFound;
    }

    private int getSet(int baddr) {
        return (baddr >> BranchPredictor.BRANCH_SHIFT) & (this.entries.size() - 1);
    }
}
