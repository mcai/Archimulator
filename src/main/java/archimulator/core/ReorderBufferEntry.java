/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
 * Reorder buffer entry.
 *
 * @author Min Cai
 */
public class ReorderBufferEntry extends AbstractReorderBufferEntry {
    private boolean effectiveAddressComputation;
    private LoadStoreQueueEntry loadStoreQueueEntry;

    private boolean effectiveAddressComputationOperandReady;

    /**
     * Create a reorder buffer entry.
     *
     * @param thread                         the thread
     * @param dynamicInstruction             the dynamic instruction
     * @param npc                            the value of the next program counter (NPC)
     * @param nnpc                           the value of the next next program counter (NNPC)
     * @param predictedNnpc                  the predicted value of the next net program counter (predicted NNPC)
     * @param returnAddressStackRecoverIndex the return address stack recover index
     * @param branchPredictorUpdate          the branch predictor update
     * @param speculative                    a value indicating whether the reorder buffer entry is speculative or not
     */
    public ReorderBufferEntry(
            Thread thread,
            DynamicInstruction dynamicInstruction,
            int npc,
            int nnpc,
            int predictedNnpc,
            int returnAddressStackRecoverIndex,
            BranchPredictorUpdate branchPredictorUpdate,
            boolean speculative
    ) {
        super(thread, dynamicInstruction, npc, nnpc, predictedNnpc, returnAddressStackRecoverIndex, branchPredictorUpdate, speculative);
    }

    @Override
    protected boolean isNeedWriteBack() {
        return !this.effectiveAddressComputation;
    }

    @Override
    public boolean isAllOperandReady() {
        if (this.effectiveAddressComputation) {
            return this.effectiveAddressComputationOperandReady;
        }

        return super.isAllOperandReady();
    }

    /**
     * Get a value indicating whether the reorder buffer entry is for effective address computation or not.
     *
     * @return a value indicating whether the reorder buffer entry is for effective address computation or not
     */
    public boolean isEffectiveAddressComputation() {
        return effectiveAddressComputation;
    }

    /**
     * Set a value indicating whether the reorder buffer entry is for effective address computation or not.
     *
     * @param effectiveAddressComputation a value indicating whether the reorder buffer entry is for effective address computation or not
     */
    public void setEffectiveAddressComputation(boolean effectiveAddressComputation) {
        this.effectiveAddressComputation = effectiveAddressComputation;
    }

    /**
     * Get the associated load/store queue entry.
     *
     * @return the associated load/store queue entry
     */
    public LoadStoreQueueEntry getLoadStoreQueueEntry() {
        return loadStoreQueueEntry;
    }

    /**
     * Set the associated load/store queue entry.
     *
     * @param loadStoreQueueEntry the associated load/store queue entry
     */
    public void setLoadStoreQueueEntry(LoadStoreQueueEntry loadStoreQueueEntry) {
        this.loadStoreQueueEntry = loadStoreQueueEntry;
    }

    /**
     * Set a value indicating whether the effective address computation operand is ready or not.
     *
     * @param effectiveAddressComputationOperandReady
     *         a value indicating whether the effective address computation operand is ready or not
     */
    public void setEffectiveAddressComputationOperandReady(boolean effectiveAddressComputationOperandReady) {
        this.effectiveAddressComputationOperandReady = effectiveAddressComputationOperandReady;
    }

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
