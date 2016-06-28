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
package archimulator.core;

import archimulator.common.BasicSimulationObject;
import archimulator.common.Experiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationEvent;
import archimulator.os.Context;
import archimulator.os.ContextKilledEvent;
import archimulator.os.ContextState;
import archimulator.os.Kernel;
import archimulator.uncore.MemoryHierarchy;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

import java.util.*;

/**
 * Basic processor.
 *
 * @author Min Cai
 */
public class BasicProcessor extends BasicSimulationObject implements Processor {
    private List<Core> cores;

    private Kernel kernel;

    private MemoryHierarchy memoryHierarchy;

    private Map<Context, Thread> contextToThreadMappings;

    /**
     * Create a basic processor.
     *
     * @param experiment              the experiment
     * @param simulation              the simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param kernel                  the kernel
     * @param memoryHierarchy         the memory hierarchy
     */
    public BasicProcessor(
            Experiment experiment,
            Simulation simulation,
            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher,
            CycleAccurateEventQueue cycleAccurateEventQueue,
            Kernel kernel,
            MemoryHierarchy memoryHierarchy
    ) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.kernel = kernel;

        this.memoryHierarchy = memoryHierarchy;

        this.cores = new ArrayList<>();

        for (int i = 0; i < getExperiment().getNumCores(); i++) {
            Core core = new BasicCore(this, i);

            for (int j = 0; j < getExperiment().getNumThreadsPerCore(); j++) {
                BasicThread thread = new BasicThread(core, j);
                core.getThreads().add(thread);
            }

            this.cores.add(core);
        }

        this.contextToThreadMappings = new HashMap<>();

        this.updateContextToThreadAssignments();
    }

    @Override
    public void updateContextToThreadAssignments() {
        for (Iterator<Context> it = this.kernel.getContexts().iterator(); it.hasNext(); ) {
            Context context = it.next();

            if (context.getThreadId() != -1 && this.contextToThreadMappings.get(context) == null) {
                context.setState(ContextState.RUNNING);

                int coreNum = context.getThreadId() / getExperiment().getNumThreadsPerCore();
                int threadNum = context.getThreadId() % getExperiment().getNumThreadsPerCore();

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

    /**
     * Kill the specified context.
     *
     * @param context the context to be killed
     */
    private void kill(Context context) {
        if (context.getState() != ContextState.FINISHED) {
            throw new IllegalArgumentException();
        }

        this.kernel.getContexts().stream().filter(childContext -> childContext.getParent() == context).forEach(this::kill);

        if (context.getParent() == null) {
            context.getProcess().closeProgram();
        }

        this.contextToThreadMappings.get(context).setContext(null);

        context.setThreadId(-1);

        this.getBlockingEventDispatcher().dispatch(new ContextKilledEvent(context));
    }

    @Override
    public List<Core> getCores() {
        return cores;
    }

    @Override
    public Kernel getKernel() {
        return kernel;
    }

    @Override
    public MemoryHierarchy getMemoryHierarchy() {
        return memoryHierarchy;
    }

    @Override
    public String getName() {
        return "processor";
    }
}
