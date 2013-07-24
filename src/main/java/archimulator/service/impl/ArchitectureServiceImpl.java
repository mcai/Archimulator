/*******************************************************************************
 * Copyright (c) 2010-2013 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.service.impl;

import archimulator.model.Architecture;
import archimulator.service.ArchitectureService;
import archimulator.service.ServiceManager;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicyType;
import com.j256.ormlite.dao.Dao;
import net.pickapack.model.WithId;
import net.pickapack.service.AbstractService;

import java.util.Arrays;
import java.util.List;

/**
 * Architecture service implementation.
 *
 * @author Min Cai
 */
public class ArchitectureServiceImpl extends AbstractService implements ArchitectureService {
    private Dao<Architecture, Long> architectures;

    /**
     * Create an architecture service implementation.
     */
    @SuppressWarnings("unchecked")
    public ArchitectureServiceImpl() {
        super(ServiceManager.getDatabaseUrl(), Arrays.<Class<? extends WithId>>asList(Architecture.class));

        this.architectures = createDao(Architecture.class);
    }

    @Override
    public void initialize() {
        this.getOrAddArchitecture(false, -1, 2, 2, 32 * 1024, 8, 32 * 1024, 8, 4 * 1024 * 1024, 16, CacheReplacementPolicyType.LRU);
    }

    @Override
    public List<Architecture> getAllArchitectures() {
        return this.getItems(this.architectures);
    }

    @Override
    public List<Architecture> getAllArchitectures(long first, long count) {
        return this.getItems(this.architectures, first, count);
    }

    @Override
    public long getNumAllArchitectures() {
        return this.getNumItems(this.architectures);
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
    public void addArchitecture(Architecture architecture) {
        this.addItem(this.architectures, architecture);
    }

    @Override
    public void removeArchitectureById(long id) {
        this.removeItemById(this.architectures, id);
    }

    @Override
    public void clearArchitectures() {
        this.clearItems(this.architectures);
    }

    @Override
    public void updateArchitecture(Architecture architecture) {
        this.updateItem(this.architectures, architecture);
    }

    @Override
    public Architecture getOrAddArchitecture(boolean dynamicSpeculativePrecomputationEnabled, int numMainThreadWaysInStaticPartitionedLRUPolicy, int numCores, int numThreadsPerCore, int l1ISize, int l1IAssociativity, int l1DSize, int l1DAssociativity, int l2Size, int l2Associativity, CacheReplacementPolicyType l2ReplacementPolicyType) {
        Architecture architecture = new Architecture(dynamicSpeculativePrecomputationEnabled, numMainThreadWaysInStaticPartitionedLRUPolicy, numCores, numThreadsPerCore, l1ISize, l1IAssociativity, l1DSize, l1DAssociativity, l2Size, l2Associativity, l2ReplacementPolicyType);

        Architecture architectureWithSameTitle = getArchitectureByTitle(architecture.getTitle());
        if (architectureWithSameTitle == null) {
            addArchitecture(architecture);
            return architecture;
        }

        return architectureWithSameTitle;
    }
}
