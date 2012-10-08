package archimulator.sim.core;

import archimulator.sim.core.bpred.BranchPredictorUpdate;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Min Cai
 */
public abstract class AbstractReorderBufferEntry {
    /**
     *
     */
    protected long id;

    /**
     *
     */
    protected int npc;
    /**
     *
     */
    protected int nnpc;
    /**
     *
     */
    protected int predictedNnpc;

    /**
     *
     */
    protected Thread thread;

    /**
     *
     */
    protected DynamicInstruction dynamicInstruction;

    /**
     *
     */
    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> oldPhysicalRegisters;

    /**
     *
     */
    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> targetPhysicalRegisters;
    /**
     *
     */
    protected Map<Integer, PhysicalRegisterFile.PhysicalRegister> sourcePhysicalRegisters;

    /**
     *
     */
    protected boolean speculative;

    /**
     *
     */
    protected int returnAddressStackRecoverIndex;
    /**
     *
     */
    protected BranchPredictorUpdate branchPredictorUpdate;

    /**
     *
     */
    protected boolean dispatched;
    /**
     *
     */
    protected boolean issued;
    /**
     *
     */
    protected boolean completed;
    /**
     *
     */
    protected boolean squashed;

    private int numNotReadyOperands;

    /**
     *
     * @param thread
     * @param dynamicInstruction
     * @param npc
     * @param nnpc
     * @param predictedNnpc
     * @param returnAddressStackRecoverIndex
     * @param branchPredictorUpdate
     * @param speculative
     */
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

    /**
     *
     */
    public void writeBack() {
        if (this.isNeedWriteBack()) {
            this.doWriteBack();
        }
    }

    /**
     *
     * @return
     */
    protected abstract boolean isNeedWriteBack();

    private void doWriteBack() {
        for (Map.Entry<Integer, PhysicalRegisterFile.PhysicalRegister> entry : this.targetPhysicalRegisters.entrySet()) {
            if (entry.getKey() != 0) {
                entry.getValue().writeback();
            }
        }
    }

    /**
     *
     */
    public void signalCompleted() {
        if (!this.squashed) {
            this.thread.getCore().getOooEventQueue().add(this);
        }
    }

    /**
     *
     * @return
     */
    public boolean isAllOperandReady() {
        return this.numNotReadyOperands == 0;
    }

    /**
     *
     * @return
     */
    public long getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public int getNpc() {
        return npc;
    }

    /**
     *
     * @return
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     *
     * @return
     */
    public int getPredictedNnpc() {
        return predictedNnpc;
    }

    /**
     *
     * @return
     */
    public Thread getThread() {
        return thread;
    }

    /**
     *
     * @return
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    /**
     *
     * @return
     */
    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getOldPhysicalRegisters() {
        return oldPhysicalRegisters;
    }

    /**
     *
     * @return
     */
    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getTargetPhysicalRegisters() {
        return targetPhysicalRegisters;
    }

    /**
     *
     * @param targetPhysicalRegisters
     */
    public void setTargetPhysicalRegisters(Map<Integer, PhysicalRegisterFile.PhysicalRegister> targetPhysicalRegisters) {
        this.targetPhysicalRegisters = targetPhysicalRegisters;
    }

    /**
     *
     * @return
     */
    public Map<Integer, PhysicalRegisterFile.PhysicalRegister> getSourcePhysicalRegisters() {
        return sourcePhysicalRegisters;
    }

    /**
     *
     * @param sourcePhysicalRegisters
     */
    public void setSourcePhysicalRegisters(Map<Integer, PhysicalRegisterFile.PhysicalRegister> sourcePhysicalRegisters) {
        this.sourcePhysicalRegisters = sourcePhysicalRegisters;
    }

    /**
     *
     * @return
     */
    public boolean isDispatched() {
        return dispatched;
    }

    /**
     *
     */
    public void setDispatched() {
        this.dispatched = true;
    }

    /**
     *
     * @return
     */
    public boolean isIssued() {
        return issued;
    }

    /**
     *
     */
    public void setIssued() {
        this.issued = true;
    }

    /**
     *
     * @return
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     *
     */
    public void setCompleted() {
        this.completed = true;
    }

    /**
     *
     * @return
     */
    public boolean isSpeculative() {
        return speculative;
    }

    /**
     *
     * @return
     */
    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    /**
     *
     * @return
     */
    public BranchPredictorUpdate getBranchPredictorUpdate() {
        return branchPredictorUpdate;
    }

    /**
     *
     * @return
     */
    public boolean isSquashed() {
        return squashed;
    }

    /**
     *
     */
    public void setSquashed() {
        this.squashed = true;
    }

    /**
     *
     * @return
     */
    public int getNumNotReadyOperands() {
        return numNotReadyOperands;
    }

    /**
     *
     * @param numNotReadyOperands
     */
    public void setNumNotReadyOperands(int numNotReadyOperands) {
        this.numNotReadyOperands = numNotReadyOperands;
    }
}
