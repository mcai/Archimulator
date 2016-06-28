/**
 * ****************************************************************************
 * Copyright (c) 2010-2016 by Min Cai (min.cai.china@gmail.com).
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

import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.cache.replacement.CacheReplacementPolicy;
import archimulator.uncore.cache.setDueling.AbstractSetDuelingUnit;
import archimulator.uncore.cache.setDueling.HelperThreadPrefetchAccuracyBasedSetDuelingUnit;

import java.io.Serializable;

/**
 * Helper thread prefetch accuracy based set dueling cache replacement policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class HelperThreadPrefetchAccuracyBasedSetDuelingCacheReplacementPolicy<StateT extends Serializable> extends AbstractSetDuelingCacheReplacementPolicy<StateT> {
    /**
     * Create a helper thread prefetch accuracy based set dueling cache replacement policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public HelperThreadPrefetchAccuracyBasedSetDuelingCacheReplacementPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT>... policies) {
        super(cache, policies);
    }

    protected AbstractSetDuelingUnit createSetDuelingUnit(EvictableCache<StateT> cache, int numPolicies) {
        return new HelperThreadPrefetchAccuracyBasedSetDuelingUnit(cache, numPolicies, 2);
    }
}
