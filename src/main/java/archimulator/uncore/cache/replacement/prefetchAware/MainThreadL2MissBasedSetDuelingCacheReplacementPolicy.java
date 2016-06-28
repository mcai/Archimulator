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
import archimulator.uncore.cache.setDueling.MainThreadL2MissBasedSetDuelingUnit;

import java.io.Serializable;

/**
 * Main thread L2 miss based set dueling based policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class MainThreadL2MissBasedSetDuelingCacheReplacementPolicy<StateT extends Serializable> extends AbstractSetDuelingCacheReplacementPolicy<StateT> {
    /**
     * Create a main thread L2 miss based set dueling based policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public MainThreadL2MissBasedSetDuelingCacheReplacementPolicy(EvictableCache<StateT> cache, CacheReplacementPolicy<StateT>... policies) {
        super(cache, policies);
    }

    protected MainThreadL2MissBasedSetDuelingUnit createSetDuelingUnit(EvictableCache<StateT> cache, int numPolicies) {
        return new MainThreadL2MissBasedSetDuelingUnit(cache, numPolicies, 2);
    }
}
