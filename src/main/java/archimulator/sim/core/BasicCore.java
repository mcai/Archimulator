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

import archimulator.sim.isa.StaticInstructionType;
import archimulator.sim.os.ContextState;
import archimulator.util.Reference;
import archimulator.util.RoundRobinScheduler;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;
import archimulator.util.action.Function1;
import archimulator.util.action.Predicate;

import java.util.Iterator;
import java.util.List;

public class BasicCore extends AbstractBasicCore {
    private RoundRobinScheduler<Thread> registerRenameScheduler;
    private RoundRobinScheduler<Thread> dispatchScheduler;

    private int decodeWidth;
    private int issueWidth;

    public BasicCore(Processor processor, int num) {
        super(processor, num);

        this.decodeWidth = this.processor.getConfig().getDecodeWidth();
        this.issueWidth = this.processor.getConfig().getIssueWidth();

        this.registerRenameScheduler = new RoundRobinScheduler<Thread>(this.threads, new Predicate<Thread>() {
            public boolean apply(Thread thread) {
                if (thread.getContext() == null) {
                    return false;
                } else if (thread.getDecodeBuffer().isEmpty()) {
                    thread.incRegisterRenameStallsOnDecodeBufferIsEmpty();
                    return false;
                } else if (thread.getReorderBuffer().isFull()) {
                    thread.incRegisterRenameStallsOnReorderBufferIsFull();
                    return false;
                } else {
                    return true;
                }
            }
        }, new Function1<Thread, Boolean>() {
            public Boolean apply(Thread thread) {
                return thread.registerRenameOne();
            }
        }, this.decodeWidth
        );

        this.dispatchScheduler = new RoundRobinScheduler<Thread>(this.threads, new Predicate<Thread>() {
            public boolean apply(Thread thread) {
                return thread.getContext() != null;
            }
        }, new Function1<Thread, Boolean>() {
            public Boolean apply(Thread thread) {
                return thread.dispatchOne();
            }
        }, this.decodeWidth
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
        this.wakeup(this.waitingInstructionQueue, this.readyInstructionQueue);
        this.wakeup(this.waitingStoreQueue, this.readyStoreQueue);
    }

    private void wakeup(List<AbstractReorderBufferEntry> waitingQueue, List<AbstractReorderBufferEntry> readyQueue) {
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
        Reference<Integer> quant = new Reference<Integer>(this.issueWidth);

        this.issueInstructionQueue(quant);
        this.issueLoadQueue(quant);
        this.issueStoreQueue(quant);
    }

    private void issueInstructionQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyInstructionQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final ReorderBufferEntry reorderBufferEntry = (ReorderBufferEntry) it.next();

            if (reorderBufferEntry.getDynamicInst().getStaticInst().getMnemonic().getFuOperationType() != FunctionalUnitOperationType.NONE) {
                if (this.fuPool.acquire(reorderBufferEntry, new Action1<ReorderBufferEntry>() {
                    public void apply(ReorderBufferEntry readyQueueEntry1) {
                        reorderBufferEntry.signalCompleted();
                    }
                })) {
                    reorderBufferEntry.setIssued();
                } else {
                    reorderBufferEntry.getThread().incSelectionStallOnNoFreeFunctionalUnit();
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

    private void issueLoadQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyLoadQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final LoadStoreQueueEntry loadStoreQueueEntry = (LoadStoreQueueEntry) it.next();

            boolean hitInLoadStoreQueue = false;

            for (LoadStoreQueueEntry loadStoreQueueEntry1 : loadStoreQueueEntry.getThread().getLoadStoreQueue().getEntries()) {
                if (loadStoreQueueEntry1.getDynamicInst().getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE && loadStoreQueueEntry1.getEffectiveAddress() == loadStoreQueueEntry.getEffectiveAddress()) {
                    hitInLoadStoreQueue = true;
                    break;
                }
            }

            if (hitInLoadStoreQueue) {
                loadStoreQueueEntry.setIssued();
                loadStoreQueueEntry.signalCompleted();
            } else {
                if (!this.canLoad(loadStoreQueueEntry.getThread(), loadStoreQueueEntry.getEffectiveAddress())) {
                    loadStoreQueueEntry.getThread().incSelectionStallOnCanNotLoad();
                    break;
                }

                this.load(loadStoreQueueEntry.getDynamicInst(), loadStoreQueueEntry.getEffectiveAddress(), loadStoreQueueEntry.getDynamicInst().getPc(), new Action() {
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

    private void issueStoreQueue(Reference<Integer> quant) {
        for (Iterator<AbstractReorderBufferEntry> it = this.readyStoreQueue.iterator(); quant.get() > 0 && it.hasNext(); ) {
            final LoadStoreQueueEntry loadStoreQueueEntry = (LoadStoreQueueEntry) it.next();

            if (!this.canStore(loadStoreQueueEntry.getThread(), loadStoreQueueEntry.getEffectiveAddress())) {
                loadStoreQueueEntry.getThread().incSelectionStallOnCanNotStore();
                break;
            }

            this.store(loadStoreQueueEntry.getDynamicInst(), loadStoreQueueEntry.getEffectiveAddress(), loadStoreQueueEntry.getDynamicInst().getPc(), new Action() {
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
