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

import archimulator.sim.core.Thread;
import archimulator.sim.isa.Mnemonic;
import net.pickapack.Reference;

/**
 *
 * @author Min Cai
 */
public abstract class BranchPredictor {
    private String name;
    private BranchPredictorType type;

    /**
     *
     */
    protected long numHits;
    /**
     *
     */
    protected long numMisses;

    private Thread thread;

    /**
     *
     * @param thread
     * @param name
     * @param type
     */
    public BranchPredictor(Thread thread, String name, BranchPredictorType type) {
        this.thread = thread;
        this.name = name;
        this.type = type;
    }

    /**
     *
     * @param branchAddress
     * @param branchTarget
     * @param mnemonic
     * @param dirUpdate
     * @param returnAddressStackRecoverIndex
     * @return
     */
    public abstract int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate dirUpdate, Reference<Integer> returnAddressStackRecoverIndex);

    /**
     *
     * @param branchAddress
     * @param branchTarget
     * @param taken
     * @param predictedTaken
     * @param correct
     * @param mnemonic
     * @param branchPredictorUpdate
     */
    public void update(int branchAddress, int branchTarget, boolean taken, boolean predictedTaken, boolean correct, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate) {
        if (correct) {
            this.numHits++;
        } else {
            this.numMisses++;
        }
    }

    /**
     *
     * @return
     */
    public abstract boolean isDynamic();

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public BranchPredictorType getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public long getNumAccesses() {
        return numHits + numMisses;
    }

    /**
     *
     * @return
     */
    public long getNumHits() {
        return numHits;
    }

    /**
     *
     * @return
     */
    public long getNumMisses() {
        return numMisses;
    }

    /**
     *
     * @return
     */
    public double getHitRatio() {
        return this.getNumAccesses() > 0 ? (double) this.numHits / this.getNumAccesses() : 0.0;
    }

    /**
     *
     * @return
     */
    public Thread getThread() {
        return thread;
    }

    /**
     *
     */
    public static final int BRANCH_SHIFT = 2;
}
