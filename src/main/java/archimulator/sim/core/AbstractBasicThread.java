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

import archimulator.model.base.BasicSimulationObject;
import archimulator.model.event.DumpStatEvent;
import archimulator.model.event.ResetStatEvent;
import archimulator.sim.core.bpred.*;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.RegisterDependencyType;
import archimulator.sim.os.Context;
import archimulator.sim.uncore.CacheAccessType;
import archimulator.sim.uncore.coherence.event.CoherentCacheBeginCacheAccessEvent;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import archimulator.util.action.Action1;

public abstract class AbstractBasicThread extends BasicSimulationObject implements Thread {
    protected int num;
    protected Context context;
    protected int id;
    protected String name;
    protected Core core;

    protected BranchPredictor bpred;

    protected RegisterRenameTable renameTable;

    protected PipelineBuffer<DecodeBufferEntry> decodeBuffer;
    protected PipelineBuffer<ReorderBufferEntry> reorderBuffer;
    protected PipelineBuffer<LoadStoreQueueEntry> loadStoreQueue;

    private TranslationLookasideBuffer itlb;
    private TranslationLookasideBuffer dtlb;

    protected PhysicalRegisterFile intPhysicalRegisterFile;
    protected PhysicalRegisterFile fpPhysicalRegisterFile;
    protected PhysicalRegisterFile miscPhysicalRegisterFile;

    protected long totalInsts;

    private long llcReadMisses;
    private long llcWriteMisses;

    protected long decodeBufferFull;
    protected long reorderBufferFull;
    protected long loadStoreQueueFull;

    protected long intPhysicalRegisterFileFull;
    protected long fpPhysicalRegisterFileFull;
    protected long miscPhysicalRegisterFileFull;

    protected long fetchStallsOnDecodeBufferIsFull;

    protected long registerRenameStallsOnDecodeBufferIsEmpty;
    protected long registerRenameStallsOnReorderBufferIsFull;
    protected long registerRenameStallsOnLoadStoreQueueFull;

    protected long selectionStallOnCanNotLoad;
    protected long selectionStallOnCanNotStore;
    protected long selectionStallOnNoFreeFunctionalUnit;

