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
package archimulator.sim.os;

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.Memory;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.os.event.SystemEvent;
import archimulator.sim.os.signal.SignalAction;
import net.pickapack.action.Predicate;
import net.pickapack.io.buffer.CircularByteBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
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
     *
     */
    public int currentPid = 1000;
    /**
     *
     */
    public int currentMemoryId = 0;
    /**
     *
     */
    public int currentContextId = 0;
    /**
     *
     */
    public int currentFd = 100;

    /**
     *
     * @param simulation
     */
    public Kernel(Simulation simulation) {
        super(simulation);

        this.pipes = new ArrayList<Pipe>();
        this.systemEvents = new ArrayList<SystemEvent>();

        this.signalActions = new ArrayList<SignalAction>();
        for (int i = 0; i < Kernel.MAX_SIGNAL; i++) {
            this.signalActions.add(new SignalAction());
        }

        this.contexts = new ArrayList<Context>();
        this.processes = new ArrayList<Process>();

        this.systemCallEmulation = new SystemCallEmulation(this);
    }

    /**
     *
     * @param id
     * @return
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
     *
     * @param id
     * @return
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
     *
     * @param processId
     * @return
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
     *
     * @param contextToMap
     * @param predicate
     * @return
     */
    public boolean map(Context contextToMap, Predicate<Integer> predicate) {
        if (contextToMap.getThreadId() != -1) {
            throw new IllegalArgumentException();
        }

        for (int coreNum = 0; coreNum < this.getExperiment().getArchitecture().getNumCores(); coreNum++) {
            for (int threadNum = 0; threadNum < this.getExperiment().getArchitecture().getNumThreadsPerCore(); threadNum++) {
                int threadId = coreNum * this.getExperiment().getArchitecture().getNumThreadsPerCore() + threadNum;

                boolean hasMapped = false;

                for (Context context : this.getContexts()) {
                    if (context.getThreadId() == threadId) {
                        hasMapped = true;
                        break;
                    }
                }

                if (!hasMapped && predicate.apply(threadId)) {
                    contextToMap.setThreadId(threadId);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     *
     * @param event
     */
    public void scheduleSystemEvent(SystemEvent event) {
        this.systemEvents.add(event);
    }

    /**
     *
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
     *
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
     *
     * @param fileDescriptors
     */
    public void createPipe(int[] fileDescriptors) {
        fileDescriptors[0] = currentFd++;
        fileDescriptors[1] = currentFd++;
        this.pipes.add(new Pipe(fileDescriptors));
    }

    /**
     *
     * @param fileDescriptor
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
     *
     * @param fileDescriptor
     * @return
     */
    public CircularByteBuffer getReadBuffer(int fileDescriptor) {
        return this.getBuffer(fileDescriptor, 0);
    }

    /**
     *
     * @param fileDescriptor
     * @return
     */
    public CircularByteBuffer getWriteBuffer(int fileDescriptor) {
        return this.getBuffer(fileDescriptor, 1);
    }

    private CircularByteBuffer getBuffer(int fileDescriptor, int index) {
        for (Pipe pipe : this.pipes) {
            if (pipe.getFileDescriptors()[index] == fileDescriptor) {
                return pipe.getBuffer();
            }
        }

        return null;
    }

    /**
     *
     * @param context
     * @param signal
     */
    public void runSignalHandler(Context context, int signal) {
        try {
            if (signalActions.get(signal - 1).getHandler() == 0) {
                throw new RuntimeException();
            }

//            System.out.printf("%s 0x%08x: executing signal %d handler\n", context.getThread().getName(), signalActions[signal - 1].getHandler(), signal);

            context.getSignalMasks().getPending().clear(signal);

            ArchitecturalRegisterFile oldRegisterFile = (ArchitecturalRegisterFile) context.getRegisterFile().clone();

            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_A0, signal);
            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_T9, signalActions.get(signal - 1).getHandler());
            context.getRegisterFile().setGpr(ArchitecturalRegisterFile.REGISTER_RA, 0xffffffff);
            context.getRegisterFile().setNpc(signalActions.get(signal - 1).getHandler());
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
     *
     * @param context
     * @param signal
     * @return
     */
    public boolean mustProcessSignal(Context context, int signal) {
        return context.getSignalMasks().getPending().contains(signal) && !context.getSignalMasks().getBlocked().contains(signal);
    }

    /**
     *
     */
    public void advanceOneCycle() {
        if (this.currentCycle % 1000 == 0) {
            this.processSystemEvents();
            this.processSignals();
        }

        this.currentCycle++;
    }

    /**
     *
     * @return
     */
    public List<SignalAction> getSignalActions() {
        return signalActions;
    }

    /**
     *
     * @return
     */
    public List<Context> getContexts() {
        return contexts;
    }

    /**
     *
     * @return
     */
    public List<Process> getProcesses() {
        return processes;
    }

    /**
     *
     * @return
     */
    public List<Memory> getMemories() {
        List<Memory> memories = new ArrayList<Memory>();

        for(Process process : processes) {
            memories.add(process.getMemory());
        }

        return memories;
    }

    /**
     *
     * @return
     */
    public long getCurrentCycle() {
        return currentCycle;
    }

    /**
     *
     * @return
     */
    public SystemCallEmulation getSystemCallEmulation() {
        return systemCallEmulation;
    }

    @Override
    public String getName() {
        return "kernel";
    }

    /**
     *
     */
    public static final int MAX_SIGNAL = 64;
}
