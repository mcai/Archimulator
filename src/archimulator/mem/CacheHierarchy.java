package archimulator.mem;

import archimulator.mem.coherence.FirstLevelCache;
import archimulator.mem.coherence.LastLevelCache;
import archimulator.mem.dram.MainMemory;
import archimulator.mem.net.L2ToMemNet;
import archimulator.mem.net.Net;
import archimulator.mem.tlb.TranslationLookasideBuffer;
import archimulator.sim.SimulationObject;

import java.util.List;

public interface CacheHierarchy extends SimulationObject {
    void dumpState();

    MainMemory getMainMemory();

    LastLevelCache getL2Cache();

    List<FirstLevelCache> getInstructionCaches();

    List<FirstLevelCache> getDataCaches();

    List<TranslationLookasideBuffer> getItlbs();

    List<TranslationLookasideBuffer> getDtlbs();

    Net getL1sToL2Network();

    L2ToMemNet getL2ToMemNetwork();
}
