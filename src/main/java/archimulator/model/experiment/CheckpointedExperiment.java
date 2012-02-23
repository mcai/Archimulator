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
package archimulator.model.experiment;

import archimulator.model.simulation.ContextConfig;
import archimulator.model.simulation.SimulationStartingImage;
import archimulator.model.strategy.checkpoint.CheckpointToInstructionCountBasedDetailedSimulationStrategy;
import archimulator.model.strategy.checkpoint.RoiBasedRunToCheckpointFunctionalSimulationStrategy;

import java.util.List;

public class CheckpointedExperiment extends Experiment {
    private int maxInsts;

    public CheckpointedExperiment(String title, int numCores, int numThreadsPerCore, List<ContextConfig> contextConfigs, int maxInsts) {
        super(title, numCores, numThreadsPerCore, contextConfigs);
        this.maxInsts = maxInsts;
    }

    @Override
    protected void doStart() {
        SimulationStartingImage simulationStartingImage = new SimulationStartingImage();

        this.doSimulation(this.getTitle() + ".checkpointedSimulation.phase0", new RoiBasedRunToCheckpointFunctionalSimulationStrategy(this.getPhaser(), simulationStartingImage), getBlockingEventDispatcher(), getCycleAccurateEventQueue());

        getBlockingEventDispatcher().clearListeners();
        getCycleAccurateEventQueue().resetCurrentCycle();

        this.doSimulation(this.getTitle() + ".checkpointedSimulation.phase1", new CheckpointToInstructionCountBasedDetailedSimulationStrategy(this.getPhaser(), this.maxInsts, simulationStartingImage), getBlockingEventDispatcher(), getCycleAccurateEventQueue());
    }
}
