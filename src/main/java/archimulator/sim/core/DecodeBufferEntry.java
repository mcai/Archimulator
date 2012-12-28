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
package archimulator.sim.core;

import archimulator.sim.core.bpred.BranchPredictorUpdate;

/**
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
     *
     * @param dynamicInstruction
     * @param npc
     * @param nnpc
     * @param predictedNnpc
     * @param returnAddressStackRecoverIndex
     * @param branchPredictorUpdate
     * @param speculative
     */
    public DecodeBufferEntry(DynamicInstruction dynamicInstruction, int npc, int nnpc, int predictedNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate branchPredictorUpdate, boolean speculative) {
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
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    /**
     *
     * @return
     */
    public int getNpc() {
        return npc;
    }

    /**
     *
     * @return
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     *
     * @return
     */
    public int getPredictedNnpc() {
        return predictedNnpc;
    }

    /**
     *
     * @return
     */
    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    /**
     *
     * @return
     */
    public BranchPredictorUpdate getBranchPredictorUpdate() {
        return branchPredictorUpdate;
    }

    /**
     *
     * @return
     */
    public boolean isSpeculative() {
        return speculative;
    }
}
