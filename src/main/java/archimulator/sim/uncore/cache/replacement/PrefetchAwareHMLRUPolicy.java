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
package archimulator.sim.uncore.cache.replacement;

import archimulator.sim.uncore.MemoryHierarchyAccess;
import archimulator.sim.uncore.cache.EvictableCache;
import archimulator.sim.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Prefetch aware HM Least recently used (LRU) policy.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public class PrefetchAwareHMLRUPolicy<StateT extends Serializable> extends LRUPolicy<StateT> {
    /**
     * Create a prefetch aware HM least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public PrefetchAwareHMLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (!HelperThreadingHelper.isHelperThread(this.getCache().getLine(set, way).getAccess().getThread())) {
            this.setMRU(set, way);
        }
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if (HelperThreadingHelper.isHelperThread(access.getThread())) {
            this.setLRU(set, way);
        }
        else {
            this.setMRU(set, way);
        }
    }
}
