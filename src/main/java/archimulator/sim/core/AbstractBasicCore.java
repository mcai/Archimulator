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

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.uncore.MemoryAccessInitiatedEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.coherence.msi.controller.CacheController;
import net.pickapack.action.Action;
import net.pickapack.math.Counter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Min Cai
 */
public abstract class AbstractBasicCore extends BasicSimulationObject implements Core {
    /**
     *
     */
    protected int num;
    /**
     *
     */
    protected String name;

    /**
     *
     */
    protected Processor processor;

    /**
     *
     */
    protected List<Thread> threads;

    /**
     *
     */
    protected CacheController l1ICacheController;
    /**
     *
     */
    protected CacheController l1DCacheController;

    /**
     *
     */
    protected FunctionalUnitPool functionalUnitPool;

    /**
     *
     */
    protected List<AbstractReorderBufferEntry> waitingInstructionQueue;
    /**
     *
     */
    protected List<AbstractReorderBufferEntry> readyInstructionQueue;

    /**
     *
     */
    protected List<AbstractReorderBufferEntry> readyLoadQueue;

    /**
     *
     */
    protected List<AbstractReorderBufferEntry> waitingStoreQueue;
    /**
     *
     */
    protected List<AbstractReorderBufferEntry> readyStoreQueue;

    /**
     *
     */
    protected List<AbstractReorderBufferEntry> oooEventQueue;

    /**
     *
     * @param processor
     * @param num
     */
    public AbstractBasicCore(Processor processor, int num) {
        super(processor);

        this.num = num;
        this.name = "c" + this.num;

        this.processor = processor;

        this.threads = new ArrayList<Thread>();

        this.functionalUnitPool = new FunctionalUnitPool(this);

        this.waitingInstructionQueue = new ArrayList<AbstractReorderBufferEntry>();
        this.readyInstructionQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.readyLoadQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.waitingStoreQueue = new ArrayList<AbstractReorderBufferEntry>();
        this.readyStoreQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.oooEventQueue = new ArrayList<AbstractReorderBufferEntry>();
    }

    /**
     *
     */
    public void doFastForwardOneCycle() {
        for (Thread thread : this.threads) {
            thread.fastForwardOneCycle();
        }
    }

    /**
     *
     */
    public void doCacheWarmupOneCycle() {
        for (Thread thread : this.threads) {
            thread.warmupCacheOneCycle();
        }
    }

    /**
     *
     */
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
     *
     */
    protected abstract void fetch();

    /**
     *
     */
    protected abstract void registerRename(); //TODO: to be merged with dispatch()

    /**
     *
     */
    protected abstract void dispatch();

    /**
     *
     */
    protected abstract void wakeUp(); //TODO: to be removed

    /**
     *
     */
    protected abstract void issue();

    /**
     *
     */
    protected abstract void writeBack();

    /**
     *
     */
    protected abstract void refreshLoadStoreQueue();

    /**
     *
     */
    protected abstract void commit();

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    public boolean canIfetch(Thread thread, int virtualAddress) {
        int physicalTag = this.l1ICacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1ICacheController.canAccess(MemoryHierarchyAccessType.IFETCH, physicalTag);
    }

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    public boolean canLoad(Thread thread, int virtualAddress) {
        int physicalTag = this.l1DCacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1DCacheController.canAccess(MemoryHierarchyAccessType.LOAD, physicalTag);
    }

    /**
     *
     * @param thread
     * @param virtualAddress
     * @return
     */
    public boolean canStore(Thread thread, int virtualAddress) {
        int physicalTag = this.l1DCacheController.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.l1DCacheController.canAccess(MemoryHierarchyAccessType.STORE, physicalTag);
    }

