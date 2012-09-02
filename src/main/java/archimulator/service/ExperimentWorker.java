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

import archimulator.client.ExperimentPack;
import archimulator.model.Experiment;
import archimulator.model.ExperimentState;
import archimulator.model.ExperimentType;
import archimulator.sim.common.*;
import archimulator.sim.os.Kernel;
import net.pickapack.Reference;
import net.pickapack.event.BlockingEventDispatcher;
import net.pickapack.event.CycleAccurateEventQueue;

public class ExperimentWorker implements Runnable {
    private String[] args;
    private Experiment experiment;

    public ExperimentWorker(String... args) {
        this.args = args;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (experiment != null) {
                    if (experiment.getState() == ExperimentState.RUNNING) {
                        experiment.reset();
                        ServiceManager.getExperimentService().updateExperiment(experiment);
                    }
                }
            }
        });
    }

    @Override
    public void run() {
        for (; ; ) {
            if (!doRunExperiment()) {
                break;
            }
        }
    }

    private boolean doRunExperiment() {
        if(this.args == null || this.args.length == 0) {
            this.experiment = ServiceManager.getExperimentService().getFirstExperimentToRun();
        }
        else {
            for(String arg : this.args) {
                ExperimentPack experimentPack = ServiceManager.getExperimentService().getExperimentPackByTitle(arg);
                if (experimentPack != null) {
                    Experiment experiment = ServiceManager.getExperimentService().getFirstExperimentToRunByParent(experimentPack);
                    if(experiment != null) {
                        this.experiment = experiment;
                        break;
                    }
                } else {
                    Experiment experiment = ServiceManager.getExperimentService().getFirstExperimentByTitle(arg);
                    if (experiment != null) {
                        if (experiment.getState() == ExperimentState.PENDING) {
                            this.experiment = experiment;
                            break;
                        }
                    } else {
                        System.err.println("Experiment pack or experiment \"" + arg + "\" do not exist");
                    }
                }
            }
        }

        if (this.experiment != null) {
            runExperiment(this.experiment);
            this.experiment = null;
            return true;
        }

        return false;
    }

    private void runExperiment(Experiment experiment) {
        experiment.setState(ExperimentState.RUNNING);
        ServiceManager.getExperimentService().updateExperiment(experiment);

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

        ServiceManager.getExperimentService().updateExperiment(experiment);
        ServiceManager.getExperimentService().dumpExperiment(experiment);
    }

    public boolean isRunning() {
        return this.experiment != null;
    }
}
