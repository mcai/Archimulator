/*******************************************************************************
 * Copyright (c) 2010-2014 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.common.BasicSimulationObject;
import archimulator.core.bpred.*;
import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.Mnemonic;
import archimulator.isa.RegisterDependencyType;
import archimulator.isa.event.InstructionFunctionallyExecutedEvent;
import archimulator.isa.event.SystemCallExecutedEvent;
import archimulator.os.Context;
import archimulator.uncore.tlb.TranslationLookasideBuffer;

import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract basic core.
 *
 * @author Min Cai
 */
public abstract class AbstractBasicThread extends BasicSimulationObject implements Thread {
    /**
     * The number of the thread.
     */
    protected int num;

    /**
     * The context.
     */
    protected Context context;

    /**
     * The ID of the thread.
     */
    protected int id;

    /**
     * The name of the thread.
     */
    protected String name;

    /**
     * The parent core.
     */
    protected Core core;

    /**
     * The branch predictor.
     */
    protected BranchPredictor branchPredictor;

    /**
     * The rename table.
     */
    protected RegisterRenameTable renameTable;

    /**
     * The decode buffer.
     */
    protected PipelineBuffer<DecodeBufferEntry> decodeBuffer;

    /**
     * The reorder buffer.
     */
    protected PipelineBuffer<ReorderBufferEntry> reorderBuffer;

    /**
     * The load/store queue.
     */
    protected PipelineBuffer<LoadStoreQueueEntry> loadStoreQueue;

    private TranslationLookasideBuffer itlb;
    private TranslationLookasideBuffer dtlb;

    /**
     * The integer physical register file.
     */
    protected PhysicalRegisterFile intPhysicalRegisterFile;

    /**
     * The floating point physical register file.
     */
    protected PhysicalRegisterFile fpPhysicalRegisterFile;

    /**
     * The miscellaneous physical register file.
     */
    protected PhysicalRegisterFile miscPhysicalRegisterFile;

    /**
     * The number of instructions.
     */
    protected long numInstructions;

    /**
     * The number of decode buffer full stalls.
     */
    protected long numDecodeBufferFullStalls;

    /**
     * The number of reorder buffer full stalls.
     */
    protected long numReorderBufferFullStalls;

    /**
     * The number of load/store queue full stalls.
     */
    protected long numLoadStoreQueueFullStalls;

    /**
     * The number of integer physical register file full stalls.
     */
    protected long numIntPhysicalRegisterFileFullStalls;

    /**
     * The number of floating point physical register file full stalls.
     */
    protected long numFpPhysicalRegisterFileFullStalls;

    /**
     * The number of miscellaneous physical register file full stalls.
     */
    protected long numMiscPhysicalRegisterFileFullStalls;

    /**
     * The number of fetch stalls when the decode buffer is full.
     */
    protected long numFetchStallsOnDecodeBufferIsFull;

    /**
     * The number of register rename stalls when the decode buffer is empty.
     */
    protected long numRegisterRenameStallsOnDecodeBufferIsEmpty;

    /**
     * The number of register rename stalls when the reorder buffer is full.
     */
    protected long numRegisterRenameStallsOnReorderBufferIsFull;

    /**
     * The number of register rename stalls when the load/store queue is full.
     */
    protected long numRegisterRenameStallsOnLoadStoreQueueFull;

    /**
     * The number of selection stalls when loads can not be issued.
     */
    protected long numSelectionStallsOnCanNotLoad;

    /**
     * The number of selection stalls when stores can not be issued.
     */
    protected long numSelectionStallsOnCanNotStore;

    /**
     * The number of selection stalls when there is no free functional unit for a specific functional unit type.
     */
    protected long numSelectionStallsOnNoFreeFunctionalUnit;

    private Map<Mnemonic, Long> executedMnemonics;
    private Map<String, Long> executedSystemCalls;

