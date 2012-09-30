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
package archimulator.util;

import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.service.ServiceManager;
import archimulator.sim.common.*;
import archimulator.sim.os.Kernel;
import net.pickapack.Reference;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ExperimentHelper {
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    public void runExperiment(long experimentId) {
        Experiment experiment = ServiceManager.getExperimentService().getExperimentById(experimentId);
        try {
            CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

            if (experiment.getType() == ExperimentType.FUNCTIONAL) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new FunctionalSimulation(experiment.getTitle() + "/functional", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInstructions()).simulate();
            } else if (experiment.getType() == ExperimentType.DETAILED) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
                new DetailedSimulation(experiment.getTitle() + "/detailed", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInstructions()).simulate();
            } else if (experiment.getType() == ExperimentType.TWO_PHASE) {
                Reference<Kernel> kernelRef = new Reference<Kernel>();

                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();

                new ToRoiFastForwardSimulation(experiment.getTitle() + "/twoPhase/phase0", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getArchitecture().getHelperThreadPthreadSpawnIndex(), kernelRef).simulate();

                blockingEventDispatcher.clearListeners();

                cycleAccurateEventQueue.resetCurrentCycle();

                new FromRoiDetailedSimulation(experiment.getTitle() + "/twoPhase/phase1", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInstructions(), kernelRef).simulate();
            }

            experiment.setState(ExperimentState.COMPLETED);
            experiment.setFailedReason("");
        } catch (Exception e) {
            experiment.setState(ExperimentState.ABORTED);
            experiment.setFailedReason(ExceptionUtils.getStackTrace(e));
        } finally {
            ServiceManager.getExperimentService().updateExperiment(experiment);
        }
    }
}
