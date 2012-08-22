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

import archimulator.sim.core.bpred.BranchPredictor;
import archimulator.sim.isa.Mnemonic;
import archimulator.sim.os.Context;
import archimulator.sim.uncore.MemoryHierarchyThread;

import java.util.List;

public interface Thread extends MemoryHierarchyThread {
    void fastForwardOneCycle();

    void warmupCacheOneCycle();

    void updateFetchNpcAndNnpcFromRegs();

    void fetch();

    boolean registerRenameOne();

    boolean dispatchOne();

    void refreshLoadStoreQueue();

    void commit();

    void squash();

    Core getCore();

    BranchPredictor getBranchPredictor();

    PipelineBuffer<DecodeBufferEntry> getDecodeBuffer();

    PipelineBuffer<ReorderBufferEntry> getReorderBuffer();

    PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue();

    long getTotalInstructions();

    Context getContext();

    void setContext(Context context);

    boolean isLastDecodedDynamicInstructionCommitted();

    void updatePerCycleStats();

    void incrementRegisterRenameStallsOnDecodeBufferIsEmpty();

    void incrementRegisterRenameStallsOnReorderBufferIsFull();

    void incrementSelectionStallOnCanNotLoad();

    void incrementSelectionStallOnCanNotStore();

    void incrementSelectionStallOnNoFreeFunctionalUnit();

    long getDecodeBufferFull();

    long getReorderBufferFull();

    long getLoadStoreQueueFull();

    long getIntPhysicalRegisterFileFull();

    long getFpPhysicalRegisterFileFull();

    long getMiscPhysicalRegisterFileFull();

    long getFetchStallsOnDecodeBufferIsFull();

    long getRegisterRenameStallsOnDecodeBufferIsEmpty();

    long getRegisterRenameStallsOnReorderBufferIsFull();

    long getRegisterRenameStallsOnLoadStoreQueueFull();

    long getSelectionStallOnCanNotLoad();

    long getSelectionStallOnCanNotStore();

    long getSelectionStallOnNoFreeFunctionalUnit();

    List<Mnemonic> getExecutedMnemonics();

    List<String> getExecutedSystemCalls();
}
