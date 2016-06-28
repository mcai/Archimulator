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
import archimulator.uncore.cache.replacement.LRUPolicy;

import java.io.Serializable;

/**
 * Prefetch aware set dueling based HM Least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class PrefetchAwareSetDuelingHMLRUPolicy<StateT extends Serializable> extends MainThreadL2MissBasedSetDuelingCacheReplacementPolicy<StateT> {
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
                new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.H),
                new PrefetchAwareHMLRUPolicy<>(cache, PrefetchAwareHMLRUPolicy.PolicyType.HM)
        );
    }
}
