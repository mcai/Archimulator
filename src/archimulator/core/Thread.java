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

import archimulator.mem.MemoryHierarchyThread;
import archimulator.os.Context;

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

    PipelineBuffer<DecodeBufferEntry> getDecodeBuffer();

    PipelineBuffer<ReorderBufferEntry> getReorderBuffer();

    PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue();

    long getTotalInsts();

    Context getContext();

    void setContext(Context context);

    boolean isLastDecodedDynamicInstCommitted();

    long getLlcReadMisses();

    long getLlcWriteMisses();

    void updatePerCycleStats();

    void incRegisterRenameStallsOnDecodeBufferIsEmpty();

    void incRegisterRenameStallsOnReorderBufferIsFull();

    void incSelectionStallOnCanNotLoad();

    void incSelectionStallOnCanNotStore();

    void incSelectionStallOnNoFreeFunctionalUnit();
}
