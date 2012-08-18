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
import net.pickapack.dateTime.DateHelper;
import net.pickapack.model.ModelElement;

import java.util.Date;

@DatabaseTable(tableName = "Architecture")
public class Architecture implements ModelElement {
    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField(index = true)
    private String title;

    @DatabaseField
    private long createTime;

    @DatabaseField
    private int htPthreadSpawnIndex;

    @DatabaseField
    private boolean htLLCRequestProfilingEnabled;

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
    private BranchPredictorType bpredType;

    @DatabaseField
    private int twoBitBpredBimodSize;

    @DatabaseField
    private int twoBitBpredBtbSets;

    @DatabaseField
    private int twoBitBpredBtbAssoc;

    @DatabaseField
    private int twoBitBpredRetStackSize;

    @DatabaseField
    private int twoLevelBpredL1Size;

    @DatabaseField
    private int twoLevelBpredL2Size;

    @DatabaseField
    private int twoLevelBpredShiftWidth;

    @DatabaseField
    private boolean twoLevelBpredXor;

    @DatabaseField
    private int twoLevelBpredBtbSets;

    @DatabaseField
    private int twoLevelBpredBtbAssoc;

    @DatabaseField
    private int twoLevelBpredRetStackSize;

    @DatabaseField
    private int combinedBpredBimodSize;

    @DatabaseField
    private int combinedBpredL1Size;

    @DatabaseField
    private int combinedBpredL2Size;

    @DatabaseField
    private int combinedBpredMetaSize;

    @DatabaseField
    private int combinedBpredShiftWidth;

    @DatabaseField
    private boolean combinedBpredXor;

    @DatabaseField
    private int combinedBpredBtbSets;

    @DatabaseField
    private int combinedBpredBtbAssoc;

    @DatabaseField
    private int combinedBpredBtbRetStackSize;

    @DatabaseField
    private int tlbSize;

    @DatabaseField
    private int tlbAssoc;

    @DatabaseField
    private int tlbLineSize;

    @DatabaseField
    private int tlbHitLatency;

    @DatabaseField
    private int tlbMissLatency;

    @DatabaseField
    private int l1ISize;

    @DatabaseField
    private int l1IAssoc;

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
    private int l1DAssoc;

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
    private int l2Assoc;

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

