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
package archimulator.core;

import archimulator.core.bpred.BranchPredictorUpdate;

/**
 * Decode buffer entry.
 *
 * @author Min Cai
 */
public class DecodeBufferEntry {
    private long id;
    private DynamicInstruction dynamicInstruction;
    private int npc;
    private int nnpc;
    private int predictedNnpc;
    private int returnAddressStackRecoverIndex;
    private BranchPredictorUpdate branchPredictorUpdate;
    private boolean speculative;

    /**
     * Create a decode buffer entry.
     *
     * @param dynamicInstruction             the dynamic instruction
     * @param npc                            the value of the next program counter (NPC)
     * @param nnpc                           the value of the next next program counter (NNPC)
     * @param predictedNnpc                  the predicted value of the next next program counter (predicted NNPC)
     * @param returnAddressStackRecoverIndex the return address stack recover index
     * @param branchPredictorUpdate          the branch predictor update
     * @param speculative                    a value indicating whether the decode buffer entry is speculative or not
     */
    public DecodeBufferEntry(
            DynamicInstruction dynamicInstruction,
            int npc,
            int nnpc,
            int predictedNnpc,
            int returnAddressStackRecoverIndex,
            BranchPredictorUpdate branchPredictorUpdate,
            boolean speculative
    ) {
        this.id = dynamicInstruction.getThread().getSimulation().currentDecodeBufferEntryId++;
        this.dynamicInstruction = dynamicInstruction;
        this.npc = npc;
        this.nnpc = nnpc;
        this.predictedNnpc = predictedNnpc;
        this.returnAddressStackRecoverIndex = returnAddressStackRecoverIndex;
        this.branchPredictorUpdate = branchPredictorUpdate;
        this.speculative = speculative;
    }

    /**
     * Get the ID of the decode buffer entry.
     *
     * @return the ID of the decode buffer entry
     */
    public long getId() {
        return id;
    }

    /**
     * Get the dynamic instruction.
     *
     * @return the dynamic instruction
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    /**
     * Get the value of the next program counter (NPC).
     *
     * @return the value of the next program counter (NPC)
     */
    public int getNpc() {
        return npc;
    }

    /**
     * Get the value of the next next program counter (NNPC).
     *
     * @return the value of the next next program counter (NNPC)
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     * Get the predicted value of the next next program counter (predicted NNPC).
     *
     * @return the predicted value of the next next program counter (predicted NNPC)
     */
    public int getPredictedNnpc() {
        return predictedNnpc;
    }

    /**
     * Get the return address stack recover index.
     *
     * @return the return address stack recover index
     */
    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    /**
     * Get the branch predictor update.
     *
     * @return the branch predictor update
     */
    public BranchPredictorUpdate getBranchPredictorUpdate() {
        return branchPredictorUpdate;
    }

    /**
     * Get a value indicating whether the decode buffer entry is speculative or not.
     *
     * @return a value indicating whether the decode buffer entry is speculative or not
     */
    public boolean isSpeculative() {
        return speculative;
    }
}
