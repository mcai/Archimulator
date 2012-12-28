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
package archimulator.sim.core.speculativePrecomputation;

import archimulator.sim.uncore.delinquentLoad.DelinquentLoad;
import archimulator.sim.os.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Slice.
 */
public class Slice {
    private DelinquentLoad delinquentLoad;
    private int triggerPc;
    private Set<Integer> liveIns;
    private List<Integer> pcs;

    private int numSpawnings;

    private Context spawnedThreadContext;

    private int numDecodedInstructions;
    private int numSavedL2Misses;

    private boolean ineffective;

    /**
     * Create a slice for the specified delinquent load.
     *
     * @param delinquentLoad the delinquent load
     */
    Slice(DelinquentLoad delinquentLoad) {
        this.delinquentLoad = delinquentLoad;

        this.setTriggerPc(0);
        this.setLiveIns(new HashSet<Integer>());
        this.setPcs(new ArrayList<Integer>());

        this.setNumSpawnings(0);

        this.setNumDecodedInstructions(0);
        this.setNumSavedL2Misses(0);

        this.setIneffective(false);
    }

    public DelinquentLoad getDelinquentLoad() {
        return delinquentLoad;
    }

    public int getTriggerPc() {
        return triggerPc;
    }

    public void setTriggerPc(int triggerPc) {
        this.triggerPc = triggerPc;
    }

    public Set<Integer> getLiveIns() {
        return liveIns;
    }

    public void setLiveIns(Set<Integer> liveIns) {
        this.liveIns = liveIns;
    }

    public List<Integer> getPcs() {
        return pcs;
    }

    public void setPcs(List<Integer> pcs) {
        this.pcs = pcs;
    }

    public int getNumSpawnings() {
        return numSpawnings;
    }

    public void setNumSpawnings(int numSpawnings) {
        this.numSpawnings = numSpawnings;
    }

    public Context getSpawnedThreadContext() {
        return spawnedThreadContext;
    }

    public void setSpawnedThreadContext(Context spawnedThreadContext) {
        this.spawnedThreadContext = spawnedThreadContext;
    }

    public int getNumDecodedInstructions() {
        return numDecodedInstructions;
    }

    public void setNumDecodedInstructions(int numDecodedInstructions) {
        this.numDecodedInstructions = numDecodedInstructions;
    }

    public int getNumSavedL2Misses() {
        return numSavedL2Misses;
    }

    public void setNumSavedL2Misses(int numSavedL2Misses) {
        this.numSavedL2Misses = numSavedL2Misses;
    }

    public boolean isIneffective() {
        return ineffective;
    }

    public void setIneffective(boolean ineffective) {
        this.ineffective = ineffective;
    }
}
