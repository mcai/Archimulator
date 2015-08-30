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

import archimulator.common.SimulationObject;
import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.core.bpred.BranchPredictor;
import archimulator.isa.Mnemonic;
import archimulator.os.Context;
import archimulator.uncore.tlb.TranslationLookasideBuffer;

import java.util.Map;

/**
 * Thread.
 *
 * @author Min Cai
 */
public interface Thread extends SimulationObject, Reportable {
    /**
     * Fast forward for one cycle.
     */
    void fastForwardOneCycle();

    /**
     * Warmup cache for one cycle.
     */
    void warmupCacheOneCycle();

    /**
     * Update the fetch NPC and the fetch NNPC from the architectural register file.
     */
    void updateFetchNpcAndNnpcFromRegs();

    /**
     * Fetch.
     */
    void fetch();

    /**
     * Register rename one entry.
     *
     * @return whether there is an entry is register renamed or not
     */
    boolean registerRenameOne();

    /**
     * Dispatch one entry.
     *
     * @return whether there is an entry is dispatched or not
     */
    boolean dispatchOne();

    /**
     * Refresh the load/store queue.
     */
    void refreshLoadStoreQueue();

    /**
     * Commit.
     */
    void commit();

    /**
     * Squash the pipeline.
     */
    void squash();

    /**
     * Update statistics per cycle.
     */
    void updatePerCycleStats();

    /**
     * Get the number of the thread.
     *
     * @return the number of the thread
     */
    int getNum();

    /**
     * Get the ID of the thread.
     *
     * @return the ID of the thread
     */
    int getId();

    /**
     * Get the name of the thread.
     *
     * @return the name of the thread
     */
    String getName();

    /**
     * Get the instruction translation lookaside buffer (iTLB).
     *
     * @return the instruction translation lookaside buffer (iTLB)
     */
    default TranslationLookasideBuffer getItlb() {
        return getCore().getProcessor().getMemoryHierarchy().getItlbs().get(this.getId());
    }

    /**
     * Get the data translation lookaside buffer (dTLB).
     *
     * @return the data translation lookaside buffer (dTLB)
     */
    default TranslationLookasideBuffer getDtlb() {
        return getCore().getProcessor().getMemoryHierarchy().getDtlbs().get(this.getId());
    }

    /**
     * Get the parent core.
     *
     * @return the parent core
     */
    Core getCore();

    /**
     * Get the branch predictor.
     *
     * @return the branch predictor
     */
    BranchPredictor getBranchPredictor();

    /**
     * Get the decode buffer.
     *
     * @return the decode buffer
     */
    PipelineBuffer<DecodeBufferEntry> getDecodeBuffer();

    /**
     * Get the reorder buffer.
     *
     * @return the reorder buffer
     */
    PipelineBuffer<ReorderBufferEntry> getReorderBuffer();

    /**
     * Get the load/store queue.
     *
     * @return the load/store queue
     */
    PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue();

    /**
     * Get the number of instructions.
     *
     * @return the number of instructions
     */
    long getNumInstructions();

    /**
     * Get the context.
     *
     * @return the context
     */
    Context getContext();

    /**
     * Set the context.
     *
     * @param context the context
     */
    void setContext(Context context);

    /**
     * Get a value indicating whether the last decoded dynamic instruction has been committed or not.
     *
     * @return a value indicating whether the last decoded dynamic instruction has been committed or not
     */
    boolean isLastDecodedDynamicInstructionCommitted();

    /**
     * Increment the number of register rename stalls when the decode buffer is empty.
     */
    void incrementNumRegisterRenameStallsOnDecodeBufferIsEmpty();

    /**
     * Increment the number of register rename stalls when the reorder buffer is full.
     */
    void incrementNumRegisterRenameStallsOnReorderBufferIsFull();

    /**
     * Increment the number of selection stalls when loads can not be issued.
     */
    void incrementNumSelectionStallsOnCanNotLoad();

    /**
     * Increment the number of selection stalls when stores can not be issued.
     */
    void incrementNumSelectionStallsOnCanNotStore();

    /**
     * Increment the number of selection stalls when there is no free functional unit of the a specific type.
     */
    void incrementNumSelectionStallsOnNoFreeFunctionalUnit();

    /**
     * Get the number of stalls on the decode buffer is full.
     *
     * @return the number of stalls on the decode buffer is full
     */
    long getNumDecodeBufferFullStalls();

    /**
     * Get the number of stalls on the reorder buffer is full.
     *
     * @return the number of stalls on the reorder buffer is full
     */
    long getNumReorderBufferFullStalls();

    /**
     * Get the number of stalls on the load/store queue is full.
     *
     * @return the number of stalls on the load/store queue is full
     */
    long getNumLoadStoreQueueFullStalls();

    /**
     * Get the number of stalls on the integer physical register file is full.
     *
     * @return the number of stalls on the integer physical register file is full
     */
    long getNumIntPhysicalRegisterFileFullStalls();

    /**
     * Get the number of stalls on the floating point physical register file is full.
     *
     * @return the number of stalls on the floating point physical register file is full
     */
    long getNumFpPhysicalRegisterFileFullStalls();

