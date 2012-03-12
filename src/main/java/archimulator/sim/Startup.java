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
package archimulator.sim;

import archimulator.model.experiment.profile.ExperimentProfile;
import archimulator.model.experiment.profile.ProcessorProfile;
import archimulator.model.simulation.SimulatedProgram;
import archimulator.sim.ext.uncore.newHt2.LastLevelCacheHtRequestCachePollutionProfilingCapability;

import java.util.ArrayList;
import java.util.List;

public class Startup {
    public static final SimulatedProgram SIMULATED_PROGRAM_MST_BASELINE = new SimulatedProgram(
            "mst_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
            "mst.mips",
            "10000");
//            "2000");
//            "1000");
//    "400");
//    "100");

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_HT = new SimulatedProgram(
            "mst_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
            "mst.mips",
            "10000");
//            "4000");
//            "2000");
//            "1000");
//            "400");
//            "200");
//            "100");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_BASELINE = new SimulatedProgram(
            "em3d_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
            "em3d.mips",
//            "400000 128 75 1");
//            "400 128 75 1");
//            "1000 128 75 1");
//            "10000 128 75 1");
            "400000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_HT = new SimulatedProgram(
            "em3d_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
            "em3d.mips",
//            "400000 128 75 1");
//            "400 128 75 1");
//            "1000 128 75 1");
//            "4000 128 75 1");
//            "10000 128 75 1");
            "400000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_BASELINE = new SimulatedProgram(
            "429_mcf_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
            "429.mcf.mips",
            ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_HT = new SimulatedProgram(
            "429_mcf_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
            "429.mcf.mips",
            ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in");
//            getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/test/input/inp.in");

    public static final SimulatedProgram SIMULATED_PROGRAM_462_LIBQUANTUM_BASELINE = new SimulatedProgram(
            "462_libquantum_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/baseline",
            "462.libquantum.mips",
            "33 5");

    public static final SimulatedProgram SIMULATED_PROGRAM_462_LIBQUANTUM_HT = new SimulatedProgram(
            "462_libquantum_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/462.libquantum/ht",
            "462.libquantum.mips",
            "33 5");

    public static void main(String[] args) {
        List<SimulatedProgram> simulatedPrograms = new ArrayList<SimulatedProgram>();
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT);
        simulatedPrograms.add(SIMULATED_PROGRAM_EM3D_HT);
        simulatedPrograms.add(SIMULATED_PROGRAM_429_MCF_HT);

        List<Integer> l2SizeInKBytes = new ArrayList<Integer>();
        l2SizeInKBytes.add(512);
        l2SizeInKBytes.add(512 * 2);
        l2SizeInKBytes.add(512 * 4);
        l2SizeInKBytes.add(512 * 8);

        List<ExperimentProfile> experimentProfiles = new ArrayList<ExperimentProfile>();

        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
            int numCores1 = 2;
            int numThreadsPerCore1 = 2;
            int l2SizeInKByte1 = 1024 * 4;
            int l2Associativity1 = 8;

            ProcessorProfile processorProfile = new ProcessorProfile("C" + numCores1 + "T" + numThreadsPerCore1 + "-" + "L2SizeInKB_" + l2SizeInKByte1 + "-" + "L2Assoc_" + l2Associativity1, numCores1, numThreadsPerCore1, 1024 * l2SizeInKByte1, l2Associativity1);

            ExperimentProfile experimentProfile = new ExperimentProfile(processorProfile);
            experimentProfile.addWorkload(simulatedProgram);
            experimentProfile.functionallyToEnd();
            experimentProfiles.add(experimentProfile);

            for (int l2SizeInKByte : l2SizeInKBytes) {
                int numCores = 2;
                int numThreadsPerCore = 2;
                int l2Associativity = 8;
                ProcessorProfile processorProfile1 = new ProcessorProfile("C" + numCores + "T" + numThreadsPerCore + "-" + "L2SizeInKB_" + l2SizeInKByte + "-" + "L2Assoc_" + l2Associativity, numCores, numThreadsPerCore, 1024 * l2SizeInKByte, l2Associativity);

                ExperimentProfile experimentProfile1 = new ExperimentProfile(processorProfile1);
                experimentProfile1.addWorkload(simulatedProgram);
                experimentProfile1.functionallyToPseudoCallAndInDetailForMaxInsts(3720, 2000000000).addSimulationCapabilityClass(LastLevelCacheHtRequestCachePollutionProfilingCapability.class);
                experimentProfiles.add(experimentProfile1);

                ExperimentProfile experimentProfile2 = new ExperimentProfile(processorProfile1);
                experimentProfile2.addWorkload(simulatedProgram);
                experimentProfile2.inDetailToEnd().addSimulationCapabilityClass(LastLevelCacheHtRequestCachePollutionProfilingCapability.class);
                experimentProfiles.add(experimentProfile2);
            }
        }
        
        for(ExperimentProfile experimentProfile : experimentProfiles) {
            experimentProfile.createExperiment().runToEnd();
        }
    }
}
