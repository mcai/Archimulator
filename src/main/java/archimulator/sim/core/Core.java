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

import archimulator.sim.uncore.MemoryHierarchyCore;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;

import java.util.List;

/**
 *
 * @author Min Cai
 */
public interface Core extends MemoryHierarchyCore {
    /**
     *
     * @param reorderBufferEntry
     */
    void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry);

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    boolean canIfetch(Thread thread, int virtualAddress);

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    boolean canLoad(Thread thread, int virtualAddress);

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    boolean canStore(Thread thread, int virtualAddress);

    /**
     *
     * @param thread
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    void ifetch(Thread thread, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     *
     * @param dynamicInst
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    void load(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     *
     * @param dynamicInst
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    void store(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, Action onCompletedCallback);

    /**
     *
     * @return
     */
    String getName();

    /**
     *
     * @return
     */
    Processor getProcessor();

    /**
     *
     * @return
     */
    List<Thread> getThreads();

    /**
     *
     * @return
     */
    FunctionalUnitPool getFunctionalUnitPool();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getWaitingInstructionQueue();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getReadyInstructionQueue();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getReadyLoadQueue();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getWaitingStoreQueue();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getReadyStoreQueue();

    /**
     *
     * @return
     */
    List<AbstractReorderBufferEntry> getOooEventQueue();

    /**
     *
     */
    void doFastForwardOneCycle();

    /**
     *
     */
    void doCacheWarmupOneCycle();

    /**
     *
     */
    void doMeasurementOneCycle();

    /**
     *
     * @return
     */
    CacheController getL1ICacheController();

    /**
     *
     * @param l1ICacheController
     */
    void setL1ICacheController(CacheController l1ICacheController);

    /**
     *
     * @return
     */
    CacheController getL1DCacheController();

    /**
     *
     * @param l1DCacheController
     */
    void setL1DCacheController(CacheController l1DCacheController);

    /**
     *
     */
    void updatePerCycleStats();
}
