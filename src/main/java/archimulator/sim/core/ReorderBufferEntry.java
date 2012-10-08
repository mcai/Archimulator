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
package archimulator.sim.core;

import archimulator.sim.core.bpred.BranchPredictorUpdate;

/**
 *
 * @author Min Cai
 */
public class ReorderBufferEntry extends AbstractReorderBufferEntry {
    private boolean effectiveAddressComputation;
    private LoadStoreQueueEntry loadStoreQueueEntry;

    private boolean effectiveAddressComputationOperandReady;

    /**
     *
     * @param thread
     * @param dynamicInst
     * @param npc
     * @param nnpc
     * @param predictedNnpc
     * @param returnAddressStackRecoverIndex
     * @param branchPredictorUpdate
     * @param speculative
     */
    public ReorderBufferEntry(Thread thread, DynamicInstruction dynamicInst, int npc, int nnpc, int predictedNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate branchPredictorUpdate, boolean speculative) {
        super(thread, dynamicInst, npc, nnpc, predictedNnpc, returnAddressStackRecoverIndex, branchPredictorUpdate, speculative);
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean isNeedWriteBack() {
        return !this.effectiveAddressComputation;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isAllOperandReady() {
        if (this.effectiveAddressComputation) {
            return this.effectiveAddressComputationOperandReady;
        }

        return super.isAllOperandReady();
    }

    /**
     *
     * @return
     */
    public boolean isEffectiveAddressComputation() {
        return effectiveAddressComputation;
    }

    /**
     *
     * @param effectiveAddressComputation
     */
    public void setEffectiveAddressComputation(boolean effectiveAddressComputation) {
        this.effectiveAddressComputation = effectiveAddressComputation;
    }

    /**
     *
     * @return
     */
    public LoadStoreQueueEntry getLoadStoreQueueEntry() {
        return loadStoreQueueEntry;
    }

    /**
     *
     * @param loadStoreQueueEntry
     */
    public void setLoadStoreQueueEntry(LoadStoreQueueEntry loadStoreQueueEntry) {
        this.loadStoreQueueEntry = loadStoreQueueEntry;
    }

    /**
     *
     * @param effectiveAddressComputationOperandReady
     */
    public void setEffectiveAddressComputationOperandReady(boolean effectiveAddressComputationOperandReady) {
        this.effectiveAddressComputationOperandReady = effectiveAddressComputationOperandReady;
    }

    /**
     *
     * @param numNotReadyOperands
     */
    @Override
    public void setNumNotReadyOperands(int numNotReadyOperands) {
        super.setNumNotReadyOperands(numNotReadyOperands);

        if (numNotReadyOperands == 0) {
            if (this.effectiveAddressComputation && !this.effectiveAddressComputationOperandReady) {
                throw new IllegalArgumentException();
            }
        }
    }
}
