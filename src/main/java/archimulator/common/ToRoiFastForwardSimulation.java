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
package archimulator.common;

import archimulator.isa.event.PseudoCallEncounteredEvent;
import archimulator.os.Kernel;
import archimulator.util.Reference;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * "To ROI" fast forward simulation.
 *
 * @author Min Cai
 */
public class ToRoiFastForwardSimulation extends Simulation {
    private boolean pthreadHasSpawned;

    /**
     * Create a "to ROI" fast forward simulation.
     *
     * @param experiment              the parent experiment
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     * @param kernelRef               the kernel reference
     */
    public ToRoiFastForwardSimulation(CPUExperiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, Reference<Kernel> kernelRef) {
        super(SimulationType.FAST_FORWARD, experiment, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef);
    }

    /**
     * Get a value indicating whether it can do fast forward for one cycle or not.
     *
     * @return a value indicating whether it can do fast forward for one cycle or not
     */
    @Override
    public boolean canDoFastForwardOneCycle() {
        return !pthreadHasSpawned;
    }

    /**
     * Get a value indicating whether it can do warmup for one cycle or not.
     *
     * @return a value indicating whether it can do warmup for one cycle or not
     */
    @Override
    public boolean canDoWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    /**
     * Get a value indicating whether it can do measurement for one cycle or not.
     *
     * @return a value indicating whether it can do measurement for one cycle or not
     */
    @Override
    public boolean canDoMeasurementOneCycle() {
        throw new IllegalArgumentException();
    }

    /**
     * Begin the simulation.
     */
    @Override
    public void beginSimulation() {
        this.pthreadHasSpawned = false;

        this.getProcessor().getKernel().getBlockingEventDispatcher().addListener(PseudoCallEncounteredEvent.class, event -> {
            if (event.getPseudoCall().getImm() == getExperiment().getConfig().getHelperThreadPthreadSpawnIndex()) {
                pthreadHasSpawned = true;
            }
        });
    }

    /**
     * End the simulation.
     */
    @Override
    public void endSimulation() {
        this.kernelRef.set(this.getProcessor().getKernel());
    }

    /**
     * Get the title prefix.
     *
     * @return the title prefix
     */
    @Override
    public String getPrefix() {
        return "twoPhase/phase0";
    }
}
