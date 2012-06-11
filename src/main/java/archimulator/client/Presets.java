package archimulator.client;

import archimulator.sim.base.experiment.profile.ExperimentProfile;
import archimulator.sim.base.experiment.profile.ProcessorProfile;
import archimulator.sim.base.simulation.SimulatedProgram;
import archimulator.sim.uncore.ht.HTLLCRequestProfilingCapability;
import archimulator.sim.uncore.ht.LLCReuseDistanceProfilingCapability;
import archimulator.sim.uncore.cache.eviction.EvictionPolicy;

public class Presets {
    public static final SimulatedProgram SIMULATED_PROGRAM_MST_BASELINE = new SimulatedProgram(
            "mst_baseline", ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/baseline",
            "mst.mips",
//            "10000");
//            "100");
//            "200");
//            "400");
            "1024");
    //            "2000");
//            "4000");
    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_BASELINE = new SimulatedProgram(
            "em3d_baseline", ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/baseline",
            "em3d.mips",
            "400000 128 75 1");
    public static final SimulatedProgram SIMULATED_PROGRAM_EM3D_HT = new SimulatedProgram(
            "em3d_ht", ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/em3d/ht",
            "em3d.mips",
            "400000 128 75 1");
    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_BASELINE = new SimulatedProgram(
            "429_mcf_baseline", ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline",
            "429.mcf.mips",
            ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/baseline/data/ref/input/inp.in");
    public static final SimulatedProgram SIMULATED_PROGRAM_429_MCF_HT = new SimulatedProgram(
            "429_mcf_ht", ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht",
            "429.mcf.mips",
            ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/CPU2006_Custom1/429.mcf/ht/data/ref/input/inp.in");

    public static SimulatedProgram SIMULATED_PROGRAM_MST_HT(int lookahead, int stride) {
        SimulatedProgram program = new SimulatedProgram(
                "mst_ht" + "-lookahead_" + lookahead + "-stride_" + stride, ExperimentProfile.USER_HOME_TEMPLATE_ARG + "/Archimulator/benchmarks/Olden_Custom1/mst/ht",
                "mst.mips",
//                "10000");
//                "100");
//                "200");
//                "400");
                "1024");
//                "2000");
//                "4000");
        program.setHelperThreadedProgram(true);
        program.setHtLookahead(lookahead);
        program.setHtStride(stride);
        return program;
    }

    public static ProcessorProfile processor(int numThreadsPerCore, int numCores, int l1ISizeInKByte, int l1IAssociativity, int l1DSizeInKByte, int l1DAssociativity, int l2SizeInKByte, int l2Associativity, String l2EvictionPolicyName, Class<? extends EvictionPolicy> l2EvictionPolicyClz) {
        return new ProcessorProfile("C" + numCores + "T" + numThreadsPerCore
                + "-" + "L1I_" + l1ISizeInKByte + "KB" + "_" + "Assoc" + l1IAssociativity
                + "-" + "L2_" + l1DSizeInKByte + "KB" + "_" + "Assoc" + l1DAssociativity
                + "-" + "L2_" + l2SizeInKByte + "KB" + "_" + "Assoc" + l2Associativity + "_" + l2EvictionPolicyName
                , numCores, numThreadsPerCore, 1024 * l1ISizeInKByte, l1IAssociativity, 1024 * l1DSizeInKByte, l1DAssociativity, 1024 * l2SizeInKByte, l2Associativity, l2EvictionPolicyClz);
    }

    public static ExperimentProfile baseline_lru(int pthreadSpawnedIndex, int maxInsts, ProcessorProfile processorProfile, SimulatedProgram simulatedProgram) {
        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
        experimentProfile.addWorkload(simulatedProgram);
//        experimentProfile.functionallyToEnd();
//        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
        experimentProfile.inDetailToEnd();
        experimentProfile.addSimulationCapabilityClass(LLCReuseDistanceProfilingCapability.class);
        experimentProfile.addSimulationCapabilityClass(HTLLCRequestProfilingCapability.class);
        return experimentProfile;
    }

    public static ExperimentProfile ht_lru(int pthreadSpawnedIndex, int maxInsts, ProcessorProfile processorProfile, SimulatedProgram simulatedProgram) {
        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
        experimentProfile.addWorkload(simulatedProgram);
//        experimentProfile.functionallyToEnd();
        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
//        experimentProfile.inDetailToEnd();
        experimentProfile.addSimulationCapabilityClass(LLCReuseDistanceProfilingCapability.class);
        experimentProfile.addSimulationCapabilityClass(HTLLCRequestProfilingCapability.class);
        return experimentProfile;
    }

    public static ExperimentProfile ht_ht_aware_lru(int pthreadSpawnedIndex, int maxInsts, ProcessorProfile processorProfile, SimulatedProgram simulatedProgram) {
        ExperimentProfile experimentProfile = new ExperimentProfile(simulatedProgram.getTitle() + "-" + processorProfile.getTitle(), processorProfile);
        experimentProfile.addWorkload(simulatedProgram);
//        experimentProfile.functionallyToEnd();
//        experimentProfile.fastForwardToPseudoCallAndInDetailForMaxInsts(pthreadSpawnedIndex, maxInsts);
        experimentProfile.inDetailToEnd();
        experimentProfile.addSimulationCapabilityClass(LLCReuseDistanceProfilingCapability.class);
        return experimentProfile;
    }
}
