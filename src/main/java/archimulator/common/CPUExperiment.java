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

import archimulator.experiment.Experiment;
import archimulator.os.Kernel;
import archimulator.util.Reference;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;

/**
 * CPU Experiment.
 *
 * @author Min Cai
 */
public class CPUExperiment extends Experiment<CPUExperimentConfig> {
    /**
     * Current (max) memory page ID.
     */
    public transient int currentMemoryPageId;

    /**
     * Current (max) process ID.
     */
    public transient int currentProcessId;

    /**
     * Create an experiment.
     */
    public CPUExperiment() {
        super(new CPUExperimentConfig());
    }

    /**
     * Simulate.
     */
    @Override
    protected void simulate() {
        CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

        if (getConfig().getType() == ExperimentType.FUNCTIONAL) {
            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
            new FunctionalSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
        } else if (getConfig().getType() == ExperimentType.DETAILED) {
            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
            new DetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
        } else if (getConfig().getType() == ExperimentType.TWO_PHASE) {
            Reference<Kernel> kernelRef = new Reference<>();

            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();

            new ToRoiFastForwardSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();

            blockingEventDispatcher.clearListeners();

            cycleAccurateEventQueue.resetCurrentCycle();

            new FromRoiDetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();
        }
    }

    /**
     * Get the output directory.
     *
     * @return the output directory
     */
    @Override
    protected String getOutputDirectory() {
        return getConfig().getOutputDirectory();
    }
}
