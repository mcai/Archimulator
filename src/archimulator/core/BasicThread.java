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

import archimulator.core.bpred.BranchPredictorUpdate;
import archimulator.core.bpred.DynamicBranchPredictor;
import archimulator.core.event.InstructionCommittedEvent;
import archimulator.core.event.InstructionDecodedEvent;
import archimulator.isa.RegisterDependencyType;
import archimulator.isa.StaticInstruction;
import archimulator.isa.StaticInstructionType;
import archimulator.uncore.MemoryHierarchyThread;
import archimulator.os.ContextState;
import archimulator.sim.Logger;
import archimulator.sim.event.ResetStatEvent;
import archimulator.util.Reference;
import archimulator.util.action.Action;
import archimulator.util.action.Action1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BasicThread extends AbstractBasicThread {
    private int commitWidth;
    private int lineSizeOfIcache;

    private int fetchNpc;
    private int fetchNnpc;
    private boolean fetchStalled;
    private int lastFetchedCacheLine;

    private DynamicInstruction lastDecodedDynamicInst;
    private boolean lastDecodedDynamicInstCommitted;

    private long lastCommitCycle;
    private int noInstructionCommittedCounterThreshold;

    private DynamicInstruction nextInstructionInCacheWarmupPhase;

    public BasicThread(Core core, int num) {
        super(core, num);

        this.commitWidth = this.core.getProcessor().getConfig().getCommitWidth();

        this.lineSizeOfIcache = this.core.getInstructionCache().getConfig().getGeometry().getLineSize();

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                BasicThread.this.noInstructionCommittedCounterThreshold = 0;
            }
        });
    }

    public void fastForwardOneCycle() {
        if (this.context != null && this.context.getState() == ContextState.RUNNING) {
            StaticInstruction staticInst;
            do {
                staticInst = this.context.decodeNextInstruction();
                StaticInstruction.execute(staticInst, this.context);

                if (!this.context.isPseudocallEncounteredInLastInstructionExecution() && staticInst.getMnemonic().getType() != StaticInstructionType.NOP) {
                    this.totalInsts++;
                }

            }
            while (this.context != null && this.context.getState() == ContextState.RUNNING && (this.context.isPseudocallEncounteredInLastInstructionExecution() || staticInst.getMnemonic().getType() == StaticInstructionType.NOP));
        }
    }

    public void warmupCacheOneCycle() {
        if (this.context != null && this.context.getState() == ContextState.RUNNING && !this.fetchStalled) {
            if (this.nextInstructionInCacheWarmupPhase == null) {
                StaticInstruction staticInst;
                do {
                    staticInst = this.context.decodeNextInstruction();
                    this.nextInstructionInCacheWarmupPhase = new DynamicInstruction(this, this.context.getRegs().getPc(), staticInst);
                    StaticInstruction.execute(staticInst, this.context);

                    if (!this.context.isPseudocallEncounteredInLastInstructionExecution() && staticInst.getMnemonic().getType() != StaticInstructionType.NOP) {
                        this.totalInsts++;
                    }
                }
                while (this.context != null && this.context.getState() == ContextState.RUNNING && !this.fetchStalled && (this.context.isPseudocallEncounteredInLastInstructionExecution() || staticInst.getMnemonic().getType() == StaticInstructionType.NOP));
            }

            int pc = this.nextInstructionInCacheWarmupPhase.getPc();

            int cacheLineToFetch = aligned(pc, this.lineSizeOfIcache);
            if (cacheLineToFetch != this.lastFetchedCacheLine) {
                if (this.core.canIfetch(this, pc)) {
                    this.core.ifetch(this, pc, pc, new Action() {
                        public void apply() {
                            fetchStalled = false;
                        }
                    });

                    this.fetchStalled = true;
                    this.lastFetchedCacheLine = cacheLineToFetch;
                } else {
                    return;
                }
            }

            int effectiveAddress = this.nextInstructionInCacheWarmupPhase.getEffectiveAddress();

            if (this.nextInstructionInCacheWarmupPhase.getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD) {
                if (this.core.canLoad(this, effectiveAddress)) {
                    this.core.load(this.nextInstructionInCacheWarmupPhase, effectiveAddress, pc, new Action() {
                        public void apply() {
                        }
                    });

                    this.nextInstructionInCacheWarmupPhase = null;
                }
            } else if (this.nextInstructionInCacheWarmupPhase.getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) {
                if (this.core.canStore(this, effectiveAddress)) {
                    this.core.store(this.nextInstructionInCacheWarmupPhase, effectiveAddress, pc, new Action() {
                        public void apply() {
                        }
                    });

                    this.nextInstructionInCacheWarmupPhase = null;
                }
            } else {
                this.nextInstructionInCacheWarmupPhase = null;
            }
        }
    }

    public void updateFetchNpcAndNnpcFromRegs() {
        this.fetchNpc = this.context.getRegs().getNpc();
        this.fetchNnpc = this.context.getRegs().getNnpc();

        this.lastCommitCycle = this.getCycleAccurateEventQueue().getCurrentCycle();
    }

    private boolean canFetch() {
        if (!this.context.useICache()) {
            this.lastFetchedCacheLine = aligned(this.fetchNpc, this.lineSizeOfIcache);
            return true;
        } else {
            if (this.fetchStalled) {
                return false;
            }

            int cacheLineToFetch = aligned(this.fetchNpc, this.lineSizeOfIcache);
            if (cacheLineToFetch != this.lastFetchedCacheLine) {
                if (!this.core.canIfetch(this, this.fetchNpc)) {
                    return false;
                } else {
                    this.core.ifetch(this, this.fetchNpc, this.fetchNpc, new Action() {
                        public void apply() {
                            fetchStalled = false;
                        }
                    });

                    this.fetchStalled = true;
                    this.lastFetchedCacheLine = cacheLineToFetch;

                    return false;
                }
            }

            return true;
        }
    }

    public void fetch() {
        if (!this.canFetch()) {
            return;
        }

        boolean hasDone = false;

        while (!hasDone) {
            if (this.context.getState() != ContextState.RUNNING) {
                break;
            }

            if (this.decodeBuffer.isFull()) {
                this.fetchStallsOnDecodeBufferIsFull++;
                break;
            }

            if (this.context.getRegs().getNpc() != this.fetchNpc) {
                if (this.context.isSpeculative()) {
                    this.context.getRegs().setNpc(this.fetchNpc);
                } else {
                    this.context.enterSpeculativeState();
                }
            }

            DynamicInstruction dynamicInst;

            do {
                StaticInstruction staticInst = this.context.decodeNextInstruction();
                dynamicInst = new DynamicInstruction(this, this.context.getRegs().getPc(), staticInst);
                StaticInstruction.execute(staticInst, this.context);

                if (this.context.isPseudocallEncounteredInLastInstructionExecution() || dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.NOP) {
                    this.updateFetchNpcAndNnpcFromRegs();
                }

            }
            while (this.context.isPseudocallEncounteredInLastInstructionExecution() || dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.NOP);

            this.fetchNpc = this.fetchNnpc;

            if (!this.context.isSpeculative() && this.context.getState() != ContextState.RUNNING) {
                this.lastDecodedDynamicInst = dynamicInst;
                this.lastDecodedDynamicInstCommitted = false;
            }

            this.getBlockingEventDispatcher().dispatch(new InstructionDecodedEvent(dynamicInst));

            if ((this.fetchNpc + 4) % this.lineSizeOfIcache == 0) {
                hasDone = true;
            }

            BranchPredictorUpdate dirUpdate = new BranchPredictorUpdate();

            Reference<Integer> returnAddressStackRecoverIndexRef = new Reference<Integer>(0);
            int dest = dynamicInst.getStaticInst().getMnemonic().isControl() ? this.bpred.predict(this.fetchNpc, 0, dynamicInst.getStaticInst().getMnemonic(), dirUpdate, returnAddressStackRecoverIndexRef) : this.fetchNpc + 4;

            this.fetchNnpc = dest <= 1 ? this.fetchNpc + 4 : dest;

            if (this.fetchNnpc != this.fetchNpc + 4) {
                hasDone = true;
            }

            this.decodeBuffer.getEntries().add(new DecodeBufferEntry(dynamicInst, this.context.getRegs().getNpc(), this.context.getRegs().getNnpc(), this.fetchNnpc, returnAddressStackRecoverIndexRef.get(), dirUpdate, this.context.isSpeculative()));
        }
    }

    public boolean registerRenameOne() {
        DecodeBufferEntry decodeBufferEntry = this.decodeBuffer.getEntries().get(0);

        DynamicInstruction dynamicInst = decodeBufferEntry.getDynamicInst();

        for (Map.Entry<RegisterDependencyType, Integer> entry : dynamicInst.getStaticInst().getNumFreePhysRegsToAllocate().entrySet()) {
            if (this.getPhysicalRegisterFile(entry.getKey()).getNumFreePhysicalRegs() < entry.getValue()) {
                return false;
            }
        }

        if ((dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD || dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) && this.getLoadStoreQueue().isFull()) {
            this.registerRenameStallsOnLoadStoreQueueFull++;
            return false;
        }

        ReorderBufferEntry reorderBufferEntry = new ReorderBufferEntry(this, dynamicInst, decodeBufferEntry.getNpc(), decodeBufferEntry.getNnpc(), decodeBufferEntry.getPredNnpc(), decodeBufferEntry.getReturnAddressStackRecoverIndex(), decodeBufferEntry.getDirUpdate(), decodeBufferEntry.isSpeculative());
        reorderBufferEntry.setEffectiveAddressComputation(dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD || dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE);

        for (int idep : reorderBufferEntry.getDynamicInst().getStaticInst().getIdeps()) {
            reorderBufferEntry.getSrcPhysRegs().put(idep, this.renameTable.get(idep));
        }

        for (int odep : reorderBufferEntry.getDynamicInst().getStaticInst().getOdeps()) {
            if (odep != 0) {
                reorderBufferEntry.getOldPhysRegs().put(odep, this.renameTable.get(odep));
                PhysicalRegister physReg = this.getPhysicalRegisterFile(RegisterDependencyType.getType(odep)).allocate(odep);
                this.renameTable.put(odep, physReg);
                reorderBufferEntry.getPhysRegs().put(odep, physReg);
            }
        }

        for (PhysicalRegister physReg : reorderBufferEntry.getSrcPhysRegs().values()) {
            if (!physReg.isReady()) {
                reorderBufferEntry.setNumNotReadyOperands(reorderBufferEntry.getNumNotReadyOperands() + 1);
                physReg.getDependents().add(reorderBufferEntry);
            }
        }

        if (reorderBufferEntry.isEffectiveAddressComputation()) {
            PhysicalRegister physReg = reorderBufferEntry.getSrcPhysRegs().get(reorderBufferEntry.getDynamicInst().getStaticInst().getIdeps().get(0));
            if (!physReg.isReady()) {
                physReg.getEffectiveAddressComputationOperandDependents().add(reorderBufferEntry);
            } else {
                reorderBufferEntry.setEffectiveAddressComputationOperandReady(true);
            }
        }

        if (dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD || dynamicInst.getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) {
            LoadStoreQueueEntry loadStoreQueueEntry = new LoadStoreQueueEntry(this, dynamicInst, decodeBufferEntry.getNpc(), decodeBufferEntry.getNnpc(), decodeBufferEntry.getPredNnpc(), 0, null, false);
            loadStoreQueueEntry.setEffectiveAddress(dynamicInst.getEffectiveAddress());

            loadStoreQueueEntry.setSrcPhysRegs(reorderBufferEntry.getSrcPhysRegs());
            loadStoreQueueEntry.setPhysRegs(reorderBufferEntry.getPhysRegs());

            for (PhysicalRegister physReg : loadStoreQueueEntry.getSrcPhysRegs().values()) {
                if (!physReg.isReady()) {
                    physReg.getDependents().add(loadStoreQueueEntry);
                }
            }

            loadStoreQueueEntry.setNumNotReadyOperands(reorderBufferEntry.getNumNotReadyOperands());

            PhysicalRegister storeAddressPhysReg = loadStoreQueueEntry.getSrcPhysRegs().get(loadStoreQueueEntry.getDynamicInst().getStaticInst().getIdeps().get(0));
            if (!storeAddressPhysReg.isReady()) {
                storeAddressPhysReg.getStoreAddressDependents().add(loadStoreQueueEntry);
            } else {
                loadStoreQueueEntry.setStoreAddressReady(true);
            }

            this.loadStoreQueue.getEntries().add(loadStoreQueueEntry);

            reorderBufferEntry.setLoadStoreQueueEntry(loadStoreQueueEntry);
        }

        this.reorderBuffer.getEntries().add(reorderBufferEntry);

        this.decodeBuffer.getEntries().remove(0);

        return true;
    }

    public boolean dispatchOne() {
        for (ReorderBufferEntry reorderBufferEntry : this.reorderBuffer.getEntries()) {
            if (!reorderBufferEntry.isDispatched()) {
                if (reorderBufferEntry.isAllOperandReady()) {//TODO: is it correct or efficient?
                    this.core.getReadyInstructionQueue().add(reorderBufferEntry);
                } else {
                    this.core.getWaitingInstructionQueue().add(reorderBufferEntry);
                }

                reorderBufferEntry.setDispatched();

                if (reorderBufferEntry.getLoadStoreQueueEntry() != null) {
                    LoadStoreQueueEntry loadStoreQueueEntry = reorderBufferEntry.getLoadStoreQueueEntry();

                    if (loadStoreQueueEntry.getDynamicInst().getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) {
                        if (loadStoreQueueEntry.isAllOperandReady()) {//TODO: is it correct or efficient?
                            this.core.getReadyStoreQueue().add(loadStoreQueueEntry);
                        } else {
                            this.core.getWaitingStoreQueue().add(loadStoreQueueEntry);
                        }
                    }

                    loadStoreQueueEntry.setDispatched();
                }

                return true;
            }
        }

        return false;
    }

    public void refreshLoadStoreQueue() { //TODO: to be clarified
        List<Integer> stdUnknowns = new ArrayList<Integer>();

        for (LoadStoreQueueEntry loadStoreQueueEntry : this.loadStoreQueue.getEntries()) {
            if (loadStoreQueueEntry.getDynamicInst().getStaticInst().getMnemonic().getType() == StaticInstructionType.STORE) {
                if (loadStoreQueueEntry.isStoreAddressReady()) {
                    break;
                } else if (!loadStoreQueueEntry.isAllOperandReady()) {
                    stdUnknowns.add(loadStoreQueueEntry.getEffectiveAddress());
                } else {
                    for (int i = 0; i < stdUnknowns.size(); i++) {
                        if (stdUnknowns.get(i) == loadStoreQueueEntry.getEffectiveAddress()) {
                            stdUnknowns.set(i, 0);
                        }
                    }
                }
            }

            if (loadStoreQueueEntry.getDynamicInst().getStaticInst().getMnemonic().getType() == StaticInstructionType.LOAD && loadStoreQueueEntry.isDispatched() && !this.core.getReadyLoadQueue().contains(loadStoreQueueEntry) && !loadStoreQueueEntry.isIssued() && !loadStoreQueueEntry.isCompleted() && loadStoreQueueEntry.isAllOperandReady()) {
                if (!stdUnknowns.contains(loadStoreQueueEntry.getEffectiveAddress())) {
                    this.core.getReadyLoadQueue().add(loadStoreQueueEntry);
                }
            }
        }
    }

    public void commit() {
        int COMMIT_TIMEOUT = 1000000;

        if (this.getCycleAccurateEventQueue().getCurrentCycle() - this.lastCommitCycle > COMMIT_TIMEOUT && this.lastCommitCycle % COMMIT_TIMEOUT == 0) {
            if (noInstructionCommittedCounterThreshold > 5) {
                this.getLogger().fatalf(Logger.THREAD, "%s: No instruction committed for %d cycles, %d committed.", this.getName(), COMMIT_TIMEOUT, this.totalInsts);
            } else {
                this.lastCommitCycle = this.getCycleAccurateEventQueue().getCurrentCycle();
                this.noInstructionCommittedCounterThreshold++;
            }
        }

        int numCommitted = 0;

        while (!this.reorderBuffer.isEmpty() && numCommitted < this.commitWidth) {
            ReorderBufferEntry reorderBufferEntry = this.reorderBuffer.getEntries().get(0);

            if (!reorderBufferEntry.isCompleted()) {
                reorderBufferEntry.getDynamicInst().setCyclesSpentAtHeadOfReorderBuffer(reorderBufferEntry.getDynamicInst().getCyclesSpentAtHeadOfReorderBuffer() + 1);
                break;
            }

            if (reorderBufferEntry.isSpeculative()) {
                if (this.bpred.isDynamic()) {
                    ((DynamicBranchPredictor) this.bpred).getReturnAddressStack().recover(reorderBufferEntry.getReturnAddressStackRecoverIndex());
                }

                this.context.exitSpeculativeState();

                this.fetchNpc = this.context.getRegs().getNpc();
                this.fetchNnpc = this.context.getRegs().getNnpc();

                this.squash();
                break;
            }

            if (reorderBufferEntry.isEffectiveAddressComputation()) {
                LoadStoreQueueEntry loadStoreQueueEntry = reorderBufferEntry.getLoadStoreQueueEntry();

                if (!loadStoreQueueEntry.isCompleted()) {
                    break;
                }

                this.core.removeFromQueues(loadStoreQueueEntry);

                this.loadStoreQueue.getEntries().remove(loadStoreQueueEntry);
            }

            for (int odep : reorderBufferEntry.getDynamicInst().getStaticInst().getOdeps()) {
                if (odep != 0) {
                    reorderBufferEntry.getOldPhysRegs().get(odep).reclaim();
                    reorderBufferEntry.getPhysRegs().get(odep).commit();
                }
            }

            if (reorderBufferEntry.getDynamicInst().getStaticInst().getMnemonic().isControl()) {
                this.bpred.update(
                        reorderBufferEntry.getDynamicInst().getPc(),
                        reorderBufferEntry.getNnpc(),
                        reorderBufferEntry.getNnpc() != (reorderBufferEntry.getNpc() + 4),
                        reorderBufferEntry.getPredNnpc() != (reorderBufferEntry.getNpc() + 4),
                        reorderBufferEntry.getPredNnpc() == reorderBufferEntry.getNnpc(),
                        reorderBufferEntry.getDynamicInst().getStaticInst().getMnemonic(),
                        reorderBufferEntry.getDirUpdate()
                );
            }

            this.core.removeFromQueues(reorderBufferEntry);

            this.getBlockingEventDispatcher().dispatch(new InstructionCommittedEvent(reorderBufferEntry.getDynamicInst()));

            if (this.context.getState() == ContextState.FINISHED && reorderBufferEntry.getDynamicInst() == this.lastDecodedDynamicInst) {
                this.lastDecodedDynamicInstCommitted = true;
            }

            this.reorderBuffer.getEntries().remove(0);

            this.totalInsts++;

            this.lastCommitCycle = this.getCycleAccurateEventQueue().getCurrentCycle();

            numCommitted++;

//			Logger.infof(Logger.DEBUG, "%s: instruction committed: reorderBufferEntry.id=%d", this.getName(), reorderBufferEntry.getId());
        }
    }

    public void squash() {
//		Logger.infof(Logger.THREAD, "%s: squash", this.getName());

        while (!this.reorderBuffer.isEmpty()) {
            ReorderBufferEntry reorderBufferEntry = this.reorderBuffer.getEntries().get(this.reorderBuffer.getEntries().size() - 1);

            if (reorderBufferEntry.isEffectiveAddressComputation()) {
                LoadStoreQueueEntry loadStoreQueueEntry = reorderBufferEntry.getLoadStoreQueueEntry();

                this.core.removeFromQueues(loadStoreQueueEntry);

                this.loadStoreQueue.getEntries().remove(loadStoreQueueEntry);
            }

            this.core.removeFromQueues(reorderBufferEntry);

            for (int odep : reorderBufferEntry.getDynamicInst().getStaticInst().getOdeps()) {
                if (odep != 0) {
                    reorderBufferEntry.getPhysRegs().get(odep).recover();
                    this.renameTable.put(odep, reorderBufferEntry.getOldPhysRegs().get(odep));
                }
            }

            reorderBufferEntry.getPhysRegs().clear();

            this.reorderBuffer.getEntries().remove(reorderBufferEntry);
        }

        assert (this.reorderBuffer.getEntries().isEmpty());
        assert (this.loadStoreQueue.getEntries().isEmpty());

        this.core.getFuPool().releaseAll(); //TODO: is it correct or just release those FUs that this thread uses?

        this.decodeBuffer.getEntries().clear();
    }

    private PhysicalRegisterFile getPhysicalRegisterFile(RegisterDependencyType type) {
        switch (type) {
            case INTEGER:
                return this.intPhysicalRegisterFile;
            case FLOAT:
                return this.fpPhysicalRegisterFile;
            default:
                return this.miscPhysicalRegisterFile;
        }
    }

    public boolean isLastDecodedDynamicInstCommitted() {
        return this.lastDecodedDynamicInst == null || lastDecodedDynamicInstCommitted;
    }

    private static int aligned(int n, int alignment) {
        return n & ~(alignment - 1);
    }

    public static boolean isMainThread(MemoryHierarchyThread thread) {
        return isMainThread(thread.getId());
    }

    public static boolean isMainThread(int threadId) {
        return threadId == getMainThreadId();
    }

    public static int getMainThreadId() {
        return 0; //TODO: main thread should not be hard coded.
    }

    public static boolean isHelperThread(MemoryHierarchyThread thread) {
        return isHelperThread(thread.getId());
    }

    public static boolean isHelperThread(int threadId) {
        return threadId == getHelperThreadId();
    }

    public static int getHelperThreadId() {
        return 2; //TODO: helper thread should not be hard coded.
    }
}
