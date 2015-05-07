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
package archimulator.sim.core.speculativePrecomputation;

import archimulator.sim.os.Context;
import archimulator.sim.uncore.delinquentLoad.DelinquentLoad;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Slice.
 *
 * @author Min Cai
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
        this.setLiveIns(new HashSet<>());
        this.setPcs(new ArrayList<>());

        this.setNumSpawnings(0);

        this.setNumDecodedInstructions(0);
        this.setNumSavedL2Misses(0);

        this.setIneffective(false);
    }

    /**
     * Get the delinquent load.
     *
     * @return the delinquent load
     */
    public DelinquentLoad getDelinquentLoad() {
        return delinquentLoad;
    }

    /**
     * Get the trigger PC.
     *
     * @return the trigger PC
     */
    public int getTriggerPc() {
        return triggerPc;
    }

    /**
     * Set the trigger PC.
     *
     * @param triggerPc the trigger PC
     */
    public void setTriggerPc(int triggerPc) {
        this.triggerPc = triggerPc;
    }

    /**
     * Get the set of live ins.
     *
     * @return the set of live ins
     */
    public Set<Integer> getLiveIns() {
        return liveIns;
    }

    /**
     * Set the set of live ins.
     *
     * @param liveIns the set of live ins
     */
    public void setLiveIns(Set<Integer> liveIns) {
        this.liveIns = liveIns;
    }

    /**
     * Get the list of PCs.
     *
     * @return the list of PCs
     */
    public List<Integer> getPcs() {
        return pcs;
    }

    /**
     * Set the list of PCs.
     *
     * @param pcs the list of PCs
     */
    public void setPcs(List<Integer> pcs) {
        this.pcs = pcs;
    }

    /**
     * Get the number of spawnings.
     *
     * @return the number of spawnings
     */
    public int getNumSpawnings() {
        return numSpawnings;
    }

    /**
     * Set the number of spawnings.
     *
     * @param numSpawnings the number of spawnings
     */
    public void setNumSpawnings(int numSpawnings) {
        this.numSpawnings = numSpawnings;
    }

    /**
     * Get the spawned thread context.
     *
     * @return the spawned thread context
     */
    public Context getSpawnedThreadContext() {
        return spawnedThreadContext;
    }

    /**
     * Set the spawned thread context.
     *
     * @param spawnedThreadContext the spawned thread context
     */
    public void setSpawnedThreadContext(Context spawnedThreadContext) {
        this.spawnedThreadContext = spawnedThreadContext;
    }

    /**
     * Get the number of decoded instructions.
     *
     * @return the number of decoded instructions
     */
    public int getNumDecodedInstructions() {
        return numDecodedInstructions;
    }

    /**
     * Set the number of decoded instructions.
     *
     * @param numDecodedInstructions the number of decoded instructions
     */
    public void setNumDecodedInstructions(int numDecodedInstructions) {
        this.numDecodedInstructions = numDecodedInstructions;
    }

    /**
     * Get the number of saved L2 cache misses.
     *
     * @return the number of saved L2 cache misses
     */
    public int getNumSavedL2Misses() {
        return numSavedL2Misses;
    }

    /**
     * Set the number of saved L2 cache misses.
     *
     * @param numSavedL2Misses the number of saved L2 cache misses
     */
    public void setNumSavedL2Misses(int numSavedL2Misses) {
        this.numSavedL2Misses = numSavedL2Misses;
    }

    /**
     * Get a value indicating whether it is ineffective or not.
     *
     * @return a value indicating whether it is ineffective or not
     */
    public boolean isIneffective() {
        return ineffective;
    }

    /**
     * Set a value indicating whether it is ineffective or not.
     *
     * @param ineffective a value indicating whether it is ineffective or not
     */
    public void setIneffective(boolean ineffective) {
        this.ineffective = ineffective;
    }
}
