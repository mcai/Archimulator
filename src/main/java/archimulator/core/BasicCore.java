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

import archimulator.core.functionalUnit.FunctionalUnitOperationType;
import archimulator.isa.StaticInstructionType;
import archimulator.os.ContextState;
import archimulator.util.Reference;
import archimulator.util.RoundRobinScheduler;

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

        this.registerRenameScheduler = new RoundRobinScheduler<>(
                this.threads,
                thread -> {
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
                },
                Thread::registerRenameOne,
                getExperiment().getConfig().getDecodeWidth()
        );

        this.dispatchScheduler = new RoundRobinScheduler<>(
                this.threads,
                thread -> thread.getContext() != null,
                Thread::dispatchOne,
                getExperiment().getConfig().getDecodeWidth()
        );
    }

    @Override
    protected void fetch() {
        this.threads.stream().filter(
                thread -> thread.getContext() != null && thread.getContext().getState() == ContextState.RUNNING
        ).forEach(Thread::fetch);
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
        waitingQueue.stream().filter(AbstractReorderBufferEntry::isAllOperandReady).forEach(readyQueue::add);
        waitingQueue.removeIf(AbstractReorderBufferEntry::isAllOperandReady);
    }

    @Override
    protected void issue() {
        Reference<Integer> quant = new Reference<>(getExperiment().getConfig().getIssueWidth());

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
                if (this.functionalUnitPool.acquire(reorderBufferEntry, reorderBufferEntry::signalCompleted)) {
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

            boolean hitInLoadStoreQueue = loadStoreQueueEntry.getThread().getLoadStoreQueue().getEntries().stream().anyMatch(
                    loadStoreQueueEntryFound
                            -> loadStoreQueueEntryFound.getDynamicInstruction().getStaticInstruction().getMnemonic().getType() == StaticInstructionType.STORE
                            && loadStoreQueueEntryFound.getEffectiveAddress() == loadStoreQueueEntry.getEffectiveAddress()
            );

            if (hitInLoadStoreQueue) {
                loadStoreQueueEntry.setIssued();
                loadStoreQueueEntry.signalCompleted();
            } else {
                if (!this.canLoad(loadStoreQueueEntry.getThread(), loadStoreQueueEntry.getEffectiveAddress())) {
                    loadStoreQueueEntry.getThread().incrementNumSelectionStallsOnCanNotLoad();
                    break;
                }

                this.load(
                        loadStoreQueueEntry.getDynamicInstruction(),
                        loadStoreQueueEntry.getEffectiveAddress(),
                        loadStoreQueueEntry.getDynamicInstruction().getPc(),
                        loadStoreQueueEntry::signalCompleted
                );
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

            this.store(loadStoreQueueEntry.getDynamicInstruction(), loadStoreQueueEntry.getEffectiveAddress(), loadStoreQueueEntry.getDynamicInstruction().getPc(), () -> {
//                    loadStoreQueueEntry.signalCompleted(); //TODO: should we need to wait for store to complete?
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
        this.threads.stream().filter(thread -> thread.getContext() != null).forEach(Thread::refreshLoadStoreQueue);
    }

    @Override
    protected void commit() {
        this.threads.stream().filter(thread -> thread.getContext() != null).forEach(Thread::commit);
    }
}
