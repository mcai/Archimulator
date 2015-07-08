/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.common.BasicSimulationObject;
import archimulator.common.Simulation;
import archimulator.common.SimulationObject;
import archimulator.isa.ArchitecturalRegisterFile;
import archimulator.isa.Memory;
import archimulator.isa.StaticInstruction;
import archimulator.os.event.SystemEvent;
import archimulator.os.signal.SignalAction;
import archimulator.util.buffer.CircularByteBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Kernel.
 *
 * @author Min Cai
 */
public class Kernel extends BasicSimulationObject implements SimulationObject {
    private List<Pipe> pipes;
    private List<SystemEvent> systemEvents;
    private List<SignalAction> signalActions;

    private List<Context> contexts;
    private List<Process> processes;

    private SystemCallEmulation systemCallEmulation;

    private long currentCycle;

    /**
     * Current/maximum process ID.
     */
    public int currentPid = 1000;

    /**
     * Current/maximum memory ID.
     */
    public int currentMemoryId = 0;

    /**
     * Current/maximum context ID.
     */
    public int currentContextId = 0;

    /**
     * Current/maximum file descriptor ID.
     */
    public int currentFd = 100;

    /**
     * Create a kernel.
     *
     * @param simulation the simulation object
     */
    public Kernel(Simulation simulation) {
        super(simulation);

        this.pipes = new ArrayList<>();
        this.systemEvents = new ArrayList<>();

        this.signalActions = new ArrayList<>();
        for (int i = 0; i < Kernel.MAX_SIGNAL; i++) {
            this.signalActions.add(new SignalAction());
        }

        this.contexts = new ArrayList<>();
        this.processes = new ArrayList<>();

        this.systemCallEmulation = new SystemCallEmulation(this);
    }

    /**
     * Get the process object from the specified process ID.
     *
     * @param id the process ID
     * @return the process object matching the specified process ID
     */
    public Process getProcessFromId(int id) {
        for (Process process : this.processes) {
            if (process.getId() == id) {
                return process;
            }
        }

        return null;
    }

    /**
     * Get the context object from the specified context ID.
     *
     * @param id the context ID
     * @return the context object matching the specified context ID
     */
    public Context getContextFromId(int id) {
        for (Context context : this.contexts) {
            if (context.getId() == id) {
                return context;
            }
        }

        return null;
    }

    /**
     * Get the context object from the specified process ID.
     *
     * @param processId the process ID
     * @return the context object matching the specified process ID
     */
    public Context getContextFromProcessId(int processId) {
        for (Context context : this.contexts) {
            if (context.getProcessId() == processId) {
                return context;
            }
        }

        return null;
    }