    /**
     * Create an abstract basic thread.
     *
     * @param core the parent core
     * @param num  the number of the thread
     */
    public AbstractBasicThread(Core core, int num) {
        super(core);

        this.core = core;

        this.num = num;
        this.id = this.core.getNum() * getExperiment().getNumThreadsPerCore() + this.num;

        this.name = "c" + this.core.getNum() + "t" + this.num;

        switch (getExperiment().getBranchPredictorType()) {
            case PERFECT:
                this.branchPredictor = new PerfectBranchPredictor(this, this.name + "/branchPredictor");
                break;
            case TAKEN:
                this.branchPredictor = new TakenBranchPredictor(this, this.name + "/branchPredictor");
                break;
            case NOT_TAKEN:
                this.branchPredictor = new NotTakenBranchPredictor(this, this.name + "/branchPredictor");
                break;
            case TWO_BIT:
                this.branchPredictor = new TwoBitBranchPredictor(this, this.name + "/branchPredictor");
                break;
            case TWO_LEVEL:
                this.branchPredictor = new TwoLevelBranchPredictor(this, this.name + "/branchPredictor");
                break;
            case COMBINED:
                this.branchPredictor = new CombinedBranchPredictor(this, this.name + "/branchPredictor");
                break;
            default:
                throw new IllegalArgumentException();
        }

        this.intPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/intPhysicalRegisterFile", getExperiment().getPhysicalRegisterFileCapacity());
        this.fpPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/fpPhysicalRegisterFile", getExperiment().getPhysicalRegisterFileCapacity());
        this.miscPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/miscPhysicalRegisterFile", getExperiment().getPhysicalRegisterFileCapacity());

        this.renameTable = new RegisterRenameTable(this.name + "/renameTable");

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_INT_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, i);
            PhysicalRegister physReg = this.intPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, i);
            PhysicalRegister physReg = this.fpPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_MISC_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, i);
            PhysicalRegister physReg = this.miscPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        this.decodeBuffer = new PipelineBuffer<>(getExperiment().getDecodeBufferCapacity());
        this.reorderBuffer = new PipelineBuffer<>(getExperiment().getReorderBufferCapacity());
        this.loadStoreQueue = new PipelineBuffer<>(getExperiment().getLoadStoreQueueCapacity());

        this.executedMnemonics = new TreeMap<>();
        this.executedSystemCalls = new TreeMap<>();

        this.getBlockingEventDispatcher().addListener(InstructionFunctionallyExecutedEvent.class, event -> {
            if (event.getContext() == context) {
                Mnemonic mnemonic = event.getStaticInstruction().getMnemonic();
                if (!executedMnemonics.containsKey(mnemonic)) {
                    executedMnemonics.put(mnemonic, 0L);
                }
                executedMnemonics.put(mnemonic, executedMnemonics.get(mnemonic) + 1);
            }
        });

        this.getBlockingEventDispatcher().addListener(SystemCallExecutedEvent.class, event -> {
            if (event.getContext() == context) {
                String systemCallName = event.getSystemCallName();
                if (!executedSystemCalls.containsKey(systemCallName)) {
                    executedSystemCalls.put(systemCallName, 0L);
                }
                executedSystemCalls.put(systemCallName, executedSystemCalls.get(systemCallName) + 1);
            }
        });
    }

    @Override
    public void updatePerCycleStats() {
        if (this.decodeBuffer.isFull()) {
            this.numDecodeBufferFullStalls++;
        }

        if (this.reorderBuffer.isFull()) {
            this.numReorderBufferFullStalls++;
        }

        if (this.loadStoreQueue.isFull()) {
            this.numLoadStoreQueueFullStalls++;
        }

        if (this.intPhysicalRegisterFile.isFull()) {
            this.numIntPhysicalRegisterFileFullStalls++;
        }

        if (this.fpPhysicalRegisterFile.isFull()) {
            this.numFpPhysicalRegisterFileFullStalls++;
        }

        if (this.miscPhysicalRegisterFile.isFull()) {
            this.numMiscPhysicalRegisterFileFullStalls++;
        }
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Core getCore() {
        return core;
    }

    @Override
    public BranchPredictor getBranchPredictor() {
        return branchPredictor;
    }

    @Override
    public PipelineBuffer<DecodeBufferEntry> getDecodeBuffer() {
        return decodeBuffer;
    }

    @Override
    public PipelineBuffer<ReorderBufferEntry> getReorderBuffer() {
        return reorderBuffer;
    }

    @Override
    public PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue() {
        return loadStoreQueue;
    }

    @Override
    public long getNumInstructions() {
        return numInstructions;
    }

    @Override
    public TranslationLookasideBuffer getItlb() {
        return itlb;
    }

    @Override
    public void setItlb(TranslationLookasideBuffer itlb) {
        this.itlb = itlb;
    }

    @Override
    public TranslationLookasideBuffer getDtlb() {
        return dtlb;
    }

    @Override
    public void setDtlb(TranslationLookasideBuffer dtlb) {
        this.dtlb = dtlb;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void incrementNumRegisterRenameStallsOnDecodeBufferIsEmpty() {
        this.numRegisterRenameStallsOnDecodeBufferIsEmpty++;
    }

    @Override
    public void incrementNumRegisterRenameStallsOnReorderBufferIsFull() {
        this.numRegisterRenameStallsOnReorderBufferIsFull++;
    }

    @Override
    public void incrementNumSelectionStallsOnCanNotLoad() {
        this.numSelectionStallsOnCanNotLoad++;
    }

    @Override
    public void incrementNumSelectionStallsOnCanNotStore() {
        this.numSelectionStallsOnCanNotStore++;
    }

    @Override
    public void incrementNumSelectionStallsOnNoFreeFunctionalUnit() {
        this.numSelectionStallsOnNoFreeFunctionalUnit++;
    }

    @Override
    public long getNumDecodeBufferFullStalls() {
        return numDecodeBufferFullStalls;
    }

    @Override
    public long getNumReorderBufferFullStalls() {
        return numReorderBufferFullStalls;
    }

    @Override
    public long getNumLoadStoreQueueFullStalls() {
        return numLoadStoreQueueFullStalls;
    }

    @Override
    public long getNumIntPhysicalRegisterFileFullStalls() {
        return numIntPhysicalRegisterFileFullStalls;
    }

    @Override
    public long getNumFpPhysicalRegisterFileFullStalls() {
        return numFpPhysicalRegisterFileFullStalls;
    }

    @Override
    public long getNumMiscPhysicalRegisterFileFullStalls() {
        return numMiscPhysicalRegisterFileFullStalls;
    }

    @Override
    public long getNumFetchStallsOnDecodeBufferIsFull() {
        return numFetchStallsOnDecodeBufferIsFull;
    }

    @Override
    public long getNumRegisterRenameStallsOnDecodeBufferIsEmpty() {
        return numRegisterRenameStallsOnDecodeBufferIsEmpty;
    }

    @Override
    public long getNumRegisterRenameStallsOnReorderBufferIsFull() {
        return numRegisterRenameStallsOnReorderBufferIsFull;
    }

    @Override
    public long getNumRegisterRenameStallsOnLoadStoreQueueFull() {
        return numRegisterRenameStallsOnLoadStoreQueueFull;
    }

    @Override
    public long getNumSelectionStallsOnCanNotLoad() {
        return numSelectionStallsOnCanNotLoad;
    }

    @Override
    public long getNumSelectionStallsOnCanNotStore() {
        return numSelectionStallsOnCanNotStore;
    }

    @Override
    public long getNumSelectionStallsOnNoFreeFunctionalUnit() {
        return numSelectionStallsOnNoFreeFunctionalUnit;
    }

    @Override
    public Map<Mnemonic, Long> getExecutedMnemonics() {
        return executedMnemonics;
    }

    @Override
    public Map<String, Long> getExecutedSystemCalls() {
        return executedSystemCalls;
    }
}
