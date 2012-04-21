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

            this.archimulatorService = (ArchimulatorService) factory.create(ArchimulatorService.class, SERVICE_URL);
        } catch (MalformedURLException e) {
            recordException(e);
            throw new RuntimeException(e);
        }
    }

    private void submitSimulatedProgramsAndProcessorProfiles() throws SQLException {
        this.archimulatorService.setRunningExperimentEnabled(false);
        
        this.archimulatorService.clearData();

        List<SimulatedProgram> simulatedPrograms = new ArrayList<SimulatedProgram>();
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_BASELINE);

        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(10, 10));
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(20, 10));
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(20, 20));
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(320, 320));
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(640, 320));
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_MST_HT(640, 640));

        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_EM3D_BASELINE);
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_EM3D_HT);

        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_429_MCF_BASELINE);
        simulatedPrograms.add(Presets.SIMULATED_PROGRAM_429_MCF_HT);

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
                this.archimulatorService.addProcessorProfile(Presets.processor(l2SizeInKByte, l2Associativity, numCores, numThreadsPerCore, "LRU", LRUPolicy.class));
                this.archimulatorService.addProcessorProfile(Presets.processor(l2SizeInKByte, l2Associativity, numCores, numThreadsPerCore, "LLCHTAwareLRU", LLCHTAwareLRUPolicy.class));
            }
        }
    }

    private void submitExperimentProfiles() throws SQLException {
        List<ExperimentProfile> experimentProfiles = new ArrayList<ExperimentProfile>();

        List<SimulatedProgram> simulatedPrograms = this.archimulatorService.getSimulatedProgramsAsList();

        int pthreadSpawnedIndex = 3720;
        int maxInsts = 200000000;
//        int maxInsts = 20000;

        List<ProcessorProfile> processorProfiles = this.archimulatorService.getProcessorProfilesAsList();

        for(ProcessorProfile processorProfile : processorProfiles) {
            if(processorProfile.getL2EvictionPolicyClz().equals(LRUPolicy.class)) {
                for(SimulatedProgram simulatedProgram : simulatedPrograms) {
                    if(simulatedProgram.getTitle().startsWith("mst_baseline")) {
                        experimentProfiles.add(Presets.baseline_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram));
                    }
                    else if(simulatedProgram.getTitle().startsWith("mst_ht")) {
                        experimentProfiles.add(Presets.ht_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram));
                    }
                }
            }
            else if(processorProfile.getL2EvictionPolicyClz().equals(LLCHTAwareLRUPolicy.class)) {
                for(SimulatedProgram simulatedProgram : simulatedPrograms) {
                    if(simulatedProgram.getTitle().startsWith("mst_ht")) {
                        experimentProfiles.add(Presets.ht_ht_aware_lru(pthreadSpawnedIndex, maxInsts, processorProfile, simulatedProgram));
                    }
                }
            }
        }

        ///////////////////

//        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
//            if(simulatedProgram.getTitle().startsWith("mst_baseline")) {
//                for(ProcessorProfile processorProfile : processorProfiles) {
//                    if(processorProfile.getL2EvictionPolicyClz().equals(LRUPolicy.class)) {
//                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
//                        experimentProfile.addWorkload(simulatedProgram);
//                        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
//                        experimentProfiles.add(experimentProfile);
//                    }
//                }
//            }
//            else if(simulatedProgram.getTitle().startsWith("mst_ht")) {
//                for(ProcessorProfile processorProfile : processorProfiles) {
//                    if(processorProfile.getL2EvictionPolicyClz().equals(LRUPolicy.class)) {
//                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
//                        experimentProfile.addWorkload(simulatedProgram);
//                        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
//                        experimentProfile.addSimulationCapabilityClass(HTLLCRequestProfilingCapability.class);
//                        experimentProfiles.add(experimentProfile);
//                    }
//                }
//            }
//        }
//
//        for (SimulatedProgram simulatedProgram : simulatedPrograms) {
//            if(simulatedProgram.getTitle().startsWith("mst_ht")) {
//                for(ProcessorProfile processorProfile : processorProfiles) {
//                    if(processorProfile.getL2EvictionPolicyClz().equals(LLCHTAwareLRUPolicy.class)) {
//                        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
//                        experimentProfile.addWorkload(simulatedProgram);
//                        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
//                        experimentProfiles.add(experimentProfile);
//                    }
//                }
//            }
//        }

        for(ExperimentProfile experimentProfile : experimentProfiles) {
            System.out.println("Submitting experiment profile: " + experimentProfile);
            this.archimulatorService.addExperimentProfile(experimentProfile);
        }

        this.archimulatorService.setRunningExperimentEnabled(true);
    }

    private void resetAdminPassword() throws SQLException {
        this.archimulatorService.setUserPassword(ArchimulatorServiceImpl.USER_ID_ADMIN, ArchimulatorServiceImpl.USER_INITIAL_PASSWORD_ADMIN);
    }

    //    public static final String SERVICE_URL = "http://204.152.205.131:8080/archimulator/archimulator";
    //    public static final String SERVICE_URL = "http://50.117.112.114:8080/archimulator/archimulator";
    //    public static final String SERVICE_URL = "http://[2607:f358:10:13::2]:8080/archimulator/archimulator";
        public static final String SERVICE_URL = "http://localhost:8080/api";
//    public static final String SERVICE_URL = "http://[2607:f358:10:13::2]/api";
//        public static final String SERVICE_URL = "http://www.archimulator.com/api";

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
