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

import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * Basic simulation object.
 *
 * @author Min Cai
 */
public abstract class BasicSimulationObject implements SimulationObject {
    private BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher;
    private CycleAccurateEventQueue cycleAccurateEventQueue;
    private Experiment experiment;
    private Simulation simulation;

    /**
     * Create a basic simulation object.
     *
     * @param experiment              the parent experiment
     * @param simulation              the parent simulation
     * @param blockingEventDispatcher the blocking event dispatcher
     * @param cycleAccurateEventQueue the cycle accurate event queue
     */
    public BasicSimulationObject(Experiment experiment, Simulation simulation, BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher, CycleAccurateEventQueue cycleAccurateEventQueue) {
        this.experiment = experiment;
        this.simulation = simulation;
        this.blockingEventDispatcher = blockingEventDispatcher;
        this.cycleAccurateEventQueue = cycleAccurateEventQueue;
    }

    /**
     * Create a basic simulation object.
     *
     * @param parent the parent simulation object
     */
    public BasicSimulationObject(SimulationObject parent) {
        this(parent.getExperiment(), parent.getSimulation(), parent.getBlockingEventDispatcher(), parent.getCycleAccurateEventQueue());
    }

    /**
     * Get the blocking event dispatcher.
     *
     * @return the blocking event dispatcher
     */
    public BlockingEventDispatcher<SimulationEvent> getBlockingEventDispatcher() {
        return blockingEventDispatcher;
    }

    /**
     * Get the cycle accurate queue.
     *
     * @return the cycle accurate queue
     */
    public CycleAccurateEventQueue getCycleAccurateEventQueue() {
        return cycleAccurateEventQueue;
    }

    /**
     * Get the parent experiment.
     *
     * @return the parent experiment
     */
    public Experiment getExperiment() {
        return experiment;
    }

    /**
     * Get the parent simulation.
     *
     * @return the parent simulation
     */
    public Simulation getSimulation() {
        return simulation;
    }
}
