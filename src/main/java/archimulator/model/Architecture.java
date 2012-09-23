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
package archimulator.model;

import archimulator.sim.core.bpred.BranchPredictorType;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MainMemoryType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import net.pickapack.StorageUnit;
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;
import net.pickapack.util.StorageUnitHelper;

import java.util.Date;

@DatabaseTable(tableName = "Architecture")
public class Architecture implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private int helperThreadPthreadSpawnIndex;

    @DatabaseField
    private boolean helperThreadL2CacheRequestProfilingEnabled;

    @DatabaseField
    private int numCores;

    @DatabaseField
    private int numThreadsPerCore;

    @DatabaseField
    private int physicalRegisterFileCapacity;

    @DatabaseField
    private int decodeWidth;

    @DatabaseField
    private int issueWidth;

    @DatabaseField
    private int commitWidth;

    @DatabaseField
    private int decodeBufferCapacity;

    @DatabaseField
    private int reorderBufferCapacity;

    @DatabaseField
    private int loadStoreQueueCapacity;

    @DatabaseField
    private BranchPredictorType branchPredictorType;

    @DatabaseField
    private int twoBitBranchPredictorBimodSize;

    @DatabaseField
    private int twoBitBranchPredictorBranchTargetBufferNumSets;

    @DatabaseField
    private int twoBitBranchPredictorBranchTargetBufferAssociativity;

    @DatabaseField
    private int twoBitBranchPredictorReturnAddressStackSize;

    @DatabaseField
    private int twoLevelBranchPredictorL1Size;

    @DatabaseField
    private int twoLevelBranchPredictorL2Size;

    @DatabaseField
    private int twoLevelBranchPredictorShiftWidth;

    @DatabaseField
    private boolean twoLevelBranchPredictorXor;

    @DatabaseField
    private int twoLevelBranchPredictorBranchTargetBufferNumSets;

    @DatabaseField
    private int twoLevelBranchPredictorBranchTargetBufferAssociativity;

    @DatabaseField
    private int twoLevelBranchPredictorReturnAddressStackSize;

    @DatabaseField
    private int combinedBranchPredictorBimodSize;

    @DatabaseField
    private int combinedBranchPredictorL1Size;

    @DatabaseField
    private int combinedBranchPredictorL2Size;

    @DatabaseField
    private int combinedBranchPredictorMetaSize;

    @DatabaseField
    private int combinedBranchPredictorShiftWidth;

    @DatabaseField
    private boolean combinedBranchPredictorXor;

    @DatabaseField
    private int combinedBranchPredictorBranchTargetBufferNumSets;

    @DatabaseField
    private int combinedBranchPredictorBranchTargetBufferAssociativity;

    @DatabaseField
    private int combinedBranchPredictorReturnAddressStackSize;

    @DatabaseField
    private int tlbSize;

    @DatabaseField
    private int tlbAssociativity;

    @DatabaseField
    private int tlbLineSize;

    @DatabaseField
    private int tlbHitLatency;

    @DatabaseField
    private int tlbMissLatency;

    @DatabaseField
    private int l1ISize;

    @DatabaseField
    private int l1IAssociativity;

    @DatabaseField
    private int l1ILineSize;

    @DatabaseField
    private int l1IHitLatency;

    @DatabaseField
    private int l1INumReadPorts;

    @DatabaseField
    private int l1INumWritePorts;

    @DatabaseField
    private CacheReplacementPolicyType l1IReplacementPolicyType;

    @DatabaseField
    private int l1DSize;

    @DatabaseField
    private int l1DAssociativity;

    @DatabaseField
    private int l1DLineSize;

    @DatabaseField
    private int l1DHitLatency;

    @DatabaseField
    private int l1DNumReadPorts;

    @DatabaseField
    private int l1DNumWritePorts;

    @DatabaseField
    private CacheReplacementPolicyType l1DReplacementPolicyType;

    @DatabaseField
    private int l2Size;

    @DatabaseField
    private int l2Associativity;

    @DatabaseField
    private int l2LineSize;

    @DatabaseField
    private int l2HitLatency;

    @DatabaseField
    private CacheReplacementPolicyType l2ReplacementPolicyType;

    @DatabaseField
    private MainMemoryType mainMemoryType;

    @DatabaseField
    private int mainMemoryLineSize;

    @DatabaseField
    private int fixedLatencyMainMemoryLatency;

    @DatabaseField
    private int simpleMainMemoryMemoryLatency;

    @DatabaseField
    private int simpleMainMemoryMemoryTrunkLatency;

    @DatabaseField
    private int simpleMainMemoryBusWidth;

    @DatabaseField
    private int basicMainMemoryToDramLatency;

    @DatabaseField
    private int basicMainMemoryFromDramLatency;

    @DatabaseField
    private int basicMainMemoryPrechargeLatency;

    @DatabaseField
    private int basicMainMemoryClosedLatency;

    @DatabaseField
    private int basicMainMemoryConflictLatency;

    @DatabaseField
    private int basicMainMemoryBusWidth;

    @DatabaseField
    private int basicMainMemoryNumBanks;

    @DatabaseField
    private int basicMainMemoryRowSize;

    public Architecture() {
    }

    public Architecture(boolean htLLCRequestProfilingEnabled, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.createTime = DateHelper.toTick(new Date());

        this.helperThreadPthreadSpawnIndex = 3720;
        this.helperThreadL2CacheRequestProfilingEnabled = htLLCRequestProfilingEnabled;

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

        this.mainMemoryType = MainMemoryType.FIXED_LATENCY;
        this.mainMemoryLineSize = 64;

        this.fixedLatencyMainMemoryLatency = 200;

        this.simpleMainMemoryMemoryLatency = 200;
        this.simpleMainMemoryMemoryTrunkLatency = 2;
        this.simpleMainMemoryBusWidth = 4;

        this.basicMainMemoryToDramLatency = 6;
        this.basicMainMemoryFromDramLatency = 12;
        this.basicMainMemoryPrechargeLatency = 90;
        this.basicMainMemoryClosedLatency = 90;
        this.basicMainMemoryConflictLatency = 90;
        this.basicMainMemoryBusWidth = 4;
        this.basicMainMemoryNumBanks = 8;
        this.basicMainMemoryRowSize = 2048;
    }

    public void updateTitle() {
        this.title = "C" + this.numCores + "T" + this.numThreadsPerCore
                + "-" + "L1I_" + this.l1ISize / 1024 + "KB" + "_" + "Assoc" + this.l1IAssociativity //TODO: use StorageUnit.toString(..) instead
                + "-" + "l1D_" + this.l1DSize / 1024 + "KB" + "_" + "Assoc" + this.l1DAssociativity //TODO: use StorageUnit.toString(..) instead
                + "-" + "L2_" + this.l2Size / 1024 + "KB" + "_" + "Assoc" + this.l2Associativity //TODO: use StorageUnit.toString(..) instead
                + "_" + this.l2ReplacementPolicyType;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getParentId() {
        return -1;
    }

    public String getTitle() {
        if(title == null) {
            updateTitle();
        }

        return title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    public int getHelperThreadPthreadSpawnIndex() {
        return helperThreadPthreadSpawnIndex;
    }

    public boolean getHelperThreadL2CacheRequestProfilingEnabled() {
        return helperThreadL2CacheRequestProfilingEnabled;
    }

    public int getNumCores() {
        return numCores;
    }

    public int getNumThreadsPerCore() {
        return numThreadsPerCore;
    }

    public int getPhysicalRegisterFileCapacity() {
        return physicalRegisterFileCapacity;
    }

    public int getDecodeWidth() {
        return decodeWidth;
    }

    public int getIssueWidth() {
        return issueWidth;
    }

    public int getCommitWidth() {
        return commitWidth;
    }

    public int getDecodeBufferCapacity() {
        return decodeBufferCapacity;
    }

    public int getReorderBufferCapacity() {
        return reorderBufferCapacity;
    }

    public int getLoadStoreQueueCapacity() {
        return loadStoreQueueCapacity;
    }

    public BranchPredictorType getBranchPredictorType() {
        return branchPredictorType;
    }

    public int getTwoBitBranchPredictorBimodSize() {
        return twoBitBranchPredictorBimodSize;
    }

    public int getTwoBitBranchPredictorBranchTargetBufferNumSets() {
        return twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    public int getTwoBitBranchPredictorBranchTargetBufferAssociativity() {
        return twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getTwoBitBranchPredictorReturnAddressStackSize() {
        return twoBitBranchPredictorReturnAddressStackSize;
    }

    public int getTwoLevelBranchPredictorL1Size() {
        return twoLevelBranchPredictorL1Size;
    }

    public int getTwoLevelBranchPredictorL2Size() {
        return twoLevelBranchPredictorL2Size;
    }

    public int getTwoLevelBranchPredictorShiftWidth() {
        return twoLevelBranchPredictorShiftWidth;
    }

    public boolean getTwoLevelBranchPredictorXor() {
        return twoLevelBranchPredictorXor;
    }

    public int getTwoLevelBranchPredictorBranchTargetBufferNumSets() {
        return twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    public int getTwoLevelBranchPredictorBranchTargetBufferAssociativity() {
        return twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getTwoLevelBranchPredictorReturnAddressStackSize() {
        return twoLevelBranchPredictorReturnAddressStackSize;
    }

    public int getCombinedBranchPredictorBimodSize() {
        return combinedBranchPredictorBimodSize;
    }

    public int getCombinedBranchPredictorL1Size() {
        return combinedBranchPredictorL1Size;
    }

    public int getCombinedBranchPredictorL2Size() {
        return combinedBranchPredictorL2Size;
    }

    public int getCombinedBranchPredictorMetaSize() {
        return combinedBranchPredictorMetaSize;
    }

    public int getCombinedBranchPredictorShiftWidth() {
        return combinedBranchPredictorShiftWidth;
    }

    public boolean getCombinedBranchPredictorXor() {
        return combinedBranchPredictorXor;
    }

    public int getCombinedBranchPredictorBranchTargetBufferNumSets() {
        return combinedBranchPredictorBranchTargetBufferNumSets;
    }

    public int getCombinedBranchPredictorBranchTargetBufferAssociativity() {
        return combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    public int getCombinedBranchPredictorReturnAddressStackSize() {
        return combinedBranchPredictorReturnAddressStackSize;
    }

    public int getTlbSize() {
        return tlbSize;
    }

    public int getTlbAssociativity() {
        return tlbAssociativity;
    }

    public int getTlbLineSize() {
        return tlbLineSize;
    }

    public int getTlbHitLatency() {
        return tlbHitLatency;
    }

    public int getTlbMissLatency() {
        return tlbMissLatency;
    }

    public int getL1ISize() {
        return l1ISize;
    }

    public int getL1IAssociativity() {
        return l1IAssociativity;
    }

    public int getL1ILineSize() {
        return l1ILineSize;
    }

    public int getL1IHitLatency() {
        return l1IHitLatency;
    }

    public int getL1INumReadPorts() {
        return l1INumReadPorts;
    }

    public int getL1INumWritePorts() {
        return l1INumWritePorts;
    }

    public CacheReplacementPolicyType getL1IReplacementPolicyType() {
        return l1IReplacementPolicyType;
    }

    public int getL1DSize() {
        return l1DSize;
    }

    public int getL1DAssociativity() {
        return l1DAssociativity;
    }

    public int getL1DLineSize() {
        return l1DLineSize;
    }

    public int getL1DHitLatency() {
        return l1DHitLatency;
    }

    public int getL1DNumReadPorts() {
        return l1DNumReadPorts;
    }

    public int getL1DNumWritePorts() {
        return l1DNumWritePorts;
    }

    public CacheReplacementPolicyType getL1DReplacementPolicyType() {
        return l1DReplacementPolicyType;
    }

    public int getL2Size() {
        return l2Size;
    }

    public int getL2Associativity() {
        return l2Associativity;
    }

    public int getL2LineSize() {
        return l2LineSize;
    }

    public int getL2HitLatency() {
        return l2HitLatency;
    }

    public CacheReplacementPolicyType getL2ReplacementPolicyType() {
        return l2ReplacementPolicyType;
    }

    public MainMemoryType getMainMemoryType() {
        return mainMemoryType;
    }

    public int getMainMemoryLineSize() {
        return mainMemoryLineSize;
    }

    public int getFixedLatencyMainMemoryLatency() {
        return fixedLatencyMainMemoryLatency;
    }

    public int getSimpleMainMemoryMemoryLatency() {
        return simpleMainMemoryMemoryLatency;
    }

    public int getSimpleMainMemoryBusWidth() {
        return simpleMainMemoryBusWidth;
    }

    public int getBasicMainMemoryToDramLatency() {
        return basicMainMemoryToDramLatency;
    }

    public int getBasicMainMemoryFromDramLatency() {
        return basicMainMemoryFromDramLatency;
    }

    public int getBasicMainMemoryPrechargeLatency() {
        return basicMainMemoryPrechargeLatency;
    }

    public int getBasicMainMemoryClosedLatency() {
        return basicMainMemoryClosedLatency;
    }

    public int getBasicMainMemoryConflictLatency() {
        return basicMainMemoryConflictLatency;
    }

    public int getBasicMainMemoryBusWidth() {
        return basicMainMemoryBusWidth;
    }

    public int getBasicMainMemoryNumBanks() {
        return basicMainMemoryNumBanks;
    }

    public int getBasicMainMemoryRowSize() {
        return basicMainMemoryRowSize;
    }

    public void setHelperThreadPthreadSpawnIndex(int helperThreadPthreadSpawnIndex) {
        this.helperThreadPthreadSpawnIndex = helperThreadPthreadSpawnIndex;
    }

    public void setHelperThreadL2CacheRequestProfilingEnabled(boolean helperThreadL2CacheRequestProfilingEnabled) {
        this.helperThreadL2CacheRequestProfilingEnabled = helperThreadL2CacheRequestProfilingEnabled;
    }

    public void setNumCores(int numCores) {
        this.numCores = numCores;
    }

    public void setNumThreadsPerCore(int numThreadsPerCore) {
        this.numThreadsPerCore = numThreadsPerCore;
    }

    public void setPhysicalRegisterFileCapacity(int physicalRegisterFileCapacity) {
        this.physicalRegisterFileCapacity = physicalRegisterFileCapacity;
    }

    public void setDecodeWidth(int decodeWidth) {
        this.decodeWidth = decodeWidth;
    }

    public void setIssueWidth(int issueWidth) {
        this.issueWidth = issueWidth;
    }

    public void setCommitWidth(int commitWidth) {
        this.commitWidth = commitWidth;
    }

    public void setDecodeBufferCapacity(int decodeBufferCapacity) {
        this.decodeBufferCapacity = decodeBufferCapacity;
    }

    public void setReorderBufferCapacity(int reorderBufferCapacity) {
        this.reorderBufferCapacity = reorderBufferCapacity;
    }

    public void setLoadStoreQueueCapacity(int loadStoreQueueCapacity) {
        this.loadStoreQueueCapacity = loadStoreQueueCapacity;
    }

    public void setBranchPredictorType(BranchPredictorType branchPredictorType) {
        this.branchPredictorType = branchPredictorType;
    }

    public void setTwoBitBranchPredictorBimodSize(int twoBitBranchPredictorBimodSize) {
        this.twoBitBranchPredictorBimodSize = twoBitBranchPredictorBimodSize;
    }

    public void setTwoBitBranchPredictorBranchTargetBufferNumSets(int twoBitBranchPredictorBranchTargetBufferNumSets) {
        this.twoBitBranchPredictorBranchTargetBufferNumSets = twoBitBranchPredictorBranchTargetBufferNumSets;
    }

    public void setTwoBitBranchPredictorBranchTargetBufferAssociativity(int twoBitBranchPredictorBranchTargetBufferAssociativity) {
        this.twoBitBranchPredictorBranchTargetBufferAssociativity = twoBitBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setTwoBitBranchPredictorReturnAddressStackSize(int twoBitBranchPredictorReturnAddressStackSize) {
        this.twoBitBranchPredictorReturnAddressStackSize = twoBitBranchPredictorReturnAddressStackSize;
    }

    public void setTwoLevelBranchPredictorL1Size(int twoLevelBranchPredictorL1Size) {
        this.twoLevelBranchPredictorL1Size = twoLevelBranchPredictorL1Size;
    }

    public void setTwoLevelBranchPredictorL2Size(int twoLevelBranchPredictorL2Size) {
        this.twoLevelBranchPredictorL2Size = twoLevelBranchPredictorL2Size;
    }

    public void setTwoLevelBranchPredictorShiftWidth(int twoLevelBranchPredictorShiftWidth) {
        this.twoLevelBranchPredictorShiftWidth = twoLevelBranchPredictorShiftWidth;
    }

    public void setTwoLevelBranchPredictorXor(boolean twoLevelBranchPredictorXor) {
        this.twoLevelBranchPredictorXor = twoLevelBranchPredictorXor;
    }

    public void setTwoLevelBranchPredictorBranchTargetBufferNumSets(int twoLevelBranchPredictorBranchTargetBufferNumSets) {
        this.twoLevelBranchPredictorBranchTargetBufferNumSets = twoLevelBranchPredictorBranchTargetBufferNumSets;
    }

    public void setTwoLevelBranchPredictorBranchTargetBufferAssociativity(int twoLevelBranchPredictorBranchTargetBufferAssociativity) {
        this.twoLevelBranchPredictorBranchTargetBufferAssociativity = twoLevelBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setTwoLevelBranchPredictorReturnAddressStackSize(int twoLevelBranchPredictorReturnAddressStackSize) {
        this.twoLevelBranchPredictorReturnAddressStackSize = twoLevelBranchPredictorReturnAddressStackSize;
    }

    public void setCombinedBranchPredictorBimodSize(int combinedBranchPredictorBimodSize) {
        this.combinedBranchPredictorBimodSize = combinedBranchPredictorBimodSize;
    }

    public void setCombinedBranchPredictorL1Size(int combinedBranchPredictorL1Size) {
        this.combinedBranchPredictorL1Size = combinedBranchPredictorL1Size;
    }

    public void setCombinedBranchPredictorL2Size(int combinedBranchPredictorL2Size) {
        this.combinedBranchPredictorL2Size = combinedBranchPredictorL2Size;
    }

    public void setCombinedBranchPredictorMetaSize(int combinedBranchPredictorMetaSize) {
        this.combinedBranchPredictorMetaSize = combinedBranchPredictorMetaSize;
    }

    public void setCombinedBranchPredictorShiftWidth(int combinedBranchPredictorShiftWidth) {
        this.combinedBranchPredictorShiftWidth = combinedBranchPredictorShiftWidth;
    }

    public void setCombinedBranchPredictorXor(boolean combinedBranchPredictorXor) {
        this.combinedBranchPredictorXor = combinedBranchPredictorXor;
    }

    public void setCombinedBranchPredictorBranchTargetBufferNumSets(int combinedBranchPredictorBranchTargetBufferNumSets) {
        this.combinedBranchPredictorBranchTargetBufferNumSets = combinedBranchPredictorBranchTargetBufferNumSets;
    }

    public void setCombinedBranchPredictorBranchTargetBufferAssociativity(int combinedBranchPredictorBranchTargetBufferAssociativity) {
        this.combinedBranchPredictorBranchTargetBufferAssociativity = combinedBranchPredictorBranchTargetBufferAssociativity;
    }

    public void setCombinedBranchPredictorReturnAddressStackSize(int combinedBranchPredictorReturnAddressStackSize) {
        this.combinedBranchPredictorReturnAddressStackSize = combinedBranchPredictorReturnAddressStackSize;
    }

    public void setTlbSize(int tlbSize) {
        this.tlbSize = tlbSize;
    }

    public void setTlbAssociativity(int tlbAssociativity) {
        this.tlbAssociativity = tlbAssociativity;
    }

    public void setTlbLineSize(int tlbLineSize) {
        this.tlbLineSize = tlbLineSize;
    }

    public void setTlbHitLatency(int tlbHitLatency) {
        this.tlbHitLatency = tlbHitLatency;
    }

    public void setTlbMissLatency(int tlbMissLatency) {
        this.tlbMissLatency = tlbMissLatency;
    }

    public void setL1ISize(int l1ISize) {
        this.l1ISize = l1ISize;
    }

    public void setL1IAssociativity(int l1IAssociativity) {
        this.l1IAssociativity = l1IAssociativity;
    }

    public void setL1ILineSize(int l1ILineSize) {
        this.l1ILineSize = l1ILineSize;
    }

    public void setL1IHitLatency(int l1IHitLatency) {
        this.l1IHitLatency = l1IHitLatency;
    }

    public void setL1INumReadPorts(int l1INumReadPorts) {
        this.l1INumReadPorts = l1INumReadPorts;
    }

    public void setL1INumWritePorts(int l1INumWritePorts) {
        this.l1INumWritePorts = l1INumWritePorts;
    }

    public void setL1IReplacementPolicyType(CacheReplacementPolicyType l1IReplacementPolicyType) {
        this.l1IReplacementPolicyType = l1IReplacementPolicyType;
    }

    public void setL1DSize(int l1DSize) {
        this.l1DSize = l1DSize;
    }

    public void setL1DAssociativity(int l1DAssociativity) {
        this.l1DAssociativity = l1DAssociativity;
    }

    public void setL1DLineSize(int l1DLineSize) {
        this.l1DLineSize = l1DLineSize;
    }

    public void setL1DHitLatency(int l1DHitLatency) {
        this.l1DHitLatency = l1DHitLatency;
    }

    public void setL1DNumReadPorts(int l1DNumReadPorts) {
        this.l1DNumReadPorts = l1DNumReadPorts;
    }

    public void setL1DNumWritePorts(int l1DNumWritePorts) {
        this.l1DNumWritePorts = l1DNumWritePorts;
    }

    public void setL1DReplacementPolicyType(CacheReplacementPolicyType l1DReplacementPolicyType) {
        this.l1DReplacementPolicyType = l1DReplacementPolicyType;
    }

    public void setL2Size(int l2Size) {
        this.l2Size = l2Size;
    }

    public void setL2Associativity(int l2Associativity) {
        this.l2Associativity = l2Associativity;
    }

    public void setL2LineSize(int l2LineSize) {
        this.l2LineSize = l2LineSize;
    }

    public void setL2HitLatency(int l2HitLatency) {
        this.l2HitLatency = l2HitLatency;
    }

    public void setL2ReplacementPolicyType(CacheReplacementPolicyType l2ReplacementPolicyType) {
        this.l2ReplacementPolicyType = l2ReplacementPolicyType;
    }

    public void setMainMemoryType(MainMemoryType mainMemoryType) {
        this.mainMemoryType = mainMemoryType;
    }

    public void setMainMemoryLineSize(int mainMemoryLineSize) {
        this.mainMemoryLineSize = mainMemoryLineSize;
    }

    public void setFixedLatencyMainMemoryLatency(int fixedLatencyMainMemoryLatency) {
        this.fixedLatencyMainMemoryLatency = fixedLatencyMainMemoryLatency;
    }

    public void setSimpleMainMemoryMemoryLatency(int simpleMainMemoryMemoryLatency) {
        this.simpleMainMemoryMemoryLatency = simpleMainMemoryMemoryLatency;
    }

    public int getSimpleMainMemoryMemoryTrunkLatency() {
        return simpleMainMemoryMemoryTrunkLatency;
    }

    public void setSimpleMainMemoryMemoryTrunkLatency(int simpleMainMemoryMemoryTrunkLatency) {
        this.simpleMainMemoryMemoryTrunkLatency = simpleMainMemoryMemoryTrunkLatency;
    }

    public void setSimpleMainMemoryBusWidth(int simpleMainMemoryBusWidth) {
        this.simpleMainMemoryBusWidth = simpleMainMemoryBusWidth;
    }

    public void setBasicMainMemoryToDramLatency(int basicMainMemoryToDramLatency) {
        this.basicMainMemoryToDramLatency = basicMainMemoryToDramLatency;
    }

    public void setBasicMainMemoryFromDramLatency(int basicMainMemoryFromDramLatency) {
        this.basicMainMemoryFromDramLatency = basicMainMemoryFromDramLatency;
    }

    public void setBasicMainMemoryPrechargeLatency(int basicMainMemoryPrechargeLatency) {
        this.basicMainMemoryPrechargeLatency = basicMainMemoryPrechargeLatency;
    }

    public void setBasicMainMemoryClosedLatency(int basicMainMemoryClosedLatency) {
        this.basicMainMemoryClosedLatency = basicMainMemoryClosedLatency;
    }

    public void setBasicMainMemoryConflictLatency(int basicMainMemoryConflictLatency) {
        this.basicMainMemoryConflictLatency = basicMainMemoryConflictLatency;
    }

    public void setBasicMainMemoryBusWidth(int basicMainMemoryBusWidth) {
        this.basicMainMemoryBusWidth = basicMainMemoryBusWidth;
    }

    public void setBasicMainMemoryNumBanks(int basicMainMemoryNumBanks) {
        this.basicMainMemoryNumBanks = basicMainMemoryNumBanks;
    }

    public void setBasicMainMemoryRowSize(int basicMainMemoryRowSize) {
        this.basicMainMemoryRowSize = basicMainMemoryRowSize;
    }

    public String getL1ISizeInStorageUnit() {
        return StorageUnit.toString(l1ISize);
    }

    public void setL1ISizeInStorageUnit(String l1ISizeInStorageUnit) {
        this.l1ISize = (int) StorageUnitHelper.displaySizeToByteCount(l1ISizeInStorageUnit);
    }

    public String getL1DSizeInStorageUnit() {
        return StorageUnit.toString(l1DSize);
    }

    public void setL1DSizeInStorageUnit(String l1DSizeInStorageUnit) {
        this.l1DSize = (int) StorageUnitHelper.displaySizeToByteCount(l1DSizeInStorageUnit);
    }

    public String getL2SizeInStorageUnit() {
        return StorageUnit.toString(l2Size);
    }

    public void setL2SizeInStorageUnit(String l2SizeInStorageUnit) {
        this.l2Size = (int) StorageUnitHelper.displaySizeToByteCount(l2SizeInStorageUnit);
    }

    @Override
    public String toString() {
        return title;
    }
}