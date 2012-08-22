package archimulator.sim.core;

import archimulator.sim.core.bpred.BranchPredictorUpdate;

import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractReorderBufferEntry {
    protected long id;

    protected int npc;
    protected int nnpc;
    protected int predictedNnpc;

    protected Thread thread;

    protected DynamicInstruction dynamicInstruction;

    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> oldPhysicalRegisters;

    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> targetPhysicalRegisters;
    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> sourcePhysicalRegisters;

    protected boolean speculative;

    protected int returnAddressStackRecoverIndex;
    protected BranchPredictorUpdate branchPredictorUpdate;

    protected boolean dispatched;
    protected boolean issued;
    protected boolean completed;
    protected boolean squashed;

    private int numNotReadyOperands;

    public AbstractReorderBufferEntry(Thread thread, DynamicInstruction dynamicInstruction, int npc, int nnpc, int predictedNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate branchPredictorUpdate, boolean speculative) {
        this.id = thread.getSimulation().currentReorderBufferEntryId++;

        this.thread = thread;

        this.dynamicInstruction = dynamicInstruction;

        this.npc = npc;
        this.nnpc = nnpc;
        this.predictedNnpc = predictedNnpc;
        this.returnAddressStackRecoverIndex = returnAddressStackRecoverIndex;
        this.branchPredictorUpdate = branchPredictorUpdate;
        this.speculative = speculative;

        this.oldPhysicalRegisters = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
        this.targetPhysicalRegisters = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
        this.sourcePhysicalRegisters = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
    }

    public void writeBack() {
        if (this.isNeedWriteBack()) {
            this.doWriteBack();
        }
    }

    protected abstract boolean isNeedWriteBack();

    private void doWriteBack() {
        for (Map.Entry<Integer, PhysicalRegisterFile.PhysicalRegister> entry : this.targetPhysicalRegisters.entrySet()) {
            if (entry.getKey() != 0) {
                entry.getValue().writeback();
            }
        }
    }

    public void signalCompleted() {
        if (!this.squashed) {
            this.thread.getCore().getOooEventQueue().add(this);
        }
    }

    public boolean isAllOperandReady() {
        return this.numNotReadyOperands == 0;
    }

    public long getId() {
        return id;
    }

    public int getNpc() {
        return npc;
    }

    public int getNnpc() {
        return nnpc;
    }

    public int getPredictedNnpc() {
        return predictedNnpc;
    }

    public Thread getThread() {
        return thread;
    }

    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getOldPhysicalRegisters() {
        return oldPhysicalRegisters;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getTargetPhysicalRegisters() {
        return targetPhysicalRegisters;
    }

    public void setTargetPhysicalRegisters(Map<Integer, PhysicalRegisterFile.PhysicalRegister> targetPhysicalRegisters) {
        this.targetPhysicalRegisters = targetPhysicalRegisters;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getSourcePhysicalRegisters() {
        return sourcePhysicalRegisters;
    }

    public void setSourcePhysicalRegisters(Map<Integer, PhysicalRegisterFile.PhysicalRegister> sourcePhysicalRegisters) {
        this.sourcePhysicalRegisters = sourcePhysicalRegisters;
    }

    public boolean isDispatched() {
        return dispatched;
    }

    public void setDispatched() {
        this.dispatched = true;
    }

    public boolean isIssued() {
        return issued;
    }

    public void setIssued() {
        this.issued = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public boolean isSpeculative() {
        return speculative;
    }

    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    public BranchPredictorUpdate getBranchPredictorUpdate() {
        return branchPredictorUpdate;
    }

    public boolean isSquashed() {
        return squashed;
    }

    public void setSquashed() {
        this.squashed = true;
    }

    public int getNumNotReadyOperands() {
        return numNotReadyOperands;
    }

    public void setNumNotReadyOperands(int numNotReadyOperands) {
        this.numNotReadyOperands = numNotReadyOperands;
    }
}
