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

import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationObject;
import archimulator.sim.isa.ArchitecturalRegisterFile;
import archimulator.sim.isa.StaticInstruction;
import archimulator.sim.os.event.SystemEvent;
import archimulator.sim.os.signal.SignalAction;
import net.pickapack.action.Predicate;
import net.pickapack.io.buffer.CircularByteBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Kernel extends BasicSimulationObject implements SimulationObject {
    private List<Pipe> pipes;
    private List<SystemEvent> systemEvents;
    private List<SignalAction> signalActions;

    private List<Context> contexts;
    private List<Process> processes;

    private SystemCallEmulation systemCallEmulation;

    private long currentCycle;

    private int numCores;
    private int numThreadsPerCore;

    public Kernel(Simulation simulation, int numCores, int numThreadsPerCore) {
        super(simulation);

        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

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

    public Process getProcessFromId(int id) {
        for (Process process : this.processes) {
            if (process.getId() == id) {
                return process;
            }
        }

        return null;
    }

    public Context getContextFromId(int id) {
        for (Context context : this.contexts) {
            if (context.getId() == id) {
                return context;
            }
        }

        return null;
    }

    public Context getContextFromPid(int pid) {
        for (Context context : this.contexts) {
            if (context.getPid() == pid) {
                return context;
            }
        }

        return null;
    }

    public boolean map(Context contextToMap, Predicate<Integer> predicate) {
        assert (contextToMap.getThreadId() == -1);

        for (int coreNum = 0; coreNum < this.numCores; coreNum++) {
            for (int threadNum = 0; threadNum < this.numThreadsPerCore; threadNum++) {
                int threadId = coreNum * this.numThreadsPerCore + threadNum;

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

    public void scheduleSystemEvent(SystemEvent event) {
        this.systemEvents.add(event);
    }

    public void processSystemEvents() {
        for (Iterator<SystemEvent> it = this.systemEvents.iterator(); it.hasNext(); ) {
            SystemEvent e = it.next();

            if ((e.getContext().getState() == ContextState.RUNNING || e.getContext().getState() == ContextState.BLOCKED) && !e.getContext().isSpeculative() && e.needProcess()) {
                it.remove();
                e.process();
            }
        }
    }

    public void processSignals() {
        for (Context context : this.contexts) {
            if ((context.getState() == ContextState.RUNNING || context.getState() == ContextState.BLOCKED) && !context.isSpeculative()) {
                for (int sig = 1; sig <= MAX_SIGNAL; sig++) {
                    if (this.mustProcessSignal(context, sig)) {
                        this.runSignalHandler(context, sig);
                    }
                }
            }
        }
    }

    public void createPipe(int[] fileDescriptors) {
        fileDescriptors[0] = currentFd++;
        fileDescriptors[1] = currentFd++;
        this.pipes.add(new Pipe(fileDescriptors));
    }

    public void closePipe(int fileDescriptor) {
        for (Iterator<Pipe> it = this.pipes.iterator(); it.hasNext(); ) {
            Pipe pipe = it.next();

            if (pipe.getFd()[0] == fileDescriptor) {
                pipe.getFd()[0] = -1;
            }
            if (pipe.getFd()[1] == fileDescriptor) {
                pipe.getFd()[1] = -1;
            }

            if (pipe.getFd()[0] == -1 && pipe.getFd()[1] == -1) {
                it.remove();
            }
        }
    }

    public CircularByteBuffer getReadBuffer(int fd) {
        return this.getBuffer(fd, 0);
    }

    public CircularByteBuffer getWriteBuffer(int fd) {
        return this.getBuffer(fd, 1);
    }

    private CircularByteBuffer getBuffer(int fd, int index) {
        for (Pipe pipe : this.pipes) {
            if (pipe.getFd()[index] == fd) {
                return pipe.getBuffer();
            }
        }

        return null;
    }

    public void runSignalHandler(Context context, int sig) {
        try {
            if (signalActions.get(sig - 1).getHandler() == 0) {
                throw new RuntimeException();
            }

//            System.out.printf("%s 0x%08x: executing signal %d handler\n", context.getThread().getName(), signalActions[sig - 1].getHandler(), sig);

            context.getSignalMasks().getPending().clear(sig);

            ArchitecturalRegisterFile oldRegs = (ArchitecturalRegisterFile) context.getRegs().clone();

            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_A0, sig);
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_T9, signalActions.get(sig - 1).getHandler());
            context.getRegs().setGpr(ArchitecturalRegisterFile.REG_RA, 0xffffffff);
            context.getRegs().setNpc(signalActions.get(sig - 1).getHandler());
            context.getRegs().setNnpc(context.getRegs().getNpc() + 4);

            while (context.getState() == ContextState.RUNNING && context.getRegs().getNpc() != 0xffffffff) {
                StaticInstruction.execute(context.decodeNextInstruction(), context);
            }

            context.setRegs(oldRegs);

//            System.out.printf("%s 0x%08x: return from signal %d handler\n", context.getThread().getName(), context.getRegs().getNpc(), sig);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean mustProcessSignal(Context context, int sig) {
        return context.getSignalMasks().getPending().contains(sig) && !context.getSignalMasks().getBlocked().contains(sig);
    }

    public void advanceOneCycle() {
        if (this.currentCycle % 1000 == 0) {
            this.processSystemEvents();
            this.processSignals();
        }

        this.currentCycle++;
    }

    public List<SignalAction> getSignalActions() {
        return signalActions;
    }

    public List<Context> getContexts() {
        return contexts;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public long getCurrentCycle() {
        return currentCycle;
    }

    public SystemCallEmulation getSystemCallEmulation() {
        return systemCallEmulation;
    }

    public static final int MAX_SIGNAL = 64;

    public int currentPid = 1000;
    public int currentMemoryId = 0;
    public int currentContextId = 0;
    public int currentFd = 100;
}