    /**
     *
     * @param thread
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    public void ifetch(Thread thread, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1ICacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.l1ICacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1ICacheController.beginAccess(null, thread, MemoryHierarchyAccessType.IFETCH, virtualPc, physicalAddress, physicalTag, new Action() {
            public void apply() {
                counterPending.dec();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            }
        });

        if (alias == null) {
            counterPending.inc();

            thread.getItlb().access(access, new Action() {
                public void apply() {
                    counterPending.dec();

                    if (counterPending.getValue() == 0) {
                        onCompletedCallback.apply();
                    }
                }
            });

            this.l1ICacheController.receiveIfetch(access, new Action() {
                public void apply() {
                    l1ICacheController.endAccess(physicalTag);
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(thread, virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.IFETCH));
    }

    /**
     *
     * @param dynamicInst
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    public void load(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInst.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1DCacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.l1DCacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1DCacheController.beginAccess(dynamicInst, dynamicInst.getThread(), MemoryHierarchyAccessType.LOAD, virtualPc, physicalAddress, physicalTag, new Action() {
            public void apply() {
                counterPending.dec();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            }
        });

        if (alias == null) {
            counterPending.inc();

            dynamicInst.getThread().getDtlb().access(access, new Action() {
                public void apply() {
                    counterPending.dec();

                    if (counterPending.getValue() == 0) {
                        onCompletedCallback.apply();
                    }
                }
            });

            this.l1DCacheController.receiveLoad(access, new Action() {
                public void apply() {
                    l1DCacheController.endAccess(physicalTag);
                }

                @Override
                public String toString() {
                    return "load";
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInst.getThread(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.LOAD));
    }

    /**
     *
     * @param dynamicInst
     * @param virtualAddress
     * @param virtualPc
     * @param onCompletedCallback
     */
    public void store(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInst.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.l1DCacheController.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.l1DCacheController.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.l1DCacheController.beginAccess(dynamicInst, dynamicInst.getThread(), MemoryHierarchyAccessType.STORE, virtualPc, physicalAddress, physicalTag, new Action() {
            public void apply() {
                counterPending.dec();

                if (counterPending.getValue() == 0) {
                    onCompletedCallback.apply();
                }
            }
        });

        if (alias == null) {
            counterPending.inc();

            dynamicInst.getThread().getDtlb().access(access, new Action() {
                public void apply() {
                    counterPending.dec();

                    if (counterPending.getValue() == 0) {
                        onCompletedCallback.apply();
                    }
                }
            });

            this.l1DCacheController.receiveStore(access, new Action() {
                public void apply() {
                    l1DCacheController.endAccess(physicalTag);
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInst.getThread(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.STORE));
    }

    /**
     *
     * @param reorderBufferEntry
     */
    public void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry) {
        this.oooEventQueue.remove(reorderBufferEntry);

        this.readyInstructionQueue.remove(reorderBufferEntry);
        this.waitingInstructionQueue.remove(reorderBufferEntry);

        this.readyLoadQueue.remove(reorderBufferEntry);

        this.readyStoreQueue.remove(reorderBufferEntry);
        this.waitingStoreQueue.remove(reorderBufferEntry);

        reorderBufferEntry.setSquashed();
    }

    /**
     *
     */
    public void updatePerCycleStats() {
        for (Thread thread : this.getThreads()) {
            thread.updatePerCycleStats();
        }

        this.functionalUnitPool.updatePerCycleStats();
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return
     */
    public int getNum() {
        return num;
    }

    /**
     *
     * @return
     */
    public Processor getProcessor() {
        return processor;
    }

    /**
     *
     * @return
     */
    public List<Thread> getThreads() {
        return threads;
    }

    /**
     *
     * @return
     */
    public FunctionalUnitPool getFunctionalUnitPool() {
        return functionalUnitPool;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getWaitingInstructionQueue() {
        return waitingInstructionQueue;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getReadyInstructionQueue() {
        return readyInstructionQueue;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getReadyLoadQueue() {
        return readyLoadQueue;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getWaitingStoreQueue() {
        return waitingStoreQueue;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getReadyStoreQueue() {
        return readyStoreQueue;
    }

    /**
     *
     * @return
     */
    public List<AbstractReorderBufferEntry> getOooEventQueue() {
        return oooEventQueue;
    }

    /**
     *
     * @return
     */
    public CacheController getL1ICacheController() {
        return l1ICacheController;
    }

    /**
     *
     * @param l1ICacheController
     */
    public void setL1ICacheController(CacheController l1ICacheController) {
        this.l1ICacheController = l1ICacheController;
    }

    /**
     *
     * @return
     */
    public CacheController getL1DCacheController() {
        return l1DCacheController;
    }

    /**
     *
     * @param l1DCacheController
     */
    public void setL1DCacheController(CacheController l1DCacheController) {
        this.l1DCacheController = l1DCacheController;
    }
}
