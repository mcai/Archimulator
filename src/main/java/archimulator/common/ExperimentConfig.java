package archimulator.common;

import archimulator.core.bpred.BranchPredictorType;
import archimulator.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.uncore.dram.MemoryControllerType;
import archimulator.util.StorageUnitHelper;

/**
 * Experiment config.
 *
 * @author Min Cai
 */
public class ExperimentConfig {
    private ExperimentType type;

    private String outputDirectory;

    private long numMaxInstructions;

    private int helperThreadPthreadSpawnIndex;

    private boolean dynamicSpeculativePrecomputationEnabled;

    private int numMainThreadWaysInStaticPartitionedLRUPolicy;

    private int numCores;

    private int numThreadsPerCore;

    private int physicalRegisterFileCapacity;

    private int decodeWidth;

    private int issueWidth;

    private int commitWidth;

    private int decodeBufferCapacity;

    private int reorderBufferCapacity;

    private int loadStoreQueueCapacity;

    private BranchPredictorType branchPredictorType;

    private int twoBitBranchPredictorBimodSize;

    private int twoBitBranchPredictorBranchTargetBufferNumSets;

    private int twoBitBranchPredictorBranchTargetBufferAssociativity;

    private int twoBitBranchPredictorReturnAddressStackSize;

    private int twoLevelBranchPredictorL1Size;

    private int twoLevelBranchPredictorL2Size;

    private int twoLevelBranchPredictorShiftWidth;

    private boolean twoLevelBranchPredictorXor;

    private int twoLevelBranchPredictorBranchTargetBufferNumSets;

    private int twoLevelBranchPredictorBranchTargetBufferAssociativity;

    private int twoLevelBranchPredictorReturnAddressStackSize;

    private int combinedBranchPredictorBimodSize;

    private int combinedBranchPredictorL1Size;

    private int combinedBranchPredictorL2Size;

    private int combinedBranchPredictorMetaSize;

    private int combinedBranchPredictorShiftWidth;

    private boolean combinedBranchPredictorXor;

    private int combinedBranchPredictorBranchTargetBufferNumSets;

    private int combinedBranchPredictorBranchTargetBufferAssociativity;

    private int combinedBranchPredictorReturnAddressStackSize;

    private int tlbSize;

    private int tlbAssociativity;

    private int tlbLineSize;

    private int tlbHitLatency;

    private int tlbMissLatency;

    private int l1ISize;

    private int l1IAssociativity;

    private int l1ILineSize;

    private int l1IHitLatency;

    private int l1INumReadPorts;

    private int l1INumWritePorts;

    private CacheReplacementPolicyType l1IReplacementPolicyType;

    private int l1DSize;

    private int l1DAssociativity;

    private int l1DLineSize;

    private int l1DHitLatency;

    private int l1DNumReadPorts;

    private int l1DNumWritePorts;

    private CacheReplacementPolicyType l1DReplacementPolicyType;

    private int l2Size;

    private int l2Associativity;

    private int l2LineSize;

    private int l2HitLatency;

    private CacheReplacementPolicyType l2ReplacementPolicyType;

    private MemoryControllerType memoryControllerType;

    private int memoryControllerLineSize;

    private int fixedLatencyMemoryControllerLatency;

    private int simpleMemoryControllerMemoryLatency;

    private int simpleMemoryControllerMemoryTrunkLatency;

    private int simpleMemoryControllerBusWidth;

    private int basicMemoryControllerToDramLatency;

    private int basicMemoryControllerFromDramLatency;

    private int basicMemoryControllerPrechargeLatency;

    private int basicMemoryControllerClosedLatency;

    private int basicMemoryControllerConflictLatency;

    private int basicMemoryControllerBusWidth;

    private int basicMemoryControllerNumBanks;

    private int basicMemoryControllerRowSize;