    /**
     * Map the specified context to an idle thread.
     *
     * @param contextToMap the context to be mapped
     * @param predicate    the predicate
     * @return a value indicating whether the mapping succeeds or not
     */
    public boolean map(Context contextToMap, Predicate<Integer> predicate) {
        if (contextToMap.getThreadId() != -1) {
            throw new IllegalArgumentException();
        }

        for (int coreNum = 0; coreNum < this.getExperiment().getNumCores(); coreNum++) {
            for (int threadNum = 0; threadNum < this.getExperiment().getNumThreadsPerCore(); threadNum++) {
                int threadId = coreNum * this.getExperiment().getNumThreadsPerCore() + threadNum;

                boolean hasMapped = false;

                for (Context context : this.getContexts()) {
                    if (context.getThreadId() == threadId) {
                        hasMapped = true;
                        break;
                    }
                }

                if (!hasMapped && predicate.test(threadId)) {
                    contextToMap.setThreadId(threadId);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Schedule the specified system event.
     *
     * @param event the system event to be scheduled
     */
    public void scheduleSystemEvent(SystemEvent event) {
        this.systemEvents.add(event);
    }

    /**
     * Process the pending list of system events.
     */
    public void processSystemEvents() {
        for (Iterator<SystemEvent> it = this.systemEvents.iterator(); it.hasNext(); ) {
            SystemEvent e = it.next();

            if ((e.getContext().getState() == ContextState.RUNNING || e.getContext().getState() == ContextState.BLOCKED) && !e.getContext().isSpeculative() && e.needProcess()) {
                it.remove();
                e.process();
            }
        }
    }

    /**
     * Process the pending list of signals.
     */
    public void processSignals() {
        for (Context context : this.contexts) {
            if ((context.getState() == ContextState.RUNNING || context.getState() == ContextState.BLOCKED) && !context.isSpeculative()) {
                for (int signal = 1; signal <= MAX_SIGNAL; signal++) {
                    if (this.mustProcessSignal(context, signal)) {
                        this.runSignalHandler(context, signal);
                    }
                }
            }
        }
    }

    /**
     * Create a pipe for the specified array of two file descriptor numbers.
     *
     * @param fileDescriptors the array of two descriptor numbers
     */
    public void createPipe(int[] fileDescriptors) {
        fileDescriptors[0] = this.currentFd++;
        fileDescriptors[1] = this.currentFd++;
        this.pipes.add(new Pipe(fileDescriptors));
    }

    /**
     * Close the pipes containing the specified file descriptor number.
     *
     * @param fileDescriptor the file descriptor number
     */
    public void closePipe(int fileDescriptor) {
        for (Iterator<Pipe> it = this.pipes.iterator(); it.hasNext(); ) {
            Pipe pipe = it.next();

            if (pipe.getFileDescriptors()[0] == fileDescriptor) {
                pipe.getFileDescriptors()[0] = -1;
            }
            if (pipe.getFileDescriptors()[1] == fileDescriptor) {
                pipe.getFileDescriptors()[1] = -1;
            }

            if (pipe.getFileDescriptors()[0] == -1 && pipe.getFileDescriptors()[1] == -1) {
                it.remove();
            }
        }
    }

    /**
     * Get the read buffer for the specified file descriptor number.
     *
     * @param fileDescriptor the file descriptor number
     * @return the read buffer for the specified file descriptor number
     */
    public CircularByteBuffer getReadBuffer(int fileDescriptor) {
        return this.getBuffer(fileDescriptor, 0);
    }

    /**
     * Get the write buffer for the specified file descriptor number.
     *
     * @param fileDescriptor the file descriptor number
     * @return the write buffer for the specified file descriptor number
     */
    public CircularByteBuffer getWriteBuffer(int fileDescriptor) {
        return this.getBuffer(fileDescriptor, 1);
    }

    /**
     * Get the circular buffer for the specified file descriptor number and index.
     *
     * @param fileDescriptor the file descriptor
     * @param index          the index
     * @return the circular buffer matching the specified file descriptor number and index
     */
    private CircularByteBuffer getBuffer(int fileDescriptor, int index) {
        for (Pipe pipe : this.pipes) {
            if (pipe.getFileDescriptors()[index] == fileDescriptor) {
                return pipe.getBuffer();
            }
        }

        return null;
    }

    /**
     * Run the signal handler for the specified context and signal.
     *
     * @param context the context
     * @param signal  the signal
     */
    public void runSignalHandler(Context context, int signal) {
        try {
            if (this.signalActions.get(signal - 1).getHandler() == 0) {
                throw new RuntimeException();
            }

//            System.out.printf("%s 0x%08x: executing signal %d handler\n", context.getThread().getName(), signalActions[signal - 1].getHandler(), signal);

            context.getSignalMasks().getPending().clear(signal);

            ArchitecturalRegisterFile oldRegisterFile = (ArchitecturalRegisterFile) context.getRegisterFile().clone();

            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A0, signal);
            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_T9, this.signalActions.get(signal - 1).getHandler());
            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, 0xffffffff);
            context.getRegisterFile().setNpc(this.signalActions.get(signal - 1).getHandler());
            context.getRegisterFile().setNnpc(context.getRegisterFile().getNpc() + 4);

            while (context.getState() == ContextState.RUNNING && context.getRegisterFile().getNpc() != 0xffffffff) {
                StaticInstruction.execute(context.decodeNextInstruction(), context);
            }

            context.setRegisterFile(oldRegisterFile);

//            System.out.printf("%s 0x%08x: return from signal %d handler\n", context.getThread().getName(), context.getRegisterFile().getNpc(), signal);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a value indicating whether the specified signal must be processed for the specified context or not.
     *
     * @param context the context
     * @param signal  the signal
     * @return a value indicating whether the specified signal must be processed for the specified context or not
     */
    public boolean mustProcessSignal(Context context, int signal) {
        return context.getSignalMasks().getPending().contains(signal) && !context.getSignalMasks().getBlocked().contains(signal);
    }

    /**
     * Advance one cycle.
     */
    public void advanceOneCycle() {
        if (this.currentCycle % 1000 == 0) {
            this.processSystemEvents();
            this.processSignals();
        }

        this.currentCycle++;
    }

    /**
     * Get the list of signal actions.
     *
     * @return the list of signal actions
     */
    public List<SignalAction> getSignalActions() {
        return signalActions;
    }

    /**
     * Get the list of contexts.
     *
     * @return the list of contexts
     */
    public List<Context> getContexts() {
        return contexts;
    }

    /**
     * Get the list of processes.
     *
     * @return the list of processes
     */
    public List<Process> getProcesses() {
        return processes;
    }

    /**
     * Get the list of memories.
     *
     * @return the list of memories
     */
    public List<Memory> getMemories() {
        return processes.stream().map(Process::getMemory).collect(Collectors.toList());
    }

    /**
     * Get the current cycle.
     *
     * @return the current cycle
     */
    public long getCurrentCycle() {
        return currentCycle;
    }

    /**
     * Get the system call emulation object.
     *
     * @return the system call emulation object
     */
    public SystemCallEmulation getSystemCallEmulation() {
        return systemCallEmulation;
    }

    /**
     * Get the name of the kernel.
     *
     * @return the name of the kernel
     */
    @Override
    public String getName() {
        return "kernel";
    }

    /**
     * Maximum signal.
     */
    public static final int MAX_SIGNAL = 64;
}
