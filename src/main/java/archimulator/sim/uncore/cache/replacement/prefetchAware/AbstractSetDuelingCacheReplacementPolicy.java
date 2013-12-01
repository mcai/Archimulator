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
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.MainThreadL2MissBasedSetDuelingUnit;
import archimulator.sim.uncore.cache.SetDuelingUnit;
import archimulator.sim.uncore.cache.replacement.AbstractCacheReplacementPolicy;
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
public class AbstractSetDuelingCacheReplacementPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> implements SetDuelingCacheReplacementPolicy<StateT> {
    private SetDuelingUnit setDuelingUnit;
    private List<CacheReplacementPolicy<StateT>> policies;

    /**
     * Create a set dueling based policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public AbstractSetDuelingCacheReplacementPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT>... policies) {
        super(cache);

        this.policies = Arrays.asList(policies);

        this.setDuelingUnit = new MainThreadL2MissBasedSetDuelingUnit(cache, cache.getExperiment().getArchitecture().getNumCores(), this.policies.size(), 2);
    }

    /**
     * Get the cache replacement policy for the specified access and set index.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @return the cache replacement policy for the specified access and set index
     */
    public CacheReplacementPolicy<StateT> getPolicy(MemoryHierarchyAccess access, int set) {
        return this.policies.get(this.setDuelingUnit.getPolicyId(set, access.getThread().getCore().getNum()));
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Get the set dueling unit.
     *
     * @return the set dueling unit
     */
    public SetDuelingUnit getSetDuelingUnit() {
        return setDuelingUnit;
    }
}