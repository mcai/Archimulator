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
package archimulator.sim.os;

import archimulator.model.ContextMapping;
import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.core.Processor;
import archimulator.sim.core.Thread;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.os.signal.SignalMasks;

import java.io.Serializable;
import java.util.Stack;

/**
 *
 * @author Min Cai
 */
public class Context extends BasicSimulationObject implements SimulationObject, Serializable {
    private int id;

    private ContextState state;

    private SignalMasks signalMasks;

    private int signalFinish;

    private ArchitecturalRegisterFile registerFile;

    private Kernel kernel;

    private int threadId = -1;

    private int userId;
    private int effectiveUserId;
    private int groupId;
    private int effectiveGroupId;
    private int processId;

    private Process process;
    private Context parent;

    private Stack<FunctionCallContext> functionCallContextStack;

    private boolean pseudoCallEncounteredInLastInstructionExecution;

    private boolean speculative;

    private ArchitecturalRegisterFile speculativeRegisterFile;

    /**
     *
     * @param kernel
     * @param simulationDirectory
     * @param contextMapping
     * @return
     */
    public static Context load(Kernel kernel, String simulationDirectory, ContextMapping contextMapping) {
        Process process = new BasicProcess(kernel, simulationDirectory, contextMapping);

        ArchitecturalRegisterFile regs = new ArchitecturalRegisterFile(process.isLittleEndian());
        regs.setNpc(process.getProgramEntry());
        regs.setNnpc(regs.getNpc() + 4);
        regs.setGpr(ArchitecturalRegisterFile.REGISTER_SP, process.getEnvironmentBase());

        return new Context(kernel, process, null, regs, 0);
    }

    /**
     *
     * @param parent
     * @param registerFile
     * @param signalFinish
     */
    public Context(Context parent, ArchitecturalRegisterFile registerFile, int signalFinish) {
        this(parent.kernel, parent.process, parent, registerFile, signalFinish);
    }

    /**
     *
     * @param kernel
     * @param process
     * @param parent
     * @param registerFile
     * @param signalFinish
     */
    public Context(Kernel kernel, Process process, Context parent, ArchitecturalRegisterFile registerFile, int signalFinish) {
        super(kernel);

        this.kernel = kernel;

        this.parent = parent;

        this.registerFile = registerFile;
        this.signalFinish = signalFinish;

        this.id = kernel.currentContextId++;

        this.userId = (int) NativeSystemCalls.LIBC.getuid();
        this.effectiveUserId = (int) NativeSystemCalls.LIBC.geteuid();
        this.groupId = (int) NativeSystemCalls.LIBC.getgid();
        this.effectiveGroupId = (int) NativeSystemCalls.LIBC.getegid();

        this.processId = kernel.currentPid++;

        this.signalMasks = new SignalMasks();

        this.state = ContextState.IDLE;

        this.functionCallContextStack = new Stack<FunctionCallContext>();

        this.process = process;
    }

    /**
     *
     */
    public void enterSpeculativeState() {
        try {
            this.process.getMemory().enterSpeculativeState();
            this.speculativeRegisterFile = (ArchitecturalRegisterFile) this.registerFile.clone();
            this.speculative = true;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public void exitSpeculativeState() {
        this.process.getMemory().exitSpeculativeState();
        this.speculativeRegisterFile = null;
        this.speculative = false;
    }

    /**
     *
     * @return
     */
    public StaticInstruction decodeNextInstruction() {
        this.getRegisterFile().setPc(this.getRegisterFile().getNpc());
        this.getRegisterFile().setNpc(this.getRegisterFile().getNnpc());
        this.getRegisterFile().setNnpc(this.getRegisterFile().getNnpc() + 4);
        this.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_ZERO, 0);

        this.pseudoCallEncounteredInLastInstructionExecution = false;

        return this.decode(this.getRegisterFile().getPc());
    }

    /**
     *
     * @param mappedPc
     * @return
     */
    protected StaticInstruction decode(int mappedPc) {
        return this.process.getStaticInstruction(mappedPc);
    }

    /**
     *
     */
    public void suspend() {
        if ((this.state == ContextState.BLOCKED)) {
            throw new IllegalArgumentException();
        }
        this.state = ContextState.BLOCKED;

//        Logger.infof(Logger.THREAD, "%s: thread suspended\n", this.getThread().getName());
    }

    /**
     *
     */
    public void resume() {
        if ((this.state != ContextState.BLOCKED)) {
            throw new IllegalArgumentException();
        }
        this.state = ContextState.RUNNING;

//        Logger.infof(Logger.THREAD, "%s: thread resumed\n", this.getThread().getName());
    }

    /**
     *
     */
    public void finish() {
        if (this.state == ContextState.FINISHED) {
            throw new IllegalArgumentException();
        }

        this.state = ContextState.FINISHED;

        for (Context context : this.kernel.getContexts()) {
            if (context.getState() != ContextState.FINISHED && context.getParent() == this) {
                context.finish();
            }
        }

        if (this.signalFinish != 0 && this.parent != null) {
            this.parent.getSignalMasks().getPending().set(this.signalFinish);
        }

//        Logger.infof(Logger.THREAD, "%s: thread finished\n", this.getThread().getName());
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
    public ContextState getState() {
        return state;
    }

    /**
     *
     * @param state
     */
    public void setState(ContextState state) {
        this.state = state;
    }

    /**
     *
     * @return
     */
    public ArchitecturalRegisterFile getRegisterFile() {
        return !speculative ? registerFile : speculativeRegisterFile;
    }

    /**
     *
     * @param registerFile
     */
    public void setRegisterFile(ArchitecturalRegisterFile registerFile) {
        this.registerFile = registerFile;
    }

    /**
     *
     * @return
     */
    public SignalMasks getSignalMasks() {
        return signalMasks;
    }

    /**
     *
     * @return
     */
    public Context getParent() {
        return parent;
    }

    /**
     *
     * @return
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     *
     * @return
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     *
     * @param threadId
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     *
     * @param processor
     * @return
     */
    public Thread getThread(Processor processor) {
        int coreNum = this.threadId / processor.getCores().size();
        int threadNum = this.threadId % processor.getCores().size();

        return processor.getCores().get(coreNum).getThreads().get(threadNum);
    }

    /**
     *
     * @return
     */
    public Process getProcess() {
        return process;
    }

    /**
     *
     * @return
     */
    public int getUserId() {
        return userId;
    }

    /**
     *
     * @return
     */
    public int getEffectiveUserId() {
        return effectiveUserId;
    }

    /**
     *
     * @return
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     *
     * @return
     */
    public int getEffectiveGroupId() {
        return effectiveGroupId;
    }

    /**
     *
     * @return
     */
    public int getProcessId() {
        return processId;
    }

    /**
     *
     * @return
     */
    public int getParentProcessId() {
        return parent == null ? 1 : parent.getProcessId();
    }

    /**
     *
     * @return
     */
    public Stack<FunctionCallContext> getFunctionCallContextStack() {
        return functionCallContextStack;
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
    public boolean useICache() {
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isPseudoCallEncounteredInLastInstructionExecution() {
        return pseudoCallEncounteredInLastInstructionExecution;
    }

    /**
     *
     * @param pseudoCallEncounteredInLastInstructionExecution
     */
    public void setPseudoCallEncounteredInLastInstructionExecution(boolean pseudoCallEncounteredInLastInstructionExecution) {
        this.pseudoCallEncounteredInLastInstructionExecution = pseudoCallEncounteredInLastInstructionExecution;
    }
}
