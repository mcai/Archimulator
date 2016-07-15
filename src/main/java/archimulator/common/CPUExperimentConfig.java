package archimulator.common;

import archimulator.core.bpred.BranchPredictorType;
import archimulator.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.uncore.dram.MemoryControllerType;
import archimulator.util.StorageUnitHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * CPU experiment config.
 *
 * @author Min Cai
 */
public class CPUExperimentConfig implements NoCConfig {
    private ExperimentType type;

    private String outputDirectory;

    private List<ContextMapping> contextMappings;

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

    private int randSeed;

    private String routing;

    private String selection;

    private int maxInjectionBufferSize;

    private int maxInputBufferSize;

    private int numVirtualChannels;

    private int linkWidth;
    private int linkDelay;

    private int antPacketSize;
    private double antPacketInjectionRate;

    private double acoSelectionAlpha;
    private double reinforcementFactor;

    /**
     * Create a CPU experiment config.
     */
    public CPUExperimentConfig() {
        this.type = ExperimentType.DETAILED;

        this.outputDirectory = "";

        this.contextMappings = new ArrayList<>();

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

        this.randSeed = 13;
        this.routing = "oddEven";
        this.selection = "aco";
        this.maxInjectionBufferSize = 32;
        this.maxInputBufferSize = 4;
        this.numVirtualChannels = 4;
        this.linkWidth = 4;
        this.linkDelay = 1;
        this.antPacketSize = 4;
        this.antPacketInjectionRate = 0.01;
        this.acoSelectionAlpha = 0.5;
        this.reinforcementFactor = 0.05;
    }

    /**
     * Get the experiment type.
     *
     * @return the experiment type
     */
    public ExperimentType getType() {
        return type;
    }

    /**
     * Set the experiment type.
     *
     * @param type the experiment type
     */
    public void setType(ExperimentType type) {
        this.type = type;
    }

    /**
     * Get the output directory.
     *
     * @return the output directory
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Set the output directory.
     *
     * @param outputDirectory the output directory
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Get the list of context mappings.
     *
     * @return the list of context mappings
     */
    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    /**
     * Get the maximum number of dynamic instructions to be simulated.
     *
     * @return the maximum number of dynamic instructions to be simulated
     */
    public long getNumMaxInstructions() {
        return numMaxInstructions;
    }

    /**
     * Set the maximum number of dynamic instructions to be simulated.
     *
     * @param numMaxInstructions the maximum number of dynamic instructions to be simulated
     */
    public void setNumMaxInstructions(long numMaxInstructions) {
        this.numMaxInstructions = numMaxInstructions;
    }

    /**
     * Get the helper thread pthread spawning pseudo call index.
     *
     * @return the helper thread pthread spawning pseudo call index
     */
    public int getHelperThreadPthreadSpawnIndex() {
        return helperThreadPthreadSpawnIndex;
    }

    /**
     * Set the helper  thread pthread spawning pseudo call index.
     *
     * @param helperThreadPthreadSpawnIndex the helper thread pthread spawning pseudo call index
     */
    public void setHelperThreadPthreadSpawnIndex(int helperThreadPthreadSpawnIndex) {
        this.helperThreadPthreadSpawnIndex = helperThreadPthreadSpawnIndex;
    }

