/*******************************************************************************
 * Copyright (c) 2010-2011 by Min Cai (min.cai.china@gmail.com).
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
import archimulator.sim.Simulation;

public class DecodeBufferEntry {
    private long id;
    private DynamicInstruction dynamicInst;
    private int npc;
    private int nnpc;
    private int predNnpc;
    private int returnAddressStackRecoverIndex;
    private BranchPredictorUpdate dirUpdate;
    private boolean speculative;

    public DecodeBufferEntry(DynamicInstruction dynamicInstruction, int npc, int nnpc, int predNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate dirUpdate, boolean speculative) {
        this.id = Simulation.currentDecodeBufferEntryId++;
        this.dynamicInst = dynamicInstruction;
        this.npc = npc;
        this.nnpc = nnpc;
        this.predNnpc = predNnpc;
        this.returnAddressStackRecoverIndex = returnAddressStackRecoverIndex;
        this.dirUpdate = dirUpdate;
        this.speculative = speculative;
    }

    public long getId() {
        return id;
    }

    public DynamicInstruction getDynamicInst() {
        return dynamicInst;
    }

    public int getNpc() {
        return npc;
    }

    public int getNnpc() {
        return nnpc;
    }

    public int getPredNnpc() {
        return predNnpc;
    }

    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    public BranchPredictorUpdate getDirUpdate() {
        return dirUpdate;
    }

    public boolean isSpeculative() {
        return speculative;
    }
}
