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
package archimulator.sim.core;

import archimulator.model.Experiment;
import archimulator.sim.common.BasicSimulationObject;
import archimulator.sim.common.Simulation;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.os.Context;
import archimulator.sim.os.ContextKilledEvent;
import archimulator.sim.os.ContextState;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.MemoryHierarchy;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

import java.util.*;

/**
 *
 * @author Min Cai
 */
public class BasicProcessor extends BasicSimulationObject implements Processor {
    private List<Core> cores;

    private Kernel kernel;

    private MemoryHierarchy memoryHierarchy;

    private Map<Context, Thread> contextToThreadMappings;

    /**
     *
     * @param experiment
     * @param simulation
     * @param blockingEventDispatcher
     * @param cycleAccurateEventQueue
     * @param kernel
     * @param memoryHierarchy
     */
    public BasicProcessor(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Kernel kernel, MemoryHierarchy memoryHierarchy) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.kernel = kernel;

        this.memoryHierarchy = memoryHierarchy;

        this.cores = new ArrayList<Core>();

        for (int i = 0; i < getExperiment().getArchitecture().getNumCores(); i++) {
            Core core = new BasicCore(this, i);

            core.setL1ICacheController(memoryHierarchy.getL1ICacheControllers().get(i));
            core.setL1DCacheController(memoryHierarchy.getL1DCacheControllers().get(i));

            for (int j = 0; j < getExperiment().getArchitecture().getNumThreadsPerCore(); j++) {
                BasicThread thread = new BasicThread(core, j);
                core.getThreads().add(thread);

                thread.setItlb(memoryHierarchy.getItlbs().get(thread.getId()));

                thread.setDtlb(memoryHierarchy.getDtlbs().get(thread.getId()));
            }

            this.cores.add(core);
        }

        this.contextToThreadMappings = new HashMap<Context, Thread>();

        this.updateContextToThreadAssignments();
    }

    /**
     *
     */
    public void updateContextToThreadAssignments() {
        for (Iterator<Context> it = this.kernel.getContexts().iterator(); it.hasNext(); ) {
            Context context = it.next();

            if (context.getThreadId() != -1 && this.contextToThreadMappings.get(context) == null) {
                context.setState(ContextState.RUNNING);

                int coreNum = context.getThreadId() / getExperiment().getArchitecture().getNumThreadsPerCore();
                int threadNum = context.getThreadId() % getExperiment().getArchitecture().getNumThreadsPerCore();

                Thread candidateThread = this.getCores().get(coreNum).getThreads().get(threadNum);

                this.contextToThreadMappings.put(context, candidateThread);

                candidateThread.setContext(context);
                candidateThread.updateFetchNpcAndNnpcFromRegs();
            } else if (context.getState() == ContextState.FINISHED) {
                Thread thread = this.contextToThreadMappings.get(context);
                if (thread.isLastDecodedDynamicInstructionCommitted() && thread.getReorderBuffer().isEmpty()) {
                    this.kill(context);
                    it.remove();
                }
            }
        }
    }

    private void kill(Context context) {
        assert (context.getState() == ContextState.FINISHED);

        for (Context childContext : this.kernel.getContexts()) {
            if (childContext.getParent() == context) {
                this.kill(childContext);
            }
        }

        if (context.getParent() == null) {
            context.getProcess().closeProgram();
        }

        this.contextToThreadMappings.get(context).setContext(null);

        context.setThreadId(-1);

        this.getBlockingEventDispatcher().dispatch(new ContextKilledEvent(context));
    }

    /**
     *
     * @return
     */
    public List<Core> getCores() {
        return cores;
    }

    /**
     *
     * @return
     */
    public List<Thread> getThreads() {
        List<Thread> threads = new ArrayList<Thread>();

        for(Core core : cores) {
            threads.addAll(core.getThreads());
        }

        return threads;
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
    public MemoryHierarchy getMemoryHierarchy() {
        return memoryHierarchy;
    }

    @Override
    public String getName() {
        return "processor";
    }
}
