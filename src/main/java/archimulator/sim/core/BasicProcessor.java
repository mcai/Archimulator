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

import archimulator.sim.base.event.ResetStatEvent;
import archimulator.sim.base.event.SimulationEvent;
import archimulator.sim.base.simulation.BasicSimulationObject;
import archimulator.sim.os.Context;
import archimulator.sim.os.ContextKilledEvent;
import archimulator.sim.os.ContextState;
import archimulator.sim.os.Kernel;
import archimulator.sim.uncore.CacheHierarchy;
import net.pickapack.action.Action1;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

import java.util.*;

public class BasicProcessor extends BasicSimulationObject implements Processor {
    private List<Core> cores;

    private ProcessorConfig config;

    private Kernel kernel;

    private CacheHierarchy cacheHierarchy;

    private Map<Context, Thread> contextToThreadMappings;

    public BasicProcessor(BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, ProcessorConfig processorConfig, Kernel kernel, CacheHierarchy cacheHierarchy) {
        super(blockingEventDispatcher, cycleAccurateEventQueue);

        this.config = processorConfig;

        this.kernel = kernel;

        this.cacheHierarchy = cacheHierarchy;

        this.cores = new ArrayList<Core>();

        for (int i = 0; i < this.config.getNumCores(); i++) {
            Core core = new BasicCore(this, i);

            core.setL1ICacheController(cacheHierarchy.getL1ICacheControllers().get(i));
            core.setL1DCacheController(cacheHierarchy.getL1DCacheControllers().get(i));

            for (int j = 0; j < this.config.getNumThreadsPerCore(); j++) {
                BasicThread thread = new BasicThread(core, j);
                core.getThreads().add(thread);

                thread.setItlb(cacheHierarchy.getItlbs().get(thread.getId()));

                thread.setDtlb(cacheHierarchy.getDtlbs().get(thread.getId()));
            }

            this.cores.add(core);
        }

        this.contextToThreadMappings = new HashMap<Context, Thread>();

        this.updateContextToThreadAssignments();

        this.getBlockingEventDispatcher().addListener(ResetStatEvent.class, new Action1<ResetStatEvent>() {
            public void apply(ResetStatEvent event) {
                resetStat();
            }
        });
    }

    public void updateContextToThreadAssignments() {
        for (Iterator<Context> it = this.kernel.getContexts().iterator(); it.hasNext(); ) {
            Context context = it.next();

            if (context.getThreadId() != -1 && this.contextToThreadMappings.get(context) == null) {
                context.setState(ContextState.RUNNING);

                int coreNum = context.getThreadId() / this.config.getNumThreadsPerCore();
                int threadNum = context.getThreadId() % this.config.getNumThreadsPerCore();

                Thread candidateThread = this.getCores().get(coreNum).getThreads().get(threadNum);

                this.contextToThreadMappings.put(context, candidateThread);

                candidateThread.setContext(context);
                candidateThread.updateFetchNpcAndNnpcFromRegs();
            } else if (context.getState() == ContextState.FINISHED) {
                Thread thread = this.contextToThreadMappings.get(context);
                if (thread.isLastDecodedDynamicInstCommitted() && thread.getReorderBuffer().isEmpty()) {
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

    private void resetStat() {
        this.getCycleAccurateEventQueue().resetCurrentCycle();
    }

    public ProcessorConfig getConfig() {
        return config;
    }

    public List<Core> getCores() {
        return cores;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public CacheHierarchy getCacheHierarchy() {
        return cacheHierarchy;
    }

}
