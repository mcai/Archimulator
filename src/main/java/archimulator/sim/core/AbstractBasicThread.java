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
import archimulator.sim.core.bpred.*;
import archimulator.sim.isa.*;
import archimulator.sim.isa.event.InstructionFunctionallyExecutedEvent;
import archimulator.sim.isa.event.SystemCallExecutedEvent;
import archimulator.sim.os.Context;
import archimulator.sim.uncore.tlb.TranslationLookasideBuffer;
import net.pickapack.action.Action1;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Min Cai
 */
public abstract class AbstractBasicThread extends BasicSimulationObject implements Thread {
    /**
     *
     */
    protected int num;
    /**
     *
     */
    protected Context context;
    /**
     *
     */
    protected int id;
    /**
     *
     */
    protected String name;
    /**
     *
     */
    protected Core core;

    /**
     *
     */
    protected BranchPredictor branchPredictor;

    /**
     *
     */
    protected RegisterRenameTable renameTable;

    /**
     *
     */
    protected PipelineBuffer<DecodeBufferEntry> decodeBuffer;
    /**
     *
     */
    protected PipelineBuffer<ReorderBufferEntry> reorderBuffer;
    /**
     *
     */
    protected PipelineBuffer<LoadStoreQueueEntry> loadStoreQueue;

    private TranslationLookasideBuffer itlb;
    private TranslationLookasideBuffer dtlb;

    /**
     *
     */
    protected PhysicalRegisterFile intPhysicalRegisterFile;
    /**
     *
     */
    protected PhysicalRegisterFile fpPhysicalRegisterFile;
    /**
     *
     */
    protected PhysicalRegisterFile miscPhysicalRegisterFile;

    /**
     *
     */
    protected long numInstructions;

    /**
     *
     */
    protected long numDecodeBufferFullStalls;
    /**
     *
     */
    protected long numReorderBufferFullStalls;
    /**
     *
     */
    protected long numLoadStoreQueueFullStalls;

    /**
     *
     */
    protected long numIntPhysicalRegisterFileFullStalls;
    /**
     *
     */
    protected long numFpPhysicalRegisterFileFullStalls;
    /**
     *
     */
    protected long numMiscPhysicalRegisterFileFullStalls;

    /**
     *
     */
    protected long numFetchStallsOnDecodeBufferIsFull;

    /**
     *
     */
    protected long numRegisterRenameStallsOnDecodeBufferIsEmpty;
    /**
     *
     */
    protected long numRegisterRenameStallsOnReorderBufferIsFull;
    /**
     *
     */
    protected long numRegisterRenameStallsOnLoadStoreQueueFull;

    /**
     *
     */
    protected long numSelectionStallsOnCanNotLoad;
    /**
     *
     */
    protected long numSelectionStallsOnCanNotStore;
    /**
     *
     */
    protected long numSelectionStallsOnNoFreeFunctionalUnit;

    private Map<Mnemonic, Long> executedMnemonics;
    private Map<String, Long> executedSystemCalls;

    /**
     *
     * @param core
     * @param num
     */
    public AbstractBasicThread(Core core, int num) {
        super(core);

        this.core = core;

        this.num = num;
        this.id = this.core.getNum() * getExperiment().getArchitecture().getNumThreadsPerCore() + this.num;

        this.name = "c" + this.core.getNum() + "t" + this.num;

        switch (getExperiment().getArchitecture().getBranchPredictorType()) {
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

        this.intPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/intPhysicalRegisterFile", getExperiment().getArchitecture().getPhysicalRegisterFileCapacity());
        this.fpPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/fpPhysicalRegisterFile", getExperiment().getArchitecture().getPhysicalRegisterFileCapacity());
        this.miscPhysicalRegisterFile = new PhysicalRegisterFile(this.name + "/miscPhysicalRegisterFile", getExperiment().getArchitecture().getPhysicalRegisterFileCapacity());

        this.renameTable = new RegisterRenameTable(this.name + "/renameTable");

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_INT_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.INTEGER, i);
            PhysicalRegisterFile.PhysicalRegister physReg = this.intPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_FLOAT_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.FLOAT, i);
            PhysicalRegisterFile.PhysicalRegister physReg = this.fpPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        for (int i = 0; i < ArchitecturalRegisterFile.NUM_MISC_REGISTERS; i++) {
            int dep = RegisterDependencyType.toRegisterDependency(RegisterDependencyType.MISC, i);
            PhysicalRegisterFile.PhysicalRegister physReg = this.miscPhysicalRegisterFile.getRegisters().get(i);
            physReg.reserve(dep);
            this.renameTable.put(dep, physReg);
        }

        this.decodeBuffer = new PipelineBuffer<DecodeBufferEntry>(getExperiment().getArchitecture().getDecodeBufferCapacity());
        this.reorderBuffer = new PipelineBuffer<ReorderBufferEntry>(getExperiment().getArchitecture().getReorderBufferCapacity());
        this.loadStoreQueue = new PipelineBuffer<LoadStoreQueueEntry>(getExperiment().getArchitecture().getLoadStoreQueueCapacity());

        this.executedMnemonics = new TreeMap<Mnemonic, Long>();
        this.executedSystemCalls = new TreeMap<String, Long>();

