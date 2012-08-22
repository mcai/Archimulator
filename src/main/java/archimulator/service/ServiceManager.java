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

public class ServiceManager {
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    public static final String DATABASE_REVISION = "1";
    public static final String DATABASE_DIRECTORY = System.getProperty("user.dir") + "/" + "experiments";
    public static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_DIRECTORY + "/v" + DATABASE_REVISION + ".sqlite";

    private static SimulatedProgramService simulatedProgramService;
    private static ArchitectureService architectureService;
    private static ExperimentService experimentService;

    public static boolean runningExperiments = false;

    static {
        simulatedProgramService = new SimulatedProgramServiceImpl();
        architectureService = new ArchitectureServiceImpl();
        experimentService = new ExperimentServiceImpl();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (runningExperiments) {
                    for (Experiment experiment : experimentService.getAllExperiments()) {
                        if (experiment.getState() == ExperimentState.RUNNING) {
                            experiment.setFailedReason("");
                            experiment.setState(ExperimentState.PENDING);
                            experiment.getStats().clear();
                            experimentService.updateExperiment(experiment);
                        }
                    }
                }
            }
        });
    }

    public static SimulatedProgramService getSimulatedProgramService() {
        return simulatedProgramService;
    }

    public static ArchitectureService getArchitectureService() {
        return architectureService;
    }

    public static ExperimentService getExperimentService() {
        return experimentService;
    }
}