    public AbstractBasicThread(Core core, int num) {
        super(core);

        this.core = core;

        this.num = num;
        this.id = this.core.getNum() * this.core.getProcessor().getConfig().getNumThreadsPerCore() + this.num;

        this.name = "c" + this.core.getNum() + "t" + this.num;

        switch (core.getProcessor().getConfig().getBpred().getType()) {
            case PERFECT:
                this.bpred = new PerfectBranchPredictor(this, this.name + ".bpred");
                break;
            case TAKEN:
                this.bpred = new TakenBranchPredictor(this, this.name + ".bpred");
                break;
            case NOT_TAKEN:
                this.bpred = new NotTakenBranchPredictor(this, this.name + ".bpred");
                break;
            case TWO_BIT:
                this.bpred = new TwoBitBranchPredictor(this, this.name + ".bpred", (TwoBitBranchPredictorConfig) core.getProcessor().getConfig().getBpred());
                break;
            case TWO_LEVEL:
                this.bpred = new TwoLevelBranchPredictor(this, this.name + ".bpred", (TwoLevelBranchPredictorConfig) core.getProcessor().getConfig().getBpred());
                break;
            case COMBINED:
                this.bpred = new CombinedBranchPredictor(this, this.name + ".bpred", (CombinedBranchPredictorConfig) core.getProcessor().getConfig().getBpred());
                break;
            default:
                throw new IllegalArgumentException();
        }

        this.intPhysicalRegisterFile = new PhysicalRegisterFile(this.name + ".intPhysicalRegisterFile", core.getProcessor().getConfig().getPhysicalRegisterFileCapacity());
        this.fpPhysicalRegisterFile = new PhysicalRegisterFile(this.name + ".fpPhysicalRegisterFile", core.getProcessor().getConfig().getPhysicalRegisterFileCapacity());
        this.miscPhysicalRegisterFile = new PhysicalRegisterFile(this.name + ".miscPhysicalRegisterFile", core.getProcessor().getConfig().getPhysicalRegisterFileCapacity());

        this.renameTable = new RegisterRenameTable(this.name + ".renameTable");

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_INT_REGS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, i);
            PhysicalRegister physReg = this.intPhysicalRegisterFile.getEntries().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_FLOAT_REGS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, i);
            PhysicalRegister physReg = this.fpPhysicalRegisterFile.getEntries().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_MISC_REGS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, i);
            PhysicalRegister physReg = this.miscPhysicalRegisterFile.getEntries().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        this.decodeBuffer = new PipelineBuffer<DecodeBufferEntry>(this.core.getProcessor().getConfig().getDecodeBufferCapacity());
        this.reorderBuffer = new PipelineBuffer<ReorderBufferEntry>(this.core.getProcessor().getConfig().getReorderBufferCapacity());
        this.loadStoreQueue = new PipelineBuffer<LoadStoreQueueEntry>(this.core.getProcessor().getConfig().getLoadStoreQueueCapacity());

        this.getBlockingEventDispatcher().addListener(CoherentCacheBeginCacheAccessEvent.class, new Action1<CoherentCacheBeginCacheAccessEvent>() {
            public void apply(CoherentCacheBeginCacheAccessEvent event) {
                if (!event.getCacheAccess().isHitInCache() && event.getCache().isLastLevelCache()) {
                    if (event.getAccess().getThread() == AbstractBasicThread.this) {
                        CacheAccessType accessType = event.getCacheAccess().getReference().getAccessType();
                        if (!accessType.isUpward()) {
                            if (accessType.isDownwardRead() || accessType.isDownwardWrite()) {
                                llcReadMisses++;
                            } else if (accessType.isWriteback()) {
                                llcWriteMisses++;
                            }
                        }
                    }
                }
            }
        });

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                AbstractBasicThread.this.totalInsts = 0;
                AbstractBasicThread.this.llcReadMisses = 0;
                AbstractBasicThread.this.llcWriteMisses = 0;

                AbstractBasicThread.this.decodeBufferFull = 0;
                AbstractBasicThread.this.reorderBufferFull = 0;
                AbstractBasicThread.this.loadStoreQueueFull = 0;

                AbstractBasicThread.this.intPhysicalRegisterFileFull = 0;
                AbstractBasicThread.this.fpPhysicalRegisterFileFull = 0;
                AbstractBasicThread.this.miscPhysicalRegisterFileFull = 0;

                AbstractBasicThread.this.fetchStallsOnDecodeBufferIsFull = 0;

                AbstractBasicThread.this.registerRenameStallsOnDecodeBufferIsEmpty = 0;
                AbstractBasicThread.this.registerRenameStallsOnReorderBufferIsFull = 0;
                AbstractBasicThread.this.registerRenameStallsOnLoadStoreQueueFull = 0;

                AbstractBasicThread.this.selectionStallOnCanNotLoad = 0;
                AbstractBasicThread.this.selectionStallOnCanNotStore = 0;
                AbstractBasicThread.this.selectionStallOnNoFreeFunctionalUnit = 0;
            }
        });

        this.getBlockingEventDispatcher().addListener(DumpStatEvent.class, new Action1<DumpStatEvent>() {
            public void apply(DumpStatEvent event) {
                event.getStats().put(AbstractBasicThread.this.getName() + ".totalInsts", String.valueOf(AbstractBasicThread.this.totalInsts));

                if (event.getType() == DumpStatEvent.Type.DETAILED_SIMULATION) {
                    event.getStats().put(AbstractBasicThread.this.getName() + ".llcReadMisses", String.valueOf(AbstractBasicThread.this.llcReadMisses));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".llcWriteMisses", String.valueOf(AbstractBasicThread.this.llcWriteMisses));

                    event.getStats().put(AbstractBasicThread.this.getName() + ".decodeBufferFull", String.valueOf(AbstractBasicThread.this.decodeBufferFull));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".reorderBufferFull", String.valueOf(AbstractBasicThread.this.reorderBufferFull));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".loadStoreQueueFull", String.valueOf(AbstractBasicThread.this.loadStoreQueueFull));

                    event.getStats().put(AbstractBasicThread.this.getName() + ".intPhysicalRegisterFileFull", String.valueOf(AbstractBasicThread.this.intPhysicalRegisterFileFull));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".fpPhysicalRegisterFileFull", String.valueOf(AbstractBasicThread.this.fpPhysicalRegisterFileFull));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".miscPhysicalRegisterFileFull", String.valueOf(AbstractBasicThread.this.miscPhysicalRegisterFileFull));

                    event.getStats().put(AbstractBasicThread.this.getName() + ".fetchStallsOnDecodeBufferIsFull", String.valueOf(AbstractBasicThread.this.fetchStallsOnDecodeBufferIsFull));

                    event.getStats().put(AbstractBasicThread.this.getName() + ".registerRenameStallsOnDecodeBufferIsEmpty", String.valueOf(AbstractBasicThread.this.registerRenameStallsOnDecodeBufferIsEmpty));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".registerRenameStallsOnReorderBufferIsFull", String.valueOf(AbstractBasicThread.this.registerRenameStallsOnReorderBufferIsFull));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".registerRenameStallsOnLoadStoreQueueFull", String.valueOf(AbstractBasicThread.this.registerRenameStallsOnLoadStoreQueueFull));

                    event.getStats().put(AbstractBasicThread.this.getName() + ".selectionStallOnCanNotLoad", String.valueOf(AbstractBasicThread.this.selectionStallOnCanNotLoad));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".selectionStallOnCanNotStore", String.valueOf(AbstractBasicThread.this.selectionStallOnCanNotStore));
                    event.getStats().put(AbstractBasicThread.this.getName() + ".selectionStallOnNoFreeFunctionalUnit", String.valueOf(AbstractBasicThread.this.selectionStallOnNoFreeFunctionalUnit));
                }
            }
        });
    }

    public void updatePerCycleStats() {
        if (this.decodeBuffer.isFull()) {
            this.decodeBufferFull++;
        }

        if (this.reorderBuffer.isFull()) {
            this.reorderBufferFull++;
        }

        if (this.loadStoreQueue.isFull()) {
            this.loadStoreQueueFull++;
        }

        if (this.intPhysicalRegisterFile.isFull()) {
            this.intPhysicalRegisterFileFull++;
        }

        if (this.fpPhysicalRegisterFile.isFull()) {
            this.fpPhysicalRegisterFileFull++;
        }

        if (this.miscPhysicalRegisterFile.isFull()) {
            this.miscPhysicalRegisterFileFull++;
        }
    }

    public int getNum() {
        return num;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Core getCore() {
        return core;
    }

    public PipelineBuffer<DecodeBufferEntry> getDecodeBuffer() {
        return decodeBuffer;
    }

    public PipelineBuffer<ReorderBufferEntry> getReorderBuffer() {
        return reorderBuffer;
    }

    public PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue() {
        return loadStoreQueue;
    }

    public long getTotalInsts() {
        return totalInsts;
    }

    public TranslationLookasideBuffer getItlb() {
        return itlb;
    }

    public void setItlb(TranslationLookasideBuffer itlb) {
        this.itlb = itlb;
    }

    public TranslationLookasideBuffer getDtlb() {
        return dtlb;
    }

    public void setDtlb(TranslationLookasideBuffer dtlb) {
        this.dtlb = dtlb;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public long getLlcReadMisses() {
        return llcReadMisses;
    }

    public long getLlcWriteMisses() {
        return llcWriteMisses;
    }

    public void incRegisterRenameStallsOnDecodeBufferIsEmpty() {
        this.registerRenameStallsOnDecodeBufferIsEmpty++;
    }

    public void incRegisterRenameStallsOnReorderBufferIsFull() {
        this.registerRenameStallsOnReorderBufferIsFull++;
    }

    public void incSelectionStallOnCanNotLoad() {
        this.selectionStallOnCanNotLoad++;
    }

    public void incSelectionStallOnCanNotStore() {
        this.selectionStallOnCanNotStore++;
    }

    public void incSelectionStallOnNoFreeFunctionalUnit() {
        this.selectionStallOnNoFreeFunctionalUnit++;
    }
}