    /**
     * Get the number of stalls on the miscellaneous physical register file is full.
     *
     * @return the number of stalls on the miscellaneous physical register file is full
     */
    long getNumMiscPhysicalRegisterFileFullStalls();

    /**
     * Get the number of fetch stalls on the decode buffer is full.
     *
     * @return the number of fetch stalls on the decode buffer is full
     */
    long getNumFetchStallsOnDecodeBufferIsFull();

    /**
     * Get the number of register rename stalls on the decode buffer is empty.
     *
     * @return the number of register rename stalls on the decode buffer is empty
     */
    long getNumRegisterRenameStallsOnDecodeBufferIsEmpty();

    /**
     * Get the number of register rename stalls on the reorder buffer is full.
     *
     * @return the number of register rename stalls on the reorder buffer is full
     */
    long getNumRegisterRenameStallsOnReorderBufferIsFull();

    /**
     * Get the number of register rename stalls on the load/store queue is full.
     *
     * @return the number of register rename stalls on the load/store queue is full
     */
    long getNumRegisterRenameStallsOnLoadStoreQueueFull();

    /**
     * Get the number of selection stalls on loads can not be issued.
     *
     * @return the number of selection stalls on loads can not be issued
     */
    long getNumSelectionStallsOnCanNotLoad();

    /**
     * Get the number of selection stalls on stores can not be issued.
     *
     * @return the number of selection stalls on stores can not be issued
     */
    long getNumSelectionStallsOnCanNotStore();

    /**
     * Get the number of selection stalls on there is no free functional unit of a specific type.
     *
     * @return the number of selection stalls on there is no free functional unit of a specific type
     */
    long getNumSelectionStallsOnNoFreeFunctionalUnit();

    /**
     * Get the executed mnemonics.
     *
     * @return the executed mnemonics
     */
    Map<Mnemonic, Long> getExecutedMnemonics();

    /**
     * get the executed system call names.
     *
     * @return the executed system call names
     */
    Map<String, Long> getExecutedSystemCalls();

    default void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            getChildren().add(new ReportNode(this, "numInstructions", getNumInstructions() + ""));
            getChildren().add(new ReportNode(this, "executedMnemonics", getExecutedMnemonics() + "")); //TODO
            getChildren().add(new ReportNode(this, "executedSystemCalls", getExecutedSystemCalls() + "")); //TODO

            getChildren().add(new ReportNode(this, "branchPredictor/type", getBranchPredictor().getType() + ""));
            getChildren().add(new ReportNode(this, "branchPredictor/hitRatio", getBranchPredictor().getHitRatio() + ""));
            getChildren().add(new ReportNode(this, "branchPredictor/numAccesses", getBranchPredictor().getNumAccesses() + ""));
            getChildren().add(new ReportNode(this, "branchPredictor/numHits", getBranchPredictor().getNumHits() + ""));
            getChildren().add(new ReportNode(this, "branchPredictor/numMisses", getBranchPredictor().getNumMisses() + ""));

            getChildren().add(new ReportNode(this, "numDecodeBufferFullStalls", getNumDecodeBufferFullStalls() + ""));
            getChildren().add(new ReportNode(this, "numReorderBufferFullStalls", getNumReorderBufferFullStalls() + ""));
            getChildren().add(new ReportNode(this, "numLoadStoreQueueFullStalls", getNumLoadStoreQueueFullStalls() + ""));

            getChildren().add(new ReportNode(this, "numIntPhysicalRegisterFileFullStalls", getNumIntPhysicalRegisterFileFullStalls() + ""));
            getChildren().add(new ReportNode(this, "numFpPhysicalRegisterFileFullStalls", getNumFpPhysicalRegisterFileFullStalls() + ""));
            getChildren().add(new ReportNode(this, "numMiscPhysicalRegisterFileFullStalls", getNumMiscPhysicalRegisterFileFullStalls() + ""));

            getChildren().add(new ReportNode(this, "numFetchStallsOnDecodeBufferIsFull", getNumFetchStallsOnDecodeBufferIsFull() + ""));

            getChildren().add(new ReportNode(this, "numRegisterRenameStallsOnDecodeBufferIsEmpty", getNumRegisterRenameStallsOnDecodeBufferIsEmpty() + ""));
            getChildren().add(new ReportNode(this, "numRegisterRenameStallsOnReorderBufferIsFull", getNumRegisterRenameStallsOnReorderBufferIsFull() + ""));
            getChildren().add(new ReportNode(this, "numRegisterRenameStallsOnLoadStoreQueueFull", getNumRegisterRenameStallsOnLoadStoreQueueFull() + ""));

            getChildren().add(new ReportNode(this, "numSelectionStallsOnCanNotLoad", getNumSelectionStallsOnCanNotLoad() + ""));
            getChildren().add(new ReportNode(this, "numSelectionStallsOnCanNotStore", getNumSelectionStallsOnCanNotStore() + ""));
            getChildren().add(new ReportNode(this, "numSelectionStallsOnNoFreeFunctionalUnit", getNumSelectionStallsOnNoFreeFunctionalUnit() + ""));
        }});
    }
}
