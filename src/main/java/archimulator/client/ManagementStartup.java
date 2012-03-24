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
package archimulator.client;

import archimulator.service.ArchimulatorServiceImpl;
import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.service.ArchimulatorService;
import archimulator.sim.ext.uncore.cache.eviction.LLCHTAwareLRUPolicy;
import archimulator.sim.ext.uncore.llc.LLCHTRequestProfilingCapability;
import archimulator.sim.uncore.cache.eviction.LRUPolicy;
import archimulator.util.DateHelper;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ManagementStartup {
    private ArchimulatorService archimulatorService;

    public ManagementStartup() {
        try {
            HessianProxyFactory factory = new HessianProxyFactory();
            factory.setReadTimeout(30000);
            factory.setConnectTimeout(20000);
            factory.setOverloadEnabled(true);

            this.archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, GuestStartup.SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }
    }

    private void submitSimulatedProgramsAndProcessorProfiles() throws SQLException {
        this.archimulatorService.setRunningExperimentEnabled(false);

        List<SimulatedProgram> simulatedPrograms = new ArrayList<SimulatedProgram>();
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_BASELINE);

        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(10, 10));
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(20, 10));
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(20, 20));
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(320, 320));
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(640, 320));
        simulatedPrograms.add(SIMULATED_PROGRAM_MST_HT(640, 640));

        simulatedPrograms.add(SIMULATED_PROGRAM_EM3D_BASELINE);
        simulatedPrograms.add(SIMULATED_PROGRAM_EM3D_HT);

        simulatedPrograms.add(SIMULATED_PROGRAM_429_MCF_BASELINE);
        simulatedPrograms.add(SIMULATED_PROGRAM_429_MCF_HT);

        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
            this.archimulatorService.addSimulatedProgram(simulatedProgram);
        }

        List<Integer> l2SizeInKBytes = new ArrayList<Integer>();
        l2SizeInKBytes.add(1024);
        l2SizeInKBytes.add(1024 * 2);
        l2SizeInKBytes.add(1024 * 4);

        List<Integer> l2Associativities = new ArrayList<Integer>();
        l2Associativities.add(4);
        l2Associativities.add(8);

        for (int l2SizeInKByte : l2SizeInKBytes) {
            for (int l2Associativity : l2Associativities) {
                int numCores = 2;
                int numThreadsPerCore = 2;
                this.archimulatorService.addProcessorProfile(new ProcessorProfile("C" + numCores + "T" + numThreadsPerCore + "-" + "L2_" + l2SizeInKByte + "KB" + "_" + "Assoc" + l2Associativity + "_" + "LRU", numCores, numThreadsPerCore, 1024 * l2SizeInKByte, l2Associativity, LRUPolicy.class));
                this.archimulatorService.addProcessorProfile(new ProcessorProfile("C" + numCores + "T" + numThreadsPerCore + "-" + "L2_" + l2SizeInKByte + "KB" + "_" + "Assoc" + l2Associativity + "_" + "LLCHTAwareLRU", numCores, numThreadsPerCore, 1024 * l2SizeInKByte, l2Associativity, LLCHTAwareLRUPolicy.class));
            }
        }
    }

    private void submitExperimentProfiles() throws SQLException {
        List<ExperimentProfile> experimentProfiles = new ArrayList<ExperimentProfile>();

        List<SimulatedProgram> simulatedPrograms = this.archimulatorService.getSimulatedProgramsAsList();

        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
            if(simulatedProgram.getTitle().startsWith("mst_baseline")) {
                List<ProcessorProfile> processorProfiles = this.archimulatorService.getProcessorProfilesAsList();

                for(ProcessorProfile processorProfile : processorProfiles) {
                    if(processorProfile.getL2EvictionPolicyClz().equals(LRUPolicy.class)) {
                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
                        experimentProfile.addWorkload(simulatedProgram);
                        experimentProfile.inDetailToEnd();
                        experimentProfiles.add(experimentProfile);
                    }
                }
            }
            else if(simulatedProgram.getTitle().startsWith("mst_ht")) {
                List<ProcessorProfile> processorProfiles = this.archimulatorService.getProcessorProfilesAsList();

                for(ProcessorProfile processorProfile : processorProfiles) {
                    if(processorProfile.getL2EvictionPolicyClz().equals(LRUPolicy.class)) {
                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
                        experimentProfile.addWorkload(simulatedProgram);
                        experimentProfile.inDetailToEnd();
                        experimentProfile.addSimulationCapabilityClass(LLCHTRequestProfilingCapability.class);
                        experimentProfiles.add(experimentProfile);
                    }
                }
            }
        }

        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
            if(simulatedProgram.getTitle().startsWith("mst_ht")) {
                List<ProcessorProfile> processorProfiles = this.archimulatorService.getProcessorProfilesAsList();

                for(ProcessorProfile processorProfile : processorProfiles) {
                    if(processorProfile.getL2EvictionPolicyClz().equals(LLCHTAwareLRUPolicy.class)) {
                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
                        experimentProfile.addWorkload(simulatedProgram);
                        experimentProfile.inDetailToEnd();
                        experimentProfiles.add(experimentProfile);
                    }
                }
            }
        }

        for(ExperimentProfile experimentProfile : experimentProfiles) {
            this.archimulatorService.addExperimentProfile(experimentProfile);
        }
    }
    
    private void resetAdminPassword() throws SQLException {
        this.archimulatorService.setUserPassword(ArchimulatorServiceImpl.USER_ID_ADMIN, ArchimulatorServiceImpl.USER_INITIAL_PASSWORD_ADMIN);
    }

    public static final SimulatedProgram SIMULATED_PROGRAM_MST_BASELINE = new SimulatedProgram(
            "mst_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
            "mst.mips",
            "1000");

    public static SimulatedProgram SIMULATED_PROGRAM_MST_HT(int lookahead, int stride) {
        SimulatedProgram program = new SimulatedProgram(
                "mst_ht" + "-lookahead_" + lookahead + "-stride_" + stride, ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                "mst.mips",
                "1000");
        program.setHelperThreadedProgram(true);
        program.setHtLookahead(lookahead);
        program.setHtStride(stride);
        return program;
    }

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_BASELINE = new SimulatedProgram(
            "em3d_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
            "em3d.mips",
            "400000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_HT = new SimulatedProgram(
            "em3d_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
            "em3d.mips",
            "400000 128 75 1");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_BASELINE = new SimulatedProgram(
            "429_mcf_baseline", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
            "429.mcf.mips",
            ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in");

    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_HT = new SimulatedProgram(
            "429_mcf_ht", ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
            "429.mcf.mips",
            ExperimentProfile.getUserHome() + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in");

    public static void recordException(Exception e) {
        System.out.print(String.format("[%s Exception] %s\r\n", DateHelper.toString(new Date()), e));
        e.printStackTrace();
    }

    public static void main(String[] args) throws SQLException {
        ManagementStartup startup = new ManagementStartup();
        startup.submitSimulatedProgramsAndProcessorProfiles();
        startup.submitExperimentProfiles();
//        startup.resetAdminPassword();
    }
}
