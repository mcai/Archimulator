/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.common;

import archimulator.core.bpred.BranchPredictorType;
import archimulator.os.Kernel;
import archimulator.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.uncore.dram.MemoryControllerType;
import archimulator.uncore.net.simple.routing.RoutingAlgorithmType;
import archimulator.util.Reference;
import archimulator.util.StorageUnit;
import archimulator.util.StorageUnitHelper;
import archimulator.util.dateTime.DateHelper;
import archimulator.util.event.BlockingEventDispatcher;
import archimulator.util.event.CycleAccurateEventQueue;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Experiment.
 *
 * @author Min Cai
 */
public class Experiment {
    private long createTime;

    private ExperimentType type;

    private ExperimentState state;

    private String failedReason;

    private String outputDirectory;

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

    private RoutingAlgorithmType routingAlgorithmType;

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

    private long numMaxInstructions;

    private ArrayList<ContextMapping> contextMappings;

    private List<ExperimentStat> stats;

    /**
     * Current (max) memory page ID.
     */
    public transient int currentMemoryPageId;

    /**
     * Current (max) process ID.
     */
    public transient int currentProcessId;

    /**
     * Create an experiment.
     * @param type               the experiment type
     * @param outputDirectory     the output directory
     * @param dynamicSpeculativePrecomputationEnabled
 *                                a value indicating whether the dynamic speculative precomputation is enabled or not
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy
*                                the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache
     * @param numCores                the number of cores
     * @param numThreadsPerCore       the number of threads per core
     * @param l1ISize                 the size of the L1I caches in bytes
     * @param l1IAssoc                the associativity of the L1I caches
     * @param l1DSize                 the size of the L1D caches in bytes
     * @param l1DAssoc                the associativity of the L1D caches
     * @param l2Size                  the size of the L2 caches in bytes
     * @param l2Assoc                 the associativity of the L2 cache
     * @param l2ReplacementPolicyType the replacement policy type of the L2 cache
     * @param routingAlgorithmType the routing algorithm type
     * @param memoryControllerType    the memory controller type
     * @param numMaxInstructions the upper limit of the number of instructions executed on the first hardware thread
     * @param contextMappings    the context mappings
     */
    public Experiment(ExperimentType type, String outputDirectory, boolean dynamicSpeculativePrecomputationEnabled, int numMainThreadWaysInStaticPartitionedLRUPolicy, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType, RoutingAlgorithmType routingAlgorithmType, MemoryControllerType memoryControllerType, long numMaxInstructions, List<ContextMapping> contextMappings) {
        this.type = type;
        this.outputDirectory = outputDirectory;
        this.state = ExperimentState.PENDING;
        this.failedReason = "";
        this.numMaxInstructions = numMaxInstructions;
        this.contextMappings = new ArrayList<>(contextMappings);

        this.helperThreadPthreadSpawnIndex = 3720;

        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;

        this.numMainThreadWaysInStaticPartitionedLRUPolicy = numMainThreadWaysInStaticPartitionedLRUPolicy;

        this.numCores = numCores;
        this.numThreadsPerCore = numThreadsPerCore;

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

        this.l1ISize = l1ISize;
        this.l1IAssociativity = l1IAssoc;
        this.l1ILineSize = 64;
        this.l1IHitLatency = 1;
        this.l1INumReadPorts = 128;
        this.l1INumWritePorts = 128;
        this.l1IReplacementPolicyType = CacheReplacementPolicyType.LRU;

        this.l1DSize = l1DSize;
        this.l1DAssociativity = l1DAssoc;
        this.l1DLineSize = 64;
        this.l1DHitLatency = 1;
        this.l1DNumReadPorts = 128;
        this.l1DNumWritePorts = 128;
        this.l1DReplacementPolicyType = CacheReplacementPolicyType.LRU;

        this.l2Size = l2Size;
        this.l2Associativity = l2Assoc;
        this.l2LineSize = 64;
        this.l2HitLatency = 10;
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;

        this.routingAlgorithmType = routingAlgorithmType;

        this.memoryControllerType = memoryControllerType;
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

        this.createTime = DateHelper.toTick(new Date());
        this.stats = new ArrayList<>();
    }