    /**
     * Get a boolean value indicating whether dynamic speculative precomputation is enabled or not.
     *
     * @return a boolean value indicating whether dynamic speculative precomputation is enabled or not
     */
    public boolean isDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Set a boolean value indicating whether dynamic speculative precomputation is enabled or not.
     *
     * @param dynamicSpeculativePrecomputationEnabled a boolean value indicating whether dynamic speculative precomputation is enabled or not
     */
    public void setDynamicSpeculativePrecomputationEnabled(boolean dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Get the number of main thread ways used in the static partitioned LRU policy.
     *
     * @return the number of main thread ways used in the static partitioned LRU policy
     */
    public int getNumMainThreadWaysInStaticPartitionedLRUPolicy() {
        return numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    /**
     * Set the number of main thread ways used in the static partitioned LRU policy.
     *
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy the number of main thread ways used in the static partitioned LRU policy
     */
    public void setNumMainThreadWaysInStaticPartitionedLRUPolicy(int numMainThreadWaysInStaticPartitionedLRUPolicy) {
        this.numMainThreadWaysInStaticPartitionedLRUPolicy = numMainThreadWaysInStaticPartitionedLRUPolicy;
    }

    /**
     * Get the number of cores.
     *
     * @return the number of cores
     */
    public int getNumCores() {
        return numCores;
    }

    /**
     * Set the number of cores.
     *
     * @param numCores the number of cores
     */
    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    /**
     * Get the number of threads per core.
     *
     * @return the number of threads per core
     */
    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    /**
     * Set the number of threads per core.
     *
     * @param numThreadsPerCore the number of threads per core
     */
    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    /**
     * Get the physical register file capacity.
     *
     * @return the physical register file capacity
     */
    public int getPhysicalRegisterFileCapacity() {
        return physicalRegisterFileCapacity;
    }

    /**
     * Set the physical register file capacity.
     *
     * @param physicalRegisterFileCapacity the physical register file capacity
     */
    public void setPhysicalRegisterFileCapacity(int physicalRegisterFileCapacity) {
        this.physicalRegisterFileCapacity = physicalRegisterFileCapacity;
    }

    /**
     * Get the decode width.
     *
     * @return the decode width
     */
    public int getDecodeWidth() {
        return decodeWidth;
    }

    /**
     * Set the decode width.
     *
     * @param decodeWidth the decode width
     */
    public void setDecodeWidth(int decodeWidth) {
        this.decodeWidth = decodeWidth;
    }

    /**
     * Get the issue width.
     *
     * @return the issue width
     */
    public int getIssueWidth() {
        return issueWidth;
    }

    /**
     * Set the issue width.
     *
     * @param issueWidth the issue width
     */
    public void setIssueWidth(int issueWidth) {
        this.issueWidth = issueWidth;
    }

    /**
     * Get the commit width.
     *
     * @return the commit width
     */
    public int getCommitWidth() {
        return commitWidth;
    }

    /**
     * Set the commit width.
     *
     * @param commitWidth the commit width
     */
    public void setCommitWidth(int commitWidth) {
        this.commitWidth = commitWidth;
    }

    /**
     * Get the decode buffer capacity.
     *
     * @return the decode buffer capacity
     */
    public int getDecodeBufferCapacity() {
        return decodeBufferCapacity;
    }

    /**
     * Set the decode buffer capacity.
     *
     * @param decodeBufferCapacity the decode buffer capacity
     */
    public void setDecodeBufferCapacity(int decodeBufferCapacity) {
        this.decodeBufferCapacity = decodeBufferCapacity;
    }

    /**
     * Get the reorder buffer capacity.
     *
     * @return the reorder buffer capacity
     */
    public int getReorderBufferCapacity() {
        return reorderBufferCapacity;
    }

    /**
     * Set the reorder buffer capacity.
     *
     * @param reorderBufferCapacity the reorder buffer capacity
     */
    public void setReorderBufferCapacity(int reorderBufferCapacity) {
        this.reorderBufferCapacity = reorderBufferCapacity;
    }

    /**
     * Get the load store queue capacity.
     *
     * @return the load store queue capacity
     */
    public int getLoadStoreQueueCapacity() {
        return loadStoreQueueCapacity;
    }

    /**
     * Set the load store queue capacity.
     *
     * @param loadStoreQueueCapacity the load store queue capacity
     */
    public void setLoadStoreQueueCapacity(int loadStoreQueueCapacity) {
        this.loadStoreQueueCapacity = loadStoreQueueCapacity;
    }

    /**
     * Get the branch predictor type.
     *
     * @return the branch predictor type
     */
    public BranchPredictorType getBranchPredictorType() {
        return branchPredictorType;
    }

    /**
     * Set the branch predictor type.
     *
     * @param branchPredictorType the branch predictor type
     */
    public void setBranchPredictorType(BranchPredictorType branchPredictorType) {
        this.branchPredictorType = branchPredictorType;
    }

    /**
     * Get the bimod size used in the two bit branch predictor.
     *
     * @return the bimod size used in the two bit branch predictor
     */
    public int getTwoBitBranchPredictorBimodSize() {
        return twoBitBranchPredictorBimodSize;
    }

    /**
     * Set the bimod size in the two bit branch predictor.
     *
     * @param twoBitBranchPredictorBimodSize the bimod size in the two bit branch predictor
     */
    public void setTwoBitBranchPredictorBimodSize(int twoBitBranchPredictorBimodSize) {
        this.twoBitBranchPredictorBimodSize = twoBitBranchPredictorBimodSize;
    }

    /**
     * Get the number of sets in the branch target buffer used in the two bit branch predictor.
     *
     * @return the number of sets in the branch target buffer used in the two bit branch predictor
     */
    public int getTwoBitBranchPredictorBranchTargetBufferNumSets() {
        return twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the number of sets in the branch target buffer used in the two bit branch predictor.
     *
     * @param twoBitBranchPredictorBranchTargetBufferNumSets the number of sets in the branch target buffer used in the two bit branch predictor
     */
    public void setTwoBitBranchPredictorBranchTargetBufferNumSets(int twoBitBranchPredictorBranchTargetBufferNumSets) {
        this.twoBitBranchPredictorBranchTargetBufferNumSets = twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Get the associativity of the branch target buffer used in the two bit branch predictor.
     *
     * @return the associativity of the branch target buffer used in the two bit branch predictor
     */
    public int getTwoBitBranchPredictorBranchTargetBufferAssociativity() {
        return twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the associativity of the branch target buffer used in the two bit branch predictor.
     *
     * @param twoBitBranchPredictorBranchTargetBufferAssociativity the associativity of the branch target buffer used in the two bit branch predictor
     */
    public void setTwoBitBranchPredictorBranchTargetBufferAssociativity(int twoBitBranchPredictorBranchTargetBufferAssociativity) {
        this.twoBitBranchPredictorBranchTargetBufferAssociativity = twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Get the size of the return address stack used in the two bit branch predictor.
     *
     * @return the size of the return address stack used in the two bit branch predictor
     */
    public int getTwoBitBranchPredictorReturnAddressStackSize() {
        return twoBitBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the size of the return address stack used in the two bit branch predictor.
     *
     * @param twoBitBranchPredictorReturnAddressStackSize the size of the return address stack used in the two bit branch predictor
     */
    public void setTwoBitBranchPredictorReturnAddressStackSize(int twoBitBranchPredictorReturnAddressStackSize) {
        this.twoBitBranchPredictorReturnAddressStackSize = twoBitBranchPredictorReturnAddressStackSize;
    }

    /**
     * Get the size of the L1 cache used in the two level branch predictor.
     *
     * @return the size of the L1 cache used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorL1Size() {
        return twoLevelBranchPredictorL1Size;
    }

    /**
     * Set the size of the L1 cache used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorL1Size the size of the L1 cache used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorL1Size(int twoLevelBranchPredictorL1Size) {
        this.twoLevelBranchPredictorL1Size = twoLevelBranchPredictorL1Size;
    }

    /**
     * Get the size of the L2 cache used in the two level branch predictor.
     *
     * @return the size of the L2 cache used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorL2Size() {
        return twoLevelBranchPredictorL2Size;
    }

    /**
     * Set the size of the L2 cache used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorL2Size the size of the L2 cache used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorL2Size(int twoLevelBranchPredictorL2Size) {
        this.twoLevelBranchPredictorL2Size = twoLevelBranchPredictorL2Size;
    }

    /**
     * Get the shift width used in the two level branch predictor.
     *
     * @return the shift width used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorShiftWidth() {
        return twoLevelBranchPredictorShiftWidth;
    }

    /**
     * Set the shift width used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorShiftWidth the shift width used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorShiftWidth(int twoLevelBranchPredictorShiftWidth) {
        this.twoLevelBranchPredictorShiftWidth = twoLevelBranchPredictorShiftWidth;
    }

    /**
     * Get the xor used in the two level branch predictor.
     *
     * @return the xor used in the two level branch predictor
     */
    public boolean isTwoLevelBranchPredictorXor() {
        return twoLevelBranchPredictorXor;
    }

    /**
     * Set the xor used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorXor the xor used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorXor(boolean twoLevelBranchPredictorXor) {
        this.twoLevelBranchPredictorXor = twoLevelBranchPredictorXor;
    }

    /**
     * Get the number of sets in the branch target buffer used in the two level branch predictor.
     *
     * @return the number of sets in the branch target buffer used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorBranchTargetBufferNumSets() {
        return twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the number of sets in the branch target buffer used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorBranchTargetBufferNumSets the number of sets in the branch target buffer used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorBranchTargetBufferNumSets(int twoLevelBranchPredictorBranchTargetBufferNumSets) {
        this.twoLevelBranchPredictorBranchTargetBufferNumSets = twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Get the associativity of the branch target buffer used in the two level branch predictor.
     *
     * @return the associativity of the branch target buffer used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorBranchTargetBufferAssociativity() {
        return twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the associativity of the branch target buffer used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorBranchTargetBufferAssociativity the associativity of the branch target buffer used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorBranchTargetBufferAssociativity(int twoLevelBranchPredictorBranchTargetBufferAssociativity) {
        this.twoLevelBranchPredictorBranchTargetBufferAssociativity = twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Get the size of the return address stack used in the two level branch predictor.
     *
     * @return the size of the return address stack used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorReturnAddressStackSize() {
        return twoLevelBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the size of the return address stack used in the two level branch predictor.
     *
     * @param twoLevelBranchPredictorReturnAddressStackSize the size of the return address stack used in the two level branch predictor
     */
    public void setTwoLevelBranchPredictorReturnAddressStackSize(int twoLevelBranchPredictorReturnAddressStackSize) {
        this.twoLevelBranchPredictorReturnAddressStackSize = twoLevelBranchPredictorReturnAddressStackSize;
    }

    /**
     * Get the bimod size used in the combined branch predictor.
     *
     * @return the bimod size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorBimodSize() {
        return combinedBranchPredictorBimodSize;
    }

    /**
     * Set the bimod size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBimodSize the bimod size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBimodSize(int combinedBranchPredictorBimodSize) {
        this.combinedBranchPredictorBimodSize = combinedBranchPredictorBimodSize;
    }

    /**
     * Get the L1 cache size used in the combined branch predictor.
     *
     * @return the L1 cache size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorL1Size() {
        return combinedBranchPredictorL1Size;
    }

    /**
     * Set the L1 cache size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorL1Size the L1 cache size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorL1Size(int combinedBranchPredictorL1Size) {
        this.combinedBranchPredictorL1Size = combinedBranchPredictorL1Size;
    }

    /**
     * Get the L2 cache size used in the combined branch predictor.
     *
     * @return the L2 cache size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorL2Size() {
        return combinedBranchPredictorL2Size;
    }

    /**
     * Set the L2 cache size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorL2Size the L2 cache size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorL2Size(int combinedBranchPredictorL2Size) {
        this.combinedBranchPredictorL2Size = combinedBranchPredictorL2Size;
    }

    /**
     * Get the meta size used in the combined branch predictor.
     *
     * @return the meta size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorMetaSize() {
        return combinedBranchPredictorMetaSize;
    }

    /**
     * Set the meta size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorMetaSize the meta size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorMetaSize(int combinedBranchPredictorMetaSize) {
        this.combinedBranchPredictorMetaSize = combinedBranchPredictorMetaSize;
    }

    /**
     * Get the shift width used in the combined branch predictor.
     *
     * @return the shift width used in the combined branch predictor
     */
    public int getCombinedBranchPredictorShiftWidth() {
        return combinedBranchPredictorShiftWidth;
    }

    /**
     * Set the shift width used in the combined branch predictor.
     *
     * @param combinedBranchPredictorShiftWidth the shift width used in the combined branch predictor
     */
    public void setCombinedBranchPredictorShiftWidth(int combinedBranchPredictorShiftWidth) {
        this.combinedBranchPredictorShiftWidth = combinedBranchPredictorShiftWidth;
    }

    /**
     * Get the xor used in the combined branch predictor.
     *
     * @return the xor used in the combined branch predictor
     */
    public boolean isCombinedBranchPredictorXor() {
        return combinedBranchPredictorXor;
    }

    /**
     * Set the xor used in the combined branch predictor.
     *
     * @param combinedBranchPredictorXor the xor used in the combined branch predictor
     */
    public void setCombinedBranchPredictorXor(boolean combinedBranchPredictorXor) {
        this.combinedBranchPredictorXor = combinedBranchPredictorXor;
    }

    /**
     * Get the number of sets in the branch target buffer used in the combined branch predictor.
     *
     * @return the number of sets in the branch target buffer used in the combined branch predictor
     */
    public int getCombinedBranchPredictorBranchTargetBufferNumSets() {
        return combinedBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the number of sets in the branch target buffer used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBranchTargetBufferNumSets the number of sets in the branch target buffer used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBranchTargetBufferNumSets(int combinedBranchPredictorBranchTargetBufferNumSets) {
        this.combinedBranchPredictorBranchTargetBufferNumSets = combinedBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Get the associativity of the branch target buffer used in the combined branch predictor.
     *
     * @return the associativity of the branch target buffer used in the combined branch predictor
     */
    public int getCombinedBranchPredictorBranchTargetBufferAssociativity() {
        return combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the associativity of the branch target buffer used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBranchTargetBufferAssociativity the associativity of the branch target buffer used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBranchTargetBufferAssociativity(int combinedBranchPredictorBranchTargetBufferAssociativity) {
        this.combinedBranchPredictorBranchTargetBufferAssociativity = combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Get the size of the return address stack used in the combined branch predictor.
     *
     * @return the size of the return address stack used in the combined branch predictor
     */
    public int getCombinedBranchPredictorReturnAddressStackSize() {
        return combinedBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the size of the return address stack used in the combined branch predictor.
     *
     * @param combinedBranchPredictorReturnAddressStackSize the size of the return address stack used in the combined branch predictor
     */
    public void setCombinedBranchPredictorReturnAddressStackSize(int combinedBranchPredictorReturnAddressStackSize) {
        this.combinedBranchPredictorReturnAddressStackSize = combinedBranchPredictorReturnAddressStackSize;
    }

    /**
     * Get the size of the translation lookaside buffer (TLB).
     *
     * @return the size of the translation lookaside buffer (TLB)
     */
    public int getTlbSize() {
        return tlbSize;
    }

    /**
     * Set the size of the translation lookaside buffer (TLB).
     *
     * @param tlbSize the size of the translation lookaside buffer (TLB)
     */
    public void setTlbSize(int tlbSize) {
        this.tlbSize = tlbSize;
    }

    /**
     * Get the associativity of the translation lookaside buffer (TLB).
     *
     * @return the associativity of the translation lookaside buffer (TLB)
     */
    public int getTlbAssociativity() {
        return tlbAssociativity;
    }

    /**
     * Set the associativity of the translation lookaside buffer (TLB).
     *
     * @param tlbAssociativity the associativity of the translation lookaside buffer (TLB)
     */
    public void setTlbAssociativity(int tlbAssociativity) {
        this.tlbAssociativity = tlbAssociativity;
    }

    /**
     * Get the line size of the translation lookaside buffer (TLB).
     *
     * @return the line size of the translation lookaside buffer (TLB)
     */
    public int getTlbLineSize() {
        return tlbLineSize;
    }

    /**
     * Set the line size of translation lookaside buffer (TLB).
     *
     * @param tlbLineSize the line size of the translation lookaside buffer (TLB)
     */
    public void setTlbLineSize(int tlbLineSize) {
        this.tlbLineSize = tlbLineSize;
    }

    /**
     * Get the hit latency of the translation lookaside buffer (TLB).
     *
     * @return the hit latency of the translation lookaside buffer (TLB)
     */
    public int getTlbHitLatency() {
        return tlbHitLatency;
    }

    /**
     * Set the hit latency of the translation lookaside buffer (TLB).
     *
     * @param tlbHitLatency the hit latency of the translation lookaside buffer (TLB)
     */
    public void setTlbHitLatency(int tlbHitLatency) {
        this.tlbHitLatency = tlbHitLatency;
    }

    /**
     * Get the miss latency of the translation lookaside buffer (TLB).
     *
     * @return the miss latency of the translation lookaside buffer (TLB)
     */
    public int getTlbMissLatency() {
        return tlbMissLatency;
    }

    /**
     * Set the miss latency of the translation lookaside buffer (TLB).
     *
     * @param tlbMissLatency the miss latency of the translation lookaside buffer (TLB)
     */
    public void setTlbMissLatency(int tlbMissLatency) {
        this.tlbMissLatency = tlbMissLatency;
    }

    /**
     * Get the size of the L1 cache.
     *
     * @return the size of the L1 cache
     */
    public int getL1ISize() {
        return l1ISize;
    }

    /**
     * Set the size of the L1 cache.
     *
     * @param l1ISize the size of the L1 cache
     */
    public void setL1ISize(int l1ISize) {
        this.l1ISize = l1ISize;
    }

    /**
     * Get the associativity of the L1 cache.
     *
     * @return the associativity of the L1 cache
     */
    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    /**
     * Set the associativity of the L1 cache.
     *
     * @param l1IAssociativity the associativity of the L1 cache
     */
    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    /**
     * Get the line size of the L1 cache.
     *
     * @return the line size of the L1 cache
     */
    public int getL1ILineSize() {
        return l1ILineSize;
    }

    /**
     * Set the line size of the L1 cache.
     *
     * @param l1ILineSize the line size of the L1 cache
     */
    public void setL1ILineSize(int l1ILineSize) {
        this.l1ILineSize = l1ILineSize;
    }

    /**
     * Get the hit latency of the L1 cache.
     *
     * @return the hit latency of the L1 cache
     */
    public int getL1IHitLatency() {
        return l1IHitLatency;
    }

    /**
     * Set the hit latency of the L1 cache.
     *
     * @param l1IHitLatency the hit latency of the L1 cache
     */
    public void setL1IHitLatency(int l1IHitLatency) {
        this.l1IHitLatency = l1IHitLatency;
    }

    /**
     * Get the number of read ports in the L1 instruction cache.
     *
     * @return the number of read ports in the L1 instruction cache
     */
    public int getL1INumReadPorts() {
        return l1INumReadPorts;
    }

    /**
     * Set the number of read ports in the L1 instruction cache.
     *
     * @param l1INumReadPorts the number of read ports in the L1 instruction cache
     */
    public void setL1INumReadPorts(int l1INumReadPorts) {
        this.l1INumReadPorts = l1INumReadPorts;
    }

    /**
     * Get the number of write ports in the L1 instruction cache.
     *
     * @return the number of write ports in the L1 instruction cache
     */
    public int getL1INumWritePorts() {
        return l1INumWritePorts;
    }

    /**
     * Set the number of write ports in the L1 instruction cache.
     *
     * @param l1INumWritePorts the number of write ports in the L1 instruction cache
     */
    public void setL1INumWritePorts(int l1INumWritePorts) {
        this.l1INumWritePorts = l1INumWritePorts;
    }

    /**
     * Get the replacement policy type of the L1 instruction cache.
     *
     * @return the replacement policy type of the L1 instruction cache
     */
    public CacheReplacementPolicyType getL1IReplacementPolicyType() {
        return l1IReplacementPolicyType;
    }

    /**
     * Set the replacement policy type of the L1 instruction cache.
     *
     * @param l1IReplacementPolicyType the replacement policy type of the L1 instruction cache
     */
    public void setL1IReplacementPolicyType(CacheReplacementPolicyType l1IReplacementPolicyType) {
        this.l1IReplacementPolicyType = l1IReplacementPolicyType;
    }

    /**
     * Get the size of the L1 data cache.
     *
     * @return the size of the L1 data cache
     */
    public int getL1DSize() {
        return l1DSize;
    }

    /**
     * Set the size of the L1 data cache.
     *
     * @param l1DSize the size of the L1 data cache
     */
    public void setL1DSize(int l1DSize) {
        this.l1DSize = l1DSize;
    }

    /**
     * Get the associativity of the L1 data cache.
     *
     * @return the associativity of the L1 data cache
     */
    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    /**
     * Set the associativity of the L1 data cache.
     *
     * @param l1DAssociativity the associativity of the L1 data cache
     */
    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    /**
     * Get the line size of the L1 data cache.
     *
     * @return the line size of the L1 data cache
     */
    public int getL1DLineSize() {
        return l1DLineSize;
    }

    /**
     * Set the line size of the L1 data cache.
     *
     * @param l1DLineSize the line size of the L1 data cache
     */
    public void setL1DLineSize(int l1DLineSize) {
        this.l1DLineSize = l1DLineSize;
    }

    /**
     * Get the hit latency of the L1 data cache.
     *
     * @return the hit latency of the L1 data cache
     */
    public int getL1DHitLatency() {
        return l1DHitLatency;
    }

    /**
     * Set the hit latency of the L1 data cache.
     *
     * @param l1DHitLatency the hit latency of the L1 data cache
     */
    public void setL1DHitLatency(int l1DHitLatency) {
        this.l1DHitLatency = l1DHitLatency;
    }

    /**
     * Get the number of read ports in the L1 data cache.
     *
     * @return the number of read ports in the L1 data cache
     */
    public int getL1DNumReadPorts() {
        return l1DNumReadPorts;
    }

    /**
     * Set the number of read ports in the L1 data cache.
     *
     * @param l1DNumReadPorts the number of read ports in the L1 data cache
     */
    public void setL1DNumReadPorts(int l1DNumReadPorts) {
        this.l1DNumReadPorts = l1DNumReadPorts;
    }

    /**
     * Get the number of write ports in the L1 data cache.
     *
     * @return the number of write ports in the L1 data cache
     */
    public int getL1DNumWritePorts() {
        return l1DNumWritePorts;
    }

    /**
     * Set the number of write ports in the L1 data cache.
     *
     * @param l1DNumWritePorts the number of write ports in the L1 data cache
     */
    public void setL1DNumWritePorts(int l1DNumWritePorts) {
        this.l1DNumWritePorts = l1DNumWritePorts;
    }

    /**
     * Get the replacement policy type of the L1 data cache.
     *
     * @return the replacement policy type of the L1 data cache
     */
    public CacheReplacementPolicyType getL1DReplacementPolicyType() {
        return l1DReplacementPolicyType;
    }

    /**
     * Set the replacement policy type of the L1 data cache.
     *
     * @param l1DReplacementPolicyType the replacement policy type of the L1 data cache
     */
    public void setL1DReplacementPolicyType(CacheReplacementPolicyType l1DReplacementPolicyType) {
        this.l1DReplacementPolicyType = l1DReplacementPolicyType;
    }

    /**
     * Get the size of the L2 cache.
     *
     * @return the size of the L2 cache
     */
    public int getL2Size() {
        return l2Size;
    }

    /**
     * Set the size of the L2 cache.
     *
     * @param l2Size the size of the L2 cache
     */
    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
    }

    /**
     * Get the associativity of the L2 cache.
     *
     * @return the associativity of the L2 cache
     */
    public int getL2Associativity() {
        return l2Associativity;
    }

    /**
     * Set the associativity of the L2 cache.
     *
     * @param l2Associativity the associativity of the L2 cache
     */
    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    /**
     * Get the line size of the L2 cache.
     *
     * @return the line size of the L2 cache
     */
    public int getL2LineSize() {
        return l2LineSize;
    }

    /**
     * Set the line size of the L2 cache.
     *
     * @param l2LineSize the line size of the L2 cache
     */
    public void setL2LineSize(int l2LineSize) {
        this.l2LineSize = l2LineSize;
    }

    /**
     * Get the hit latency of the L2 cache.
     *
     * @return the hit latency of the L2 cache
     */
    public int getL2HitLatency() {
        return l2HitLatency;
    }

    /**
     * Set the hit latency of the L2 cache.
     *
     * @param l2HitLatency the hit latency of the L2 cache
     */
    public void setL2HitLatency(int l2HitLatency) {
        this.l2HitLatency = l2HitLatency;
    }

    /**
     * Get the replacement policy type of the L2 cache.
     *
     * @return the replacement policy type of the L2 cache
     */
    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    /**
     * Set the replacement policy type of the L2 cache.
     *
     * @param l2ReplacementPolicyType the replacement policy type of the L2 cache
     */
    public void setL2ReplacementPolicyType(CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    /**
     * Get the type of the memory controller.
     *
     * @return the type of the memory controller
     */
    public MemoryControllerType getMemoryControllerType() {
        return memoryControllerType;
    }

    /**
     * Set the type of the memory controller.
     *
     * @param memoryControllerType the type of the memory controller
     */
    public void setMemoryControllerType(MemoryControllerType memoryControllerType) {
        this.memoryControllerType = memoryControllerType;
    }

    /**
     * Get the line size of the memory controller.
     *
     * @return the line size of the memory controller
     */
    public int getMemoryControllerLineSize() {
        return memoryControllerLineSize;
    }

    /**
     * Set the line size of the memory controller.
     *
     * @param memoryControllerLineSize the line size of the memory controller
     */
    public void setMemoryControllerLineSize(int memoryControllerLineSize) {
        this.memoryControllerLineSize = memoryControllerLineSize;
    }

    /**
     * Get the latency of the fixed latency memory controller.
     *
     * @return the latency of the fixed latency memory controller
     */
    public int getFixedLatencyMemoryControllerLatency() {
        return fixedLatencyMemoryControllerLatency;
    }

    /**
     * Set the latency of the fixed latency memory controller.
     *
     * @param fixedLatencyMemoryControllerLatency the latency of the fixed latency memory controller
     */
    public void setFixedLatencyMemoryControllerLatency(int fixedLatencyMemoryControllerLatency) {
        this.fixedLatencyMemoryControllerLatency = fixedLatencyMemoryControllerLatency;
    }

    /**
     * Get the memory latency of the simple memory controller.
     *
     * @return the memory latency of the simple memory controller
     */
    public int getSimpleMemoryControllerMemoryLatency() {
        return simpleMemoryControllerMemoryLatency;
    }

    /**
     * Set the memory latency of the simple memory controller.
     *
     * @param simpleMemoryControllerMemoryLatency the memory latency of the simple memory controller
     */
    public void setSimpleMemoryControllerMemoryLatency(int simpleMemoryControllerMemoryLatency) {
        this.simpleMemoryControllerMemoryLatency = simpleMemoryControllerMemoryLatency;
    }

    /**
     * Get the memory trunk latency of the simple memory controller.
     *
     * @return the memory trunk latency of the simple memory  controller
     */
    public int getSimpleMemoryControllerMemoryTrunkLatency() {
        return simpleMemoryControllerMemoryTrunkLatency;
    }

    /**
     * Set the memory trunk latency of the simple memory controller.
     *
     * @param simpleMemoryControllerMemoryTrunkLatency the memory trunk latency of the simple memory controller
     */
    public void setSimpleMemoryControllerMemoryTrunkLatency(int simpleMemoryControllerMemoryTrunkLatency) {
        this.simpleMemoryControllerMemoryTrunkLatency = simpleMemoryControllerMemoryTrunkLatency;
    }

    /**
     * Get the bus width of the simple memory controller.
     *
     * @return the bus width of the simple memory controller
     */
    public int getSimpleMemoryControllerBusWidth() {
        return simpleMemoryControllerBusWidth;
    }

    /**
     * Set the bus width of the simple memory controller.
     *
     * @param simpleMemoryControllerBusWidth the bus width of the simple memory controller
     */
    public void setSimpleMemoryControllerBusWidth(int simpleMemoryControllerBusWidth) {
        this.simpleMemoryControllerBusWidth = simpleMemoryControllerBusWidth;
    }

    /**
     * Get the "to dram" latency of the basic memory controller.
     *
     * @return the "to dram" latency of the basic memory controller
     */
    public int getBasicMemoryControllerToDramLatency() {
        return basicMemoryControllerToDramLatency;
    }

    /**
     * Set the "to dram" latency of the basic memory controller.
     *
     * @param basicMemoryControllerToDramLatency the "to dram" latency of the basic memory controller
     */
    public void setBasicMemoryControllerToDramLatency(int basicMemoryControllerToDramLatency) {
        this.basicMemoryControllerToDramLatency = basicMemoryControllerToDramLatency;
    }

    /**
     * Get the "from dram" latency of the basic memory controller.
     *
     * @return the "from dram" latency of the basic memory controller
     */
    public int getBasicMemoryControllerFromDramLatency() {
        return basicMemoryControllerFromDramLatency;
    }

    /**
     * Set the "from dram" latency of the basic memory controller.
     *
     * @param basicMemoryControllerFromDramLatency the "from dram" latency of the basic memory controller
     */
    public void setBasicMemoryControllerFromDramLatency(int basicMemoryControllerFromDramLatency) {
        this.basicMemoryControllerFromDramLatency = basicMemoryControllerFromDramLatency;
    }

    /**
     * Get the precharge latency of the basic memory controller.
     *
     * @return the precharge latency of the basic memory controller
     */
    public int getBasicMemoryControllerPrechargeLatency() {
        return basicMemoryControllerPrechargeLatency;
    }

    /**
     * Set the precharge latency of the basic memory controller.
     *
     * @param basicMemoryControllerPrechargeLatency the precharge latency of the basic memory controller
     */
    public void setBasicMemoryControllerPrechargeLatency(int basicMemoryControllerPrechargeLatency) {
        this.basicMemoryControllerPrechargeLatency = basicMemoryControllerPrechargeLatency;
    }

    /**
     * Get the closed latency of the basic memory controller.
     *
     * @return the closed latency of the basic memory controller
     */
    public int getBasicMemoryControllerClosedLatency() {
        return basicMemoryControllerClosedLatency;
    }

    /**
     * Set the closed latency of the basic memory controller.
     *
     * @param basicMemoryControllerClosedLatency the closed latency of the basic memory controller
     */
    public void setBasicMemoryControllerClosedLatency(int basicMemoryControllerClosedLatency) {
        this.basicMemoryControllerClosedLatency = basicMemoryControllerClosedLatency;
    }

    /**
     * Get the conflict latency of the basic memory controller.
     *
     * @return the conflict latency of the basic memory controller
     */
    public int getBasicMemoryControllerConflictLatency() {
        return basicMemoryControllerConflictLatency;
    }

    /**
     * Set the conflict latency of the basic memory controller.
     *
     * @param basicMemoryControllerConflictLatency the conflict latency of the basic memory controller
     */
    public void setBasicMemoryControllerConflictLatency(int basicMemoryControllerConflictLatency) {
        this.basicMemoryControllerConflictLatency = basicMemoryControllerConflictLatency;
    }

    /**
     * Get the bus width of the basic memory controller.
     *
     * @return the bus width of the basic memory controller
     */
    public int getBasicMemoryControllerBusWidth() {
        return basicMemoryControllerBusWidth;
    }

    /**
     * Set the bus width of the basic memory controller.
     *
     * @param basicMemoryControllerBusWidth the bus width of the basic memory controller
     */
    public void setBasicMemoryControllerBusWidth(int basicMemoryControllerBusWidth) {
        this.basicMemoryControllerBusWidth = basicMemoryControllerBusWidth;
    }

    /**
     * Get the number of banks in the basic memory controller.
     *
     * @return the number of banks in the basic memory controller
     */
    public int getBasicMemoryControllerNumBanks() {
        return basicMemoryControllerNumBanks;
    }

    /**
     * Set the number of banks in the basic memory controller.
     *
     * @param basicMemoryControllerNumBanks the number of banks in the basic memory controller
     */
    public void setBasicMemoryControllerNumBanks(int basicMemoryControllerNumBanks) {
        this.basicMemoryControllerNumBanks = basicMemoryControllerNumBanks;
    }

    /**
     * Get the row size of the basic memory controller.
     *
     * @return the row size of the basic memory controller
     */
    public int getBasicMemoryControllerRowSize() {
        return basicMemoryControllerRowSize;
    }

    /**
     * Set the row size of the basic memory controller.
     *
     * @param basicMemoryControllerRowSize the row size of the basic memory controller
     */
    public void setBasicMemoryControllerRowSize(int basicMemoryControllerRowSize) {
        this.basicMemoryControllerRowSize = basicMemoryControllerRowSize;
    }

    /**
     * Get the random seed.
     *
     * @return the random seed
     */
    @Override
    public int getRandSeed() {
        return randSeed;
    }

    /**
     * Set the random seed.
     *
     * @param randSeed the random seed
     */
    public void setRandSeed(int randSeed) {
        this.randSeed = randSeed;
    }

    /**
     * Get the routing algorithm in the NoCs.
     *
     * @return the routing algorithm in the NoCs
     */
    @Override
    public String getRouting() {
        return routing;
    }

    /**
     * Set the routing algorithm in the NoCs.
     *
     * @param routing the routing algorithm in the NoCs
     */
    public void setRouting(String routing) {
        this.routing = routing;
    }

    /**
     * Get the selection policy in the NoCs.
     *
     * @return the selection policy in the NoCs
     */
    @Override
    public String getSelection() {
        return selection;
    }

    /**
     * Set the selection policy in the NoCs.
     *
     * @param selection the selection policy in the NoCs
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }

    /**
     * Get the maximum size of the injection buffer.
     *
     * @return the maximum size of the injection buffer
     */
    @Override
    public int getMaxInjectionBufferSize() {
        return maxInjectionBufferSize;
    }

    /**
     * Set the maximum size of the injection buffer.
     *
     * @param maxInjectionBufferSize the maximum size of the injection buffer
     */
    public void setMaxInjectionBufferSize(int maxInjectionBufferSize) {
        this.maxInjectionBufferSize = maxInjectionBufferSize;
    }

    /**
     * Get the maximum size of the input buffer.
     *
     * @return the maximum size of the input buffer
     */
    @Override
    public int getMaxInputBufferSize() {
        return maxInputBufferSize;
    }

    /**
     * Set the maximum size of the input buffer.
     *
     * @param maxInputBufferSize the maximum size of the input buffer
     */
    public void setMaxInputBufferSize(int maxInputBufferSize) {
        this.maxInputBufferSize = maxInputBufferSize;
    }

    /**
     * Get the number of virtual channels.
     *
     * @return the number of virtual channels
     */
    @Override
    public int getNumVirtualChannels() {
        return numVirtualChannels;
    }

    /**
     * Set the number of virtual channels.
     *
     * @param numVirtualChannels the number of virtual channels
     */
    public void setNumVirtualChannels(int numVirtualChannels) {
        this.numVirtualChannels = numVirtualChannels;
    }

    /**
     * Get the link width.
     *
     * @return the link width
     */
    @Override
    public int getLinkWidth() {
        return linkWidth;
    }

    /**
     * Set the link width.
     *
     * @param linkWidth the link width
     */
    public void setLinkWidth(int linkWidth) {
        this.linkWidth = linkWidth;
    }

    /**
     * Get the link delay.
     *
     * @return the link delay
     */
    @Override
    public int getLinkDelay() {
        return linkDelay;
    }

    /**
     * Set the link delay.
     *
     * @param linkDelay the link delay
     */
    public void setLinkDelay(int linkDelay) {
        this.linkDelay = linkDelay;
    }

    /**
     * Get the size of an ant packet.
     *
     * @return the size of an ant packet
     */
    @Override
    public int getAntPacketSize() {
        return antPacketSize;
    }

    /**
     * Set the size of an ant packet.
     *
     * @param antPacketSize the size of an ant packet
     */
    public void setAntPacketSize(int antPacketSize) {
        this.antPacketSize = antPacketSize;
    }

    /**
     * Get the ant packet injection rate.
     *
     * @return the ant packet injection rate
     */
    @Override
    public double getAntPacketInjectionRate() {
        return antPacketInjectionRate;
    }

    /**
     * Set the ant packet injection rate.
     *
     * @param antPacketInjectionRate the ant packet injection rate
     */
    public void setAntPacketInjectionRate(double antPacketInjectionRate) {
        this.antPacketInjectionRate = antPacketInjectionRate;
    }

    /**
     * Get the ACO selection alpha.
     *
     * @return the ACO selection alpha
     */
    @Override
    public double getAcoSelectionAlpha() {
        return acoSelectionAlpha;
    }

    /**
     * Set the ACO selection alpha.
     *
     * @param acoSelectionAlpha the ACO selection alpha
     */
    public void setAcoSelectionAlpha(double acoSelectionAlpha) {
        this.acoSelectionAlpha = acoSelectionAlpha;
    }

    /**
     * Get the reinforcement factor.
     *
     * @return the reinforcement factor
     */
    @Override
    public double getReinforcementFactor() {
        return reinforcementFactor;
    }

    /**
     * Set the reinforcement factor.
     *
     * @param reinforcementFactor the reinforcement factor
     */
    public void setReinforcementFactor(double reinforcementFactor) {
        this.reinforcementFactor = reinforcementFactor;
    }
}