    public ExperimentConfig() {
        this.type = ExperimentType.DETAILED;
        this.outputDirectory = "";
        this.numMaxInstructions = -1;

        this.helperThreadPthreadSpawnIndex = 3720;

        this.dynamicSpeculativePrecomputationEnabled = false;

        this.numMainThreadWaysInStaticPartitionedLRUPolicy = -1;

        this.numCores = 2;
        this.numThreadsPerCore = 2;

        this.physicalRegisterFileCapacity = 128;
        this.decodeWidth = 4;
        this.issueWidth = 4;
        this.commitWidth = 4;
        this.decodeBufferCapacity = 96;
        this.reorderBufferCapacity = 96;
        this.loadStoreQueueCapacity = 48;

        this.branchPredictorType = BranchPredictorType.PERFECT;

        this.twoBitBranchPredictorBimodSize = 2048;
        this.twoBitBranchPredictorBranchTargetBufferNumSets = 512;
        this.twoBitBranchPredictorBranchTargetBufferAssociativity = 4;
        this.twoBitBranchPredictorReturnAddressStackSize = 8;

        this.twoLevelBranchPredictorL1Size = 1;
        this.twoLevelBranchPredictorL2Size = 1024;
        this.twoLevelBranchPredictorShiftWidth = 8;
        this.twoLevelBranchPredictorXor = false;
        this.twoLevelBranchPredictorBranchTargetBufferNumSets = 512;
        this.twoLevelBranchPredictorBranchTargetBufferAssociativity = 4;
        this.twoLevelBranchPredictorReturnAddressStackSize = 8;

        this.combinedBranchPredictorBimodSize = 2048;
        this.combinedBranchPredictorL1Size = 1;
        this.combinedBranchPredictorL2Size = 1024;
        this.combinedBranchPredictorMetaSize = 1024;
        this.combinedBranchPredictorShiftWidth = 8;
        this.combinedBranchPredictorXor = false;
        this.combinedBranchPredictorBranchTargetBufferNumSets = 512;
        this.combinedBranchPredictorBranchTargetBufferAssociativity = 4;
        this.combinedBranchPredictorReturnAddressStackSize = 8;

        this.tlbSize = 32768;
        this.tlbAssociativity = 4;
        this.tlbLineSize = 64;
        this.tlbHitLatency = 2;
        this.tlbMissLatency = 30;

        this.l1ISize = (int) StorageUnitHelper.displaySizeToByteCount("64 KB");
        this.l1IAssociativity = 4;
        this.l1ILineSize = 64;
        this.l1IHitLatency = 1;
        this.l1INumReadPorts = 128;
        this.l1INumWritePorts = 128;
        this.l1IReplacementPolicyType = CacheReplacementPolicyType.LRU;

        this.l1DSize = (int) StorageUnitHelper.displaySizeToByteCount("64 KB");
        this.l1DAssociativity = 4;
        this.l1DLineSize = 64;
        this.l1DHitLatency = 1;
        this.l1DNumReadPorts = 128;
        this.l1DNumWritePorts = 128;
        this.l1DReplacementPolicyType = CacheReplacementPolicyType.LRU;

        this.l2Size = (int) StorageUnitHelper.displaySizeToByteCount("512 KB");
        this.l2Associativity = 16;
        this.l2LineSize = 64;
        this.l2HitLatency = 10;
        this.l2ReplacementPolicyType = CacheReplacementPolicyType.LRU;

        this.memoryControllerType = MemoryControllerType.FIXED_LATENCY;
        this.memoryControllerLineSize = 64;

        this.fixedLatencyMemoryControllerLatency = 200;

        this.simpleMemoryControllerMemoryLatency = 200;
        this.simpleMemoryControllerMemoryTrunkLatency = 2;
        this.simpleMemoryControllerBusWidth = 4;

        this.basicMemoryControllerToDramLatency = 6;
        this.basicMemoryControllerFromDramLatency = 12;
        this.basicMemoryControllerPrechargeLatency = 90;
        this.basicMemoryControllerClosedLatency = 90;
        this.basicMemoryControllerConflictLatency = 90;
        this.basicMemoryControllerBusWidth = 4;
        this.basicMemoryControllerNumBanks = 8;
        this.basicMemoryControllerRowSize = 2048;
    }

    public ExperimentType getType() {
        return type;
    }