        this.getBlockingEventDispatcher().addListener(InstructionFunctionallyExecutedEvent.class, new Action1<InstructionFunctionallyExecutedEvent>() {
            @Override
            public void apply(InstructionFunctionallyExecutedEvent event) {
                if (event.getContext() == context) {
                    Mnemonic mnemonic = event.getStaticInstruction().getMnemonic();
                    if (!executedMnemonics.containsKey(mnemonic)) {
                        executedMnemonics.put(mnemonic, 0L);
                    }
                    executedMnemonics.put(mnemonic, executedMnemonics.get(mnemonic) + 1);
                }
            }
        });

        this.getBlockingEventDispatcher().addListener(SystemCallExecutedEvent.class, new Action1<SystemCallExecutedEvent>() {
            @Override
            public void apply(SystemCallExecutedEvent event) {
                if (event.getContext() == context) {
                    String systemCallName = event.getSystemCallName();
                    if (!executedSystemCalls.containsKey(systemCallName)) {
                        executedSystemCalls.put(systemCallName, 0L);
                    }
                    executedSystemCalls.put(systemCallName, executedSystemCalls.get(systemCallName) + 1);
                }
            }
        });
    }

    /**
     *
     */
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
    public int getId() {
        return id;
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
    public Core getCore() {
        return core;
    }

    /**
     *
     * @return
     */
    @Override
    public BranchPredictor getBranchPredictor() {
        return branchPredictor;
    }

    /**
     *
     * @return
     */
    public PipelineBuffer<DecodeBufferEntry> getDecodeBuffer() {
        return decodeBuffer;
    }

    /**
     *
     * @return
     */
    public PipelineBuffer<ReorderBufferEntry> getReorderBuffer() {
        return reorderBuffer;
    }

    /**
     *
     * @return
     */
    public PipelineBuffer<LoadStoreQueueEntry> getLoadStoreQueue() {
        return loadStoreQueue;
    }

    /**
     *
     * @return
     */
    public long getNumInstructions() {
        return numInstructions;
    }

    /**
     *
     * @return
     */
    public TranslationLookasideBuffer getItlb() {
        return itlb;
    }

    /**
     *
     * @param itlb
     */
    public void setItlb(TranslationLookasideBuffer itlb) {
        this.itlb = itlb;
    }

    /**
     *
     * @return
     */
    public TranslationLookasideBuffer getDtlb() {
        return dtlb;
    }

    /**
     *
     * @param dtlb
     */
    public void setDtlb(TranslationLookasideBuffer dtlb) {
        this.dtlb = dtlb;
    }

    /**
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     *
     * @param context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     *
     */
    public void incrementNumRegisterRenameStallsOnDecodeBufferIsEmpty() {
        this.numRegisterRenameStallsOnDecodeBufferIsEmpty++;
    }

    /**
     *
     */
    public void incrementNumRegisterRenameStallsOnReorderBufferIsFull() {
        this.numRegisterRenameStallsOnReorderBufferIsFull++;
    }

    /**
     *
     */
    public void incrementNumSelectionStallsOnCanNotLoad() {
        this.numSelectionStallsOnCanNotLoad++;
    }

    /**
     *
     */
    public void incrementNumSelectionStallsOnCanNotStore() {
        this.numSelectionStallsOnCanNotStore++;
    }

    /**
     *
     */
    public void incrementNumSelectionStallsOnNoFreeFunctionalUnit() {
        this.numSelectionStallsOnNoFreeFunctionalUnit++;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumDecodeBufferFullStalls() {
        return numDecodeBufferFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumReorderBufferFullStalls() {
        return numReorderBufferFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumLoadStoreQueueFullStalls() {
        return numLoadStoreQueueFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumIntPhysicalRegisterFileFullStalls() {
        return numIntPhysicalRegisterFileFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumFpPhysicalRegisterFileFullStalls() {
        return numFpPhysicalRegisterFileFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumMiscPhysicalRegisterFileFullStalls() {
        return numMiscPhysicalRegisterFileFullStalls;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumFetchStallsOnDecodeBufferIsFull() {
        return numFetchStallsOnDecodeBufferIsFull;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumRegisterRenameStallsOnDecodeBufferIsEmpty() {
        return numRegisterRenameStallsOnDecodeBufferIsEmpty;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumRegisterRenameStallsOnReorderBufferIsFull() {
        return numRegisterRenameStallsOnReorderBufferIsFull;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumRegisterRenameStallsOnLoadStoreQueueFull() {
        return numRegisterRenameStallsOnLoadStoreQueueFull;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumSelectionStallsOnCanNotLoad() {
        return numSelectionStallsOnCanNotLoad;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumSelectionStallsOnCanNotStore() {
        return numSelectionStallsOnCanNotStore;
    }

    /**
     *
     * @return
     */
    @Override
    public long getNumSelectionStallsOnNoFreeFunctionalUnit() {
        return numSelectionStallsOnNoFreeFunctionalUnit;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<Mnemonic, Long> getExecutedMnemonics() {
        return executedMnemonics;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, Long> getExecutedSystemCalls() {
        return executedSystemCalls;
    }
}
