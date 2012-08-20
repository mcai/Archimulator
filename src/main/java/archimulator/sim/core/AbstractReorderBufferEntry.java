package archimulator.sim.core;

import archimulator.sim.core.bpred.BranchPredictorUpdate;

import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractReorderBufferEntry {
    protected long id;

    protected int npc;
    protected int nnpc;
    protected int predNnpc;

    protected Thread thread;

    protected DynamicInstruction dynamicInst;

    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> oldPhysRegs;

    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> physRegs;
    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> srcPhysRegs;

    protected boolean speculative;

    protected int returnAddressStackRecoverIndex;
    protected BranchPredictorUpdate dirUpdate;

    protected boolean dispatched;
    protected boolean issued;
    protected boolean completed;
    protected boolean squashed;

    private int numNotReadyOperands;

    public AbstractReorderBufferEntry(Thread thread, DynamicInstruction dynamicInst, int npc, int nnpc, int predNnpc, int returnAddressStackRecoverIndex, BranchPredictorUpdate dirUpdate, boolean speculative) {
        this.id = thread.getSimulation().currentReorderBufferEntryId++;

        this.thread = thread;

        this.dynamicInst = dynamicInst;

        this.npc = npc;
        this.nnpc = nnpc;
        this.predNnpc = predNnpc;
        this.returnAddressStackRecoverIndex = returnAddressStackRecoverIndex;
        this.dirUpdate = dirUpdate;
        this.speculative = speculative;

        this.oldPhysRegs = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
        this.physRegs = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
        this.srcPhysRegs = new TreeMap<Integer, PhysicalRegisterFile.PhysicalRegister>();
    }

    public void writeBack() {
        if (this.isNeedWriteBack()) {
            this.doWriteBack();
        }
    }

    protected abstract boolean isNeedWriteBack();

    private void doWriteBack() {
        for (Map.Entry<Integer, PhysicalRegisterFile.PhysicalRegister> entry : this.physRegs.entrySet()) {
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

    public int getPredNnpc() {
        return predNnpc;
    }

    public Thread getThread() {
        return thread;
    }

    public DynamicInstruction getDynamicInst() {
        return dynamicInst;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getOldPhysRegs() {
        return oldPhysRegs;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getPhysRegs() {
        return physRegs;
    }

    public void setPhysRegs(Map<Integer, PhysicalRegisterFile.PhysicalRegister> physRegs) {
        this.physRegs = physRegs;
    }

    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getSrcPhysRegs() {
        return srcPhysRegs;
    }

    public void setSrcPhysRegs(Map<Integer, PhysicalRegisterFile.PhysicalRegister> srcPhysRegs) {
        this.srcPhysRegs = srcPhysRegs;
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

    public BranchPredictorUpdate getDirUpdate() {
        return dirUpdate;
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
