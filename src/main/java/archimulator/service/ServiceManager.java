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

import archimulator.model.Architecture;
import archimulator.model.SimulatedProgram;
import archimulator.sim.core.bpred.BranchPredictorType;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MainMemoryType;

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
        if(architectureService.getFirstArchitecture() == null) {
            architectureService.addArchitecture(createArchitecture("default", true, 32768, 4, 32768, 4, 32768 * 3, 8, CacheReplacementPolicyType.LRU));
            architectureService.addArchitecture(createArchitecture("q6600", true, 32 * 1024, 8, 32 * 1024, 8, 4 * 1024 * 1024, 16, CacheReplacementPolicyType.LRU));
        }
    }

    private static Architecture createArchitecture(String title, boolean htLLCRequestProfilingEnabled, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType) {
        Architecture architecture = new Architecture(title);

        architecture.setHtPthreadSpawnIndex(3720);
        architecture.setHtLLCRequestProfilingEnabled(htLLCRequestProfilingEnabled);

        architecture.setNumCores(2);
        architecture.setNumThreadsPerCore(2);

        architecture.setPhysicalRegisterFileCapacity(128);
        architecture.setDecodeWidth(4);
        architecture.setIssueWidth(4);
        architecture.setCommitWidth(4);
        architecture.setDecodeBufferCapacity(96);
        architecture.setReorderBufferCapacity(96);
        architecture.setLoadStoreQueueCapacity(48);

        architecture.setBpredType(BranchPredictorType.PERFECT);

        architecture.setTwoBitBpredBimodSize(2048);
        architecture.setTwoBitBpredBtbSets(512);
        architecture.setTwoBitBpredBtbAssoc(4);
        architecture.setTwoBitBpredRetStackSize(8);

        architecture.setTwoLevelBpredL1Size(1);
        architecture.setTwoLevelBpredL2Size(1024);
        architecture.setTwoLevelBpredShiftWidth(8);
        architecture.setTwoLevelBpredXor(false);
        architecture.setTwoLevelBpredBtbSets(512);
        architecture.setTwoLevelBpredBtbAssoc(4);
        architecture.setTwoLevelBpredRetStackSize(8);

        architecture.setCombinedBpredBimodSize(2048);
        architecture.setCombinedBpredL1Size(1);
        architecture.setCombinedBpredL2Size(1024);
        architecture.setCombinedBpredMetaSize(1024);
        architecture.setCombinedBpredShiftWidth(8);
        architecture.setCombinedBpredXor(false);
        architecture.setCombinedBpredBtbSets(512);
        architecture.setCombinedBpredBtbAssoc(4);
        architecture.setCombinedBpredBtbRetStackSize(8);

        architecture.setTlbSize(32768);
        architecture.setTlbAssoc(4);
        architecture.setTlbLineSize(64);
        architecture.setTlbHitLatency(2);
        architecture.setTlbMissLatency(30);

        architecture.setL1ISize(l1ISize);
        architecture.setL1IAssoc(l1IAssoc);
        architecture.setL1ILineSize(64);
        architecture.setL1IHitLatency(1);
        architecture.setL1INumReadPorts(128);
        architecture.setL1INumWritePorts(128);
        architecture.setL1IReplacementPolicyType(CacheReplacementPolicyType.LRU);

        architecture.setL1DSize(l1DSize);
        architecture.setL1DAssoc(l1DAssoc);
        architecture.setL1DLineSize(64);
        architecture.setL1DHitLatency(1);
        architecture.setL1DNumReadPorts(128);
        architecture.setL1DNumWritePorts(128);
        architecture.setL1DReplacementPolicyType(CacheReplacementPolicyType.LRU);

        architecture.setL2Size(l2Size);
        architecture.setL2Assoc(l2Assoc);
        architecture.setL2LineSize(64);
        architecture.setL2HitLatency(10);
        architecture.setL2ReplacementPolicyType(l2ReplacementPolicyType);

        architecture.setMainMemoryType(MainMemoryType.FIXED_LATENCY);
        architecture.setMainMemoryLineSize(64);

        architecture.setFixedLatencyMainMemoryLatency(200);

        architecture.setSimpleMainMemoryMemoryLatency(200);
        architecture.setSimpleMainMemoryMemoryTrunkLatency(2);
        architecture.setSimpleMainMemoryBusWidth(4);

        architecture.setBasicMainMemoryToDramLatency(6);
        architecture.setBasicMainMemoryFromDramLatency(12);
        architecture.setBasicMainMemoryPrechargeLatency(90);
        architecture.setBasicMainMemoryClosedLatency(90);
        architecture.setBasicMainMemoryConflictLatency(90);
        architecture.setBasicMainMemoryBusWidth(4);
        architecture.setBasicMainMemoryNumBanks(8);
        architecture.setBasicMainMemoryRowSize(2048);

        return architecture;
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
