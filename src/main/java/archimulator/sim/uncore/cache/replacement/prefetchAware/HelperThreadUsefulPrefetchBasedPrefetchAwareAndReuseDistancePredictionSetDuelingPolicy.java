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

import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.cache.replacement.reuseDistancePrediction.ReuseDistancePredictionPolicy;

import java.io.Serializable;

/**
 * Helper thread useful prefetch based prefetch aware and reuse distance prediction set dueling policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy<StateT extends Serializable> extends HelperThreadUsefulPrefetchBasedSetDuelingCacheReplacementPolicy<StateT> {
    /**
     * Create a helper thread useful prefetch based prefetch aware and reuse distance prediction set dueling policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public HelperThreadUsefulPrefetchBasedPrefetchAwareAndReuseDistancePredictionSetDuelingPolicy(EvictableCache<StateT> cache) {
        super(
                cache,
                new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.RHM),
                new ReuseDistancePredictionPolicy<>(cache)
        );
    }
}