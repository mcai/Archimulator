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
     * The ID of the abstract reorder buffer entry.
     */
    protected long id;

    /**
     * The value of the next program counter (NPC).
     */
    protected int npc;

    /**
     * The value of the next next program counter (NNPC).
     */
    protected int nnpc;

    /**
     * The predicted value of the next next program counter (predicted NNPC).
     */
    protected int predictedNnpc;

    /**
     * The thread.
     */
    protected Thread thread;

    /**
     * The dynamic instruction.
     */
    protected DynamicInstruction dynamicInstruction;

    /**
     * The map of the old physical registers.
     */
    protected Map<Integer, PhysicalRegister> oldPhysicalRegisters;

    /**
     * The map of the target physical registers.
     */
    protected Map<Integer, PhysicalRegister> targetPhysicalRegisters;

    /**
     * The map of the source physical registers.
     */
    protected Map<Integer, PhysicalRegister> sourcePhysicalRegisters;

    /**
     * A value indicating whether the reorder buffer entry is speculative or not.
     */
    protected boolean speculative;

    /**
     * The return address stack recover index.
     */
    protected int returnAddressStackRecoverIndex;

    /**
     * Branch predictor update.
     */
    protected BranchPredictorUpdate branchPredictorUpdate;

    /**
     * A value indicating whether the reorder buffer entry is dispatched or not.
     */
    protected boolean dispatched;

    /**
     * A value indicating whether the reorder buffer entry is issued or not.
     */
    protected boolean issued;

    /**
     * A value indicating whether the reorder buffer entry is completed or not.
     */
    protected boolean completed;

    /**
     * A value indicating whether the reorder buffer entry is squashed or not.
     */
    protected boolean squashed;

    private int numNotReadyOperands;

    /**
     * Create an abstract reorder buffer entry.
     *
     * @param thread the thread
     * @param dynamicInstruction the dynamic instruction
     * @param npc the value of the next program counter (NPC)
     * @param nnpc the value of the next next program counter (NNPC)
     * @param predictedNnpc the predicted value of the next next program counter (predicted NNPC)
     * @param returnAddressStackRecoverIndex the return address stack recover index
     * @param branchPredictorUpdate the branch predictor update
     * @param speculative a value indicating whether the reorder buffer entry is speculative or not
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

        this.oldPhysicalRegisters = new TreeMap<Integer, PhysicalRegister>();
        this.targetPhysicalRegisters = new TreeMap<Integer, PhysicalRegister>();
        this.sourcePhysicalRegisters = new TreeMap<Integer, PhysicalRegister>();
    }

    /**
     * Write back.
     */
    public void writeBack() {
        if (this.isNeedWriteBack()) {
            this.doWriteBack();
        }
    }

    /**
     * Get a value indicating whether the write back is needed or not.
     *
     * @return a value indicating whether the write back is needed or not
     */
    protected abstract boolean isNeedWriteBack();

    /**
     * Do the write back process.
     */
    private void doWriteBack() {
        for (Map.Entry<Integer, PhysicalRegister> entry : this.targetPhysicalRegisters.entrySet()) {
            if (entry.getKey() != 0) {
                entry.getValue().writeback();
            }
        }
    }

    /**
     * Signal the reorder buffer entry is completed.
     */
    public void signalCompleted() {
        if (!this.squashed) {
            this.thread.getCore().getOooEventQueue().add(this);
        }
    }

    /**
     * Get a value indicating whether all of the operands involved are ready.
     *
     * @return a value indicating whether all of the operands involved are ready
     */
    public boolean isAllOperandReady() {
        return this.numNotReadyOperands == 0;
    }

    /**
     * Get the ID of the reorder buffer entry.
     *
     * @return the ID of the reorder buffer entry
     */
    public long getId() {
        return id;
    }

    /**
     * Get the value of the next program counter (NPC).
     *
     * @return the value of the next program counter (NPC)
     */
    public int getNpc() {
        return npc;
    }

    /**
     * Get the value of the next next program counter (NNPC).
     *
     * @return the value of the next next program counter (NNPC)
     */
    public int getNnpc() {
        return nnpc;
    }

    /**
     * Get the predicted value of the next next program counter (predicted NNPC).
     *
     * @return the predicted value of the next next program counter (predicted NNPC)
     */
    public int getPredictedNnpc() {
        return predictedNnpc;
    }

    /**
     * Get the thread.
     *
     * @return the thread
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Get the dynamic instruction.
     *
     * @return the dynamic instruction
     */
    public DynamicInstruction getDynamicInstruction() {
        return dynamicInstruction;
    }

    /**
     * Get the map of the old physical registers.
     *
     * @return the map of the old physical registers
     */
    public Map<Integer, PhysicalRegister> getOldPhysicalRegisters() {
        return oldPhysicalRegisters;
    }

    /**
     * Get the map of the target physical registers.
     *
     * @return the map of the target physical registers
     */
    public Map<Integer, PhysicalRegister> getTargetPhysicalRegisters() {
        return targetPhysicalRegisters;
    }

    /**
     * Set the the map of the target physical registers.
     *
     * @param targetPhysicalRegisters the map of the target physical registers
     */
    public void setTargetPhysicalRegisters(Map<Integer, PhysicalRegister> targetPhysicalRegisters) {
        this.targetPhysicalRegisters = targetPhysicalRegisters;
    }

    /**
     * Get the map of the source physical registers.
     *
     * @return the map of the source physical registers
     */
    public Map<Integer, PhysicalRegister> getSourcePhysicalRegisters() {
        return sourcePhysicalRegisters;
    }

    /**
     * Set the map of the source physical registers.
     *
     * @param sourcePhysicalRegisters the map of the source physical registers
     */
    public void setSourcePhysicalRegisters(Map<Integer, PhysicalRegister> sourcePhysicalRegisters) {
        this.sourcePhysicalRegisters = sourcePhysicalRegisters;
    }

    /**
     * Get a value indicating whether the reorder buffer entry is dispatched or not.
     *
     * @return a value indicating whether the reorder buffer entry is dispatched or not
     */
    public boolean isDispatched() {
        return dispatched;
    }

    /**
     * Set the reorder buffer entry is dispatched.
     */
    public void setDispatched() {
        this.dispatched = true;
    }

    /**
     * Get a value indicating whether the reorder buffer entry is issued or not.
     *
     * @return a value indicating whether the reorder buffer entry is issued or not
     */
    public boolean isIssued() {
        return issued;
    }

    /**
     * Set the reorder buffer entry is issued.
     */
    public void setIssued() {
        this.issued = true;
    }

    /**
     * Get a value indicating whether the reorder buffer entry is completed or not.
     *
     * @return a value indicating whether the reorder buffer entry is completed or not
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Set the reorder buffer entry is completed.
     */
    public void setCompleted() {
        this.completed = true;
    }

    /**
     * Get a value indicating whether the reorder buffer entry is squashed or not.
     *
     * @return a value indicating whether the reorder buffer entry is squashed or not
     */
    public boolean isSquashed() {
        return squashed;
    }

    /**
     * Set the reorder buffer entry is squashed.
     */
    public void setSquashed() {
        this.squashed = true;
    }

    /**
     * Get a value indicating whether the reorder buffer entry is speculative or not.
     *
     * @return a value indicating whether the reorder buffer entry is speculative or not
     */
    public boolean isSpeculative() {
        return speculative;
    }

    /**
     * Get the return address stack recover index.
     *
     * @return the return address stack recover index
     */
    public int getReturnAddressStackRecoverIndex() {
        return returnAddressStackRecoverIndex;
    }

    /**
     * Get the branch predictor update.
     *
     * @return the branch predictor update
     */
    public BranchPredictorUpdate getBranchPredictorUpdate() {
        return branchPredictorUpdate;
    }

    /**
     * Get the number of "not ready" operands.
     *
     * @return the number of "not ready" operands
     */
    public int getNumNotReadyOperands() {
        return numNotReadyOperands;
    }

    /**
     * Set the number of "not ready" operands.
     *
     * @param numNotReadyOperands the number of "not ready" operands
     */
    public void setNumNotReadyOperands(int numNotReadyOperands) {
        this.numNotReadyOperands = numNotReadyOperands;
    }
}
