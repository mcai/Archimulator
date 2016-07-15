/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.os;

import archimulator.common.*;
import archimulator.core.Processor;
import archimulator.core.Thread;
import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.StaticInstruction;
import archimulator.os.signal.SignalMasks;

import java.io.Serializable;
import java.util.Stack;

/**
 * Context.
 *
 * @author Min Cai
 */
public class Context extends BasicSimulationObject<CPUExperiment, Simulation>
        implements SimulationObject<CPUExperiment, Simulation>, Serializable {
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
     * Create a context.
     *
     * @param kernel              the kernel
     * @param contextMapping      the context mapping
     * @return the newly created context
     */
    public static Context load(Kernel kernel, ContextMapping contextMapping) {
        Process process = new BasicProcess(kernel, contextMapping);

        ArchitecturalRegisterFile regs = new ArchitecturalRegisterFile(process.isLittleEndian());
        regs.setNpc(process.getProgramEntry());
        regs.setNnpc(regs.getNpc() + 4);
        regs.setGpr(ArchitecturalRegisterFile.REGISTER_SP, process.getEnvironmentBase());

        return new Context(kernel, process, null, regs, 0);
    }

    /**
     * Create a context.
     *
     * @param parent       the parent context
     * @param registerFile the architectural register file
     * @param signalFinish the "finish" signal
     */
    public Context(Context parent, ArchitecturalRegisterFile registerFile, int signalFinish) {
        this(parent.kernel, parent.process, parent, registerFile, signalFinish);
    }

    /**
     * Create a context.
     *
     * @param kernel       the kernel
     * @param process      the process
     * @param parent       the parent context
     * @param registerFile the architectural register file
     * @param signalFinish the "finish" signal
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

        this.functionCallContextStack = new Stack<>();

        this.process = process;

        this.getBlockingEventDispatcher().dispatch(new ContextCreatedEvent(this));
    }

    /**
     * Enter the speculative state.
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
     * Exit the speculative state.
     */
    public void exitSpeculativeState() {
        this.process.getMemory().exitSpeculativeState();
        this.speculativeRegisterFile = null;
        this.speculative = false;
    }

    /**
     * Decode and return the next static instruction.
     *
     * @return the next static instruction
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
     * Decode the static instruction at the specified mapped PC (program counter).
     *
     * @param mappedPc the mapped PC (program counter)
     * @return the static instruction at the specified mapped PC (program counter)
     */
    protected StaticInstruction decode(int mappedPc) {
        return this.process.getStaticInstruction(mappedPc);
    }

    /**
     * Suspend the running of the context.
     */
    public void suspend() {
        if ((this.state == ContextState.BLOCKED)) {
            throw new IllegalArgumentException();
        }
        this.state = ContextState.BLOCKED;

//        Logger.infof(Logger.THREAD, "%s: thread suspended\n", this.getThread().getName());
    }

    /**
     * Resume the running of the context.
     */
    public void resume() {
        if ((this.state != ContextState.BLOCKED)) {
            throw new IllegalArgumentException();
        }
        this.state = ContextState.RUNNING;

//        Logger.infof(Logger.THREAD, "%s: thread resumed\n", this.getThread().getName());
    }

    /**
     * Finish the running of the context.
     */
    public void finish() {
        if (this.state == ContextState.FINISHED) {
            throw new IllegalArgumentException();
        }

        this.state = ContextState.FINISHED;

        this.kernel.getContexts().stream().filter(context -> context.getState() != ContextState.FINISHED && context.getParent() == this).forEach(Context::finish);

        if (this.signalFinish != 0 && this.parent != null) {
            this.parent.getSignalMasks().getPending().set(this.signalFinish);
        }

//        Logger.infof(Logger.THREAD, "%s: thread finished\n", this.getThread().getName());
    }

    /**
     * Get the context's ID.
     *
     * @return the context's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the context's state.
     *
     * @return the context's state
     */
    public ContextState getState() {
        return state;
    }

    /**
     * Set the context's state.
     *
     * @param state the context's state
     */
    public void setState(ContextState state) {
        this.state = state;
    }

    /**
     * Get the currently in-use architectural register file, depending on whether the context is currently in the speculative mode or not.
     *
     * @return the currently in-use architectural register file, depending on whether the context is currently in the speculative mode or not
     */
    public ArchitecturalRegisterFile getRegisterFile() {
        return !speculative ? registerFile : speculativeRegisterFile;
    }

    /**
     * Set the architectural register file.
     *
     * @param registerFile the architectural register file
     */
    public void setRegisterFile(ArchitecturalRegisterFile registerFile) {
        this.registerFile = registerFile;
    }

    /**
     * Get the signal masks.
     *
     * @return the signal masks
     */
    public SignalMasks getSignalMasks() {
        return signalMasks;
    }

    /**
     * Get the parent context.
     *
     * @return the parent context if any exists; otherwise null
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Get the kernel creating the context.
     *
     * @return the kernel creating the context
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Get the ID of the hardware thread that the context is mapped to.
     *
     * @return the ID of the hardware thread that the context is mapped to
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Set the ID of the hardware thread that the context is mapped to.
     *
     * @param threadId the ID of the hardware thread that the context is mapped to
     */
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Get the hardware thread that the context is mapped to.
     *
     * @param processor the processor object
     * @return the hardware thread that the context is mapped to
     */
    public Thread getThread(Processor processor) {
        int coreNum = this.threadId / processor.getCores().size();
        int threadNum = this.threadId % processor.getCores().size();

        return processor.getCores().get(coreNum).getThreads().get(threadNum);
    }

    /**
     * Get the process.
     *
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Get the user ID.
     *
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Get the effective user ID.
     *
     * @return the effective user ID
     */
    public int getEffectiveUserId() {
        return effectiveUserId;
    }

    /**
     * Get the group ID.
     *
     * @return the group ID
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Get the effective group ID.
     *
     * @return the effective group ID
     */
    public int getEffectiveGroupId() {
        return effectiveGroupId;
    }

    /**
     * Get the process's ID.
     *
     * @return the process's ID
     */
    public int getProcessId() {
        return processId;
    }

    /**
     * Get the parent process's ID.
     *
     * @return the parent process's ID
     */
    public int getParentProcessId() {
        return parent == null ? 1 : parent.getProcessId();
    }

    /**
     * Get the stack of the function call contexts.
     *
     * @return the stack of the function call contexts
     */
    public Stack<FunctionCallContext> getFunctionCallContextStack() {
        return functionCallContextStack;
    }

    /**
     * Get a value indicating whether the context is currently in the speculative mode or not.
     *
     * @return a value indicating whether the context is currently in the speculative mode or not
     */
    public boolean isSpeculative() {
        return speculative;
    }

    /**
     * Get a value indicating whether the context is fetching instructions from the L1I cache or not (typically in the speculative execution scheme's hardware constructed context/thread).
     *
     * @return a value indicating whether the context is fetching instructions from the L1I cache or not
     */
    public boolean useICache() {
        return true;
    }

    /**
     * Get a value indicating whether a pseudocall is encountered in the execution of the last instruction or not.
     *
     * @return a value indicating whether a pseudocall is encountered in the execution of the last instruction or not
     */
    public boolean isPseudoCallEncounteredInLastInstructionExecution() {
        return pseudoCallEncounteredInLastInstructionExecution;
    }

    /**
     * Set a value indicating whether a pseudocall is encountered in the execution of the last instruction or not.
     *
     * @param pseudoCallEncounteredInLastInstructionExecution a value indicating whether a pseudocall is encountered in  the execution of the last instruction or not
     */
    public void setPseudoCallEncounteredInLastInstructionExecution(boolean pseudoCallEncounteredInLastInstructionExecution) {
        this.pseudoCallEncounteredInLastInstructionExecution = pseudoCallEncounteredInLastInstructionExecution;
    }

    @Override
    public String getName() {
        return "ctx" + threadId;
    }
}
