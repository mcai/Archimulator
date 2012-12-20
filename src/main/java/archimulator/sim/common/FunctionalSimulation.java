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
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

/**
 * Functional simulation.
 *
 * @author Min Cai
 */
public class FunctionalSimulation extends Simulation {
    /**
     * Create a functional simulation.
     *
     * @param experiment              the parent experiment
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public FunctionalSimulation(Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(SimulationType.FAST_FORWARD, experiment, blockingEventDispatcher, cycleAccurateEventQueue, null);
    }

    /**
     * Get a value indicating whether it can do fast forward for one cycle or not.
     *
     * @return a value indicating whether it can do fast forward for one cycle or not
     */
    @Override
    public boolean canDoFastForwardOneCycle() {
        return this.getExperiment().getNumMaxInstructions() == -1 || this.getProcessor().getCores().get(0).getThreads().get(0).getTotalInstructions() < this.getExperiment().getNumMaxInstructions();
    }

    /**
     * Get a value indicating whether it can do cache warmup for one cycle or not.
     *
     * @return a value indicating whether it can do cache warmup for one cycle or not
     */
    @Override
    public boolean canDoCacheWarmupOneCycle() {
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
    }

    /**
     * End the simulation.
     */
    @Override
    public void endSimulation() {
    }

    /**
     * Get the title prefix.
     *
     * @return the title prefix
     */
    @Override
    public String getPrefix() {
        return "functional";
    }
}
