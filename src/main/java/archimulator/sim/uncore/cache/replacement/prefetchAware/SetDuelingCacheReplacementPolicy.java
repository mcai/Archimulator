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
package archimulator.sim.uncore.cache.replacement.prefetchAware;

import archimulator.sim.common.report.ReportNode;
import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.CacheAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.MainThreadL2MissBasedSetDuelingUnit;
import archimulator.sim.uncore.cache.SetDuelingUnit;
import archimulator.sim.uncore.cache.replacement.CacheReplacementPolicy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Set dueling based policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class SetDuelingCacheReplacementPolicy<StateT extends Serializable> extends CacheReplacementPolicy<StateT> {
    private SetDuelingUnit setDuelingUnit;
    private List<CacheReplacementPolicy<StateT>> policies;

    /**
     * Create a set dueling based policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public SetDuelingCacheReplacementPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT>... policies) {
        super(cache);

        this.policies = Arrays.asList(policies);

        this.setDuelingUnit = new MainThreadL2MissBasedSetDuelingUnit(cache, cache.getExperiment().getArchitecture().getNumCores(), this.policies.size(), 2);
    }

    @Override
    public CacheAccess<StateT> handleReplacement(MemoryHierarchyAccess access, int set, int tag) {
        return this.getPolicy(this.setDuelingUnit.getPolicyId(set, access.getThread().getCore().getNum())).handleReplacement(access, set, tag);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        this.getPolicy(this.setDuelingUnit.getPolicyId(set, access.getThread().getCore().getNum())).handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        this.getPolicy(this.setDuelingUnit.getPolicyId(set, access.getThread().getCore().getNum())).handleInsertionOnMiss(access, set, way);
    }

    private CacheReplacementPolicy<StateT> getPolicy(int policyType) {
        return this.policies.get(policyType);
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }
}
