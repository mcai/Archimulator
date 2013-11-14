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
import archimulator.sim.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;

/**
 * Prefetch aware set dueling based HM Least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class PrefetchAwareSetDuelingHMLRUPolicy<StateT extends Serializable> extends SetDuelingPolicy<StateT> {
    /**
     * Create a prefetch aware set dueling based HM least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    @SuppressWarnings("unchecked")
    public PrefetchAwareSetDuelingHMLRUPolicy(EvictableCache<StateT> cache) {
        super(
                cache,
                new LRUPolicy<>(cache),
                new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PrefetchAwareHMLRUPolicyType.H),
                new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PrefetchAwareHMLRUPolicyType.HM)
        );
    }
}
