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

import archimulator.uncore.MemoryHierarchyCore;
import archimulator.uncore.coherence.FirstLevelCache;
import archimulator.util.action.Action;

import java.util.List;

public interface Core extends MemoryHierarchyCore {
    void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry);

    boolean canIfetch(Thread thread, int virtualAddress);

    boolean canLoad(Thread thread, int virtualAddress);

    boolean canStore(Thread thread, int virtualAddress);

    void ifetch(Thread thread, int virtualAddress, int virtualPc, final Action onCompletedCallback);

    void load(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback);

    void store(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback);

    String getName();

    Processor getProcessor();

    List<Thread> getThreads();

    FunctionalUnitPool getFuPool();

    List<AbstractReorderBufferEntry> getWaitingInstructionQueue();

    List<AbstractReorderBufferEntry> getReadyInstructionQueue();

    List<AbstractReorderBufferEntry> getReadyLoadQueue();

    List<AbstractReorderBufferEntry> getWaitingStoreQueue();

    List<AbstractReorderBufferEntry> getReadyStoreQueue();

    List<AbstractReorderBufferEntry> getoooEventQueue();

    void doFastForwardOneCycle();

    void doCacheWarmupOneCycle();

    void doMeasurementOneCycle();

    FirstLevelCache getInstructionCache();

    void setInstructionCache(FirstLevelCache instructionCache);

    FirstLevelCache getDataCache();

    void setDataCache(FirstLevelCache dataCache);

    void updatePerCycleStats();
}
