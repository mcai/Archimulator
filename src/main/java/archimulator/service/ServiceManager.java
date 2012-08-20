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

import archimulator.model.SimulatedProgram;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;

public class ServiceManager {
    public static final String USER_HOME_TEMPLATE_ARG = "<user.home>";

    public static final String DATABASE_REVISION = "1";
    public static final String DATABASE_DIRECTORY = System.getProperty("user.dir") + "/" + "experiments";
    public static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_DIRECTORY + "/v" + DATABASE_REVISION + ".sqlite";

    private static SimulatedProgramService simulatedProgramService;
    private static ArchitectureService architectureService;
    private static ExperimentService experimentService;

    static {
        simulatedProgramService = new SimulatedProgramServiceImpl();
        architectureService = new ArchitectureServiceImpl();
        experimentService = new ExperimentServiceImpl();

        initializeData();
    }

    private static void initializeData() {
        initializeSimulatedProgramServiceData();
        initializeArchitectureServiceData();
    }

    private static void initializeSimulatedProgramServiceData() {
        if(simulatedProgramService.getFirstSimulatedProgram() == null) {
            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "mst_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
                    "mst.mips",
                    "4000"));

            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "mst_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                    "mst.mips",
                    "4000", "", true));

            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "em3d_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
                    "em3d.mips",
                    "400000 128 75 1"));

            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "em3d_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
                    "em3d.mips",
                    "400000 128 75 1", "", true));

            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "429_mcf_baseline", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in"));

            simulatedProgramService.addSimulatedProgram(new SimulatedProgram(
                    "429_mcf_ht", ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
                    "429.mcf.mips",
                    ServiceManager.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in", "", true));
        }
    }

    private static void initializeArchitectureServiceData() {
//        architectureService.getOrAddArchitecture("default", true, 32 * 1024, 4, 32 * 1024, 4, 96 * 1024, 8, CacheReplacementPolicyType.LRU);
        architectureService.getOrAddArchitecture(true, 2, 2, 32 * 1024, 8, 32 * 1024, 8, 4 * 1024 * 1024, 16, CacheReplacementPolicyType.LRU);
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
