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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.cache.EvictableCache;

import java.io.Serializable;

public class CacheReplacementPolicyFactory {
    public static <StateT extends Serializable> CacheReplacementPolicy<StateT> createCacheReplacementPolicy(CacheReplacementPolicyType cacheReplacementPolicyType, EvictableCache<StateT> cache) {
        switch (cacheReplacementPolicyType) {
            case LRU:
                return new LRUPolicy<StateT>(cache);
            case LFU:
                return new LFUPolicy<StateT>(cache);
            case RANDOM:
                return new RandomPolicy<StateT>(cache);
            case HELPER_THREAD_AWARE_LRU:
                return new HelperThreadAwareLRUPolicy<StateT>(cache, false);
            case HELPER_THREAD_AWARE_BREAKDOWN_LRU:
                return new HelperThreadAwareLRUPolicy<StateT>(cache, true);
            default:
                throw new IllegalArgumentException();
        }
    }
}