    /**
     * Run.
     */
    public void run() {
        try {
            CycleAccurateEventQueue cycleAccurateEventQueue = new CycleAccurateEventQueue();

            if (getType() == ExperimentType.FUNCTIONAL) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
                new FunctionalSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (getType() == ExperimentType.DETAILED) {
                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();
                new DetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue).simulate();
            } else if (getType() == ExperimentType.TWO_PHASE) {
                Reference<Kernel> kernelRef = new Reference<>();

                BlockingEventDispatcher<SimulationEvent> blockingEventDispatcher = new BlockingEventDispatcher<>();

                new ToRoiFastForwardSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();

                blockingEventDispatcher.clearListeners();

                cycleAccurateEventQueue.resetCurrentCycle();

                new FromRoiDetailedSimulation(this, blockingEventDispatcher, cycleAccurateEventQueue, kernelRef).simulate();
            }

            this.setState(ExperimentState.COMPLETED);
            this.setFailedReason("");
        } catch (Exception e) {
            this.setState(ExperimentState.ABORTED);
            this.setFailedReason(ExceptionUtils.getStackTrace(e));
            e.printStackTrace();
        }
    }

    /**
     * Get the time in ticks when the experiment is created.
     *
     * @return the time in ticks when the experiment is created
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get the string representation of the time when the experiment is created.
     *
     * @return the string representation of the time when the experiment is created
     */
    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
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
     * Get the experiment state.
     *
     * @return the experiment state
     */
    public ExperimentState getState() {
        return state;
    }

    /**
     * Set the experiment state.
     *
     * @param state the experiment state
     */
    public void setState(ExperimentState state) {
        this.state = state;
    }

    /**
     * Get the failed reason, being empty if the experiment is not failed at all.
     *
     * @return the failed reason
     */
    public String getFailedReason() {
        return failedReason;
    }

    /**
     * Set the failed reason, being empty if the experiment is not failed at all.
     *
     * @param failedReason the failed reason
     */
    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
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
     * Get the helper thread Pthread spawning index of pseudo calls.
     *
     * @return the helper thread Pthread spawning index.
     */
    public int getHelperThreadPthreadSpawnIndex() {
        return helperThreadPthreadSpawnIndex;
    }

    /**
     * Get a value indicating whether dynamic speculative precomputation is enabled or not.
     *
     * @return a value indicating whether dynamic speculative precomputation is enabled or not
     */
    public boolean getDynamicSpeculativePrecomputationEnabled() {
        return dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Get the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache.
     *
     * @return the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache
     */
    public int getNumMainThreadWaysInStaticPartitionedLRUPolicy() {
        return numMainThreadWaysInStaticPartitionedLRUPolicy;
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
     * Get the number of threads per core.
     *
     * @return the number of threads per core
     */
    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
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
     * Get the decode width.
     *
     * @return the decode width
     */
    public int getDecodeWidth() {
        return decodeWidth;
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
     * Get the commit width.
     *
     * @return the commit width
     */
    public int getCommitWidth() {
        return commitWidth;
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
     * Get the reorder buffer capacity.
     *
     * @return the reorder buffer capacity
     */
    public int getReorderBufferCapacity() {
        return reorderBufferCapacity;
    }

    /**
     * Get the load/store queue capacity.
     *
     * @return the load/store queue capacity
     */
    public int getLoadStoreQueueCapacity() {
        return loadStoreQueueCapacity;
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
     * Get the bimod size used in the two-bit branch predictor.
     *
     * @return the bimod size used in the two-bit branch predictor
     */
    public int getTwoBitBranchPredictorBimodSize() {
        return twoBitBranchPredictorBimodSize;
    }

    /**
     * Get the number of sets in the branch target buffer used in the two-bit branch predictor.
     *
     * @return the number of sets in the branch target buffer used in the two-bit branch predictor
     */
    public int getTwoBitBranchPredictorBranchTargetBufferNumSets() {
        return twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Get the associativity of the branch target buffer used in the two-bit branch predictor.
     *
     * @return the associativity of the branch target buffer used in the two-bit branch predictor
     */
    public int getTwoBitBranchPredictorBranchTargetBufferAssociativity() {
        return twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Get the size of the return address stack used in the two-bit branch predictor.
     *
     * @return the size of the return address stack used in the two-bit branch predictor
     */
    public int getTwoBitBranchPredictorReturnAddressStackSize() {
        return twoBitBranchPredictorReturnAddressStackSize;
    }

    /**
     * Get the L1 size used in the two-level branch predictor.
     *
     * @return the L1 size used in the two-level branch predictor
     */
    public int getTwoLevelBranchPredictorL1Size() {
        return twoLevelBranchPredictorL1Size;
    }

    /**
     * Get the L2 size used in the two-level branch predictor.
     *
     * @return the L2 size used in the two-level branch predictor
     */
    public int getTwoLevelBranchPredictorL2Size() {
        return twoLevelBranchPredictorL2Size;
    }

    /**
     * Get the shift width used in the two-level branch predictor.
     *
     * @return the shift width used in the two level branch predictor
     */
    public int getTwoLevelBranchPredictorShiftWidth() {
        return twoLevelBranchPredictorShiftWidth;
    }

    /**
     * Get the xor used in the two-level branch predictor.
     *
     * @return the xor used in the two-level branch predictor
     */
    public boolean getTwoLevelBranchPredictorXor() {
        return twoLevelBranchPredictorXor;
    }

    /**
     * Get the number of sets in the branch target buffer used in the two-level branch predictor.
     *
     * @return the number of sets in the branch target buffer used in the two-level branch predictor
     */
    public int getTwoLevelBranchPredictorBranchTargetBufferNumSets() {
        return twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Get the associativity of the branch target buffer used in the two-level branch predictor.
     *
     * @return the associativity of the branch target buffer used in the two-level branch predictor
     */
    public int getTwoLevelBranchPredictorBranchTargetBufferAssociativity() {
        return twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Get the size of the return address stack used in the two-level branch predictor.
     *
     * @return the size of the return address stack used in the two-level branch predictor.
     */
    public int getTwoLevelBranchPredictorReturnAddressStackSize() {
        return twoLevelBranchPredictorReturnAddressStackSize;
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
     * Get the L1 size used in the combined branch predictor.
     *
     * @return the L1 size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorL1Size() {
        return combinedBranchPredictorL1Size;
    }

    /**
     * Get the L2 size used in the combined branch predictor.
     *
     * @return the L2 size used in the combined branch predictor
     */
    public int getCombinedBranchPredictorL2Size() {
        return combinedBranchPredictorL2Size;
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
     * Get the shift width used in the combined branch predictor.
     *
     * @return the shift width used in the combined branch predictor
     */
    public int getCombinedBranchPredictorShiftWidth() {
        return combinedBranchPredictorShiftWidth;
    }

    /**
     * Get the xor used in the combined branch predictor.
     *
     * @return the xor used in the combined branch predictor
     */
    public boolean getCombinedBranchPredictorXor() {
        return combinedBranchPredictorXor;
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
     * Get the associativity of the branch target buffer used in the combined branch predictor.
     *
     * @return the associativity of the branch target buffer used in the combined branch predictor
     */
    public int getCombinedBranchPredictorBranchTargetBufferAssociativity() {
        return combinedBranchPredictorBranchTargetBufferAssociativity;
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
     * Get the size of the TLBs.
     *
     * @return the size of the TLBs
     */
    public int getTlbSize() {
        return tlbSize;
    }

    /**
     * Get the associativity of the TLBs.
     *
     * @return the associativity of the TLBs
     */
    public int getTlbAssociativity() {
        return tlbAssociativity;
    }

    /**
     * Get the line size of the TLBs.
     *
     * @return the line size of the TLBs.
     */
    public int getTlbLineSize() {
        return tlbLineSize;
    }

    /**
     * Get the hit latency of the TLBs.
     *
     * @return the hit latency of the TLBs
     */
    public int getTlbHitLatency() {
        return tlbHitLatency;
    }

    /**
     * Get the miss latency of the TLBs.
     *
     * @return the miss latency of the TLBs
     */
    public int getTlbMissLatency() {
        return tlbMissLatency;
    }

    /**
     * Get the size of the L1I caches.
     *
     * @return the size of the L1I caches
     */
    public int getL1ISize() {
        return l1ISize;
    }

    /**
     * Get the associativity of the L1I caches.
     *
     * @return the associativity of the L1I caches
     */
    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    /**
     * Get the line size of the L1I caches.
     *
     * @return the line size of the L1I caches
     */
    public int getL1ILineSize() {
        return l1ILineSize;
    }

    /**
     * Get the hit latency of the L1I caches.
     *
     * @return the hit latency of the L1I caches
     */
    public int getL1IHitLatency() {
        return l1IHitLatency;
    }

    /**
     * Get the number of read ports in the L1I caches.
     *
     * @return the number of read ports in the L1I caches
     */
    public int getL1INumReadPorts() {
        return l1INumReadPorts;
    }

    /**
     * Get the number of write ports in the L1I caches.
     *
     * @return the number of write ports in the L1I caches
     */
    public int getL1INumWritePorts() {
        return l1INumWritePorts;
    }

    /**
     * Get the replacement policy type in the L1I caches.
     *
     * @return the replacement policy type in the L1I caches
     */
    public CacheReplacementPolicyType getL1IReplacementPolicyType() {
        return l1IReplacementPolicyType;
    }

    /**
     * Get the size of the L1D caches.
     *
     * @return the size of the L1D caches
     */
    public int getL1DSize() {
        return l1DSize;
    }

    /**
     * Get the associativity of the L1D caches.
     *
     * @return the associativity of the L1D caches
     */
    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    /**
     * Get the line size in the L1D caches.
     *
     * @return the line size of the L1D caches
     */
    public int getL1DLineSize() {
        return l1DLineSize;
    }

    /**
     * Get the hit latency of the L1D caches.
     *
     * @return the hit latency of the L1D caches
     */
    public int getL1DHitLatency() {
        return l1DHitLatency;
    }

    /**
     * Get the number of read ports in the L1D caches.
     *
     * @return the number of read ports in the L1D caches
     */
    public int getL1DNumReadPorts() {
        return l1DNumReadPorts;
    }

    /**
     * Get the number of write ports in the L1D caches.
     *
     * @return the number of write ports in the L1D caches
     */
    public int getL1DNumWritePorts() {
        return l1DNumWritePorts;
    }

    /**
     * Get the replacement policy type of the L1D caches.
     *
     * @return the replacement policy type of the L1D caches
     */
    public CacheReplacementPolicyType getL1DReplacementPolicyType() {
        return l1DReplacementPolicyType;
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
     * Get the associativity of the L2 cache.
     *
     * @return the associativity of the L2 cache
     */
    public int getL2Associativity() {
        return l2Associativity;
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
     * Get the hit latency of the L2 cache.
     *
     * @return the hit latency of the L2 cache
     */
    public int getL2HitLatency() {
        return l2HitLatency;
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
     * Get the on-chip interconnect routing algorithm type.
     *
     * @return on-chip interconnect routing algorithm type
     */
    public RoutingAlgorithmType getRoutingAlgorithmType() {
        return routingAlgorithmType;
    }

    /**
     * Get the memory controller type.
     *
     * @return the memory controller type
     */
    public MemoryControllerType getMemoryControllerType() {
        return memoryControllerType;
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
     * Get the latency used in the fixed latency memory controller.
     *
     * @return the latency used in the fixed latency memory controller
     */
    public int getFixedLatencyMemoryControllerLatency() {
        return fixedLatencyMemoryControllerLatency;
    }

    /**
     * Get the memory latency used in the simple memory controller.
     *
     * @return the memory latency used in the simple memory controller
     */
    public int getSimpleMemoryControllerMemoryLatency() {
        return simpleMemoryControllerMemoryLatency;
    }

    /**
     * Get the memory trunk latency used in the simple memory controller.
     *
     * @return the memory trunk latency used in the simple memory controller
     */
    public int getSimpleMemoryControllerMemoryTrunkLatency() {
        return simpleMemoryControllerMemoryTrunkLatency;
    }

    /**
     * Get the bus width used in the simple memory controller.
     *
     * @return the bus width used in the simple memory controller
     */
    public int getSimpleMemoryControllerBusWidth() {
        return simpleMemoryControllerBusWidth;
    }

    /**
     * Get the "to DRAM" latency used in the basic memory controller.
     *
     * @return the "to DRAM" latency used in the basic memory controller
     */
    public int getBasicMemoryControllerToDramLatency() {
        return basicMemoryControllerToDramLatency;
    }

    /**
     * Get the "from DRAM" latency used in the basic memory controller.
     *
     * @return the "from DRAM" latency used in the basic memory controller
     */
    public int getBasicMemoryControllerFromDramLatency() {
        return basicMemoryControllerFromDramLatency;
    }

    /**
     * Get the precharge latency used in the basic memory controller.
     *
     * @return the precharge latency used in the basic memory controller
     */
    public int getBasicMemoryControllerPrechargeLatency() {
        return basicMemoryControllerPrechargeLatency;
    }

    /**
     * Get the "closed" latency used in the basic memory controller.
     *
     * @return the "closed" latency used in the basic memory controller
     */
    public int getBasicMemoryControllerClosedLatency() {
        return basicMemoryControllerClosedLatency;
    }

    /**
     * Get the conflict latency used in the basic memory controller.
     *
     * @return the conflict latency used in the basic memory controller
     */
    public int getBasicMemoryControllerConflictLatency() {
        return basicMemoryControllerConflictLatency;
    }

    /**
     * Get the bus width used in the basic memory controller.
     *
     * @return the bus width used in the basic memory controller
     */
    public int getBasicMemoryControllerBusWidth() {
        return basicMemoryControllerBusWidth;
    }

    /**
     * Get the number of banks used in the basic memory controller.
     *
     * @return the number of banks used in the basic memory controller
     */
    public int getBasicMemoryControllerNumBanks() {
        return basicMemoryControllerNumBanks;
    }

    /**
     * Get the row size used in the basic memory controller.
     *
     * @return the row size used in the basic memory controller
     */
    public int getBasicMemoryControllerRowSize() {
        return basicMemoryControllerRowSize;
    }

    /**
     * Set the helper thread Pthread spawning index.
     *
     * @param helperThreadPthreadSpawnIndex the new helper thread Pthread spawning index
     */
    public void setHelperThreadPthreadSpawnIndex(int helperThreadPthreadSpawnIndex) {
        this.helperThreadPthreadSpawnIndex = helperThreadPthreadSpawnIndex;
    }

    /**
     * Set a value indicating dynamic speculative precomputation is enabled or not.
     *
     * @param dynamicSpeculativePrecomputationEnabled
     *         a value indicating whether dynamic speculative precomputation is enabled or not
     */
    public void setDynamicSpeculativePrecomputationEnabled(boolean dynamicSpeculativePrecomputationEnabled) {
        this.dynamicSpeculativePrecomputationEnabled = dynamicSpeculativePrecomputationEnabled;
    }

    /**
     * Set the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache.
     *
     * @param numMainThreadWaysInStaticPartitionedLRUPolicy
     *         the number of main thread ways used in the static partitioned LRU policy for the shared L2 cache
     */
    public void setNumMainThreadWaysInStaticPartitionedLRUPolicy(int numMainThreadWaysInStaticPartitionedLRUPolicy) {
        this.numMainThreadWaysInStaticPartitionedLRUPolicy = numMainThreadWaysInStaticPartitionedLRUPolicy;
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
     * Set the number of threads per core.
     *
     * @param numThreadsPerCore the number of threads per core
     */
    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    /**
     * Set the capacity of the physical register file.
     *
     * @param physicalRegisterFileCapacity the capacity of the physical register file
     */
    public void setPhysicalRegisterFileCapacity(int physicalRegisterFileCapacity) {
        this.physicalRegisterFileCapacity = physicalRegisterFileCapacity;
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
     * Set the issue width.
     *
     * @param issueWidth the issue width
     */
    public void setIssueWidth(int issueWidth) {
        this.issueWidth = issueWidth;
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
     * Set the capacity of the decode buffer.
     *
     * @param decodeBufferCapacity the capacity of the decode buffer
     */
    public void setDecodeBufferCapacity(int decodeBufferCapacity) {
        this.decodeBufferCapacity = decodeBufferCapacity;
    }

    /**
     * Set the capacity of the reorder buffer.
     *
     * @param reorderBufferCapacity the capacity of the reorder buffer
     */
    public void setReorderBufferCapacity(int reorderBufferCapacity) {
        this.reorderBufferCapacity = reorderBufferCapacity;
    }

    /**
     * Set the capacity of the load/store queue.
     *
     * @param loadStoreQueueCapacity the capacity of the load/store queue
     */
    public void setLoadStoreQueueCapacity(int loadStoreQueueCapacity) {
        this.loadStoreQueueCapacity = loadStoreQueueCapacity;
    }

    /**
     * Set the branch predictor type.
     *
     * @param branchPredictorType branch predictor type
     */
    public void setBranchPredictorType(BranchPredictorType branchPredictorType) {
        this.branchPredictorType = branchPredictorType;
    }

    /**
     * Set the bimod size used in the two-bit branch predictor.
     *
     * @param twoBitBranchPredictorBimodSize the bimod size used in the two-bit branch predictor
     */
    public void setTwoBitBranchPredictorBimodSize(int twoBitBranchPredictorBimodSize) {
        this.twoBitBranchPredictorBimodSize = twoBitBranchPredictorBimodSize;
    }

    /**
     * Set the number of sets in the branch target buffer used in the two-bit branch predictor.
     *
     * @param twoBitBranchPredictorBranchTargetBufferNumSets
     *         the number of sets in the branch target buffer used in the two-bit branch predictor
     */
    public void setTwoBitBranchPredictorBranchTargetBufferNumSets(int twoBitBranchPredictorBranchTargetBufferNumSets) {
        this.twoBitBranchPredictorBranchTargetBufferNumSets = twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the associativity in the branch target buffer used in the two-bit branch predictor.
     *
     * @param twoBitBranchPredictorBranchTargetBufferAssociativity
     *         the associativity in the branch target buffer used in the two-bit branch predictor
     */
    public void setTwoBitBranchPredictorBranchTargetBufferAssociativity(int twoBitBranchPredictorBranchTargetBufferAssociativity) {
        this.twoBitBranchPredictorBranchTargetBufferAssociativity = twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the size of the return address stack used in the two-bit branch predictor.
     *
     * @param twoBitBranchPredictorReturnAddressStackSize
     *         the size of the return address stack used in the two-bit branch predictor
     */
    public void setTwoBitBranchPredictorReturnAddressStackSize(int twoBitBranchPredictorReturnAddressStackSize) {
        this.twoBitBranchPredictorReturnAddressStackSize = twoBitBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the L1 size used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorL1Size the L1 size used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorL1Size(int twoLevelBranchPredictorL1Size) {
        this.twoLevelBranchPredictorL1Size = twoLevelBranchPredictorL1Size;
    }

    /**
     * Set the L2 size used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorL2Size the L2 size used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorL2Size(int twoLevelBranchPredictorL2Size) {
        this.twoLevelBranchPredictorL2Size = twoLevelBranchPredictorL2Size;
    }

    /**
     * Set the shift width used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorShiftWidth
     *         the shift width used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorShiftWidth(int twoLevelBranchPredictorShiftWidth) {
        this.twoLevelBranchPredictorShiftWidth = twoLevelBranchPredictorShiftWidth;
    }

    /**
     * Set the xor used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorXor the xor used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorXor(boolean twoLevelBranchPredictorXor) {
        this.twoLevelBranchPredictorXor = twoLevelBranchPredictorXor;
    }

    /**
     * Set the number of sets in the branch target buffer used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorBranchTargetBufferNumSets
     *         the number of sets in the branch target buffer used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorBranchTargetBufferNumSets(int twoLevelBranchPredictorBranchTargetBufferNumSets) {
        this.twoLevelBranchPredictorBranchTargetBufferNumSets = twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the associativity of the branch target buffer used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorBranchTargetBufferAssociativity
     *         the associativity in the branch target buffer used in the two-level branch predictor
     */
    public void setTwoLevelBranchPredictorBranchTargetBufferAssociativity(int twoLevelBranchPredictorBranchTargetBufferAssociativity) {
        this.twoLevelBranchPredictorBranchTargetBufferAssociativity = twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the size of the return address stack used in the two-level branch predictor.
     *
     * @param twoLevelBranchPredictorReturnAddressStackSize
     *         the size of the return address stack used in the two-level branch predictor.
     */
    public void setTwoLevelBranchPredictorReturnAddressStackSize(int twoLevelBranchPredictorReturnAddressStackSize) {
        this.twoLevelBranchPredictorReturnAddressStackSize = twoLevelBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the bimod size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBimodSize
     *         the bimod size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBimodSize(int combinedBranchPredictorBimodSize) {
        this.combinedBranchPredictorBimodSize = combinedBranchPredictorBimodSize;
    }

    /**
     * Set the L1 size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorL1Size the L1 size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorL1Size(int combinedBranchPredictorL1Size) {
        this.combinedBranchPredictorL1Size = combinedBranchPredictorL1Size;
    }

    /**
     * Set the L2 size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorL2Size the L2 size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorL2Size(int combinedBranchPredictorL2Size) {
        this.combinedBranchPredictorL2Size = combinedBranchPredictorL2Size;
    }

    /**
     * Set the meta size used in the combined branch predictor.
     *
     * @param combinedBranchPredictorMetaSize
     *         the meta size used in the combined branch predictor
     */
    public void setCombinedBranchPredictorMetaSize(int combinedBranchPredictorMetaSize) {
        this.combinedBranchPredictorMetaSize = combinedBranchPredictorMetaSize;
    }

    /**
     * Set the shift width used in the combined branch predictor.
     *
     * @param combinedBranchPredictorShiftWidth
     *         the shift width used in the combined branch predictor
     */
    public void setCombinedBranchPredictorShiftWidth(int combinedBranchPredictorShiftWidth) {
        this.combinedBranchPredictorShiftWidth = combinedBranchPredictorShiftWidth;
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
     * Set the number of sets in the branch target buffer used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBranchTargetBufferNumSets
     *         the number of sets in the branch target buffer used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBranchTargetBufferNumSets(int combinedBranchPredictorBranchTargetBufferNumSets) {
        this.combinedBranchPredictorBranchTargetBufferNumSets = combinedBranchPredictorBranchTargetBufferNumSets;
    }

    /**
     * Set the associativity of the branch target buffer used in the combined branch predictor.
     *
     * @param combinedBranchPredictorBranchTargetBufferAssociativity
     *         the associativity of the branch target buffer used in the combined branch predictor
     */
    public void setCombinedBranchPredictorBranchTargetBufferAssociativity(int combinedBranchPredictorBranchTargetBufferAssociativity) {
        this.combinedBranchPredictorBranchTargetBufferAssociativity = combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    /**
     * Set the size of the return address stack used in the combined branch predictor.
     *
     * @param combinedBranchPredictorReturnAddressStackSize
     *         the size of the return address stack used in the combined branch predictor
     */
    public void setCombinedBranchPredictorReturnAddressStackSize(int combinedBranchPredictorReturnAddressStackSize) {
        this.combinedBranchPredictorReturnAddressStackSize = combinedBranchPredictorReturnAddressStackSize;
    }

    /**
     * Set the size of the TLBs.
     *
     * @param tlbSize the size of the TLBs
     */
    public void setTlbSize(int tlbSize) {
        this.tlbSize = tlbSize;
    }

    /**
     * Set the associativity of the TLBs.
     *
     * @param tlbAssociativity the associativity of the TLBs
     */
    public void setTlbAssociativity(int tlbAssociativity) {
        this.tlbAssociativity = tlbAssociativity;
    }

    /**
     * Set the line size of the TLBs.
     *
     * @param tlbLineSize the line size of the TLBs
     */
    public void setTlbLineSize(int tlbLineSize) {
        this.tlbLineSize = tlbLineSize;
    }

    /**
     * Set the hit latency of the TLBs.
     *
     * @param tlbHitLatency the hit latency of the TLBs
     */
    public void setTlbHitLatency(int tlbHitLatency) {
        this.tlbHitLatency = tlbHitLatency;
    }

    /**
     * Set the miss latency of the TLBs.
     *
     * @param tlbMissLatency the miss latency of the TLBs
     */
    public void setTlbMissLatency(int tlbMissLatency) {
        this.tlbMissLatency = tlbMissLatency;
    }

    /**
     * Set the size of the L1I caches.
     *
     * @param l1ISize the size of the L1I caches
     */
    public void setL1ISize(int l1ISize) {
        this.l1ISize = l1ISize;
    }

    /**
     * Set the associativity of the L1I caches.
     *
     * @param l1IAssociativity the associativity of the L1I caches
     */
    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    /**
     * Set the line size of the L1I caches.
     *
     * @param l1ILineSize the line size of the L1I caches
     */
    public void setL1ILineSize(int l1ILineSize) {
        this.l1ILineSize = l1ILineSize;
    }

    /**
     * Set the hit latency of the L1I caches.
     *
     * @param l1IHitLatency the hit latency of the L1I caches
     */
    public void setL1IHitLatency(int l1IHitLatency) {
        this.l1IHitLatency = l1IHitLatency;
    }

    /**
     * Set the number of read ports in the L1I caches.
     *
     * @param l1INumReadPorts the number of read ports in the L1I caches
     */
    public void setL1INumReadPorts(int l1INumReadPorts) {
        this.l1INumReadPorts = l1INumReadPorts;
    }

    /**
     * Set the number of write ports in the L1I caches.
     *
     * @param l1INumWritePorts the number of write ports in the L1I caches
     */
    public void setL1INumWritePorts(int l1INumWritePorts) {
        this.l1INumWritePorts = l1INumWritePorts;
    }

    /**
     * Set the replacement policy type of the L1I caches.
     *
     * @param l1IReplacementPolicyType the replacement policy type of the L1I caches
     */
    public void setL1IReplacementPolicyType(CacheReplacementPolicyType l1IReplacementPolicyType) {
        this.l1IReplacementPolicyType = l1IReplacementPolicyType;
    }

    /**
     * Set the size of the L1D caches.
     *
     * @param l1DSize the size of the L1D caches
     */
    public void setL1DSize(int l1DSize) {
        this.l1DSize = l1DSize;
    }

    /**
     * Set the size of the L1D caches.
     *
     * @param l1DAssociativity the associativity of the L1D caches
     */
    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    /**
     * Set the line size of the L1D caches.
     *
     * @param l1DLineSize the line size of the L1D caches
     */
    public void setL1DLineSize(int l1DLineSize) {
        this.l1DLineSize = l1DLineSize;
    }

    /**
     * Set the hit latency of the L1D caches.
     *
     * @param l1DHitLatency the hit latency of the L1D caches
     */
    public void setL1DHitLatency(int l1DHitLatency) {
        this.l1DHitLatency = l1DHitLatency;
    }

    /**
     * Set the number of read ports in the L1D caches.
     *
     * @param l1DNumReadPorts the number of read ports in the L1D caches
     */
    public void setL1DNumReadPorts(int l1DNumReadPorts) {
        this.l1DNumReadPorts = l1DNumReadPorts;
    }

    /**
     * Set the number of write ports in the L1D caches.
     *
     * @param l1DNumWritePorts the number of write ports in the L1D caches
     */
    public void setL1DNumWritePorts(int l1DNumWritePorts) {
        this.l1DNumWritePorts = l1DNumWritePorts;
    }

    /**
     * Set the replacement policy type of the L1D caches.
     *
     * @param l1DReplacementPolicyType the replacement policy type of the L1D caches
     */
    public void setL1DReplacementPolicyType(CacheReplacementPolicyType l1DReplacementPolicyType) {
        this.l1DReplacementPolicyType = l1DReplacementPolicyType;
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
     * Set the associativity of the L2 cache.
     *
     * @param l2Associativity the associativity of the L2 cache
     */
    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
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
     * Set the hit latency of the L2 cache.
     *
     * @param l2HitLatency the hit latency of the L2 cache
     */
    public void setL2HitLatency(int l2HitLatency) {
        this.l2HitLatency = l2HitLatency;
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
     * Set the on-chip interconnect routing algorithm type.
     *
     * @param routingAlgorithmType the on-chip interconnect  routing algorithm type
     */
    public void setRoutingAlgorithmType(RoutingAlgorithmType routingAlgorithmType) {
        this.routingAlgorithmType = routingAlgorithmType;
    }

    /**
     * Set the memory controller type.
     *
     * @param memoryControllerType the memory controller type
     */
    public void setMemoryControllerType(MemoryControllerType memoryControllerType) {
        this.memoryControllerType = memoryControllerType;
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
     * Set the latency used in the fixed latency memory controller.
     *
     * @param fixedLatencyMemoryControllerLatency
     *         the latency used in the fixed latency memory controller
     */
    public void setFixedLatencyMemoryControllerLatency(int fixedLatencyMemoryControllerLatency) {
        this.fixedLatencyMemoryControllerLatency = fixedLatencyMemoryControllerLatency;
    }

    /**
     * Set the memory latency used in the simple memory controller.
     *
     * @param simpleMemoryControllerMemoryLatency
     *         the memory latency used in the simple memory controller
     */
    public void setSimpleMemoryControllerMemoryLatency(int simpleMemoryControllerMemoryLatency) {
        this.simpleMemoryControllerMemoryLatency = simpleMemoryControllerMemoryLatency;
    }

    /**
     * Set the memory trunk latency used in the simple memory controller.
     *
     * @param simpleMemoryControllerMemoryTrunkLatency
     *         the memory trunk latency used in the simple memory controller
     */
    public void setSimpleMemoryControllerMemoryTrunkLatency(int simpleMemoryControllerMemoryTrunkLatency) {
        this.simpleMemoryControllerMemoryTrunkLatency = simpleMemoryControllerMemoryTrunkLatency;
    }

    /**
     * Set the bus width used in the simple memory controller.
     *
     * @param simpleMemoryControllerBusWidth the bus width used in the simple memory controller
     */
    public void setSimpleMemoryControllerBusWidth(int simpleMemoryControllerBusWidth) {
        this.simpleMemoryControllerBusWidth = simpleMemoryControllerBusWidth;
    }

    /**
     * Set the "to DRAM" latency used in the basic memory controller.
     *
     * @param basicMemoryControllerToDramLatency
     *         the "to DRAM" latency used in the basic memory controller
     */
    public void setBasicMemoryControllerToDramLatency(int basicMemoryControllerToDramLatency) {
        this.basicMemoryControllerToDramLatency = basicMemoryControllerToDramLatency;
    }

    /**
     * Set the "from DRAM" latency used in the basic memory controller.
     *
     * @param basicMemoryControllerFromDramLatency
     *         the "from DRAM" latency used in the basic memory controller
     */
    public void setBasicMemoryControllerFromDramLatency(int basicMemoryControllerFromDramLatency) {
        this.basicMemoryControllerFromDramLatency = basicMemoryControllerFromDramLatency;
    }

    /**
     * Set the precharge latency used in the basic memory controller.
     *
     * @param basicMemoryControllerPrechargeLatency
     *         the precharge latency used in the basic memory controller
     */
    public void setBasicMemoryControllerPrechargeLatency(int basicMemoryControllerPrechargeLatency) {
        this.basicMemoryControllerPrechargeLatency = basicMemoryControllerPrechargeLatency;
    }

    /**
     * Set the "closed" latency used in the basic memory controller.
     *
     * @param basicMemoryControllerClosedLatency
     *         the "closed" latency used in the basic memory controller
     */
    public void setBasicMemoryControllerClosedLatency(int basicMemoryControllerClosedLatency) {
        this.basicMemoryControllerClosedLatency = basicMemoryControllerClosedLatency;
    }

    /**
     * Set the conflict latency used in the basic memory controller.
     *
     * @param basicMemoryControllerConflictLatency
     *         the conflict latency used in the basic memory controller
     */
    public void setBasicMemoryControllerConflictLatency(int basicMemoryControllerConflictLatency) {
        this.basicMemoryControllerConflictLatency = basicMemoryControllerConflictLatency;
    }

    /**
     * Set the bus width used in the basic memory controller.
     *
     * @param basicMemoryControllerBusWidth the bus width used in the basic memory controller
     */
    public void setBasicMemoryControllerBusWidth(int basicMemoryControllerBusWidth) {
        this.basicMemoryControllerBusWidth = basicMemoryControllerBusWidth;
    }

    /**
     * Set the number of banks used in the basic memory controller.
     *
     * @param basicMemoryControllerNumBanks the number of banks used in the basic memory controller
     */
    public void setBasicMemoryControllerNumBanks(int basicMemoryControllerNumBanks) {
        this.basicMemoryControllerNumBanks = basicMemoryControllerNumBanks;
    }

    /**
     * Set the row size used in the basic memory controller.
     *
     * @param basicMemoryControllerRowSize the row size used in the basic memory controller
     */
    public void setBasicMemoryControllerRowSize(int basicMemoryControllerRowSize) {
        this.basicMemoryControllerRowSize = basicMemoryControllerRowSize;
    }

    /**
     * Get the size of the TLBs in storage unit.
     *
     * @return the size of the TLBs in storage unit
     */
    public String getTlbSizeInStorageUnit() {
        return StorageUnit.toString(tlbSize);
    }

    /**
     * Set the size of the TLBs in storage unit.
     *
     * @param tlbSizeInStorageUnit the size of the TLBs in storage unit
     */
    public void setTlbSizeInStorageUnit(String tlbSizeInStorageUnit) {
        this.tlbSize = (int) StorageUnitHelper.displaySizeToByteCount(tlbSizeInStorageUnit);
    }

    /**
     * Get the size of the L1I caches in storage unit.
     *
     * @return the size of the L1I caches in storage unit
     */
    public String getL1ISizeInStorageUnit() {
        return StorageUnit.toString(l1ISize);
    }

    /**
     * Set the size of the L1I caches in storage unit.
     *
     * @param l1ISizeInStorageUnit the size of the L1I caches in storage unit
     */
    public void setL1ISizeInStorageUnit(String l1ISizeInStorageUnit) {
        this.l1ISize = (int) StorageUnitHelper.displaySizeToByteCount(l1ISizeInStorageUnit);
    }

    /**
     * Get the size of the L1D caches in storage unit.
     *
     * @return the size of the L1D caches in storage unit
     */
    public String getL1DSizeInStorageUnit() {
        return StorageUnit.toString(l1DSize);
    }

    /**
     * Set the size of the L1D caches in storage unit.
     *
     * @param l1DSizeInStorageUnit the size of the L1D caches in storage unit
     */
    public void setL1DSizeInStorageUnit(String l1DSizeInStorageUnit) {
        this.l1DSize = (int) StorageUnitHelper.displaySizeToByteCount(l1DSizeInStorageUnit);
    }

    /**
     * Get the size of the L2 cache in storage unit.
     *
     * @return the size of the L2 cache in storage unit
     */
    public String getL2SizeInStorageUnit() {
        return StorageUnit.toString(l2Size);
    }

    /**
     * Set the size of the L2 cache in storage unit.
     *
     * @param l2SizeInStorageUnit the size of the L2 cache in storage unit
     */
    public void setL2SizeInStorageUnit(String l2SizeInStorageUnit) {
        this.l2Size = (int) StorageUnitHelper.displaySizeToByteCount(l2SizeInStorageUnit);
    }

    /**
     * Get the upper limit of the number of instructions executed in the first hardware thread.
     *
     * @return the upper limit of the number of instructions executed in the first hardware thread
     */
    public long getNumMaxInstructions() {
        return numMaxInstructions;
    }

    /**
     * Get the context mappings.
     *
     * @return the context mappings
     */
    public List<ContextMapping> getContextMappings() {
        return contextMappings;
    }

    /**
     * Get the in-memory list of statistics.
     *
     * @return the in-memory list of statistics
     */
    public List<ExperimentStat> getStats() {
        return stats;
    }

    /**
     * Set the in-memory list of statistics.
     *
     * @param stats the in-memory list of statistics
     */
    public void setStats(List<ExperimentStat> stats) {
        this.stats = stats;
    }

    /**
     * Get a value indicating whether the experiment is stopped or not.
     *
     * @return a value indicating whether the experiment is stopped or not
     */
    public boolean isStopped() {
        return this.state == ExperimentState.COMPLETED || this.state == ExperimentState.ABORTED;
    }

    /**
     * Get the measurement simulation title prefix.
     *
     * @return the measurement simulation title prefix
     */
    public String getMeasurementTitlePrefix() {
        return getMeasurementTitlePrefix(type);
    }

    /**
     * Get the measurement simulation title prefix from the specified experiment type.
     *
     * @param type the experiment type
     * @return the measurement simulation title prefix from the specified experiment type
     */
    public static String getMeasurementTitlePrefix(ExperimentType type) {
        switch (type) {
            case TWO_PHASE:
                return "twoPhase/phase1";
            case FUNCTIONAL:
                return "functional";
            case DETAILED:
                return "detailed";
            default:
                throw new IllegalArgumentException();
        }
    }
}