    public void setType(ExperimentType type) {
        this.type = type;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public long getNumMaxInstructions() {
        return numMaxInstructions;
    }

    public void setNumMaxInstructions(long numMaxInstructions) {
        this.numMaxInstructions = numMaxInstructions;
    }

    public int getHelperThreadPthreadSpawnIndex() {
        return helperThreadPthreadSpawnIndex;
    }

    public void setHelperThreadPthreadSpawnIndex(int helperThreadPthreadSpawnIndex) {
        this.helperThreadPthreadSpawnIndex = helperThreadPthreadSpawnIndex;
    }

    public boolean isDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    public void setDynamicSpeculativePrecomputationEnabled(boolean dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;
    }

    public int getNumMainThreadWaysInStaticPartitionedLRUPolicy() {
        return numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    public void setNumMainThreadWaysInStaticPartitionedLRUPolicy(int numMainThreadWaysInStaticPartitionedLRUPolicy) {
        this.numMainThreadWaysInStaticPartitionedLRUPolicy = numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    public int getNumCores() {
        return numCores;
    }

    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    public int getPhysicalRegisterFileCapacity() {
        return physicalRegisterFileCapacity;
    }

    public void setPhysicalRegisterFileCapacity(int physicalRegisterFileCapacity) {
        this.physicalRegisterFileCapacity = physicalRegisterFileCapacity;
    }

    public int getDecodeWidth() {
        return decodeWidth;
    }

    public void setDecodeWidth(int decodeWidth) {
        this.decodeWidth = decodeWidth;
    }

    public int getIssueWidth() {
        return issueWidth;
    }

    public void setIssueWidth(int issueWidth) {
        this.issueWidth = issueWidth;
    }

    public int getCommitWidth() {
        return commitWidth;
    }

    public void setCommitWidth(int commitWidth) {
        this.commitWidth = commitWidth;
    }

    public int getDecodeBufferCapacity() {
        return decodeBufferCapacity;
    }

    public void setDecodeBufferCapacity(int decodeBufferCapacity) {
        this.decodeBufferCapacity = decodeBufferCapacity;
    }

    public int getReorderBufferCapacity() {
        return reorderBufferCapacity;
    }

    public void setReorderBufferCapacity(int reorderBufferCapacity) {
        this.reorderBufferCapacity = reorderBufferCapacity;
    }

    public int getLoadStoreQueueCapacity() {
        return loadStoreQueueCapacity;
    }

    public void setLoadStoreQueueCapacity(int loadStoreQueueCapacity) {
        this.loadStoreQueueCapacity = loadStoreQueueCapacity;
    }

    public BranchPredictorType getBranchPredictorType() {
        return branchPredictorType;
    }

    public void setBranchPredictorType(BranchPredictorType branchPredictorType) {
        this.branchPredictorType = branchPredictorType;
    }

    public int getTwoBitBranchPredictorBimodSize() {
        return twoBitBranchPredictorBimodSize;
    }

    public void setTwoBitBranchPredictorBimodSize(int twoBitBranchPredictorBimodSize) {
        this.twoBitBranchPredictorBimodSize = twoBitBranchPredictorBimodSize;
    }

    public int getTwoBitBranchPredictorBranchTargetBufferNumSets() {
        return twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    public void setTwoBitBranchPredictorBranchTargetBufferNumSets(int twoBitBranchPredictorBranchTargetBufferNumSets) {
        this.twoBitBranchPredictorBranchTargetBufferNumSets = twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    public int getTwoBitBranchPredictorBranchTargetBufferAssociativity() {
        return twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setTwoBitBranchPredictorBranchTargetBufferAssociativity(int twoBitBranchPredictorBranchTargetBufferAssociativity) {
        this.twoBitBranchPredictorBranchTargetBufferAssociativity = twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getTwoBitBranchPredictorReturnAddressStackSize() {
        return twoBitBranchPredictorReturnAddressStackSize;
    }

    public void setTwoBitBranchPredictorReturnAddressStackSize(int twoBitBranchPredictorReturnAddressStackSize) {
        this.twoBitBranchPredictorReturnAddressStackSize = twoBitBranchPredictorReturnAddressStackSize;
    }

    public int getTwoLevelBranchPredictorL1Size() {
        return twoLevelBranchPredictorL1Size;
    }

    public void setTwoLevelBranchPredictorL1Size(int twoLevelBranchPredictorL1Size) {
        this.twoLevelBranchPredictorL1Size = twoLevelBranchPredictorL1Size;
    }

    public int getTwoLevelBranchPredictorL2Size() {
        return twoLevelBranchPredictorL2Size;
    }

    public void setTwoLevelBranchPredictorL2Size(int twoLevelBranchPredictorL2Size) {
        this.twoLevelBranchPredictorL2Size = twoLevelBranchPredictorL2Size;
    }

    public int getTwoLevelBranchPredictorShiftWidth() {
        return twoLevelBranchPredictorShiftWidth;
    }

    public void setTwoLevelBranchPredictorShiftWidth(int twoLevelBranchPredictorShiftWidth) {
        this.twoLevelBranchPredictorShiftWidth = twoLevelBranchPredictorShiftWidth;
    }

    public boolean isTwoLevelBranchPredictorXor() {
        return twoLevelBranchPredictorXor;
    }

    public void setTwoLevelBranchPredictorXor(boolean twoLevelBranchPredictorXor) {
        this.twoLevelBranchPredictorXor = twoLevelBranchPredictorXor;
    }

    public int getTwoLevelBranchPredictorBranchTargetBufferNumSets() {
        return twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    public void setTwoLevelBranchPredictorBranchTargetBufferNumSets(int twoLevelBranchPredictorBranchTargetBufferNumSets) {
        this.twoLevelBranchPredictorBranchTargetBufferNumSets = twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    public int getTwoLevelBranchPredictorBranchTargetBufferAssociativity() {
        return twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setTwoLevelBranchPredictorBranchTargetBufferAssociativity(int twoLevelBranchPredictorBranchTargetBufferAssociativity) {
        this.twoLevelBranchPredictorBranchTargetBufferAssociativity = twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getTwoLevelBranchPredictorReturnAddressStackSize() {
        return twoLevelBranchPredictorReturnAddressStackSize;
    }

    public void setTwoLevelBranchPredictorReturnAddressStackSize(int twoLevelBranchPredictorReturnAddressStackSize) {
        this.twoLevelBranchPredictorReturnAddressStackSize = twoLevelBranchPredictorReturnAddressStackSize;
    }

    public int getCombinedBranchPredictorBimodSize() {
        return combinedBranchPredictorBimodSize;
    }

    public void setCombinedBranchPredictorBimodSize(int combinedBranchPredictorBimodSize) {
        this.combinedBranchPredictorBimodSize = combinedBranchPredictorBimodSize;
    }

    public int getCombinedBranchPredictorL1Size() {
        return combinedBranchPredictorL1Size;
    }

    public void setCombinedBranchPredictorL1Size(int combinedBranchPredictorL1Size) {
        this.combinedBranchPredictorL1Size = combinedBranchPredictorL1Size;
    }

    public int getCombinedBranchPredictorL2Size() {
        return combinedBranchPredictorL2Size;
    }

    public void setCombinedBranchPredictorL2Size(int combinedBranchPredictorL2Size) {
        this.combinedBranchPredictorL2Size = combinedBranchPredictorL2Size;
    }

    public int getCombinedBranchPredictorMetaSize() {
        return combinedBranchPredictorMetaSize;
    }

    public void setCombinedBranchPredictorMetaSize(int combinedBranchPredictorMetaSize) {
        this.combinedBranchPredictorMetaSize = combinedBranchPredictorMetaSize;
    }

    public int getCombinedBranchPredictorShiftWidth() {
        return combinedBranchPredictorShiftWidth;
    }

    public void setCombinedBranchPredictorShiftWidth(int combinedBranchPredictorShiftWidth) {
        this.combinedBranchPredictorShiftWidth = combinedBranchPredictorShiftWidth;
    }

    public boolean isCombinedBranchPredictorXor() {
        return combinedBranchPredictorXor;
    }

    public void setCombinedBranchPredictorXor(boolean combinedBranchPredictorXor) {
        this.combinedBranchPredictorXor = combinedBranchPredictorXor;
    }

    public int getCombinedBranchPredictorBranchTargetBufferNumSets() {
        return combinedBranchPredictorBranchTargetBufferNumSets;
    }

    public void setCombinedBranchPredictorBranchTargetBufferNumSets(int combinedBranchPredictorBranchTargetBufferNumSets) {
        this.combinedBranchPredictorBranchTargetBufferNumSets = combinedBranchPredictorBranchTargetBufferNumSets;
    }

    public int getCombinedBranchPredictorBranchTargetBufferAssociativity() {
        return combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setCombinedBranchPredictorBranchTargetBufferAssociativity(int combinedBranchPredictorBranchTargetBufferAssociativity) {
        this.combinedBranchPredictorBranchTargetBufferAssociativity = combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getCombinedBranchPredictorReturnAddressStackSize() {
        return combinedBranchPredictorReturnAddressStackSize;
    }

    public void setCombinedBranchPredictorReturnAddressStackSize(int combinedBranchPredictorReturnAddressStackSize) {
        this.combinedBranchPredictorReturnAddressStackSize = combinedBranchPredictorReturnAddressStackSize;
    }

    public int getTlbSize() {
        return tlbSize;
    }

    public void setTlbSize(int tlbSize) {
        this.tlbSize = tlbSize;
    }

    public int getTlbAssociativity() {
        return tlbAssociativity;
    }

    public void setTlbAssociativity(int tlbAssociativity) {
        this.tlbAssociativity = tlbAssociativity;
    }

    public int getTlbLineSize() {
        return tlbLineSize;
    }

    public void setTlbLineSize(int tlbLineSize) {
        this.tlbLineSize = tlbLineSize;
    }

    public int getTlbHitLatency() {
        return tlbHitLatency;
    }

    public void setTlbHitLatency(int tlbHitLatency) {
        this.tlbHitLatency = tlbHitLatency;
    }

    public int getTlbMissLatency() {
        return tlbMissLatency;
    }

    public void setTlbMissLatency(int tlbMissLatency) {
        this.tlbMissLatency = tlbMissLatency;
    }

    public int getL1ISize() {
        return l1ISize;
    }

    public void setL1ISize(int l1ISize) {
        this.l1ISize = l1ISize;
    }

    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    public int getL1ILineSize() {
        return l1ILineSize;
    }

    public void setL1ILineSize(int l1ILineSize) {
        this.l1ILineSize = l1ILineSize;
    }

    public int getL1IHitLatency() {
        return l1IHitLatency;
    }

    public void setL1IHitLatency(int l1IHitLatency) {
        this.l1IHitLatency = l1IHitLatency;
    }

    public int getL1INumReadPorts() {
        return l1INumReadPorts;
    }

    public void setL1INumReadPorts(int l1INumReadPorts) {
        this.l1INumReadPorts = l1INumReadPorts;
    }

    public int getL1INumWritePorts() {
        return l1INumWritePorts;
    }

    public void setL1INumWritePorts(int l1INumWritePorts) {
        this.l1INumWritePorts = l1INumWritePorts;
    }

    public CacheReplacementPolicyType getL1IReplacementPolicyType() {
        return l1IReplacementPolicyType;
    }

    public void setL1IReplacementPolicyType(CacheReplacementPolicyType l1IReplacementPolicyType) {
        this.l1IReplacementPolicyType = l1IReplacementPolicyType;
    }

    public int getL1DSize() {
        return l1DSize;
    }

    public void setL1DSize(int l1DSize) {
        this.l1DSize = l1DSize;
    }

    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    public int getL1DLineSize() {
        return l1DLineSize;
    }

    public void setL1DLineSize(int l1DLineSize) {
        this.l1DLineSize = l1DLineSize;
    }

    public int getL1DHitLatency() {
        return l1DHitLatency;
    }

    public void setL1DHitLatency(int l1DHitLatency) {
        this.l1DHitLatency = l1DHitLatency;
    }

    public int getL1DNumReadPorts() {
        return l1DNumReadPorts;
    }

    public void setL1DNumReadPorts(int l1DNumReadPorts) {
        this.l1DNumReadPorts = l1DNumReadPorts;
    }

    public int getL1DNumWritePorts() {
        return l1DNumWritePorts;
    }

    public void setL1DNumWritePorts(int l1DNumWritePorts) {
        this.l1DNumWritePorts = l1DNumWritePorts;
    }

    public CacheReplacementPolicyType getL1DReplacementPolicyType() {
        return l1DReplacementPolicyType;
    }

    public void setL1DReplacementPolicyType(CacheReplacementPolicyType l1DReplacementPolicyType) {
        this.l1DReplacementPolicyType = l1DReplacementPolicyType;
    }

    public int getL2Size() {
        return l2Size;
    }

    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    public int getL2LineSize() {
        return l2LineSize;
    }

    public void setL2LineSize(int l2LineSize) {
        this.l2LineSize = l2LineSize;
    }

    public int getL2HitLatency() {
        return l2HitLatency;
    }

    public void setL2HitLatency(int l2HitLatency) {
        this.l2HitLatency = l2HitLatency;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public void setL2ReplacementPolicyType(CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public MemoryControllerType getMemoryControllerType() {
        return memoryControllerType;
    }

    public void setMemoryControllerType(MemoryControllerType memoryControllerType) {
        this.memoryControllerType = memoryControllerType;
    }

    public int getMemoryControllerLineSize() {
        return memoryControllerLineSize;
    }

    public void setMemoryControllerLineSize(int memoryControllerLineSize) {
        this.memoryControllerLineSize = memoryControllerLineSize;
    }

    public int getFixedLatencyMemoryControllerLatency() {
        return fixedLatencyMemoryControllerLatency;
    }

    public void setFixedLatencyMemoryControllerLatency(int fixedLatencyMemoryControllerLatency) {
        this.fixedLatencyMemoryControllerLatency = fixedLatencyMemoryControllerLatency;
    }

    public int getSimpleMemoryControllerMemoryLatency() {
        return simpleMemoryControllerMemoryLatency;
    }

    public void setSimpleMemoryControllerMemoryLatency(int simpleMemoryControllerMemoryLatency) {
        this.simpleMemoryControllerMemoryLatency = simpleMemoryControllerMemoryLatency;
    }

    public int getSimpleMemoryControllerMemoryTrunkLatency() {
        return simpleMemoryControllerMemoryTrunkLatency;
    }

    public void setSimpleMemoryControllerMemoryTrunkLatency(int simpleMemoryControllerMemoryTrunkLatency) {
        this.simpleMemoryControllerMemoryTrunkLatency = simpleMemoryControllerMemoryTrunkLatency;
    }

    public int getSimpleMemoryControllerBusWidth() {
        return simpleMemoryControllerBusWidth;
    }

    public void setSimpleMemoryControllerBusWidth(int simpleMemoryControllerBusWidth) {
        this.simpleMemoryControllerBusWidth = simpleMemoryControllerBusWidth;
    }

    public int getBasicMemoryControllerToDramLatency() {
        return basicMemoryControllerToDramLatency;
    }

    public void setBasicMemoryControllerToDramLatency(int basicMemoryControllerToDramLatency) {
        this.basicMemoryControllerToDramLatency = basicMemoryControllerToDramLatency;
    }

    public int getBasicMemoryControllerFromDramLatency() {
        return basicMemoryControllerFromDramLatency;
    }

    public void setBasicMemoryControllerFromDramLatency(int basicMemoryControllerFromDramLatency) {
        this.basicMemoryControllerFromDramLatency = basicMemoryControllerFromDramLatency;
    }

    public int getBasicMemoryControllerPrechargeLatency() {
        return basicMemoryControllerPrechargeLatency;
    }

    public void setBasicMemoryControllerPrechargeLatency(int basicMemoryControllerPrechargeLatency) {
        this.basicMemoryControllerPrechargeLatency = basicMemoryControllerPrechargeLatency;
    }

    public int getBasicMemoryControllerClosedLatency() {
        return basicMemoryControllerClosedLatency;
    }

    public void setBasicMemoryControllerClosedLatency(int basicMemoryControllerClosedLatency) {
        this.basicMemoryControllerClosedLatency = basicMemoryControllerClosedLatency;
    }

    public int getBasicMemoryControllerConflictLatency() {
        return basicMemoryControllerConflictLatency;
    }

    public void setBasicMemoryControllerConflictLatency(int basicMemoryControllerConflictLatency) {
        this.basicMemoryControllerConflictLatency = basicMemoryControllerConflictLatency;
    }

    public int getBasicMemoryControllerBusWidth() {
        return basicMemoryControllerBusWidth;
    }

    public void setBasicMemoryControllerBusWidth(int basicMemoryControllerBusWidth) {
        this.basicMemoryControllerBusWidth = basicMemoryControllerBusWidth;
    }

    public int getBasicMemoryControllerNumBanks() {
        return basicMemoryControllerNumBanks;
    }

    public void setBasicMemoryControllerNumBanks(int basicMemoryControllerNumBanks) {
        this.basicMemoryControllerNumBanks = basicMemoryControllerNumBanks;
    }

    public int getBasicMemoryControllerRowSize() {
        return basicMemoryControllerRowSize;
    }

    public void setBasicMemoryControllerRowSize(int basicMemoryControllerRowSize) {
        this.basicMemoryControllerRowSize = basicMemoryControllerRowSize;
    }
}
