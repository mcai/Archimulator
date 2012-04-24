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

import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.uncore.MemoryAccessInitiatedEvent;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.MemoryHierarchyAccessType;
import archimulator.sim.uncore.coherence.FirstLevelCache;
import archimulator.util.action.Action;
import archimulator.util.math.Counter;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBasicCore extends BasicSimulationObject implements Core {
    protected int num;
    protected String name;

    protected Processor processor;

    protected List<Thread> threads;

    protected FirstLevelCache instructionCache;
    protected FirstLevelCache dataCache;

    protected FunctionalUnitPool fuPool;

    protected List<AbstractReorderBufferEntry> waitingInstructionQueue;
    protected List<AbstractReorderBufferEntry> readyInstructionQueue;

    protected List<AbstractReorderBufferEntry> readyLoadQueue;

    protected List<AbstractReorderBufferEntry> waitingStoreQueue;
    protected List<AbstractReorderBufferEntry> readyStoreQueue;

    protected List<AbstractReorderBufferEntry> oooEventQueue;

    public AbstractBasicCore(Processor processor, int num) {
        super(processor);

        this.num = num;
        this.name = "c" + this.num;

        this.processor = processor;

        this.threads = new ArrayList<Thread>();

        this.fuPool = new FunctionalUnitPool(this);

        this.waitingInstructionQueue = new ArrayList<AbstractReorderBufferEntry>();
        this.readyInstructionQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.readyLoadQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.waitingStoreQueue = new ArrayList<AbstractReorderBufferEntry>();
        this.readyStoreQueue = new ArrayList<AbstractReorderBufferEntry>();

        this.oooEventQueue = new ArrayList<AbstractReorderBufferEntry>();
    }

    public void doFastForwardOneCycle() {
        for (Thread thread : this.threads) {
            thread.fastForwardOneCycle();
        }
    }

    public void doCacheWarmupOneCycle() {
        for (Thread thread : this.threads) {
            thread.warmupCacheOneCycle();
        }
    }

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

    protected abstract void fetch();

    protected abstract void registerRename(); //TODO: to be merged with dispatch()

    protected abstract void dispatch();

    protected abstract void wakeUp(); //TODO: to be removed

    protected abstract void issue();

    protected abstract void writeBack();

    protected abstract void refreshLoadStoreQueue();

    protected abstract void commit();

    public boolean canIfetch(Thread thread, int virtualAddress) {
        int physicalTag = this.instructionCache.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.instructionCache.canAccess(MemoryHierarchyAccessType.IFETCH, physicalTag);
    }

    public boolean canLoad(Thread thread, int virtualAddress) {
        int physicalTag = this.dataCache.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.dataCache.canAccess(MemoryHierarchyAccessType.LOAD, physicalTag);
    }

    public boolean canStore(Thread thread, int virtualAddress) {
        int physicalTag = this.dataCache.getCache().getTag(thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress));
        return this.dataCache.canAccess(MemoryHierarchyAccessType.STORE, physicalTag);
    }

    public void ifetch(Thread thread, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = thread.getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.instructionCache.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.instructionCache.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.instructionCache.beginAccess(null, thread, MemoryHierarchyAccessType.IFETCH, virtualPc, physicalAddress, physicalTag, new Action() {
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

            this.instructionCache.receiveIfetch(access, new Action() {
                public void apply() {
                    instructionCache.endAccess(physicalTag);
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(thread.getId(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.IFETCH));
    }

    public void load(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInst.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.dataCache.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.dataCache.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.dataCache.beginAccess(dynamicInst, dynamicInst.getThread(), MemoryHierarchyAccessType.LOAD, virtualPc, physicalAddress, physicalTag, new Action() {
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

            this.dataCache.receiveLoad(access, new Action() {
                public void apply() {
                    dataCache.endAccess(physicalTag);
                }

                @Override
                public String toString() {
                    return "load";
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInst.getThread().getId(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.LOAD));
    }

    public void store(DynamicInstruction dynamicInst, int virtualAddress, int virtualPc, final Action onCompletedCallback) {
        final int physicalAddress = dynamicInst.getThread().getContext().getProcess().getMemory().getPhysicalAddress(virtualAddress);
        final int physicalTag = this.dataCache.getCache().getTag(physicalAddress);

        final Counter counterPending = new Counter(0);

        counterPending.inc();

        MemoryHierarchyAccess alias = this.dataCache.findAccess(physicalTag);
        MemoryHierarchyAccess access = this.dataCache.beginAccess(dynamicInst, dynamicInst.getThread(), MemoryHierarchyAccessType.STORE, virtualPc, physicalAddress, physicalTag, new Action() {
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

            this.dataCache.receiveStore(access, new Action() {
                public void apply() {
                    dataCache.endAccess(physicalTag);
                }
            });
        }

        this.getBlockingEventDispatcher().dispatch(new MemoryAccessInitiatedEvent(dynamicInst.getThread().getId(), virtualPc, physicalAddress, physicalTag, MemoryHierarchyAccessType.STORE));
    }

    public void removeFromQueues(AbstractReorderBufferEntry reorderBufferEntry) {
        this.oooEventQueue.remove(reorderBufferEntry);

        this.readyInstructionQueue.remove(reorderBufferEntry);
        this.waitingInstructionQueue.remove(reorderBufferEntry);

        this.readyLoadQueue.remove(reorderBufferEntry);

        this.readyStoreQueue.remove(reorderBufferEntry);
        this.waitingStoreQueue.remove(reorderBufferEntry);

        reorderBufferEntry.setSquashed();
    }

    public void updatePerCycleStats() {
        for (Thread thread : this.getThreads()) {
            thread.updatePerCycleStats();
        }

        this.fuPool.updatePerCycleStats();
    }

    public String getName() {
        return name;
    }

    public int getNum() {
        return num;
    }

    public Processor getProcessor() {
        return processor;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public FunctionalUnitPool getFuPool() {
        return fuPool;
    }

    public List<AbstractReorderBufferEntry> getWaitingInstructionQueue() {
        return waitingInstructionQueue;
    }

    public List<AbstractReorderBufferEntry> getReadyInstructionQueue() {
        return readyInstructionQueue;
    }

    public List<AbstractReorderBufferEntry> getReadyLoadQueue() {
        return readyLoadQueue;
    }

    public List<AbstractReorderBufferEntry> getWaitingStoreQueue() {
        return waitingStoreQueue;
    }

    public List<AbstractReorderBufferEntry> getReadyStoreQueue() {
        return readyStoreQueue;
    }

    public List<AbstractReorderBufferEntry> getOooEventQueue() {
        return oooEventQueue;
    }

    public FirstLevelCache getInstructionCache() {
        return instructionCache;
    }

    public void setInstructionCache(FirstLevelCache instructionCache) {
        this.instructionCache = instructionCache;
    }

    public FirstLevelCache getDataCache() {
        return dataCache;
    }

    public void setDataCache(FirstLevelCache dataCache) {
        this.dataCache = dataCache;
    }
}
