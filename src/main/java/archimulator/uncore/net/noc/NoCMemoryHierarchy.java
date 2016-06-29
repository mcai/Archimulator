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
package archimulator.uncore.net.noc;

import archimulator.common.Experiment;
import archimulator.common.Simulation;
import archimulator.common.SimulationEvent;
import archimulator.uncore.AbstractMemoryHierarchy;
import archimulator.uncore.MemoryDevice;
import archimulator.uncore.net.Net;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * NoC memory hierarchy.
 *
 * @author Min Cai
 */
public class NoCMemoryHierarchy extends AbstractMemoryHierarchy {
    private Net net;

    /**
     * Create a basic memory hierarchy.
     *
     * @param experiment              the experiment
     * @param simulation              the simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public NoCMemoryHierarchy(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        super(experiment, simulation, blockingEventDispatcher, cycleAccurateEventQueue);

        this.net = new NoCNet(this);
    }

    @Override
    public Net getNet(MemoryDevice from, MemoryDevice to) {
        return net;
    }
}
