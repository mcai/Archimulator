/**
 * ****************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
 * <p>
 * This file is part of the Archimulator multicore architectural simulator.
 * <p>
 * Archimulator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Archimulator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Archimulator. If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package archimulator.uncore.cache.replacement.prefetchAware;

import archimulator.common.report.ReportNode;
import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.AbstractCacheReplacementPolicy;
import archimulator.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.uncore.cache.setDueling.AbstractSetDuelingUnit;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract Set dueling based cache replacement policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class AbstractSetDuelingCacheReplacementPolicy<StateT extends Serializable> extends AbstractCacheReplacementPolicy<StateT> implements SetDuelingCacheReplacementPolicy<StateT> {
    private AbstractSetDuelingUnit setDuelingUnit;
    private List<CacheReplacementPolicy<StateT>> policies;

    /**
     * Create a set dueling based cache replacement policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public AbstractSetDuelingCacheReplacementPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT>... policies) {
        super(cache);

        this.policies = Arrays.asList(policies);

        this.setDuelingUnit = this.createSetDuelingUnit(cache, policies.length);
    }

    /**
     * Create an set dueling unit.
     *
     * @param cache the parent evictable cache
     * @param numPolicies the number of policies
     * @return the newly created set dueling unit
     */
    protected abstract AbstractSetDuelingUnit createSetDuelingUnit(EvictableCache<StateT> cache, int numPolicies);

    /**
     * Get the cache replacement policy for the specified access and set index.
     *
     * @param access the memory hierarchy access
     * @param set the set index
     * @return the cache replacement policy for the specified access and set index
     */
    public CacheReplacementPolicy<StateT> getPolicy(MemoryHierarchyAccess access, int set) {
        return this.policies.get(this.setDuelingUnit.getPolicyId(set, 0));
    }

    @Override
    public void dumpStats(ReportNode reportNode) {
    }

    /**
     * Get the set dueling unit.
     *
     * @return the set dueling unit
     */
    public AbstractSetDuelingUnit getSetDuelingUnit() {
        return setDuelingUnit;
    }
}
