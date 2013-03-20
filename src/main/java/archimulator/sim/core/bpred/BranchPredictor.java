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
package archimulator.sim.core.bpred;

import archimulator.sim.core.Thread;
import archimulator.sim.isa.Mnemonic;
import net.pickapack.util.Reference;

/**
 * Branch predictor.
 *
 * @author Min Cai
 */
public abstract class BranchPredictor {
    private String name;
    private BranchPredictorType type;

    /**
     * The number of hits.
     */
    protected long numHits;

    /**
     * The number of misses.
     */
    protected long numMisses;

    private Thread thread;

    /**
     * Create a branch predictor.
     *
     * @param thread the thread
     * @param name   the name of the branch predictor
     * @param type   the type of the branch predictor
     */
    public BranchPredictor(Thread thread, String name, BranchPredictorType type) {
        this.thread = thread;
        this.name = name;
        this.type = type;
    }

    /**
     * Predict.
     *
     * @param branchAddress                  the branch address
     * @param branchTarget                   the branch target
     * @param mnemonic                       the mnemonic
     * @param branchPredictorUpdate          the branch predictor update
     * @param returnAddressStackRecoverIndex the return address stack recover index
     * @return the predicted target address
     */
    public abstract int predict(int branchAddress, int branchTarget, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate, Reference<Integer> returnAddressStackRecoverIndex);

    /**
     * Update.
     *
     * @param branchAddress         the branch address
     * @param branchTarget          the branch target
     * @param taken                 a value indicating whether the branch is taken or not
     * @param predictedTaken        a value indicating whether the branch is predicted as taken or not
     * @param correct               a value indicating whether the prediction is correct or not
     * @param mnemonic              the mnemonic
     * @param branchPredictorUpdate the branch predictor update
     */
    public void update(int branchAddress, int branchTarget, boolean taken, boolean predictedTaken, boolean correct, Mnemonic mnemonic, BranchPredictorUpdate branchPredictorUpdate) {
        if (correct) {
            this.numHits++;
        } else {
            this.numMisses++;
        }
    }

    /**
     * Get a value indicating whether the branch predictor is dynamic or not.
     *
     * @return a value indicating whether the branch predictor is dynamic or not
     */
    public abstract boolean isDynamic();

    /**
     * Get the name of the branch predictor.
     *
     * @return the name of the branch predictor
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of the branch predictor.
     *
     * @return the type of the branch predictor
     */
    public BranchPredictorType getType() {
        return type;
    }

    /**
     * Get the number of accesses.
     *
     * @return the number of accesses
     */
    public long getNumAccesses() {
        return numHits + numMisses;
    }

    /**
     * Get the number of hits.
     *
     * @return the number of hits
     */
    public long getNumHits() {
        return numHits;
    }

    /**
     * Get the number of misses.
     *
     * @return the number of misses
     */
    public long getNumMisses() {
        return numMisses;
    }

    /**
     * Get the hit ratio.
     *
     * @return the hit ratio
     */
    public double getHitRatio() {
        return this.getNumAccesses() > 0 ? (double) this.numHits / this.getNumAccesses() : 0.0;
    }

    /**
     * Get the thread.
     *
     * @return the thread
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Branch shift.
     */
    public static final int BRANCH_SHIFT = 2;
}
