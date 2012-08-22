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

public class FunctionalSimulation extends Simulation {
    private long numMaxInstructions;

    public FunctionalSimulation(String title, Experiment experiment, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue, int numMaxInstructions) {
        super(title, SimulationType.FAST_FORWARD, experiment, blockingEventDispatcher, cycleAccurateEventQueue);

        this.numMaxInstructions = numMaxInstructions;
    }

    @Override
    public boolean canDoFastForwardOneCycle() {
        return numMaxInstructions == -1 || this.getProcessor().getCores().get(0).getThreads().get(0).getTotalInstructions() < this.numMaxInstructions;
    }

    @Override
    public boolean canDoCacheWarmupOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public boolean canDoMeasurementOneCycle() {
        throw new IllegalArgumentException();
    }

    @Override
    public void beginSimulation() {
    }

    @Override
    public void endSimulation() {
    }
}
