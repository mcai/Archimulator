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
package archimulator.service;

import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.sim.common.SimulationEvent;
import archimulator.sim.common.DetailedSimulation;
import archimulator.sim.common.FromRoiDetailedSimulation;
import archimulator.sim.common.FunctionalSimulation;
import archimulator.sim.common.ToRoiFastForwardSimulation;
import archimulator.sim.os.Kernel;
import net.pickapack.Reference;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

import java.sql.SQLException;

public class ExperimentWorker {
    private boolean running;

    public ExperimentWorker() {
        Thread threadMonitor = new Thread(){
            @Override
            public void run() {
                for(;;) {
                    try {
                        if(!doMonitor()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        threadMonitor.setDaemon(true);
        threadMonitor.start();
    }

    private boolean doMonitor() throws SQLException {
        Experiment experiment = ServiceManager.getExperimentService().getFirstExperimentToRun();

        if(experiment != null) {
            running = true;
            runExperiment(experiment);
            running = false;
        }

        return false;
    }

    public void runExperiment(Experiment experiment) throws SQLException {
        experiment.setState(ExperimentState.RUNNING);
        ServiceManager.getExperimentService().updateExperiment(experiment);

        CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

        if (experiment.getType() == ExperimentType.FUNCTIONAL) {
            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
            new FunctionalSimulation(experiment.getTitle() + "/functional", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInsts()).simulate();
        } else if (experiment.getType() == ExperimentType.DETAILED) {
            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();
            new DetailedSimulation(experiment.getTitle() + "/detailed", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInsts()).simulate();
        } else if (experiment.getType() == ExperimentType.TWO_PHASE) {
            Reference<Kernel> kernelRef = new Reference<Kernel>();

            BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<SimulationEvent>();

            new ToRoiFastForwardSimulation(experiment.getTitle() + "/twoPhase/phase0", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getArchitecture().getHtPthreadSpawnIndex(), kernelRef).simulate();

            blockingEventDispatcher.clearListeners();

            cycleAccurateEventQueue.resetCurrentCycle();

            new FromRoiDetailedSimulation(experiment.getTitle() + "/twoPhase/phase1", experiment, blockingEventDispatcher, cycleAccurateEventQueue, experiment.getNumMaxInsts(), kernelRef).simulate();
        }

        experiment.setState(ExperimentState.COMPLETED);
        experiment.setFailedReason("");

        ServiceManager.getExperimentService().updateExperiment(experiment);
        ServiceManager.getExperimentService().dumpExperiment(experiment);
    }

    public boolean isRunning() {
        return running;
    }

    static {
        System.out.println("Archimulator - A Cloud Enabled Multicore Architectural Simulator Written in Java.\n");
        System.out.println("Version: 3.0.\n");
        System.out.println("Copyright (c) 2010-2012 by Min Cai (min.cai.china@gmail.com).\n");
    }
}
