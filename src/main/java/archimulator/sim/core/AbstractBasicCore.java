/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.core.functionalUnit.FunctionalUnitPool;
import archimulator.sim.uncore.MemoryAccessInitiatedEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;
import net.pickapack.math.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract basic core.
 *
 * @author Min Cai
 */
public abstract class AbstractBasicCore extends BasicSimulationObject implements Core {
    /**
     * The number of the core.
     */
    protected int num;

    /**
     * The name of the core.
     */
    protected String name;

    /**
     * The processor.
     */
    protected Processor processor;

    /**
     * The list of threads.
     */
    protected List<Thread> threads;

    /**
     * The L1I cache controller.
     */
    protected CacheController l1ICacheController;

    /**
     * The L1D cache controller.
     */
    protected CacheController l1DCacheController;

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
        super(processor);

        this.num = num;
        this.name = "c" + this.num;

        this.processor = processor;

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
    public boolean canIfetch(Thread thread, int virtualAddress) {
        int physicalTag = this.l1ICacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1ICacheController.canAccess(MemoryHierarchyAccessType.IFETCH, physicalTag);
    }

    @Override
    public boolean canLoad(Thread thread, int virtualAddress) {
        int physicalTag = this.l1DCacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1DCacheController.canAccess(MemoryHierarchyAccessType.LOAD, physicalTag);
    }

    @Override
    public boolean canStore(Thread thread, int virtualAddress) {
        int physicalTag = this.l1DCacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1DCacheController.canAccess(MemoryHierarchyAccessType.STORE, physicalTag);
    }

    @Override
    public void ifetch(Thread thread, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1ICacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.increment();

        MemoryHierarchyAccess alias = this.l1ICacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1ICacheController.beginAccess(null, thread, MemoryHierarchyAccessType.IFETCH, virtualPc, physicalAddress, physicalTag, () -> {
            counterPending.decrement();

            if (counterPending.getValue() == 0) {
                onCompletedCallback.apply();
            }
        });

        if (alias == null) {
            counterPending.increment();

            thread.getItlb().access(access, () -> {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            });

            this.l1ICacheController.receiveIfetch(access, () -> l1ICacheController.endAccess(physicalTag));
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(thread, virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.IFETCH));
    }

    @Override
    public void load(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInstruction.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1DCacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.increment();

        MemoryHierarchyAccess alias = this.l1DCacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1DCacheController.beginAccess(dynamicInstruction, dynamicInstruction.getThread(), MemoryHierarchyAccessType.LOAD, virtualPc, physicalAddress, physicalTag, new Action() {
            public void apply() {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            }
        });

        if (alias == null) {
            counterPending.increment();

            dynamicInstruction.getThread().getDtlb().access(access, () -> {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            });

            this.l1DCacheController.receiveLoad(access, () -> l1DCacheController.endAccess(physicalTag));
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInstruction.getThread(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.LOAD));
    }

    @Override
    public void store(DynamicInstruction dynamicInstruction, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInstruction.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1DCacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.increment();

        MemoryHierarchyAccess alias = this.l1DCacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1DCacheController.beginAccess(dynamicInstruction, dynamicInstruction.getThread(), MemoryHierarchyAccessType.STORE, virtualPc, physicalAddress, physicalTag, new Action() {
            public void apply() {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            }
        });

        if (alias == null) {
            counterPending.increment();

            dynamicInstruction.getThread().getDtlb().access(access, () -> {
                counterPending.decrement();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            });

            this.l1DCacheController.receiveStore(access, () -> l1DCacheController.endAccess(physicalTag));
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInstruction.getThread(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.STORE));
    }

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
    public String getName() {
        return name;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public Processor getProcessor() {
        return processor;
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

    @Override
    public CacheController getL1ICacheController() {
        return l1ICacheController;
    }

    @Override
    public void setL1ICacheController(CacheController l1ICacheController) {
        this.l1ICacheController = l1ICacheController;
    }

    @Override
    public CacheController getL1DCacheController() {
        return l1DCacheController;
    }

    @Override
    public void setL1DCacheController(CacheController l1DCacheController) {
        this.l1DCacheController = l1DCacheController;
    }
}
