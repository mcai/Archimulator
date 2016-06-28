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

import archimulator.common.report.ReportNode;
import archimulator.common.report.Reportable;
import archimulator.core.functionalUnit.FunctionalUnitPool;
import archimulator.util.action.Action;

import java.util.List;

/**
 * Core.
 *
 * @author Min Cai
 */
public interface Core extends MemoryHierarchyCore, Reportable {
    /**
     * Get a value indicating whether the specified thread can fetch the instruction at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can fetch the instruction at the specified address
     */
    boolean canIfetch(Thread thread, int virtualAddress);

    /**
     * Get a value indicating whether the specified thread can perform a load at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can perform a load at the specified address
     */
    boolean canLoad(Thread thread, int virtualAddress);

    /**
     * Get a value indicating whether the specified thread can perform a store at the specified address.
     *
     * @param thread         the thread
     * @param virtualAddress the virtual address
     * @return a value indicating whether the specified thread can perform a store at the specified address
     */
    boolean canStore(Thread thread, int virtualAddress);

    /**
     * Act on when the specified thread fetch the instruction at the specified address.
     *
     * @param thread              the thread
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the instruction fetch is completed
     */
    void ifetch(Thread thread, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Act on when the specified thread perform a load at the specified address.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the load is completed
     */
    void load(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Act on when the specified thread perform a store at the specified address.
     *
     * @param dynamicInstruction  the dynamic instruction
     * @param virtualAddress      the virtual address
     * @param virtualPc           the virtual address of the program counter (PC)
     * @param onCompletedCallback the callback action performed when the store is completed
     */
    void store(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     * Remove the entry from the queues.
     *
     * @param reorderBufferEntry the entry to be removed from the queues
     */
    void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry);

    /**
     * Do fast forward for one cycle.
     */
    default void doFastForwardOneCycle() {
        this.getThreads().forEach(Thread::fastForwardOneCycle);
    }

    /**
     * Do cache warmup for one cycle.
     */
    default void doCacheWarmupOneCycle() {
        this.getThreads().forEach(Thread::warmupCacheOneCycle);
    }

    /**
     * Update the statistics per cycle.
     */
    default void updatePerCycleStats() {
        this.getThreads().forEach(Thread::updatePerCycleStats);

        this.getFunctionalUnitPool().updatePerCycleStats();
    }

    /**
     * Get the list of SMT threads.
     *
     * @return get the lists of SMT threads
     */
    List<Thread> getThreads();

    /**
     * Get the functional unit pool.
     *
     * @return the functional unit pool
     */
    FunctionalUnitPool getFunctionalUnitPool();

    /**
     * Get the waiting instruction queue.
     *
     * @return the waiting instruction queue
     */
    List<AbstractReorderBufferEntry> getWaitingInstructionQueue();

    /**
     * Get the ready instruction queue.
     *
     * @return the ready instruction queue
     */
    List<AbstractReorderBufferEntry> getReadyInstructionQueue();

    /**
     * Get the ready load queue.
     *
     * @return the ready load queue
     */
    List<AbstractReorderBufferEntry> getReadyLoadQueue();

    /**
     * Get the waiting store queue.
     *
     * @return the waiting store queue
     */
    List<AbstractReorderBufferEntry> getWaitingStoreQueue();

    /**
     * Get the ready store queue.
     *
     * @return the ready store queue
     */
    List<AbstractReorderBufferEntry> getReadyStoreQueue();

    /**
     * Get the out-of-order event queue.
     *
     * @return the out-of-order event queue
     */
    List<AbstractReorderBufferEntry> getOooEventQueue();

    /**
     * Get the number of instructions executed on all the threads.
     *
     * @return the number of instructions executed on all the threads.
     */
    default long getNumInstructions() {
        return this.getThreads().stream().mapToLong(Thread::getNumInstructions).sum();
    }

    default void dumpStats(ReportNode reportNode) {
        reportNode.getChildren().add(new ReportNode(reportNode, getName()) {{
            getChildren().add(new ReportNode(this, "functionalUnitPool/numStallsOnNoFreeFunctionalUnit", getFunctionalUnitPool().getNumStallsOnNoFreeFunctionalUnit() + ""));
            getChildren().add(new ReportNode(this, "functionalUnitPool/numStallsOnAcquireFailedOnNoFreeFunctionalUnit", getFunctionalUnitPool().getNumStallsOnAcquireFailedOnNoFreeFunctionalUnit() + ""));
        }});
    }
}
