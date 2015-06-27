/*******************************************************************************
 * Copyright (c) 2010-2015 by Min Cai (min.cai.china@gmail.com).
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
package archimulator.uncore.cache.replacement.partitioned;

import archimulator.uncore.MemoryHierarchyAccess;
import archimulator.uncore.MemoryHierarchyAccessType;
import archimulator.uncore.cache.EvictableCache;
import archimulator.uncore.helperThread.HelperThreadingHelper;

import java.io.Serializable;

/**
 * Helper thread aware partitioned LRU policy for the specified evictable cache.
 *
 * @param <StateT> the state type of the parent evictable cache
 * @author Min Cai
 */
public abstract class HelperThreadAwarePartitionedLRUPolicy<StateT extends Serializable> extends PartitionedLRUPolicy<StateT> {
    /**
     * Create a helper thread aware partitioned least recently used (LRU) policy for the specified evictable cache.
     *
     * @param cache the parent evictable cache
     */
    public HelperThreadAwarePartitionedLRUPolicy(EvictableCache<StateT> cache) {
        super(cache);
    }

    @Override
    public void handlePromotionOnHit(MemoryHierarchyAccess access, int set, int way) {
        if (access.getType() == MemoryHierarchyAccessType.LOAD && HelperThreadingHelper.isMainThread(access.getThread()) && HelperThreadingHelper.isHelperThread(getCache().getLine(set, way).getAccess().getThread())) {
            this.setLRU(set, way);  // supposed to be never used again: low locality => Demote to LRU position
            return;
        }

        super.handlePromotionOnHit(access, set, way);
    }

    @Override
    public void handleInsertionOnMiss(MemoryHierarchyAccess access, int set, int way) {
        if(access.getType() == MemoryHierarchyAccessType.LOAD && HelperThreadingHelper.isMainThread(access.getThread()) && getCache().getSimulation().getDelinquentLoadIdentificationHelper().isDelinquentPc(access.getThread().getId(), access.getVirtualPc())) {
            this.setLRU(set, way);
            return;
        }

        super.handleInsertionOnMiss(access, set, way);
    }
}
