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
package archimulator.core;

import archimulator.core.bpred.BranchPredictorUpdate;

public class ReorderBufferEntry extends AbstractReorderBufferEntry {
    private boolean effectiveAddressComputation;
    private LoadStoreQueueEntry loadStoreQueueEntry;

    private boolean effectiveAddressComputationOperandReady;

    public ReorderBufferEntry(Thread thread, DynamicInstruction dynamicInst, int npc, int nnpc, int predNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate dirUpdate, boolean speculative) {
        super(thread, dynamicInst, npc, nnpc, predNnpc, returnAddressStackRecoverIndex, dirUpdate, speculative);
    }

    @Override
    protected boolean isNeedWriteback() {
        return !this.effectiveAddressComputation;
    }

    @Override
    public boolean isAllOperandReady() {
        if (this.effectiveAddressComputation) {
            return this.effectiveAddressComputationOperandReady;
        }

        return super.isAllOperandReady();
    }

    public boolean isEffectiveAddressComputation() {
        return effectiveAddressComputation;
    }

    public void setEffectiveAddressComputation(boolean effectiveAddressComputation) {
        this.effectiveAddressComputation = effectiveAddressComputation;
    }

    public LoadStoreQueueEntry getLoadStoreQueueEntry() {
        return loadStoreQueueEntry;
    }

    public void setLoadStoreQueueEntry(LoadStoreQueueEntry loadStoreQueueEntry) {
        this.loadStoreQueueEntry = loadStoreQueueEntry;
    }

    public void setEffectiveAddressComputationOperandReady(boolean effectiveAddressComputationOperandReady) {
        this.effectiveAddressComputationOperandReady = effectiveAddressComputationOperandReady;
    }

    @Override
    public void setNumNotReadyOperands(int numNotReadyOperands) {
        super.setNumNotReadyOperands(numNotReadyOperands);

        if (numNotReadyOperands == 0) {
            assert !this.effectiveAddressComputation || this.effectiveAddressComputationOperandReady;
        }
    }
}
