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
import archimulator.sim.core.bpred.BranchPredictorType;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import archimulator.sim.uncore.dram.MainMemoryType;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.ModelElement;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

public class ArchitectureServiceImpl extends AbstractService implements ArchitectureService {
    private Dao<Architecture, Long> architectures;

    @SuppressWarnings("unchecked")
    public ArchitectureServiceImpl() {
        super(ServiceManager.DATABASE_URL, Arrays.<Class<? extends ModelElement>>asList(Architecture.class));

        this.architectures = createDao(Architecture.class);

        this.getOrAddArchitecture(true, 2, 2, 32 * 1024, 8, 32 * 1024, 8, 4 * 1024 * 1024, 16, CacheReplacementPolicyType.LRU);
    }

    @Override
    public List<Architecture> getAllArchitectures() {
        return this.getAllItems(this.architectures);
    }

    @Override
    public Architecture getArchitectureById(long id) {
        return this.getItemById(this.architectures, id);
    }

    @Override
    public Architecture getArchitectureByTitle(String title) {
        return this.getFirstItemByTitle(this.architectures, title);
    }

    @Override
    public Architecture getFirstArchitecture() {
        return this.getFirstItem(this.architectures);
    }

    @Override
    public long addArchitecture(Architecture architecture) {
        return this.addItem(this.architectures, Architecture.class, architecture);
    }

    @Override
    public void removeArchitectureById(long id) {
        this.removeItemById(this.architectures, Architecture.class, id);
    }

    @Override
    public void clearArchitectures() {
        this.clearItems(this.architectures, Architecture.class);
    }

    @Override
    public void updateArchitecture(Architecture architecture) {
        this.updateItem(this.architectures, Architecture.class, architecture);
    }

    @Override
    public Architecture getOrAddArchitecture(boolean htLLCRequestProfilingEnabled, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssoc, int l1DSize, int l1DAssoc, int l2Size, int l2Assoc, CacheReplacementPolicyType l2ReplacementPolicyType) {
        String title = "C" + numCores + "T" + numThreadsPerCore
                + "-" + "L1I_" + l1ISize / 1024 + "KB" + "_" + "Assoc" + l1IAssoc //TODO: use StorageUnit.toString(..) instead
                + "-" + "l1D_" + l1DSize / 1024 + "KB" + "_" + "Assoc" + l1DAssoc //TODO: use StorageUnit.toString(..) instead
                + "-" + "L2_" + l2Size / 1024 + "KB" + "_" + "Assoc" + l2Assoc //TODO: use StorageUnit.toString(..) instead
                + "_" + l2ReplacementPolicyType;

        if (getArchitectureByTitle(title) != null) {
            return getArchitectureByTitle(title);
        }

        Architecture architecture = new Architecture(title);

        architecture.setHelperThreadPthreadSpawnIndex(3720);
        architecture.setHelperThreadL2CacheRequestProfilingEnabled(htLLCRequestProfilingEnabled);

        architecture.setNumCores(numCores);
        architecture.setNumThreadsPerCore(numThreadsPerCore);

        architecture.setPhysicalRegisterFileCapacity(128);
        architecture.setDecodeWidth(4);
        architecture.setIssueWidth(4);
        architecture.setCommitWidth(4);
        architecture.setDecodeBufferCapacity(96);
        architecture.setReorderBufferCapacity(96);
        architecture.setLoadStoreQueueCapacity(48);

        architecture.setBranchPredictorType(BranchPredictorType.PERFECT);

        architecture.setTwoBitBranchPredictorBimodSize(2048);
        architecture.setTwoBitBranchPredictorBranchTargetBufferNumSets(512);
        architecture.setTwoBitBranchPredictorBranchTargetBufferAssociativity(4);
        architecture.setTwoBitBranchPredictorReturnAddressStackSize(8);

        architecture.setTwoLevelBranchPredictorL1Size(1);
        architecture.setTwoLevelBranchPredictorL2Size(1024);
        architecture.setTwoLevelBranchPredictorShiftWidth(8);
        architecture.setTwoLevelBranchPredictorXor(false);
        architecture.setTwoLevelBranchPredictorBranchTargetBufferNumSets(512);
        architecture.setTwoLevelBranchPredictorBranchTargetBufferAssociativity(4);
        architecture.setTwoLevelBranchPredictorReturnAddressStackSize(8);

        architecture.setCombinedBranchPredictorBimodSize(2048);
        architecture.setCombinedBranchPredictorL1Size(1);
        architecture.setCombinedBranchPredictorL2Size(1024);
        architecture.setCombinedBranchPredictorMetaSize(1024);
        architecture.setCombinedBranchPredictorShiftWidth(8);
        architecture.setCombinedBranchPredictorXor(false);
        architecture.setCombinedBranchPredictorBranchTargetBufferNumSets(512);
        architecture.setCombinedBranchPredictorBranchTargetBufferAssociativity(4);
        architecture.setCombinedBranchPredictorReturnAddressStackSize(8);

        architecture.setTlbSize(32768);
        architecture.setTlbAssociativity(4);
        architecture.setTlbLineSize(64);
        architecture.setTlbHitLatency(2);
        architecture.setTlbMissLatency(30);

        architecture.setL1ISize(l1ISize);
        architecture.setL1IAssociativity(l1IAssoc);
        architecture.setL1ILineSize(64);
        architecture.setL1IHitLatency(1);
        architecture.setL1INumReadPorts(128);
        architecture.setL1INumWritePorts(128);
        architecture.setL1IReplacementPolicyType(CacheReplacementPolicyType.LRU);

        architecture.setL1DSize(l1DSize);
        architecture.setL1DAssociativity(l1DAssoc);
        architecture.setL1DLineSize(64);
        architecture.setL1DHitLatency(1);
        architecture.setL1DNumReadPorts(128);
        architecture.setL1DNumWritePorts(128);
        architecture.setL1DReplacementPolicyType(CacheReplacementPolicyType.LRU);

        architecture.setL2Size(l2Size);
        architecture.setL2Associativity(l2Assoc);
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

        addArchitecture(architecture);

        return architecture;
    }
}
