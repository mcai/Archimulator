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
package archimulator.os;

import archimulator.core.Processor;
import archimulator.core.Thread;
import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.StaticInstruction;
import archimulator.os.signal.SignalMasks;
import archimulator.sim.BasicSimulationObject;
import archimulator.sim.ContextConfig;
import archimulator.sim.SimulationObject;

import java.io.Serializable;
import java.util.Stack;

public class Context extends BasicSimulationObject implements SimulationObject, Serializable {
    private int id;

    private ContextState state;

    private SignalMasks signalMasks;

    private int sigFinish;

    private ArchitecturalRegisterFile regs;

    private Kernel kernel;

    private int threadId = -1;

    private int uid;
    private int euid;
    private int gid;
    private int egid;
    private int pid;

    private Process process;
    private Context parent;

    private Stack<Integer> functionCallPcStack;

    private boolean pseudocallEncounteredInLastInstructionExecution;

    private transient boolean speculative;

    private transient ArchitecturalRegisterFile speculativeRegs;

    public static Context load(Kernel kernel, String simulationDirectory, ContextConfig contextConfig) {
        Process process = new BasicProcess(kernel, simulationDirectory, contextConfig);
//        Process process = new NativeEmulatorEnhancedProcess(kernel, simulationDirectory, contextConfig);

        ArchitecturalRegisterFile regs = new ArchitecturalRegisterFile(process.isLittleEndian());
        regs.setNpc(process.getProgramEntry());
        regs.setNnpc(regs.getNpc() + 4);
        regs.setGpr(ArchitecturalRegisterFile.REG_SP, process.getEnvironBase());

        return new Context(kernel, process, null, regs, 0);
    }

    public Context(Context parent, ArchitecturalRegisterFile regs, int sigFinish) {
        this(parent.kernel, parent.process, parent, regs, sigFinish);
    }

    public Context(Kernel kernel, Process process, Context parent, ArchitecturalRegisterFile regs, int sigFinish) {
        super(kernel);

        this.kernel = kernel;

        this.parent = parent;

        this.regs = regs;
        this.sigFinish = sigFinish;

        this.id = kernel.currentContextId++;

        this.uid = (int) NativeSyscalls.LIBC.getuid();
        this.euid = (int) NativeSyscalls.LIBC.geteuid();
        this.gid = (int) NativeSyscalls.LIBC.getgid();
        this.egid = (int) NativeSyscalls.LIBC.getegid();

        this.pid = kernel.currentPid++;

        this.signalMasks = new SignalMasks();

        this.state = ContextState.IDLE;

        this.functionCallPcStack = new Stack<Integer>();

        this.process = process;
    }

    public void enterSpeculativeState() {
        try {
            this.process.getMemory().enterSpeculativeState();
            this.speculativeRegs = (ArchitecturalRegisterFile) this.regs.clone();
            this.speculative = true;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public void exitSpeculativeState() {
        this.process.getMemory().exitSpeculativeState();
        this.speculativeRegs = null;
        this.speculative = false;
    }

    public StaticInstruction decodeNextInstruction() {
        this.getRegs().setPc(this.getRegs().getNpc());
        this.getRegs().setNpc(this.getRegs().getNnpc());
        this.getRegs().setNnpc(this.getRegs().getNnpc() + 4);
        this.getRegs().setGpr(ArchitecturalRegisterFile.REG_ZERO, 0);

        this.pseudocallEncounteredInLastInstructionExecution = false;

        return this.decode(this.getRegs().getPc());
    }

    protected StaticInstruction decode(int mappedPc) {
        return this.process.getStaticInst(mappedPc);
    }

    public void suspend() {
        assert (this.state != ContextState.BLOCKED);
        this.state = ContextState.BLOCKED;

//        Logger.infof(Logger.THREAD, "%s: thread suspended\n", this.getThread().getName());
    }

    public void resume() {
        assert (this.state == ContextState.BLOCKED);
        this.state = ContextState.RUNNING;

//        Logger.infof(Logger.THREAD, "%s: thread resumed\n", this.getThread().getName());
    }

    public void finish() {
        if (this.state == ContextState.FINISHED) {
            throw new RuntimeException();
        }

        this.state = ContextState.FINISHED;

        for (Context context : this.kernel.getContexts()) {
            if (context.getState() != ContextState.FINISHED && context.getParent() == this) {
                context.finish();
            }
        }

        if (this.sigFinish != 0 && this.parent != null) {
            this.parent.getSignalMasks().getPending().set(this.sigFinish);
        }

//        Logger.infof(Logger.THREAD, "%s: thread finished\n", this.getThread().getName());
    }

    public int getId() {
        return id;
    }

    public ContextState getState() {
        return state;
    }

    public void setState(ContextState state) {
        this.state = state;
    }

    public ArchitecturalRegisterFile getRegs() {
        return !speculative ? regs : speculativeRegs;
    }

    public void setRegs(ArchitecturalRegisterFile regs) {
        this.regs = regs;
    }

    public SignalMasks getSignalMasks() {
        return signalMasks;
    }

    public Context getParent() {
        return parent;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public Thread getThread(Processor processor) {
        int coreNum = this.threadId / processor.getCores().size();
        int threadNum = this.threadId % processor.getCores().size();

        return processor.getCores().get(coreNum).getThreads().get(threadNum);
    }

    public Process getProcess() {
        return process;
    }

    public int getUid() {
        return uid;
    }

    public int getEuid() {
        return euid;
    }

    public int getGid() {
        return gid;
    }

    public int getEgid() {
        return egid;
    }

    public int getPid() {
        return pid;
    }

    public int getPpid() {
        return parent == null ? 1 : parent.getPid();
    }

    public Stack<Integer> getFunctionCallPcStack() {
        return functionCallPcStack;
    }

    public boolean isSpeculative() {
        return speculative;
    }

    public boolean useICache() {
        return true;
    }

    public boolean isPseudocallEncounteredInLastInstructionExecution() {
        return pseudocallEncounteredInLastInstructionExecution;
    }

    public void setPseudocallEncounteredInLastInstructionExecution(boolean pseudocallEncounteredInLastInstructionExecution) {
        this.pseudocallEncounteredInLastInstructionExecution = pseudocallEncounteredInLastInstructionExecution;
    }
}
