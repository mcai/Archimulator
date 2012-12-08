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
package archimulator.sim.common;

import archimulator.model.Experiment;
import archimulator.sim.isa.PseudoCallEncounteredEvent;
import archimulator.sim.os.Kernel;
import net.pickapack.Reference;
import net.pickapack.action.Action1;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

/**
 *
 * @author Min Cai
 */
public class ToRoiFastForwardSimulation extends Simulation {
    private boolean pthreadHasSpawned;

    /**
     *
     * @param experiment
     * @param blockingEventDispatcher
     * @param cycleAccurateEventQueue
     * @param kernelRef
     */
    public ToRoiFastForwardSimulation(Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Reference<Kernel> kernelRef) {
        super(SimulationType.FAST_FORWARD, experiment, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean canDoFastForwardOneCycle() {
        return !pthreadHasSpawned;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean canDoCacheWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean canDoMeasurementOneCycle() {
        throw new IllegalArgumentException();
    }

    /**
     *
     */
    @Override
    public void beginSimulation() {
        this.pthreadHasSpawned = false;

        this.getProcessor().getKernel().getBlockingEventDispatcher().addListener(PseudoCallEncounteredEvent.class, new Action1<PseudoCallEncounteredEvent>() {
            public void apply(PseudoCallEncounteredEvent event) {
                if (event.getPseudoCall().getImm() == getExperiment().getArchitecture().getHelperThreadPthreadSpawnIndex()) {
                    pthreadHasSpawned = true;
                }
            }
        });
    }

    /**
     *
     */
    @Override
    public void endSimulation() {
        this.kernelRef.set(this.getProcessor().getKernel());
    }

    @Override
    public String getPrefix() {
        return "twoPhase/phase0";
    }
}
