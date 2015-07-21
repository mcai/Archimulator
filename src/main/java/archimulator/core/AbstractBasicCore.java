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

import archimulator.core.functionalUnit.FunctionalUnitPool;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract basic core.
 *
 * @author Min Cai
 */
public abstract class AbstractBasicCore extends AbstractMemoryHierarchyCore implements Core {
    /**
     * The list of threads.
     */
    protected List<Thread> threads;

    /**
     * The functional unit pool.
     */
    protected FunctionalUnitPool functionalUnitPool;

    /**
     * The waiting instruction queue.
     */
    protected List<AbstractReorderBufferEntry> waitingInstructionQueue;

    /**
     * The ready instruction queue.
     */
    protected List<AbstractReorderBufferEntry> readyInstructionQueue;

    /**
     * The ready load queue.
     */
    protected List<AbstractReorderBufferEntry> readyLoadQueue;

    /**
     * The waiting store queue.
     */
    protected List<AbstractReorderBufferEntry> waitingStoreQueue;

    /**
     * The ready store queue.
     */
    protected List<AbstractReorderBufferEntry> readyStoreQueue;

    /**
     * The out-of-order event queue.
     */
    protected List<AbstractReorderBufferEntry> oooEventQueue;

    /**
     * Create an abstract basic core.
     *
     * @param processor the parent processor
     * @param num       the number of the core
     */
    public AbstractBasicCore(Processor processor, int num) {
        super(processor, num);

        this.threads = new ArrayList<>();

        this.functionalUnitPool = new FunctionalUnitPool(this);

        this.waitingInstructionQueue = new ArrayList<>();
        this.readyInstructionQueue = new ArrayList<>();

        this.readyLoadQueue = new ArrayList<>();

        this.waitingStoreQueue = new ArrayList<>();
        this.readyStoreQueue = new ArrayList<>();

        this.oooEventQueue = new ArrayList<>();
    }

    @Override
    public void doMeasurementOneCycle() {
        this.commit();
        this.writeBack();
        this.refreshLoadStoreQueue();
        this.wakeUp();
        this.issue();
        this.dispatch();
        this.registerRename();
        this.fetch();

        this.updatePerCycleStats();
    }

    /**
     * Fetch.
     */
    protected abstract void fetch();

    /**
     * Do register renaming.
     */
    protected abstract void registerRename(); //TODO: to be merged with dispatch()

    /**
     * Dispatch.
     */
    protected abstract void dispatch();

    /**
     * Wake up.
     */
    protected abstract void wakeUp(); //TODO: to be removed

    /**
     * Issue.
     */
    protected abstract void issue();

    /**
     * Write back.
     */
    protected abstract void writeBack();

    /**
     * Refresh the load/store queue.
     */
    protected abstract void refreshLoadStoreQueue();

    /**
     * Commit.
     */
    protected abstract void commit();

    @Override
    public void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry) {
        this.oooEventQueue.remove(reorderBufferEntry);

        this.readyInstructionQueue.remove(reorderBufferEntry);
        this.waitingInstructionQueue.remove(reorderBufferEntry);

        this.readyLoadQueue.remove(reorderBufferEntry);

        this.readyStoreQueue.remove(reorderBufferEntry);
        this.waitingStoreQueue.remove(reorderBufferEntry);

        reorderBufferEntry.setSquashed();
    }

    @Override
    public List<Thread> getThreads() {
        return threads;
    }

    @Override
    public FunctionalUnitPool getFunctionalUnitPool() {
        return functionalUnitPool;
    }

    @Override
    public List<AbstractReorderBufferEntry> getWaitingInstructionQueue() {
        return waitingInstructionQueue;
    }

    @Override
    public List<AbstractReorderBufferEntry> getReadyInstructionQueue() {
        return readyInstructionQueue;
    }

    @Override
    public List<AbstractReorderBufferEntry> getReadyLoadQueue() {
        return readyLoadQueue;
    }

    @Override
    public List<AbstractReorderBufferEntry> getWaitingStoreQueue() {
        return waitingStoreQueue;
    }

    @Override
    public List<AbstractReorderBufferEntry> getReadyStoreQueue() {
        return readyStoreQueue;
    }

    @Override
    public List<AbstractReorderBufferEntry> getOooEventQueue() {
        return oooEventQueue;
    }
}
