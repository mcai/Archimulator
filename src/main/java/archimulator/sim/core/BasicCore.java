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

import archimulator.sim.core.functionalUnit.FunctionalUnitOperationType;
import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.ContextState;
import net.pickapack.util.Reference;
import net.pickapack.util.RoundRobinScheduler;
import net.pickapack.action.Action;
import net.pickapack.action.Action1;
import net.pickapack.action.Function1;
import net.pickapack.action.Predicate;

import java.util.Iterator;
import java.util.List;

/**
 * Basic core.
 *
 * @author Min Cai
 */
public class BasicCore extends AbstractBasicCore {
    private RoundRobinScheduler<Thread> registerRenameScheduler;
    private RoundRobinScheduler<Thread> dispatchScheduler;

    /**
     * Create a basic core.
     *
     * @param processor the parent processor
     * @param num       the number of the core
     */
    public BasicCore(Processor processor, int num) {
        super(processor, num);

        this.registerRenameScheduler = new RoundRobinScheduler<Thread>(this.threads, new Predicate<Thread>() {
            public boolean apply(Thread thread) {
                if (thread.getContext() == null) {
                    return false;
                } else if (thread.getDecodeBuffer().isEmpty()) {
                    thread.incrementNumRegisterRenameStallsOnDecodeBufferIsEmpty();
                    return false;
                } else if (thread.getReorderBuffer().isFull()) {
                    thread.incrementNumRegisterRenameStallsOnReorderBufferIsFull();
                    return false;
                } else {
                    return true;
                }
            }
        }, new Function1<Thread, Boolean>() {
            public Boolean apply(Thread thread) {
                return thread.registerRenameOne();
            }
        }, getExperiment().getArchitecture().getDecodeWidth()
        );

        this.dispatchScheduler = new RoundRobinScheduler<Thread>(this.threads, new Predicate<Thread>() {
            public boolean apply(Thread thread) {
                return thread.getContext() != null;
            }
        }, new Function1<Thread, Boolean>() {
            public Boolean apply(Thread thread) {
                return thread.dispatchOne();
            }
        }, getExperiment().getArchitecture().getDecodeWidth()
        );
    }

    @Override
    protected void fetch() {
        for (Thread thread : this.threads) {
            if (thread.getContext() != null && thread.getContext().getState() == ContextState.RUNNING) {
                thread.fetch();
            }
        }
    }

    @Override
    protected void registerRename() {
        this.registerRenameScheduler.consumeNext();
    }

    @Override
    protected void dispatch() {
        this.dispatchScheduler.consumeNext();
    }

    @Override
    protected void wakeUp() {
        this.wakeUp(this.waitingInstructionQueue, this.readyInstructionQueue);
        this.wakeUp(this.waitingStoreQueue, this.readyStoreQueue);
    }

    /**
     * Wake up.
     *
     * @param waitingQueue the waiting queue
     * @param readyQueue   the ready queue
     */
    private void wakeUp(List<AbstractReorderBufferEntry> waitingQueue, List<AbstractReorderBufferEntry> readyQueue) {
        for (Iterator<AbstractReorderBufferEntry> it = waitingQueue.iterator(); it.hasNext(); ) {
            AbstractReorderBufferEntry waitingQueueEntry = it.next();

            if (waitingQueueEntry.isAllOperandReady()) {
                readyQueue.add(waitingQueueEntry);
                it.remove();
            }
        }
    }

    @Override
    protected void issue() {
        Reference<Integer> quant = new Reference<Integer>(getExperiment().getArchitecture().getIssueWidth());

        this.issueInstructionQueue(quant);
        this.issueLoadQueue(quant);
        this.issueStoreQueue(quant);
    }

    /**
     * Issue the instruction queue.
     *
     * @param quant the quant
     */
    private void issueInstructionQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyInstructionQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final ReorderBufferEntry reorderBufferEntry = (ReorderBufferEntry) it.next();