    public Architecture(String title) {
        this.title = title;
        this.createTime = DateHelper.toTick(new Date());
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
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getCreateTimeAsString() {
        return DateHelper.toString(createTime);
    }

    public int getHtPthreadSpawnIndex() {
        return htPthreadSpawnIndex;
    }

    public boolean getHtLLCRequestProfilingEnabled() {
        return htLLCRequestProfilingEnabled;
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

    public BranchPredictorType getBpredType() {
        return bpredType;
    }

    public int getTwoBitBpredBimodSize() {
        return twoBitBpredBimodSize;
    }

    public int getTwoBitBpredBtbSets() {
        return twoBitBpredBtbSets;
    }

    public int getTwoBitBpredBtbAssoc() {
        return twoBitBpredBtbAssoc;
    }

    public int getTwoBitBpredRetStackSize() {
        return twoBitBpredRetStackSize;
    }

    public int getTwoLevelBpredL1Size() {
        return twoLevelBpredL1Size;
    }

    public int getTwoLevelBpredL2Size() {
        return twoLevelBpredL2Size;
    }

    public int getTwoLevelBpredShiftWidth() {
        return twoLevelBpredShiftWidth;
    }

    public boolean isTwoLevelBpredXor() {
        return twoLevelBpredXor;
    }

    public int getTwoLevelBpredBtbSets() {
        return twoLevelBpredBtbSets;
    }

    public int getTwoLevelBpredBtbAssoc() {
        return twoLevelBpredBtbAssoc;
    }

    public int getTwoLevelBpredRetStackSize() {
        return twoLevelBpredRetStackSize;
    }

    public int getCombinedBpredBimodSize() {
        return combinedBpredBimodSize;
    }

    public int getCombinedBpredL1Size() {
        return combinedBpredL1Size;
    }

    public int getCombinedBpredL2Size() {
        return combinedBpredL2Size;
    }

    public int getCombinedBpredMetaSize() {
        return combinedBpredMetaSize;
    }

    public int getCombinedBpredShiftWidth() {
        return combinedBpredShiftWidth;
    }

    public boolean isCombinedBpredXor() {
        return combinedBpredXor;
    }

    public int getCombinedBpredBtbSets() {
        return combinedBpredBtbSets;
    }

    public int getCombinedBpredBtbAssoc() {
        return combinedBpredBtbAssoc;
    }

    public int getCombinedBpredBtbRetStackSize() {
        return combinedBpredBtbRetStackSize;
    }

    public int getTlbSize() {
        return tlbSize;
    }

    public int getTlbAssoc() {
        return tlbAssoc;
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

    public int getL1IAssoc() {
        return l1IAssoc;
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

    public int getL1DAssoc() {
        return l1DAssoc;
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

    public int getL2Assoc() {
        return l2Assoc;
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

    public String getProcessorPropertiesTitle() {
        return "C" + getNumCores() + "T" + getNumThreadsPerCore()
                + "-" + "L1I_" + getL1ISize() / 1024 + "KB" + "_" + "Assoc" + getL1IAssoc()
                + "-" + "L2_" + getL1DSize() / 1024 + "KB" + "_" + "Assoc" + getL1DAssoc()
                + "-" + "L2_" + getL2Size() / 1024 + "KB" + "_" + "Assoc" + getL2Assoc()
                + "_" + getL2ReplacementPolicyType();
    }

    public void setHtPthreadSpawnIndex(int htPthreadSpawnIndex) {
        this.htPthreadSpawnIndex = htPthreadSpawnIndex;
    }

    public void setHtLLCRequestProfilingEnabled(boolean htLLCRequestProfilingEnabled) {
        this.htLLCRequestProfilingEnabled = htLLCRequestProfilingEnabled;
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

    public void setBpredType(BranchPredictorType bpredType) {
        this.bpredType = bpredType;
    }

    public void setTwoBitBpredBimodSize(int twoBitBpredBimodSize) {
        this.twoBitBpredBimodSize = twoBitBpredBimodSize;
    }

    public void setTwoBitBpredBtbSets(int twoBitBpredBtbSets) {
        this.twoBitBpredBtbSets = twoBitBpredBtbSets;
    }

    public void setTwoBitBpredBtbAssoc(int twoBitBpredBtbAssoc) {
        this.twoBitBpredBtbAssoc = twoBitBpredBtbAssoc;
    }

    public void setTwoBitBpredRetStackSize(int twoBitBpredRetStackSize) {
        this.twoBitBpredRetStackSize = twoBitBpredRetStackSize;
    }

    public void setTwoLevelBpredL1Size(int twoLevelBpredL1Size) {
        this.twoLevelBpredL1Size = twoLevelBpredL1Size;
    }

    public void setTwoLevelBpredL2Size(int twoLevelBpredL2Size) {
        this.twoLevelBpredL2Size = twoLevelBpredL2Size;
    }

    public void setTwoLevelBpredShiftWidth(int twoLevelBpredShiftWidth) {
        this.twoLevelBpredShiftWidth = twoLevelBpredShiftWidth;
    }

    public void setTwoLevelBpredXor(boolean twoLevelBpredXor) {
        this.twoLevelBpredXor = twoLevelBpredXor;
    }

    public void setTwoLevelBpredBtbSets(int twoLevelBpredBtbSets) {
        this.twoLevelBpredBtbSets = twoLevelBpredBtbSets;
    }

    public void setTwoLevelBpredBtbAssoc(int twoLevelBpredBtbAssoc) {
        this.twoLevelBpredBtbAssoc = twoLevelBpredBtbAssoc;
    }

    public void setTwoLevelBpredRetStackSize(int twoLevelBpredRetStackSize) {
        this.twoLevelBpredRetStackSize = twoLevelBpredRetStackSize;
    }

    public void setCombinedBpredBimodSize(int combinedBpredBimodSize) {
        this.combinedBpredBimodSize = combinedBpredBimodSize;
    }

    public void setCombinedBpredL1Size(int combinedBpredL1Size) {
        this.combinedBpredL1Size = combinedBpredL1Size;
    }

    public void setCombinedBpredL2Size(int combinedBpredL2Size) {
        this.combinedBpredL2Size = combinedBpredL2Size;
    }

    public void setCombinedBpredMetaSize(int combinedBpredMetaSize) {
        this.combinedBpredMetaSize = combinedBpredMetaSize;
    }

    public void setCombinedBpredShiftWidth(int combinedBpredShiftWidth) {
        this.combinedBpredShiftWidth = combinedBpredShiftWidth;
    }

    public void setCombinedBpredXor(boolean combinedBpredXor) {
        this.combinedBpredXor = combinedBpredXor;
    }

    public void setCombinedBpredBtbSets(int combinedBpredBtbSets) {
        this.combinedBpredBtbSets = combinedBpredBtbSets;
    }

    public void setCombinedBpredBtbAssoc(int combinedBpredBtbAssoc) {
        this.combinedBpredBtbAssoc = combinedBpredBtbAssoc;
    }

    public void setCombinedBpredBtbRetStackSize(int combinedBpredBtbRetStackSize) {
        this.combinedBpredBtbRetStackSize = combinedBpredBtbRetStackSize;
    }

    public void setTlbSize(int tlbSize) {
        this.tlbSize = tlbSize;
    }

    public void setTlbAssoc(int tlbAssoc) {
        this.tlbAssoc = tlbAssoc;
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

    public void setL1IAssoc(int l1IAssoc) {
        this.l1IAssoc = l1IAssoc;
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

    public void setL1DAssoc(int l1DAssoc) {
        this.l1DAssoc = l1DAssoc;
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

    public void setL2Assoc(int l2Assoc) {
        this.l2Assoc = l2Assoc;
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

    @Override
    public String toString() {
        return title;
    }
}