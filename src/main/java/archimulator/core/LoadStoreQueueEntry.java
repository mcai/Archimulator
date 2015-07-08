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
 * Load/store queue entry.
 *
 * @author Min Cai
 */
public class LoadStoreQueueEntry extends AbstractReorderBufferEntry {
    private int effectiveAddress;
    private boolean storeAddressReady;

    /**
     * Create a load/store queue entry.
     *
     * @param thread                         the thread
     * @param dynamicInstruction             the dynamic instruction
     * @param npc                            the value of the next program counter (NPC)
     * @param nnpc                           the value of the next next program counter (NNPC)
     * @param predictedNnpc                  the predicted value of the next next program counter (predicted NNPC)
     * @param returnAddressStackRecoverIndex the return address stack recover index
     * @param branchPredictorUpdate          the branch predictor update
     * @param speculative                    a value indicating whether the load/store queue entry is speculative or not
     */
    public LoadStoreQueueEntry(
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
        return true;
    }

    /**
     * Get a value indicating whether the store address is ready or not.
     *
     * @return a value indicating whether the store address is ready or not
     */
    public boolean isStoreAddressReady() {
        return this.storeAddressReady;
    }

    /**
     * Set a value indicating whether the store address is ready or not.
     *
     * @param storeAddressReady a value indicating whether the store address is ready or not
     */
    public void setStoreAddressReady(boolean storeAddressReady) {
        this.storeAddressReady = storeAddressReady;
    }

    /**
     * Get the effective address.
     *
     * @return the effective address
     */
    public int getEffectiveAddress() {
        return effectiveAddress;
    }

    /**
     * Set the effective address.
     *
     * @param effectiveAddress the effective address
     */
    public void setEffectiveAddress(int effectiveAddress) {
        this.effectiveAddress = effectiveAddress;
    }
}