            if (reorderBufferEntry.getDynamicInstruction().getStaticInstruction().getMnemonic().getFunctionalUnitOperationType() != FunctionalUnitOperationType.NONE) {
                if (this.functionalUnitPool.acquire(reorderBufferEntry, new Action1<ReorderBufferEntry>() {
                    public void apply(ReorderBufferEntry readyQueueEntry1) {
                        reorderBufferEntry.signalCompleted();
                    }
                })) {
                    reorderBufferEntry.setIssued();
                } else {
                    reorderBufferEntry.getThread().incrementNumSelectionStallsOnNoFreeFunctionalUnit();
                    continue;
                }
            } else {
                reorderBufferEntry.setIssued();
                reorderBufferEntry.setCompleted();
                reorderBufferEntry.writeBack();
            }

            it.remove();

            quant.set(quant.get() - 1);
        }
    }

    /**
     * Issue the load queue.
     *
     * @param quant the quant
     */
    private void issueLoadQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyLoadQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final LoadStoreQueueEntry loadStoreQueueEntry = (LoadStoreQueueEntry) it.next();

            boolean hitInLoadStoreQueue = false;

            for (LoadStoreQueueEntry loadStoreQueueEntry1 : loadStoreQueueEntry.getThread().getLoadStoreQueue().getEntries()) {
                if (loadStoreQueueEntry1.getDynamicInstruction().getStaticInstruction().getMnemonic().getType() == StaticInstructionType.STORE && loadStoreQueueEntry1.getEffectiveAddress() == loadStoreQueueEntry.getEffectiveAddress()) {
                    hitInLoadStoreQueue = true;
                    break;
                }
            }

            if (hitInLoadStoreQueue) {
                loadStoreQueueEntry.setIssued();
                loadStoreQueueEntry.signalCompleted();
            } else {
                if (!this.canLoad(loadStoreQueueEntry.getThread(), loadStoreQueueEntry.getEffectiveAddress())) {
                    loadStoreQueueEntry.getThread().incrementNumSelectionStallsOnCanNotLoad();
                    break;
                }

                this.load(loadStoreQueueEntry.getDynamicInstruction(), loadStoreQueueEntry.getEffectiveAddress(), loadStoreQueueEntry.getDynamicInstruction().getPc(), new Action() {
                    public void apply() {
                        loadStoreQueueEntry.signalCompleted();
                    }
                });
                loadStoreQueueEntry.setIssued();
            }

            it.remove();

            quant.set(quant.get() - 1);
        }
    }

    /**
     * Issue the store queue.
     *
     * @param quant the store queue
     */
    private void issueStoreQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyStoreQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final LoadStoreQueueEntry loadStoreQueueEntry = (LoadStoreQueueEntry) it.next();

            if (!this.canStore(loadStoreQueueEntry.getThread(), loadStoreQueueEntry.getEffectiveAddress())) {
                loadStoreQueueEntry.getThread().incrementNumSelectionStallsOnCanNotStore();
                break;
            }

            this.store(loadStoreQueueEntry.getDynamicInstruction(), loadStoreQueueEntry.getEffectiveAddress(), loadStoreQueueEntry.getDynamicInstruction().getPc(), new Action() {
                public void apply() {
//                    loadStoreQueueEntry.signalCompleted(); //TODO: should we need to wait for store to complete?
                }
            });
            loadStoreQueueEntry.setIssued();
            loadStoreQueueEntry.signalCompleted(); //TODO: should we need to wait for store to complete?

            it.remove();

            quant.set(quant.get() - 1);
        }
    }

    @Override
    protected void writeBack() {
        for (AbstractReorderBufferEntry reorderBufferEntry : this.oooEventQueue) {
            reorderBufferEntry.setCompleted();
            reorderBufferEntry.writeBack();
        }

        this.oooEventQueue.clear();
    }

    @Override
    protected void refreshLoadStoreQueue() {
        for (Thread thread : this.threads) {
            if (thread.getContext() != null) {
                thread.refreshLoadStoreQueue();
            }
        }
    }

    @Override
    protected void commit() {
        for (Thread thread : this.threads) {
            if (thread.getContext() != null) {
                thread.commit();
            }
        }
    }
}
